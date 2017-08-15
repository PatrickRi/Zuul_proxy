package at.ac.wu.web.crawlers.thesis.cache;

import java.io.Serializable;

/**
 * Created by Patrick on 04.08.2017.
 */
public class CacheKey implements Serializable {

    private String url;
    private String contentType;

    public CacheKey(String url) {
        this(url, null);
    }

    public CacheKey(String url, String contentType) {
        this.url = url;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public CacheKey setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public CacheKey setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheKey cacheKey = (CacheKey) o;

        if (url != null ? !url.equals(cacheKey.url) : cacheKey.url != null) {
            return false;
        }
        return contentType != null ? contentType.equals(cacheKey.contentType) : cacheKey.contentType == null;

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CacheKey{" + url + " - " + contentType + "}";
    }
}
