package at.ac.wu.web.crawlers.thesis.canonicalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Service used for normalizing URLs.
 * <p>
 * Created by Patrick on 07.08.2017.
 */
@Service
public class URLCanonicalizationService {

    /**
     * Pattern to detect whether a path can be normalized.
     */
    private final static Pattern hasNormalizablePathPattern = Pattern.compile("/[./]|[.]/");
    /**
     * find encoded parts
     */
    private final static Pattern unescapeRulePattern = Pattern.compile("%([0-9A-Fa-f]{2})");
    /**
     * characters which should not be escaped
     */
    private final static boolean[] unescapedCharacters = new boolean[128];
    private static Logger LOG = LoggerFactory.getLogger(URLCanonicalizationService.class);

    static {
        for (int c = 0; c < 128; c++) {
            /*
             * https://tools.ietf.org/html/rfc3986#section-2.2 For consistency,
             * percent-encoded octets in the ranges of ALPHA (%41-%5A and
             * %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E), underscore
             * (%5F), or tilde (%7E) should not be created by URI producers and,
             * when found in a URI, should be decoded to their corresponding
             * unreserved characters by URI normalizers.
             */
            if ((0x41 <= c && c <= 0x5A) || (0x61 <= c && c <= 0x7A) || (0x30 <= c && c <= 0x39) || c == 0x2D || c ==
                    0x2E || c == 0x5F || c == 0x7E) {
                unescapedCharacters[c] = true;
            } else {
                unescapedCharacters[c] = false;
            }
        }
    }

    public String normalize(String input) {
        if (StringUtils.isEmpty(input.trim())) {
            return null;
        }
        String urlString = input.trim();

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOG.info("Malformed URL {}", urlString);
            return null;
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String file = url.getFile();

        boolean changed = false;

        if (!urlString.startsWith(protocol)) {
            changed = true;
        }

        if ("http".equals(protocol) || "https".equals(protocol)) {

            if (host != null && url.getAuthority() != null) {
                String newHost = host.toLowerCase(Locale.ROOT); // to lowercase
                // host
                if (!host.equals(newHost)) {
                    host = newHost;
                    changed = true;
                } else if (!url.getAuthority().equals(newHost)) {
                    // authority (http://<...>/) contains other elements (port,
                    // user, etc.) which will likely cause a change if left away
                    changed = true;
                }
            } else {
                // no host or authority: recompose the URL from components
                changed = true;
            }

            if (port == url.getDefaultPort()) { // uses default port
                port = -1; // so don't specify it
                changed = true;
            }

            if (file == null || "".equals(file)) { // add a slash
                file = "/";
                changed = true;
            }

            if (url.getRef() != null) { // remove the ref
                changed = true;
            }

            // check for unnecessary use of "/../", "/./", and "//"
            String file2 = null;
            try {
                file2 = getFileWithNormalizedPath(url);
            } catch (MalformedURLException e) {
                LOG.info("Malformed URL {}", url);
                return null;
            }
            if (!file.equals(file2)) {
                changed = true;
                file = file2;
            }
        }

        // properly encode characters in path/file using percent-encoding
        String file2 = unescapePath(file);
        file2 = escapePath(file2);
        if (!file.equals(file2)) {
            changed = true;
            file = file2;
        }

        if (changed) {
            try {
                urlString = new URL(protocol, host, port, file).toString();
            } catch (MalformedURLException e) {
                LOG.info("Malformed URL {}{}{}{}", protocol, host, port, file);
                return null;
            }
        }

        return urlString;
    }

    private String getFileWithNormalizedPath(URL url) throws MalformedURLException {
        String file;

        if (hasNormalizablePathPattern.matcher(url.getPath()).find()) {
            // only normalize the path if there is something to normalize
            // to avoid needless work
            try {
                file = url.toURI().normalize().toURL().getFile();
                // URI.normalize() does not normalize leading dot segments,
                // see also http://tools.ietf.org/html/rfc3986#section-5.2.4
                int start = 0;
                while (file.startsWith("/../", start)) {
                    start += 3;
                }
                if (start > 0) {
                    file = file.substring(start);
                }
            } catch (URISyntaxException e) {
                file = url.getFile();
            }
        } else {
            file = url.getFile();
        }

        // if path is empty return a single slash
        if (file.isEmpty()) {
            file = "/";
        }

        return file;
    }

    /**
     * Remove % encoding from path segment in URL for characters which should be
     * unescaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>.
     */
    private String unescapePath(String path) {
        StringBuilder sb = new StringBuilder();

        Matcher matcher = unescapeRulePattern.matcher(path);

        int end = -1;
        int letter;

        // Traverse over all encoded groups
        while (matcher.find()) {
            // Append everything up to this group
            sb.append(path.substring(end + 1, matcher.start()));

            // Get the integer representation of this hexadecimal encoded
            // character
            letter = Integer.valueOf(matcher.group().substring(1), 16);

            if (letter < 128 && unescapedCharacters[letter]) {
                // character should be unescaped in URLs
                sb.append(new Character((char) letter));
            } else {
                // Append the encoded character as uppercase
                sb.append(matcher.group().toUpperCase(Locale.ROOT));
            }

            end = matcher.start() + 2;
        }

        letter = path.length();

        // Append the rest if there's anything
        if (end <= letter - 1) {
            sb.append(path.substring(end + 1, letter));
        }

        // Ok!
        return sb.toString();
    }

    /**
     * Convert path segment of URL from Unicode to UTF-8 and escape all
     * characters which should be escaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>..
     */
    private String escapePath(String path) {
        StringBuilder sb = new StringBuilder(path.length());

        // Traverse over all bytes in this URL
        for (byte b : path.getBytes(UTF_8)) {
            // Is this a control character?
            if (b < 33 || b == 91 || b == 93) {
                // Start escape sequence
                sb.append('%');

                // Get this byte's hexadecimal representation
                String hex = Integer.toHexString(b & 0xFF).toUpperCase(Locale.ROOT);

                // Do we need to prepend a zero?
                if (hex.length() % 2 != 0) {
                    sb.append('0');
                    sb.append(hex);
                } else {
                    // No, append this hexadecimal representation
                    sb.append(hex);
                }
            } else {
                // No, just append this character as-is
                sb.append((char) b);
            }
        }

        return sb.toString();
    }
}
