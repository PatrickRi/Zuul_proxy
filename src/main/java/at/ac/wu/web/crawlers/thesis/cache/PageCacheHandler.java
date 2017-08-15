package at.ac.wu.web.crawlers.thesis.cache;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;

import static org.apache.http.HttpHeaders.*;

/**
 * This class handles the interaction with the cache itself ({@link PageCache})
 * <p>
 * Created by Patrick on 01.08.2017.
 */
@Service
public class PageCacheHandler {

    private static Logger log = LoggerFactory.getLogger(PageCacheHandler.class);

    @Autowired
    PageCache cache;

    /**
     * Tries to retrieve a cached entry using various parameters of the given request.
     *
     * @param request Request
     * @return the value if found in the cache, or
     * {@code null} if the cache contains no entry for the key
     */
    public CacheEntry getContent(HttpServletRequest request) {
        CacheEntry entry;
        try {
            String url = new URL(request.getRequestURL().toString()).toString();
            String header = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if (header == null || header.isEmpty()) {
                entry = cache.getEntry(new CacheKey(url));
                //Check if entry satisfies Cache-Control directives
                if (entry == null || !checkCacheControl(request, entry)) {
                    entry = cache.getEntry(new CacheKey(url, "*/*"));
                }
            } else {
                //Whitespaces can be contained inside Content-Type Header
                entry = cache.getEntry(new CacheKey(url, header.replaceAll("\\s", "")));
            }
            //Check if entry satisfies Cache-Control directives
            if (entry != null && checkCacheControl(request, entry)) {
                return entry;
            }
        } catch (Exception ex) {
            //do nothing, just log
            log.debug("An error occured during cache lookup for url " + request.getRequestURL().toString(), ex);
        }
        return null;
    }

    /**
     * Populates the cache with the given content, depending on the paramters.
     *
     * @param url     requested URL
     * @param headers Response headers
     * @param content Response body in binary form
     * @param request Request
     */
    public void put(URL url, Header[] headers, byte[] content, HttpServletRequest request) {
        if (headers != null && content != null && content.length > 1) {
            CacheEntry cacheEntry = createCacheEntry(url, headers, content);
            String urlString = url.toString();

            String contentTypeRequested = request.getHeader(HttpHeaders.CONTENT_TYPE);
            //If a specific content-type was requested
            if (contentTypeRequested != null && !contentTypeRequested.isEmpty()) {
                //Put with Content-Type of response
                cache.addPage(new CacheKey(urlString, contentTypeRequested), cacheEntry);
                //If there is no wildcard entry, put one into the cache
                CacheKey wildcardKey = new CacheKey(urlString, "*/*");
                if (!cache.exists(wildcardKey)) {
                    cache.addPage(wildcardKey, cacheEntry);
                }
                //If there is no url entry, put one into the cache
                CacheKey simpleKey = new CacheKey(urlString);
                if (!cache.exists(simpleKey)) {
                    cache.addPage(simpleKey, cacheEntry);
                }
            } else {
                //If no content-type was given than put in cache:
                //1. Content-Type of response
                //2. No Content-Type (default for requests without content-type)
                //3. */* for wildcard content-type
                //Existence check is not needed here because a request already cached does not get this far
                //and wildcard and pure url entries are more relevant coming from requests without Content-Type
                //because server Content-Type defaults may differ
                cache.addPage(new CacheKey(urlString, cacheEntry.getContentType()), cacheEntry);
                cache.addPage(new CacheKey(urlString), cacheEntry);
                cache.addPage(new CacheKey(urlString, "*/*"), cacheEntry);
            }
        }
    }

