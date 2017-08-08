package at.ac.wu.web.crawlers.thesis.politeness;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Represents the infinispan cache holding the information
 * which domains where crawled at which time.
 *
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class PolitenessCache {

    static Cache<String, String> cache;
    private static Logger log = LoggerFactory.getLogger(PolitenessCache.class);
    private static PolitenessCache INSTANCE;

    static {

        Configuration configuration = new ConfigurationBuilder()
                .memory()
                .size(20_000L)
                .evictionType(EvictionType.MEMORY)
                .expiration()
                .lifespan(1L, TimeUnit.HOURS)
                .jmxStatistics()
                .enable()
                .build();
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .globalJmxStatistics()
                .enable()
                .cacheManagerName("PolitenessCacheManager")
                .jmxDomain("politenessCache")
                .allowDuplicateDomains(true)
                .build();
        cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("politeness-cache");
    }

    private PolitenessConfiguration config;

    private Cache<String, String> cache() {
        if (cache == null) {
            Configuration configuration = new ConfigurationBuilder()
                    .memory()
                    .size(config.getMemory())
                    .evictionType(EvictionType.MEMORY)
                    .expiration()
                    .lifespan(1L, TimeUnit.HOURS)
                    .jmxStatistics()
                    .enable()
                    .build();
            GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                    .globalJmxStatistics()
                    .enable()
                    .cacheManagerName("PolitenessCacheManager")
                    .jmxDomain(config.getJmxDomain())
                    .allowDuplicateDomains(true)
                    .build();
            this.cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("politeness-cache");
        }
        return this.cache;
    }

    public PolitenessConfiguration getConfig() {
        return config;
    }

    @Autowired
    public void setConfig(PolitenessConfiguration config) {
        this.config = config;
    }

    public PolitenessCache getCache() {
        return this;
    }

    public void add(String domain) {
        PolitenessEntry politenessEntry = this.config.getConfig(domain);
        if (politenessEntry != null) {
            log.debug("Domain " + domain + " added to cache with lifespan of " + politenessEntry.getDelay());
            cache().put(domain + System.currentTimeMillis(), "", politenessEntry.getDelay(), TimeUnit.MILLISECONDS);
        } else {
            log.debug("Domain " + domain + " added to cache with configured default lifespan of " + this.config.getDefaultDelay());
            cache().put(domain + System.currentTimeMillis(), "", this.config.getDefaultDelay(), TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAllowed(String domain) {
        return cache().entrySet().stream().noneMatch(e -> e.getKey().startsWith(domain));
    }

    public int getDelayForDomain(final String domain) {
        if (this.config.getConfig(domain) == null) {
            return this.config.getDefaultDelay();
        } else {
            return this.config.getConfig(domain).getDelay();
        }
    }
}
