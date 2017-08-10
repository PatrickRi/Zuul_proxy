package at.ac.wu.web.crawlers.thesis.politeness;

import java.io.Serializable;

/**
 * Configuration object for politeness restrictions for a domain.
 * <p>
 * Created by Patrick on 11.07.2017.
 */
public class PolitenessEntry implements Serializable {
    private String domain;
    private int delay;
    private int robotstxt_delay;
    private int configured_delay;

    public PolitenessEntry() {
        super();
    }

    public PolitenessEntry(String domain, int delay, int robotstxt_delay, int configured_delay) {
        this.domain = domain;
        this.delay = delay;
        this.robotstxt_delay = robotstxt_delay;
        this.configured_delay = configured_delay;
    }

    public String getDomain() {
        return domain;
    }

    public PolitenessEntry setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public int getDelay() {
        return delay;
    }

    public PolitenessEntry setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public int getRobotstxt_delay() {
        return robotstxt_delay;
    }

    public PolitenessEntry setRobotstxt_delay(int robotstxt_delay) {
        this.robotstxt_delay = robotstxt_delay;
        return this;
    }

    public int getConfigured_delay() {
        return configured_delay;
    }

    public PolitenessEntry setConfigured_delay(int configured_delay) {
        this.configured_delay = configured_delay;
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

        PolitenessEntry that = (PolitenessEntry) o;

        return domain != null ? domain.equals(that.domain) : that.domain == null;
    }


    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "PolitenessEntry{" +
                "domain='" + domain + '\'' +
                ", delay=" + delay +
                ", robotstxt_delay=" + robotstxt_delay +
                ", configured_delay=" + configured_delay +
                '}';
    }
}
