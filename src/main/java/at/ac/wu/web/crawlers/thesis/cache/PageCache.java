package at.ac.wu.web.crawlers.thesis.cache;

import at.ac.wu.web.crawlers.thesis.politeness.PolitenessConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

/**
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class PageCache {

    private static Logger log = LoggerFactory.getLogger(PageCache.class);
    static Cache<String, byte[]> cache;
    private static PageCache INSTANCE;
    private PolitenessConfiguration config;

    static {

        Configuration configuration = new ConfigurationBuilder()
                .memory().size(200_000L).evictionType(EvictionType.MEMORY)
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


    public void addPage(String url, CacheEntry entry) {
        try {
            byte[] bytes = new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule()).writeValueAsBytes(entry);
            cache.put(url, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CacheEntry getEntry(String url) {
        byte[] bytes = cache.get(url);
        try {
            if(bytes != null)  {
                return new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule()).readValue(bytes, CacheEntry.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
