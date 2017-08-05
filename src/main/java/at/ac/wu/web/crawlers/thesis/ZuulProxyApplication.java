package at.ac.wu.web.crawlers.thesis;

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
    public SendErrorFilter errorFilter() { //TODO change to return special error code, not forward to /error!
        return new SendErrorFilter();
    }

//    @Bean
//    public SendResponseFilter responseFilter() {
//        return new SendResponseFilter();
//    }

    @Bean
    public PreDecorationFilter decorationFilter(RouteLocator routeLocator) {
        return new PreDecorationFilter(routeLocator, this.server.getServlet().getServletPrefix(), this.zuulProperties, new ProxyRequestHelper());
    }

//    @Bean
//    public EmbeddedServletContainerFactory servletContainer() {
//        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//            @Override
//            protected void postProcessContext(Context context) {
//                SecurityConstraint securityConstraint = new SecurityConstraint();
//                securityConstraint.setUserConstraint("CONFIDENTIAL");
//                SecurityCollection collection = new SecurityCollection();
//                collection.addPattern("/*");
//                securityConstraint.addCollection(collection);
//                context.addConstraint(securityConstraint);
//            }
//        };
//
//        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
//        return tomcat;
//    }
//
//    private Connector initiateHttpConnector() {
//        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//        connector.setScheme("http");
//        connector.setPort(8080);
//        connector.setSecure(false);
//        connector.setRedirectPort(8443);
//
//        return connector;
//    }
}
