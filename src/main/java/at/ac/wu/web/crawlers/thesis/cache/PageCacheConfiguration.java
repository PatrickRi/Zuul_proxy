package at.ac.wu.web.crawlers.thesis.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for {@link PageCache}
 * <p>
 * Created by Patrick on 05.08.2017.
 */
@Configuration
@ConfigurationProperties(prefix = "page.cache")
public class PageCacheConfiguration {

    private String directory = System.getProperty("java.io.tmpdir");
    private String jmxDomain = "pageCache";
    private long memory = 1_000_000L;
    private int threads = 4;

    public String getDirectory() {
        return directory;
    }

    public PageCacheConfiguration setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public String getJmxDomain() {
        return jmxDomain;
    }

    public PageCacheConfiguration setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
        return this;
    }

    public long getMemory() {
        return memory;
    }

    public PageCacheConfiguration setMemory(long memory) {
        this.memory = memory;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public PageCacheConfiguration setThreads(int threads) {
        this.threads = threads;
        return this;
    }
}
