package at.ac.wu.web.crawlers.thesis.monitoring;

import at.ac.wu.web.crawlers.thesis.politeness.DomainDelayCache;
import at.ac.wu.web.crawlers.thesis.politeness.PolitenessEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Patrick on 08.08.2017.
 */
@Component
public class CrawlDelayCacheEndpoint extends AbstractEndpoint<List<PolitenessEntry>> {

    private DomainDelayCache delayCache;

    @Autowired
    public CrawlDelayCacheEndpoint(DomainDelayCache delayCache) {
        super("delays");
        this.delayCache = delayCache;
    }

    @Override
    public List<PolitenessEntry> invoke() {
        return this.delayCache.getEntries();
    }
}
