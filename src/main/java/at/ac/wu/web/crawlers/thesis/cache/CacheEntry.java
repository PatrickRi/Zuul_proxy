package at.ac.wu.web.crawlers.thesis.cache;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * Created by Patrick on 01.08.2017.
 */
public class CacheEntry implements Serializable {
    private byte[] data;
    private URL url;
    private long maxAge;
    private boolean noCache;
    private boolean noStore;
    private LocalDateTime expires; //Response header controlling lifespan of page
    private LocalDateTime time; //Time at which page was loaded

    public CacheEntry(byte[] data, URL url, long maxAge, boolean noCache, boolean noStore, LocalDateTime expires, LocalDateTime time) {
        this.data = data;
        this.url = url;
        this.maxAge = maxAge;
        this.noCache = noCache;
        this.noStore = noStore;
        this.expires = expires;
        this.time = time;
    }

    public CacheEntry() {

    }

    public byte[] getData() {
        return data;
    }

    public CacheEntry setData(byte[] data) {
        this.data = data;
        return this;
    }

    public URL getUrl() {
        return url;
    }

    public CacheEntry setUrl(URL url) {
        this.url = url;
        return this;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public CacheEntry setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isNoCache() {
        return noCache;
    }

    public CacheEntry setNoCache(boolean noCache) {
        this.noCache = noCache;
        return this;
    }

    public boolean isNoStore() {
        return noStore;
    }

    public CacheEntry setNoStore(boolean noStore) {
        this.noStore = noStore;
        return this;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public CacheEntry setExpires(LocalDateTime expires) {
        this.expires = expires;
        return this;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public CacheEntry setTime(LocalDateTime time) {
        this.time = time;
        return this;
    }
}
