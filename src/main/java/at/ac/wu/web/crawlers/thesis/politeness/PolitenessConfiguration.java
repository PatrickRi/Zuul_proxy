package at.ac.wu.web.crawlers.thesis.politeness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Configuration for the politeness cache {@link PolitenessCache}
 * <p>
 * Created by Patrick on 13.07.2017.
 */
@Component
@ConfigurationProperties(prefix = "politeness")
public class PolitenessConfiguration {

    private static Logger log = LoggerFactory.getLogger(PolitenessConfiguration.class);
    @Autowired
    Environment environment;
    @Autowired
    DomainDelayCache delayCache;
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

    public PolitenessEntry getDefaultDomain() {
        return defaultDomain;
    }

    public PolitenessConfiguration setDefaultDomain(PolitenessEntry defaultDomain) {
        this.defaultDomain = defaultDomain;
        return this;
    }

    public int getDefaultDelay() {
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
