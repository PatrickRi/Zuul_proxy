package at.ac.wu.web.crawlers.thesis.http;

import org.apache.http.*;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTPS_SCHEME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTP_SCHEME;

/**
 * Created by Patrick on 18.07.2017.
 */
@Configuration
public class HttpUtils {

    private PoolingHttpClientConnectionManager connectionManager;
    private final Timer connectionManagerTimer = new Timer(
            "HttpUtils.connectionManagerTimer", true);
    private CloseableHttpClient httpClient;

    @PostConstruct
    private void initialize() {
        this.httpClient = newClient();
        this.connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (HttpUtils.this.connectionManager == null) {
                    return;
                }
                HttpUtils.this.connectionManager.closeExpiredConnections();
            }
        }, 30000, 5000);
    }

    public CloseableHttpClient newClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(100_000)
                .setConnectTimeout(100_000)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
//        if (!this.sslHostnameValidationEnabled) { //default true
//            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
//        }
        return httpClientBuilder.setConnectionManager(newConnectionManager())
                .disableContentCompression()
                .useSystemProperties().setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setRedirectStrategy(new RedirectStrategy() {
                    @Override
                    public boolean isRedirected(HttpRequest request,
                                                HttpResponse response, HttpContext context)
                            throws ProtocolException {
                        return false;
                    }

                    @Override
                    public HttpUriRequest getRedirect(HttpRequest request,
                                                      HttpResponse response, HttpContext context)
                            throws ProtocolException {
                        return null;
                    }
                }).build();
    }

    public PoolingHttpClientConnectionManager newConnectionManager() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates,
                                               String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates,
                                               String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new SecureRandom());

            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                    .<ConnectionSocketFactory>create()
                    .register(HTTP_SCHEME, PlainConnectionSocketFactory.INSTANCE);
//            if (this.sslHostnameValidationEnabled) { //default true
            registryBuilder.register(HTTPS_SCHEME,
                    new SSLConnectionSocketFactory(sslContext));
//            }
//            else {
//                registryBuilder.register(HTTPS_SCHEME, new SSLConnectionSocketFactory(
//                        sslContext, NoopHostnameVerifier.INSTANCE));
//            }
            final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

            this.connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null,
                    -1, TimeUnit.MILLISECONDS);
            this.connectionManager
                    .setMaxTotal(2000);
            this.connectionManager.setDefaultMaxPerRoute(
                    1000);
            return this.connectionManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void refreshClient()  {
        try {
            httpClient.close();
        }
        catch (IOException ex) {
        }
        this.httpClient = newClient();
    }

    public HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
                host.getProtocol());
        return httpHost;
    }

    public HttpRequest buildHttpRequest(String verb, String uri,
                                           InputStreamEntity entity, MultiValueMap<String, String> headers,
                                           MultiValueMap<String, String> params, HttpServletRequest request) {
        HttpRequest httpRequest;
        String uriWithQueryString = uri + /*(this.forceOriginalQueryStringEncoding
                ?*/ getEncodedQueryString(request)/* : this.helper.getQueryString(params))*/;

        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uriWithQueryString);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uriWithQueryString);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uriWithQueryString);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            case "DELETE":
                BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
                        verb, uriWithQueryString);
                httpRequest = entityRequest;
                entityRequest.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uriWithQueryString);
        }

        httpRequest.setHeaders(convertHeaders(headers));
        return httpRequest;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private String getEncodedQueryString(HttpServletRequest request) {
        if(request == null)  {
            return "";
        }
        String query = request.getQueryString();
        return (query != null) ? "?" + query : "";
    }
}
