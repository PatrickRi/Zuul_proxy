package at.ac.wu.web.crawlers.thesis.politeness.robotstxt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Patrick
 */
public class RobotstxtParser {
    private static final Logger logger = LoggerFactory.getLogger(RobotstxtParser.class);
    private static final Pattern RULE_PATTERN = Pattern.compile("(?i)^([A-Za-z\\-]+):(.*)");
    private static final Set<String> VALID_RULES = new HashSet<>(
        Arrays.asList("allow", "disallow", "user-agent", "crawl-delay", "host", "sitemap"));

    public static HostDirectives parse(String content, RobotstxtConfig config) {
        HostDirectives directives = new HostDirectives(config);
        StringTokenizer st = new StringTokenizer(content, "\n\r");

        Set<String> userAgents = new HashSet<>();
        UserAgentDirectives uaDirectives = null;

        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            // Strip comments
            int commentIndex = line.indexOf('#');
            if (commentIndex > -1) {
                line = line.substring(0, commentIndex);
            }

            // remove any html markup
            line = line.replaceAll("<[^>]+>", "").trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher m = RULE_PATTERN.matcher(line);
            if (m.matches()) {
                String rule = m.group(1).toLowerCase();
                String value = m.group(2).trim();

                if (VALID_RULES.contains(rule)) {
                    if (rule.equals("user-agent")) {
                        String currentUserAgent = value.toLowerCase();
                        if (uaDirectives != null) {
                            // If uaDirectives is not null, this means that one or
                            // more rules followed the User-agent: definition list
                            // In that case, it's not allowed to add more user-agents,
                            // so this is an entirely new set of directives.
                            userAgents = new HashSet<>();
                            directives.addDirectives(uaDirectives);
                            uaDirectives = null;
                        }
                        userAgents.add(currentUserAgent);
                    } else {
                        if (uaDirectives == null) {
                            // No "User-agent": clause defaults to
                            // wildcard UA
                            if (userAgents.isEmpty()) {
                                userAgents.add("*");
                            }
                            uaDirectives = new UserAgentDirectives(userAgents);
                        }
                        uaDirectives.add(rule, value);
                    }
                } else {
                    logger.info("Unrecognized rule in robots.txt: {}", rule);
                }
            } else {
                logger.debug("Unrecognized line in robots.txt: {}", line);
            }
        }

        directives.addDirectives(uaDirectives);
        return directives;
    }
}
