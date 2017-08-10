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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents the infinispan cache containing configured and computed
 * delays for domains.
 * <p>
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class DomainDelayCache implements InitializingBean {

    static Cache<String, PolitenessEntry> cache;
    private static Logger log = LoggerFactory.getLogger(DomainDelayCache.class);
    private static DomainDelayCache INSTANCE;

    @Autowired
    PolitenessConfiguration config;

    @Autowired
    Environment environment;

    private Cache<String, PolitenessEntry> cache() {
        if (cache == null) {
            Configuration configuration = new ConfigurationBuilder().persistence()
                    .passivation(false)
                    .addSingleFileStore()
                    .preload(true)
                    .shared(false)
                    .fetchPersistentState(true)
                    .ignoreModifications(false)
                    .purgeOnStartup(false)
                    .location(config.getDirectory())
                    .async()
                    .enabled(true)
                    .threadPoolSize(2)
                    .memory()
                    .size(config.getMemory())
                    .evictionType(EvictionType.MEMORY)
                    .expiration()
                    .lifespan(30, TimeUnit.DAYS)
                    .storeAsBinary()
                    .enable()
                    .jmxStatistics()
                    .enable()
                    .build();


            GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                    .globalJmxStatistics()
                    .enable()
                    .cacheManagerName("DelayCacheManager")
                    .jmxDomain(config.getDelayJmxDomain())
                    .allowDuplicateDomains(true)
                    .build();
            this.cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("delay-cache");
        }
        return this.cache;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.environment instanceof AbstractEnvironment) {
            AbstractEnvironment env = (AbstractEnvironment) this.environment;
            List<PolitenessEntry> list = new PropertyExtractionHelper().getEntriesFromProperties(env);
            for (PolitenessEntry entry : list) {
                PolitenessEntry cache_entry = this.getEntry(entry.getDomain());
                if (cache_entry == null) {
                    this.putEntry(entry);
                    log.debug("New Entry found and put in delay cache " + entry);
                } else if (cache_entry.getDelay() < entry.getDelay()) {
                    this.putEntry(entry);
                    log.debug("Updated entry for " + entry.getDomain() + " because new delay "
                                      + entry.getDelay() + "configured bigger than " + cache_entry.getDelay());
                } else if (entry.getConfigured_delay() != cache_entry.getConfigured_delay()) {
                    //Merge entries to behold configured delay in cache (just for monitoring)
                    this.putEntry(new PolitenessEntry(entry.getDomain(), cache_entry.getDelay(), cache_entry
                            .getRobotstxt_delay(), entry.getConfigured_delay()));
                }
            }
        }
    }

    public Cache<String, PolitenessEntry> getCache() {
        return this.cache();
    }

    public PolitenessEntry getEntry(final String key) {
        return this.cache().get(key);
    }

    public void putEntry(final PolitenessEntry entry) {
        this.cache().put(entry.getDomain(), entry);
    }

    public List<PolitenessEntry> getEntries() {
        if (cache().size() > 2000) {
            return null;
        }
        return cache().values().stream().collect(Collectors.toList());
    }

    public void updateRobotsDelay(String domain, Double crawlDelay) {
        PolitenessEntry entry = this.cache().get(domain);
        if (entry == null) {
            entry = new PolitenessEntry(domain, crawlDelay.intValue(), crawlDelay.intValue(), 0);
            log.debug("New entry originated from robots.txt " + entry);
        } else {
            entry.setRobotstxt_delay(crawlDelay.intValue());
            if (entry.getDelay() < crawlDelay.intValue()) {
                entry.setDelay(crawlDelay.intValue());
            }
            log.debug("Updated entry originated from robots.txt " + entry);
        }
        putEntry(entry);
    }
}
