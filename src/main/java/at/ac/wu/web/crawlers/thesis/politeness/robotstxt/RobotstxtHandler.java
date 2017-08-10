package at.ac.wu.web.crawlers.thesis.politeness.robotstxt;

import at.ac.wu.web.crawlers.thesis.http.HttpUtils;
import at.ac.wu.web.crawlers.thesis.politeness.DomainDelayCache;
import com.google.common.io.ByteSource;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick
 */
@Configuration
public class RobotstxtHandler {

    private static final Logger logger = LoggerFactory.getLogger(RobotstxtHandler.class);
    protected final Map<String, HostDirectives> host2directivesCache = new HashMap<>();
    private final int maxBytes = 16384;
    @Autowired
    HttpUtils httpUtils;
    @Autowired
    RobotstxtConfiguration config;
    @Autowired
    DomainDelayCache delayCache;

    private static String getHost(URL url) {
        return url.getHost().toLowerCase();
    }

    public boolean allows(URL url) {
        if (!config.isEnabled()) {
            return true;
        }
        try {
            String host = getHost(url);
            String path = url.getPath();

            HostDirectives directives = host2directivesCache.get(host);

            if (directives != null && directives.needsRefetch()) {
                synchronized (host2directivesCache) {
                    host2directivesCache.remove(host);
                    directives = null;
                }
            }
            if (directives == null) {
                directives = fetchDirectives(url);
            }
            return directives.allows(path);
        } catch (Exception e) {
            logger.error("Bad URL in Robots.txt: " + url, e);
        }

        logger.warn("RobotstxtServer: default: allow", url);
        return true;
    }

    private HostDirectives fetchDirectives(URL url) {
        String host = getHost(url);
        String port = ((url.getPort() == url.getDefaultPort()) || (url.getPort() == -1)) ? "" :
                (":" + url.getPort());
        String proto = url.getProtocol();

        HostDirectives directives = null;
        try {
            URL robotsUrl = new URL(proto + "://" + host + port + "/robots.txt");

            HttpHost httpHost = httpUtils.getHttpHost(robotsUrl);
            HttpRequest httpRequest = httpUtils.buildHttpRequest("GET", robotsUrl.toString(), null, new HttpHeaders(), new HttpHeaders(), null);
            CloseableHttpResponse httpResponse = httpUtils.getHttpClient().execute(httpHost, httpRequest);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contentType = httpResponse.getFirstHeader("content-type") != null ? httpResponse.getFirstHeader("content-type").getValue() : "";
                ByteSource source = new ByteSource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        return httpResponse.getEntity().getContent();
                    }
                };
                if (contentType.contains("text") && !contentType.contains("html")) {
                    String content;
                    if (httpResponse.getFirstHeader("content-charset") == null) {
                        content = source.asCharSource(Charset.forName("UTF-8")).read();
                    } else {
                        content = source.asCharSource(Charset.forName(httpResponse.getFirstHeader("content-charset").getValue())).read();
                    }
                    directives = RobotstxtParser.parse(content, config);
                } else if (contentType.contains("html")) {
                    // html tags
                    String content = source.asCharSource(Charset.forName("UTF-8")).read();
                    directives = RobotstxtParser.parse(content, config);
                } else {
                    logger.warn(
                            "Can't read this robots.txt: {}  as it is not written in plain text, " +
                                    "contentType: {}", url, contentType);
                }
                if (directives != null && directives.getRules() != null) {
                    for (UserAgentDirectives uad : directives.getRules()) {
                        if (uad.match(directives.getUserAgent()) != 0 && uad.getCrawlDelay() != null) {
                            delayCache.updateRobotsDelay(host, uad.getCrawlDelay());
                        }
                    }
                }
            } else {
                logger.debug("Can't read this robots.txt: {}  as it's status code is {}",
                        url, httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception ex) {
            logger.error("Error occurred while fetching (robots) url: " + url, ex);
        }

        if (directives == null) {
            // We still need to have this object to keep track of the time we fetched it
            directives = new HostDirectives(config);
        }
        synchronized (host2directivesCache) {
            if (host2directivesCache.size() == config.getCacheSize()) {
                String minHost = null;
                long minAccessTime = Long.MAX_VALUE;
                for (Map.Entry<String, HostDirectives> entry : host2directivesCache.entrySet()) {
                    long entryAccessTime = entry.getValue().getLastAccessTime();
                    if (entryAccessTime < minAccessTime) {
                        minAccessTime = entryAccessTime;
                        minHost = entry.getKey();
                    }
                }
                host2directivesCache.remove(minHost);
            }
            host2directivesCache.put(host, directives);
        }
        return directives;
    }
}
