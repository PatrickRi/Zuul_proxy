package at.ac.wu.web.crawlers.thesis.service;

/**
 * Stores the result of the check whether an URL can be crawled and when.
 * <p>
 * Created by Patrick on 06.08.2017.
 */
public class URLPoliteness {
    private String url;
    private String error;
    private boolean allowed;
    private Integer retryAfter;

    public URLPoliteness(String url, String error, boolean allowed, Integer retryAfter) {
        this.url = url;
        this.error = error;
        this.allowed = allowed;
        this.retryAfter = retryAfter;
    }

    public String getUrl() {
        return url;
    }

    public URLPoliteness setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getError() {
        return error;
    }

    public URLPoliteness setError(String error) {
        this.error = error;
        return this;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public URLPoliteness setAllowed(boolean allowed) {
        this.allowed = allowed;
        return this;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }

    public URLPoliteness setRetryAfter(Integer retryAfter) {
        this.retryAfter = retryAfter;
        return this;
    }

    @Override
    public String toString() {
        return "URLPoliteness{" +
                "url='" + url + '\'' +
                ", error='" + error + '\'' +
                ", allowed=" + allowed +
                ", retryAfter=" + retryAfter +
                '}';
    }
}
