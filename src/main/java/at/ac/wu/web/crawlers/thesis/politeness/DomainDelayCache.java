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

/**
 * Represents the infinispan cache containting configured and computed
 * delays for domains.
 * <p>
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class DomainDelayCache {

    static Cache<String, String> cache;
    private static Logger log = LoggerFactory.getLogger(DomainDelayCache.class);
    private static DomainDelayCache INSTANCE;

    @Autowired
    PolitenessConfiguration config;

    private Cache<String, String> cache() {
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

    public DomainDelayCache getCache() {
        return this;
    }
}
