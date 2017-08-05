package at.ac.wu.web.crawlers.thesis.cache;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Created by Patrick on 01.08.2017.
 */
public class CacheEntry implements Serializable {
    private byte[] data;
    private String url;
    private long maxAge;
    private boolean noCache;
    private boolean noStore;
    private LocalDateTime expires; //Response header controlling lifespan of page
    private LocalDateTime time; //Time at which page was loaded
    private HashMap<String, String> headers;
    private String contentType;

    public CacheEntry(byte[] data, String url, long maxAge, boolean noCache, boolean noStore, LocalDateTime expires, LocalDateTime time, HashMap<String, String> headers, boolean gZipped, String contentType) {
        this.data = data;
        this.url = url;
        this.maxAge = maxAge;
        this.noCache = noCache;
        this.noStore = noStore;
        this.expires = expires;
        this.time = time;
        this.headers = headers;
        this.contentType = contentType;
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

    public String getUrl() {
        return url;
    }

    public CacheEntry setUrl(String url) {
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

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public CacheEntry setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public CacheEntry setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
                "data=" + data.length +
                ", url='" + url + '\'' +
                ", maxAge=" + maxAge +
                ", noCache=" + noCache +
                ", noStore=" + noStore +
                ", expires=" + expires +
                ", time=" + time +
                ", headers=" + headers +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
