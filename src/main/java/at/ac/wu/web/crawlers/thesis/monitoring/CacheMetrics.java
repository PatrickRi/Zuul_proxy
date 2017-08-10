package at.ac.wu.web.crawlers.thesis.monitoring;

import at.ac.wu.web.crawlers.thesis.cache.PageCache;
import at.ac.wu.web.crawlers.thesis.politeness.DomainDelayCache;
import at.ac.wu.web.crawlers.thesis.politeness.PolitenessCache;
import org.infinispan.stats.Stats;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.Collection;
import java.util.HashSet;

/**
 * A {@link PublicMetrics} implementation that provides cache statistics such as:
 * <ul>
 * <li>hits</li>
 * <li>size</li>
 * <li>misses</li>
 * <li>retrievals</li>
 * <li>hitratio</li>
 * <li>memory</li>
 * </ul>
 * Created by Patrick on 08.08.2017.
 */

public class CacheMetrics implements PublicMetrics {

    private DomainDelayCache delayCache;
    private PolitenessCache politenessCache;
    private PageCache pageCache;

    public CacheMetrics(DomainDelayCache delayCache, PolitenessCache politenessCache, PageCache pageCache) {
        this.delayCache = delayCache;
        this.politenessCache = politenessCache;
        this.pageCache = pageCache;
    }

    @Override
    public Collection<Metric<?>> metrics() {
        Collection<Metric<?>> metrics = new HashSet<>();
        addMetric(metrics, delayCache.getCache().getName(), this.delayCache.getCache().getAdvancedCache().getStats());
        addMetric(metrics, politenessCache.getCache().getName(), this.politenessCache.getCache().getAdvancedCache()
                .getStats());
        addMetric(metrics, pageCache.getCache().getName(), this.pageCache.getCache().getAdvancedCache().getStats());
        return metrics;
    }

    private void addMetric(Collection<Metric<?>> metrics, String cacheName, Stats stats) {
        String prefix = "cache." + cacheName + ".";
        metrics.add(new Metric<>(prefix + "hits", stats.getHits()));
        metrics.add(new Metric<>(prefix + "size", stats.getCurrentNumberOfEntries()));
        metrics.add(new Metric<>(prefix + "misses", stats.getMisses()));
        metrics.add(new Metric<>(prefix + "retrievals", stats.getRetrievals()));
        metrics.add(new Metric<>(prefix + "hitratio", getHitRatio(stats.getHits(), stats.getMisses())));
        metrics.add(new Metric<>(prefix + "memory", stats.getOffHeapMemoryUsed()));
    }

    private long getHitRatio(long hits, long misses) {
        if (hits == 0 || misses == 0) {
            return 0;
        } else {
            return hits / (hits + misses);
        }
    }


}
