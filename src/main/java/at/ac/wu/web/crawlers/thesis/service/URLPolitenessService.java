package at.ac.wu.web.crawlers.thesis.service;

import at.ac.wu.web.crawlers.thesis.politeness.PolitenessCache;
import at.ac.wu.web.crawlers.thesis.politeness.robotstxt.RobotstxtHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for checking URLs against politeness constraints.
 * <p>
 * Created by Patrick on 06.08.2017.
 */
@Service
@Configuration
public class URLPolitenessService {

    @Autowired
    PolitenessCache cache;

    @Autowired
    RobotstxtHandler robotsTxt;

    /**
     * The URLs are filtered if they are null.
     * Then the constraints of the robots.txt and the politeness delays are applied.
     *
     * @param input set of URLs
     * @return URLs which are allowed to be crawled
     */
    public List<String> isCrawlable(List<String> input) {
        List<URL> urls = input.stream().map(this::toUrl).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> visitedDomains = new ArrayList<>();
        List<String> result = new ArrayList<>(input.size());
        for (URL url : urls) {
            URL targetURL = null;
            if (!visitedDomains.contains(url.toString())) {
                if (robotsTxt.allows(targetURL)) {
                    if (cache.isAllowed(url.getHost())) {
                        visitedDomains.add(url.getHost());
                        result.add(url.toString());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Applies the same logic as {@link #isCrawlable(List)}. However this method
     * returns a set of {@link URLPoliteness} objects, containing additional
     * information about when to crawl a URL and eventual error messages.
     *
     * @param input set of URLs
     * @return set of Politeness items
     */
    public List<URLPoliteness> isCrawlableVerbose(List<String> input) {
        List<URL> urls = input.stream().map(this::toUrl).filter(Objects::nonNull).collect(Collectors.toList());
        Map<String, Integer> visitedDomains = new HashMap<>();
        List<URLPoliteness> result = new ArrayList<>(input.size());
        for (URL url : urls) {
            final String host = url.getHost();
            int delay = cache.getDelayForDomain(host);
            if (robotsTxt.allows(url)) {
                if (cache.isAllowed(host)) {
                    if (visitedDomains.containsKey(host)) {
                        addToMap(visitedDomains, host, delay);
                        String message = "There must be a delay of " + delay + " milliseconds between each request.";
                        result.add(new URLPoliteness(url.toString(), message, false, visitedDomains.get(host)));
                    } else {
                        addToMap(visitedDomains, host, delay);
                        result.add(new URLPoliteness(url.toString(), null, true, -1));
                    }
                } else {
                    addToMap(visitedDomains, host, delay);
                    String message = "There must be a delay of " + delay + " milliseconds between each request.";
                    result.add(new URLPoliteness(url.toString(), message, false, delay));
                }
            } else {
                result.add(new URLPoliteness(url.toString(), "Blocked because URL is excluded from allowed URLs.", false, -1));
            }
        }
        return result;
    }

    private void addToMap(Map<String, Integer> map, String domain, int delay) {
        map.compute(domain, (d, count) -> count == null ? delay : count + delay);
    }

    private URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
