package at.ac.wu.web.crawlers.thesis.cache;

import org.apache.http.Header;
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

/**
 * Created by Patrick on 01.08.2017.
 */
@Service
public class CacheHandler {

    private static Logger log = LoggerFactory.getLogger(CacheHandler.class);

    @Autowired
    PageCache cache;

    public CacheEntry getContent(HttpServletRequest request) {
        try {
            URL url = new URL(request.getRequestURL().toString());
            CacheEntry entry = cache.getEntry(url.toString());
            if (entry != null && checkCacheControl(request, entry)) {
                return entry;
            }
        } catch (Exception ex) {
            //do nothing, just log
            log.debug("An error occured during cache lookup for url " + request.getRequestURL().toString(), ex);
        }
        return null;
    }

    public void put(URL url, Header[] headers, byte[] content) {
        if (headers != null && content != null && content.length > 1) {
            LocalDateTime expires = null;
            boolean noCache = false;
            boolean noStore = false;
            long maxAge = -1;
            for (Header header : headers) {
                String name = header.getName();
                if (name.equalsIgnoreCase("expires")) {
                    try {
                        expires = LocalDateTime.parse(header.getValue());
                    } catch (Exception ex)  {
                        expires = LocalDateTime.now(Clock.systemUTC());
                    }
                }
                if (name.equalsIgnoreCase("cache-control")) {
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
            }
            cache.addPage(url.toString(), new CacheEntry(content, url.toString(), maxAge, noCache, noStore, expires, LocalDateTime.now(Clock.systemUTC()), headers));
        }
    }

    private boolean checkCacheControl(HttpServletRequest request, CacheEntry entry) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if ("Cache-Control".equalsIgnoreCase(name)) {
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

    //Unfortunalety there is no easier way to do it in Java 8
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

        return seconds + minutes * 60 + hours * 60 * 60 + days * 60 * 60 * 24 + months * 60 * 60 * 24 * 30 + years * 60 * 60 * 24 * 30 * 12;
    }
}
