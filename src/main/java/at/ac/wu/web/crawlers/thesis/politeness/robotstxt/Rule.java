package at.ac.wu.web.crawlers.thesis.politeness.robotstxt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class Rule {
    protected static final Logger logger = LoggerFactory.getLogger(Rule.class);

    public int type;
    public Pattern pattern;

    public Rule(int type, String pattern) {
        this.type = type;
        this.pattern = robotsPatternToRegexp(pattern);
    }

    /**
     * Match a pattern defined in a robots.txt file to a specific path
     *
     * @param pattern The pattern to convert
     * @return The compiled regexp pattern created from the robots.txt pattern
     */
    public static Pattern robotsPatternToRegexp(String pattern) {
        StringBuilder regexp = new StringBuilder();
        regexp.append('^');
        StringBuilder quoteBuf = new StringBuilder();
        boolean terminated = false;

        // If the pattern is empty, match only completely empty entries, e.g., none as
        // there will always be a leading forward slash.
        if (pattern.isEmpty()) {
            return Pattern.compile("^$");
        }

        // Iterate over the characters
        for (int pos = 0; pos < pattern.length(); ++pos) {
            char ch = pattern.charAt(pos);

            if (ch == '\\') {
                // Handle escaped * and $ characters
                char nch = pos < pattern.length() - 1 ? pattern.charAt(pos + 1) : 0;
                if (nch == '*' || ch == '$') {
                    quoteBuf.append(nch);
                    ++pos;
                } else {
                    quoteBuf.append(ch);
                }
            } else if (ch == '*') {
                if (quoteBuf.length() > 0) {
                    // The quoted character buffer is not empty, so add them before adding
                    // the wildcard matcher
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                }
                if (pos == pattern.length() - 1) {
                    terminated = true;
                    regexp.append(".*");
                } else {
                    regexp.append(".+");
                }
            } else if (ch == '$' && pos == pattern.length() - 1) {
                if (quoteBuf.length() > 0) {
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                }
                regexp.append(ch);
                terminated = true;
            } else {
                quoteBuf.append(ch);
            }
        }

        if (quoteBuf.length() > 0) {
            regexp.append("\\Q").append(quoteBuf).append("\\E");
        }

        if (!terminated) {
            regexp.append(".*");
        }

        return Pattern.compile(regexp.toString());
    }

    /**
     * Check if the specified path matches this rule
     *
     * @param path The path
     * @return True when the path matches, false when it does not
     */
    public boolean matches(String path) {
        return this.pattern.matcher(path).matches();
    }
}
