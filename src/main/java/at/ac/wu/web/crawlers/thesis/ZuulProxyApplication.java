package at.ac.wu.web.crawlers.thesis;

import at.ac.wu.web.crawlers.thesis.cache.PageCache;
import at.ac.wu.web.crawlers.thesis.canonicalization.URLCanonicalizationService;
import at.ac.wu.web.crawlers.thesis.monitoring.CacheMetrics;
import at.ac.wu.web.crawlers.thesis.politeness.DomainDelayCache;
import at.ac.wu.web.crawlers.thesis.politeness.PolitenessCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulServer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.TraceProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.DebugFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

//to start with alternative config:
//java -jar zuul_proxy-0.0.1-SNAPSHOT.jar --spring.config.location=file:///C:/Users/Patrick/Desktop/application.yml

//curl -u monuser:monuser localhost:8081/monitor/env
//curl -X "POST" -u monuser:monuser -H "monuser:monuser" localhost:8081/monitor/env -d property=key
//Refresh needed, also if yml is changed
//curl -X "POST" -u monuser:monuser localhost:8081/monitor/refresh
@SpringBootApplication
@RestController
@EnableZuulServer
public class ZuulProxyApplication {

    @Autowired
    protected ZuulProperties zuulProperties;
    @Autowired
    protected ServerProperties server;
    @Autowired(required = false)
    private TraceRepository traces;

    public static void main(String[] args) {
        SpringApplication.run(ZuulProxyApplication.class, args);
    }

    @Bean
    public TraceProxyRequestHelper proxyRequestHelper() {
        TraceProxyRequestHelper helper = new TraceProxyRequestHelper();
        if (this.traces != null) {
            helper.setTraces(this.traces);
        }
        helper.setIgnoredHeaders(this.zuulProperties.getIgnoredHeaders());
        helper.setTraceRequestBody(this.zuulProperties.isTraceRequestBody());
        return helper;
    }

    @Bean
    @ConditionalOnMissingBean(SimpleFilter.class)
    public SimpleFilter simpleFilter() {
        return new SimpleFilter(new TraceProxyRequestHelper());
    }

    @Bean
    public DebugFilter debugFilter() {
        return new DebugFilter();
    }

    @Bean
    public SendErrorFilter errorFilter() {
        return new SendErrorFilter();
    }

    @Bean
    public PreDecorationFilter decorationFilter(RouteLocator routeLocator) {
        return new PreDecorationFilter(routeLocator, this.server.getServlet().getServletPrefix(), this.zuulProperties, new ProxyRequestHelper());
    }

    @Bean
    public URLCanonicalizationService canonicalizationService() {
        return new URLCanonicalizationService();
    }

    //Eventually try this:
    //https://stackoverflow.com/questions/16251273/can-i-watch-for-single-file-change-with-watchservice-not-the-whole-directory
//    @Bean(name = "configChangeandRefreshBean")
//    public FileSystemWatcher watcher(ContextRefresher contextRefresher)  {
//        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(true, 2000, 500);
//        fileSystemWatcher.setTriggerFilter(new TriggerFileFilter("application.yml"));
//        fileSystemWatcher.addListener((files) -> {
//            contextRefresher.refresh();
//        });
////        new File("./src/main/resources/
////        fileSystemWatcher.addSourceFolders(new ClassPathFolders(new DefaultRestartInitializer().getInitialUrls(Thread.currentThread())));
//        fileSystemWatcher.addSourceFolder(new File("./src/main/resources/"));
//        fileSystemWatcher.start();
//        return fileSystemWatcher;
//    }

    @Bean
    public CacheMetrics cacheMetrics(DomainDelayCache delayCache, PolitenessCache politenessCache, PageCache
            pageCache) {
        return new CacheMetrics(delayCache, politenessCache, pageCache);
    }
}
