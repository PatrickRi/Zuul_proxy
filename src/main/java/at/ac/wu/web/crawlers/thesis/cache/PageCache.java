package at.ac.wu.web.crawlers.thesis.cache;

import at.ac.wu.web.crawlers.thesis.politeness.PolitenessConfiguration;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class PageCache {

    private static Logger log = LoggerFactory.getLogger(PageCache.class);
    static Cache<URL, CacheEntry> cache;
    private static PageCache INSTANCE;
    private PolitenessConfiguration config;

    static {

        Configuration configuration = new ConfigurationBuilder()
                .memory().size(20_000L).evictionType(EvictionType.MEMORY)
                .expiration().lifespan(100L, TimeUnit.SECONDS)
                .build();
        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .globalJmxStatistics()
                .cacheManagerName("PageCacheManager")
                .jmxDomain("pageCache")
                .build();
        cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("page-cache");
    }

    public PageCache getCache() {
        return this;
    }


    public void addPage() {

    }

    public CacheEntry getEntry(URL url) {
        return cache.get(url);
    }
}
