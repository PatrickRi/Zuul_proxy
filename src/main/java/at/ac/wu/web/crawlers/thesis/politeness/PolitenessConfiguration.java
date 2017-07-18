package at.ac.wu.web.crawlers.thesis.politeness;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;

/**
 * Created by Patrick on 13.07.2017.
 */
@Component
@PropertySource("classpath:politeness.yml")
@ConfigurationProperties
public class PolitenessConfiguration {

    private HashSet<PolitenessEntry> domains = new HashSet<>();

//    @Value("${abc}")
    private PolitenessEntry abc;

//    @Value("${use-default-domain}")
//    private String filePath;

    public HashSet<PolitenessEntry> getDomains() {
        return domains;
    }

    public PolitenessConfiguration setDomains(HashSet<PolitenessEntry> domains) {
        this.domains = domains;
        return this;
    }

    public PolitenessConfiguration setAbc(PolitenessEntry abc) {
        this.abc = abc;
        return this;
    }

    public PolitenessEntry getConfig(String domain) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        for (PolitenessEntry entry : this.domains) {
//            if(Pattern.compile(entry.getDomain()).matcher(domain).matches())  {
//                return entry;
//            }
            if (matcher.matchStart(entry.getDomain(), domain)) {
                return entry;
            }
        }
        return null;
    }

    public int getDefaultDelay()  {
        return abc.getDelay();
    }
}