    private CacheEntry createCacheEntry(URL url, Header[] headers, byte[] content) {
        LocalDateTime expires = null;
        boolean noCache = false;
        boolean noStore = false;
        long maxAge = -1;
        String contentType = "*/*";
        for (Header header : headers) {
            String name = header.getName();
            if (name.equalsIgnoreCase(EXPIRES)) {
                try {
                    expires = LocalDateTime.parse(header.getValue());
                } catch (Exception ex) {
                    expires = LocalDateTime.now(Clock.systemUTC());
                }
            }
            if (name.equalsIgnoreCase(CACHE_CONTROL)) {
                String[] cacheDirectives = header.getValue().split(",");
                for (String directive : cacheDirectives) {
                    if (directive.trim().equalsIgnoreCase("no-cache")) {
                        noCache = true;
                    } else if (directive.trim().equalsIgnoreCase("no-store")) {
                        noStore = true;
                    } else if (directive.trim().startsWith("max-age")) {
                        maxAge = extractHeaderValue(directive);
                    }
                }
            }
            if (name.equalsIgnoreCase(CONTENT_TYPE)) {
                //Whitespaces can be contained inside Content-Type Header
                contentType = header.getValue().replaceAll("\\s", "");
            }
        }
        HashMap<String, String> map = new HashMap<>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, header.getValue());
            } else {
                String value = map.get(name);
                value = value + "---_ENTRY_---";
                map.put(name, value);
            }
        }
        return new CacheEntry(content, url.toString(),
                              maxAge, noCache, noStore, expires, LocalDateTime.now(Clock.systemUTC()), map, false,
                              contentType);
    }

    private boolean checkCacheControl(HttpServletRequest request, CacheEntry entry) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (CACHE_CONTROL.equalsIgnoreCase(name)) {
                    String cacheControl = request.getHeader(name);
                    String[] elements = cacheControl.split(",");
                    for (String element : elements) {
                        String directive = element.toLowerCase();
                        if (directive.equals("no-cache")) {
                            return false;
                        } else if (directive.startsWith("max-age")) {
                            long maxAge = extractHeaderValue(directive);
                            if (maxAge != -1 && calculateAge(entry) > maxAge) {
                                return false;
                            }
                        } else if (directive.startsWith("max-stale")) {
                            long maxStale = extractHeaderValue(directive);
                            if (maxStale != -1) {
                                long staleness = calculateStaleness(entry);
                                if (staleness != -1 && staleness > maxStale) {
                                    return false;
                                }
                            }
                        } else if (directive.startsWith("min-fresh")) {
                            long minFresh = extractHeaderValue(directive);
                            if (minFresh == -1) {
                                return false;
                            } else {
                                long freshness = calculateFreshness(entry);
                                if (freshness == -1) {
                                    return false;
                                } else if (freshness < minFresh) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    //Returns the extracted header of type long
    //Requires the header to be in the following format: [name]=[value]?
    //If no value is given or an exception occured -1 is returned
    private long extractHeaderValue(String header) {
        String[] maxAgeArray = header.split("=");
        if (maxAgeArray.length == 2) {
            try {
                return Long.parseLong(maxAgeArray[1]);
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private long calculateStaleness(CacheEntry entry) {
        long maxAge = entry.getMaxAge();
        LocalDateTime expirationMaxAge = entry.getTime().plus(maxAge, ChronoUnit.SECONDS);
        LocalDateTime expirationDate = entry.getExpires();

        //Take more restricted one of Expires or max-Age
        if (expirationDate.isBefore(expirationMaxAge)) {
            //If already stale
            if (expirationDate.isAfter(LocalDateTime.now(Clock.systemUTC()))) {
                //Return seconds
                return dateDiff(LocalDateTime.now(Clock.systemUTC()), expirationDate);
            } else {
                return -1;
            }
        } else {
            //If already stale
            if (expirationMaxAge.isAfter(LocalDateTime.now(Clock.systemUTC()))) {
                //Return seconds
                return dateDiff(LocalDateTime.now(Clock.systemUTC()), expirationMaxAge);
            }
        }
        //-1 if fresh and not actually stale
        return -1;
    }

    private long calculateFreshness(CacheEntry entry) {
        long maxAge = entry.getMaxAge();
        LocalDateTime expirationMaxAge = entry.getTime().plus(maxAge, ChronoUnit.SECONDS);
        LocalDateTime expirationDate = entry.getExpires();
        //Take more restricted one of Expires or max-Age
        if (expirationDate.isBefore(expirationMaxAge)) {
            //If not stale already
            if (expirationDate.isBefore(LocalDateTime.now(Clock.systemUTC()))) {
                //Return seconds
                return dateDiff(expirationDate, LocalDateTime.now(Clock.systemUTC()));
            } else {
                return -1;
            }
        } else {
            //If not stale already
            if (expirationMaxAge.isBefore(LocalDateTime.now(Clock.systemUTC()))) {
                //Return seconds
                return dateDiff(expirationMaxAge, LocalDateTime.now(Clock.systemUTC()));
            }
        }
        //-1 if already stale
        return -1;
    }

    //Age = Now - Time fetched
    private long calculateAge(CacheEntry entry) {
        return dateDiff(entry.getTime(), LocalDateTime.now(Clock.systemUTC()));
    }

    //Unfortunately there is no easier way to do it in Java 8
    private long dateDiff(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

        long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);

        return seconds + minutes * 60 + hours * 60 * 60 + days * 60 * 60 * 24 + months * 60 * 60 * 24 * 30 + years *
                60 * 60 * 24 * 30 * 12;
    }
}
