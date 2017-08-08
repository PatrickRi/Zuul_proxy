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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the infinispan cache holding cached pages.
 *
 * Created by Patrick on 11.07.2017.
 */
@Component
@org.springframework.context.annotation.Configuration
public class PageCache {

    static Cache<CacheKey, byte[]> cache;
    private static Logger log = LoggerFactory.getLogger(PageCache.class);
    private static PageCache INSTANCE;

    @Autowired
    PageCacheConfiguration cacheProperties;

    public PageCache getCache() {
        return this;
    }

    private Cache<CacheKey, byte[]> cache() {
        if (cache == null) {
            Configuration configuration = new ConfigurationBuilder().persistence()
                    .passivation(false)
                    .addSingleFileStore()
                    .preload(true)
                    .shared(false)
                    .fetchPersistentState(true)
                    .ignoreModifications(false)
                    .purgeOnStartup(false)
                    .location(cacheProperties.getDirectory())
                    .async()
                    .enabled(true)
                    .threadPoolSize(cacheProperties.getThreads())
                    .memory()
                    .size(cacheProperties.getMemory())
                    .evictionType(EvictionType.MEMORY)
                    .storeAsBinary()
                    .enable()
                    .jmxStatistics()
                    .enable()
                    .build();


            GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                    .globalJmxStatistics()
                    .enable()
                    .cacheManagerName("PageCacheManager")
                    .jmxDomain(cacheProperties.getJmxDomain())
                    .allowDuplicateDomains(true)
                    .build();
            this.cache = new DefaultCacheManager(globalConfiguration, configuration).getCache("page-cache");
        }
        return this.cache;
    }

    /**
     * Puts the given entry into the cache.
     *
     * @param key   Key identifying each entry
     * @param entry Entry to put into the cache
     */
    public void addPage(CacheKey key, CacheEntry entry) {
        try {
            byte[] bytes = mapper().writeValueAsBytes(entry);
            cache().put(key, bytes);
            log.debug(key + " added to cache " + entry);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ObjectMapper mapper() {
        return new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
    }

    /**
     * Checks if the cache contains an entry under the given key.
     *
     * @param key Key
     * @return <tt>true</tt> if the cache contains a mapping for the specified
     *         key
     */
    public boolean exists(CacheKey key) {
        return cache().containsKey(key);
    }

    /**
     * Retrieves an entry from the cache using the given key.
     *
     * @param key Key
     * @return the value to which the specified key is mapped, or
     *         {@code null} if the cache contains no mapping for the key
     */
    public CacheEntry getEntry(CacheKey key) {
        byte[] bytes = cache().get(key);
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
