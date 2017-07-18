package at.ac.wu.web.crawlers.thesis;

import at.ac.wu.web.crawlers.thesis.http.HttpUtils;
import at.ac.wu.web.crawlers.thesis.politeness.PolitenessCache;
import at.ac.wu.web.crawlers.thesis.politeness.robotstxt.RobotstxtServer;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.TraceProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static j2html.TagCreator.*;

/**
 * Created by Patrick on 04.03.2017.
 */
public class SimpleFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);
    private final ProxyRequestHelper helper;

    @Autowired
    PolitenessCache cache;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    RobotstxtServer robotsTxt;

    @EventListener
    public void onPropertyChange(EnvironmentChangeEvent event) {
        boolean createNewClient = false;

        for (String key : event.getKeys()) {
            if (key.startsWith("zuul.host.")) {
                createNewClient = true;
                break;
            }
        }

        if (createNewClient) {
            httpUtils.refreshClient();
        }
    }

    public SimpleFilter(TraceProxyRequestHelper helper) {
        this.helper = helper;
    }

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        try {
            URL targetURL = new URL(request.getRequestURL().toString());
            if (!robotsTxt.allows(targetURL)) {
                log.debug(request.getRequestURL().toString() + " blocked because of robots.txt");
                String html = html(
                        head(
                                title("Request Blocked")
                        ),
                        body(h1("Request Blocked"), p("Blocked because URL is excluded from allowed URLS.")
                        )
                ).render();
                InputStream content = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
                this.helper.setResponse(403, content, new HttpHeaders());
                return null;
            }
            if (!cache.getCache().isAllowed(httpUtils.getHttpHost(targetURL).getHostName())) {
                int delayForDomain = cache.getCache().getDelayForDomain(httpUtils.getHttpHost(targetURL).getHostName());
                log.debug(request.getRequestURL().toString() + " blocked because of configured delay of " + delayForDomain);
                //https://tools.ietf.org/html/rfc6585 - include retry header and html error
                String html = html(
                        head(
                                title("Too Many Requests")
                        ),
                        body(h1("Too Many Requests"), p("There must be a delay of " + delayForDomain + " milliseconds between each request.")
                        )
                ).render();
                InputStream content = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
                HttpHeaders headers = new HttpHeaders();
                headers.set("Retry-After", ""+ delayForDomain);
                this.helper.setResponse(429, content, headers);
                return null;
            }
        } catch (Exception ex) {
            throw new ZuulRuntimeException(ex);
        }
        MultiValueMap<String, String> headers = this.helper
                .buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper
                .buildZuulRequestQueryParams(request);
        String verb = request.getMethod().toUpperCase();
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        String uri = this.helper.buildZuulRequestURI(request);
        this.helper.addIgnoredHeaders();

        try {
            CloseableHttpResponse response = forward(httpUtils.getHttpClient(), verb, request.getRequestURL().toString(), request,
                    headers, params, requestEntity);
            setResponse(response);
        } catch (Exception ex) {
            throw new ZuulRuntimeException(ex);
        }
        return null;
    }

    private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb,
                                          String uri, HttpServletRequest request, MultiValueMap<String, String> headers,
                                          MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
                requestEntity);
        HttpHost httpHost = httpUtils.getHttpHost(new URL(uri));
        int contentLength = request.getContentLength();

        ContentType contentType = null;

        if (request.getContentType() != null) {
            contentType = ContentType.parse(request.getContentType());
        }

        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength, contentType);

        HttpRequest httpRequest = httpUtils.buildHttpRequest(verb, uri, entity, headers, params, request);
        try {
            log.debug(httpHost.getHostName() + " " + httpHost.getPort() + " "
                    + httpHost.getSchemeName());
            CloseableHttpResponse zuulResponse = forwardRequest(httpclient, httpHost,
                    httpRequest);
            this.cache.getCache().add(httpHost.getHostName());
            this.helper.appendDebug(info, zuulResponse.getStatusLine().getStatusCode(),
                    revertHeaders(zuulResponse.getAllHeaders()));
            return zuulResponse;
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            // httpclient.getConnectionManager().shutdown();
        }
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient,
                                                 HttpHost httpHost, HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private void setResponse(HttpResponse response) throws IOException {
        RequestContext.getCurrentContext().set("zuulResponse", response);
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

}