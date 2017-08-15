package at.ac.wu.web.crawlers.thesis.politeness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.*;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Helper class used to extract all properties from the Spring environment
 * and build a List of {@link PolitenessEntry} from the relevant ones.
 * <p>
 * Created by Patrick on 08.08.2017.
 */
//Totally stolen from org.springframework.cloud.context.refresh.ContextRefresher
public class PropertyExtractionHelper {

    private static Logger log = LoggerFactory.getLogger(PropertyExtractionHelper.class);

    private static Set<String> standardSources = new HashSet<>(
            Arrays.asList(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                          StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                          StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME,
                          StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME,
                          StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));

    private static Pattern p = Pattern.compile("politeness.domains\\[\\d\\].domain");

    /**
     * Extracts all properties from the given environment and builds a list
     * out of the relevant properties (politeness.domains[\d].*)
     *
     * @param env Environment
     * @return List of {@link PolitenessEntry}
     */
    public List<PolitenessEntry> getEntriesFromProperties(AbstractEnvironment env) {
        Map<String, Object> properties = extract(env.getPropertySources());
        List<PolitenessEntry> list = new ArrayList<>();
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            if (p.matcher(prop.getKey()).matches()) {
                Object value_delay = properties.get(getDelayKey(prop.getKey()));
                try {
                    int delay = Integer.parseInt(value_delay.toString());
                    String domain = prop.getValue().toString();
                    list.add(new PolitenessEntry(domain, delay, 0, delay));
                } catch (Exception ex) {
                    //do nothing
                    log.debug("An error occured parsing data for " + prop.getKey(), ex);
                }
            }
        }
        return list;
    }

    private String getDelayKey(String key) {
        String[] elements = key.split("\\.");
        return elements[0] + "." + elements[1] + ".delay";
    }


    private Map<String, Object> extract(MutablePropertySources propertySources) {
        Map<String, Object> result = new HashMap<>();
        List<PropertySource<?>> sources = new ArrayList<>();
        for (PropertySource<?> source : propertySources) {
            sources.add(0, source);
        }
        for (PropertySource<?> source : sources) {
            if (!this.standardSources.contains(source.getName())) {
                extract(source, result);
            }
        }
        return result;
    }

    private void extract(PropertySource<?> parent, Map<String, Object> result) {
        if (parent instanceof CompositePropertySource) {
            try {
                List<PropertySource<?>> sources = new ArrayList<>();
                for (PropertySource<?> source : ((CompositePropertySource) parent)
                        .getPropertySources()) {
                    sources.add(0, source);
                }
                for (PropertySource<?> source : sources) {
                    extract(source, result);
                }
            } catch (Exception e) {
                return;
            }
        } else if (parent instanceof EnumerablePropertySource) {
            for (String key : ((EnumerablePropertySource<?>) parent).getPropertyNames()) {
                result.put(key, parent.getProperty(key));
            }
        }
    }
}
