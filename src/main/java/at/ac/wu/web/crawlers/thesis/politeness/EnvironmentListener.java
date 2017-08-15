package at.ac.wu.web.crawlers.thesis.politeness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by Patrick on 15.08.2017.
 */
@Component
public class EnvironmentListener implements ApplicationListener<EnvironmentChangeEvent> {

    @Autowired
    Environment environment;

    @Autowired
    DomainDelayCache delayCache;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        for (String property : event.getKeys()) {
            if (property.endsWith("delay")) {
                PolitenessEntry cache_entry = delayCache.getEntry(environment.getProperty(getPropertyKey(property,
                                                                                                         "domain")));
                if (cache_entry != null) {
                    int delay = Integer.parseInt(environment.getProperty(property));
                    delayCache.putEntry(new PolitenessEntry(cache_entry.getDomain(), delay, cache_entry
                            .getRobotstxt_delay(), delay));
                }
            } else {
                int delay = Integer.parseInt(environment.getProperty(property));
                delayCache.putEntry(new PolitenessEntry(getPropertyKey(property, "domain"), delay, 0, delay));
            }
        }
    }

    private String getPropertyKey(String key, String appendix) {
        String[] elements = key.split("\\.");
        return elements[0] + "." + elements[1] + "." + appendix;
    }
}
