package at.ac.wu.web.crawlers.thesis.politeness;

/**
 * Configuration object for politeness restrictions for a domain.
 *
 * Created by Patrick on 11.07.2017.
 */
public class PolitenessEntry {
    private String domain;
    private int timeunit_count;
    private int count;
    private int timeunit_delay;
    private int delay;

    public PolitenessEntry()  {
        super();
    }

    public PolitenessEntry(String domain, int timeunit_count, int count, int timeunit_delay, int delay) {
        this.domain = domain;
        this.timeunit_count = timeunit_count;
        this.count = count;
        this.timeunit_delay = timeunit_delay;
        this.delay = delay;
    }

    public String getDomain() {
        return domain;
    }

    public PolitenessEntry setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public int getTimeunit_count() {
        return timeunit_count;
    }

    public PolitenessEntry setTimeunit_count(int timeunit_count) {
        this.timeunit_count = timeunit_count;
        return this;
    }

    public int getCount() {
        return count;
    }

    public PolitenessEntry setCount(int count) {
        this.count = count;
        return this;
    }

    public int getTimeunit_delay() {
        return timeunit_delay;
    }

    public PolitenessEntry setTimeunit_delay(int timeunit_delay) {
        this.timeunit_delay = timeunit_delay;
        return this;
    }

    public int getDelay() {
        return delay;
    }

    public PolitenessEntry setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
                ", timeunit_count=" + timeunit_count +
                ", count=" + count +
                ", timeunit_delay=" + timeunit_delay +
                ", delay=" + delay +
                '}';
    }
}
