package at.ac.wu.web.crawlers.thesis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Various services to hand over a set of URLs and get
 * a set of URLs back which are allowed to be crawled.
 * <p>
 * Created by Patrick on 06.08.2017.
 */
@RestController
@CrossOrigin(origins = "http://localhost:9999")
public class PolitenessController {

    @Autowired
    URLPolitenessService urlService;

    //http://localhost:9999/politeness?urls=121312,1231232
    @GetMapping(path = "/politeness")
    public List<String> getPoliteURLs(@RequestParam("urls") List<String> urls) {
        return filterURLs(urls);
    }

    //curl -X "POST" -H "Content-Type: application/json" localhost:9999/politeness --data "@data.json"
    //data.json with simple array ["http://.....","...."]
    @PostMapping(path = "/politeness")
    public List<String> postPoliteURLs(@RequestBody List<String> urls) {
        return filterURLs(urls);
    }

    @PostMapping(path = "/politeness/verbose")
    public List<URLPoliteness> postPoliteURLsVerbose(@RequestBody List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        } else {
            return urlService.isCrawlableVerbose(urls);
        }
    }

    private List<String> filterURLs(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        }
        return urlService.isCrawlable(urls);
    }
}
