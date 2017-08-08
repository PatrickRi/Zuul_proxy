package at.ac.wu.web.crawlers.thesis.politeness;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;

/**
 * Configuration for the politeness cache {@link PolitenessCache}
 *
 * Created by Patrick on 13.07.2017.
 */
@Component
@ConfigurationProperties(prefix = "politeness")
public class PolitenessConfiguration {

    private HashSet<PolitenessEntry> domains = new HashSet<>();

    private PolitenessEntry defaultDomain;

    private boolean useDefault = true;

    private String jmxDomain = "politenessCache";
    private String delayJmxDomain = "delayCache";
    private long memory = 100_000L;
    private String directory = System.getProperty("java.io.tmpdir");

    public HashSet<PolitenessEntry> getDomains() {
        return domains;
    }

    public PolitenessConfiguration setDomains(HashSet<PolitenessEntry> domains) {
        this.domains = domains;
        return this;
    }

    public PolitenessEntry getConfig(String domain) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        for (PolitenessEntry entry : this.domains) {
            if (matcher.matchStart(entry.getDomain(), domain)) {
                return entry;
            }
        }
        return null;
    }

    public PolitenessEntry getDefaultDomain() {
        return defaultDomain;
    }

    public PolitenessConfiguration setDefaultDomain(PolitenessEntry defaultDomain) {
        this.defaultDomain = defaultDomain;
        return this;
    }

    public int getDefaultDelay()  {
        return this.defaultDomain.getDelay();
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public PolitenessConfiguration setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
        return this;
    }

    public String getJmxDomain() {
        return jmxDomain;
    }

    public PolitenessConfiguration setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
        return this;
    }

    public long getMemory() {
        return memory;
    }

    public PolitenessConfiguration setMemory(long memory) {
        this.memory = memory;
        return this;
    }

    public String getDelayJmxDomain() {
        return delayJmxDomain;
    }

    public PolitenessConfiguration setDelayJmxDomain(String delayJmxDomain) {
        this.delayJmxDomain = delayJmxDomain;
        return this;
    }

    public String getDirectory() {
        return directory;
    }

    public PolitenessConfiguration setDirectory(String directory) {
        this.directory = directory;
        return this;
    }
}
