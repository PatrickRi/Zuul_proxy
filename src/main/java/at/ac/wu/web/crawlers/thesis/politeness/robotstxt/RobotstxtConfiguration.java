package at.ac.wu.web.crawlers.thesis.politeness.robotstxt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class RobotstxtConfiguration {

    /**
     * Should the crawler respect the Robots.txt protocol?
     */
    private boolean enabled = true;

    /**
     * user-agent name
     */
    private String userAgentName = "wu-is-crawler";

    /**
     * The maximum number of hosts for which their robots.txt is cached.
     */
    private int cacheSize = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserAgentName() {
        return userAgentName;
    }

    public void setUserAgentName(String userAgentName) {
        this.userAgentName = userAgentName;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
