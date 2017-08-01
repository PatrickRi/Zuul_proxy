package at.ac.wu.web.crawlers.thesis.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Enumeration;

import static org.aspectj.bridge.Version.getTime;

/**
 * Created by Patrick on 01.08.2017.
 */
@Service
public class CacheHandler {

    private static Logger log = LoggerFactory.getLogger(CacheHandler.class);

    @Autowired
    PageCache cache;

    public byte[] getContent(HttpServletRequest request) {
        try {
            URL url = new URL(request.getRequestURL().toString());
            CacheEntry entry = cache.getEntry(url);
            boolean b = checkCacheControl(request, entry);
            if (entry != null && checkCacheControl(request, entry)) {
                return entry.getData();
            }
        } catch (Exception ex) {
            //do nothing, just log
            log.debug("An error occured during cache lookup for url " + request.getRequestURL().toString(), ex);
        }
        return null;
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
                            if (maxAge == -1) {
                                return true;
                            } else {
                                //TODO Check MaxAge
                            }
                        } else if (directive.startsWith("max-stale")) {
                            long maxStale = extractHeaderValue(directive);
                            if (maxStale == -1) {
                                return true;
                            } else {
                                //TODO check freshness
                            }
                        } else if (directive.startsWith("min-fresh")) {
                            long minFresh = extractHeaderValue(directive);
                            if (minFresh == -1) {
                                return false;
                            } else {
                                
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
    
    private long calculateAge(CacheEntry entry)  {
        //long maxAge = entry.getMaxAge();
        LocalDateTime now = LocalDateTime.now();
        //boolean expired = entry.getExpires().isBefore(now);
        //boolean maxAgeReached = entry.getTime().plus(maxAge, ChronoUnit.SECONDS).isBefore(now);



        return now.get
    }


}
