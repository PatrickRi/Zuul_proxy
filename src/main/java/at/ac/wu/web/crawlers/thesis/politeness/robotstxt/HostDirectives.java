package at.ac.wu.web.crawlers.thesis.politeness.robotstxt;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Patrick
 */
public class HostDirectives {
    public static final int ALLOWED = 1;
    public static final int DISALLOWED = 2;
    public static final int UNDEFINED = 3;
    // Refetch after 24hours
    private static final long EXPIRATION = 24 * 60 * 1000L;
    private final long timeFetched;
    /** A list of rule sets, sorted on match with the configured user agent */
    private Set<UserAgentDirectives> rules;
    private long timeLastAccessed;
    private RobotstxtConfiguration config;
    private String userAgent;

    public HostDirectives(RobotstxtConfiguration configuration) {
        timeFetched = System.currentTimeMillis();
        config = configuration;
        userAgent = config.getUserAgentName().toLowerCase();
        rules = new TreeSet<>(
            new UserAgentDirectives.UserAgentComparator(userAgent));
    }

    /**
     * Checks if the robots.txt must be refetched
     *
     * @return True if the robots.txt must be refetched.
     */
    public boolean needsRefetch() {
        return ((System.currentTimeMillis() - timeFetched) > EXPIRATION);
    }

    /**
     * Check if the given path is allowed.
     *
     * @param path The path
     * @return True if the path is allowed, false if it is disallowed
     */
    public boolean allows(String path) {
        return checkAccess(path) != DISALLOWED;
    }

    /**
     * Check access for the given path.
     *
     * @param path The path to check
     * @return ALLOWED, DISALLOWED or UNDEFINED
     */
    public int checkAccess(String path) {
        timeLastAccessed = System.currentTimeMillis();
        int result = UNDEFINED;
        String myUA = config.getUserAgentName();

        for (UserAgentDirectives ua : rules) {
            int score = ua.match(myUA);

            // If the user agent is different or not ignored, end here
            if (score == 0) {
                break;
            }
            result = ua.checkAccess(path, userAgent);
            if (result != DISALLOWED) {
                break;
            }
        }
        return result;
    }

    public void addDirectives(UserAgentDirectives directives) {
        rules.add(directives);
    }

    public long getLastAccessTime() {
        return timeLastAccessed;
    }

    public Set<UserAgentDirectives> getRules() {
        return rules;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
