package at.ac.wu.web.crawlers.thesis.cache;

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

    static Cache<CacheKey, byte[]> cache;
    private static Logger log = LoggerFactory.getLogger(PageCache.class);
    private static PageCache INSTANCE;

    static {

        Configuration configuration = new ConfigurationBuilder()
                .memory()
                .size(200_000L)
                .evictionType(EvictionType.MEMORY)
                .storeAsBinary()
                .enable()
                .build();

        new ConfigurationBuilder().persistence()
                .passivation(false)
                .addSingleFileStore()
                .preload(true)
                .shared(false)
                .fetchPersistentState(true)
                .ignoreModifications(false)
                .purgeOnStartup(false)
                .location(System.getProperty("java.io.tmpdir"))
                .async()
                .enabled(true)
                .threadPoolSize(5)
                .singleton()
                .enabled(true)
                .pushStateWhenCoordinator(true)
                .pushStateTimeout(20000);


        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .globalJmxStatistics()
                .enable()
                .cacheManagerName("PageCacheManager")
                .jmxDomain("pageCache")
                .allowDuplicateDomains(true)
                .build();
        cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("page-cache");
    }

    public PageCache getCache() {
        return this;
    }


    public void addPage(CacheKey key, CacheEntry entry) {
        try {
            byte[] bytes = mapper().writeValueAsBytes(entry);
            cache.put(key, bytes);
            log.debug(key + " added to cache " + entry);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ObjectMapper mapper() {
        return new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
    }

    public boolean exists(CacheKey key) {
        return cache.containsKey(key);
    }

    public CacheEntry getEntry(CacheKey key) {
        byte[] bytes = cache.get(key);
        log.debug("Lookup " + key + " in cache");
        try {
            if (bytes != null) {
                log.debug("Entry for " + key + " found");
                return mapper().readValue(bytes, CacheEntry.class);
            }
            log.debug("Entry for " + key + " not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
