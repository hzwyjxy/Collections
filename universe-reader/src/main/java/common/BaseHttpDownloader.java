package common;

import model.HttpRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseHttpDownloader {

    /**
     * 默认请求头，请求可通过 HttpRequest#addHeader 覆盖
     */
    private static final Map<String, String> DEFAULT_HEADERS = new LinkedHashMap<>();

    static {
        DEFAULT_HEADERS.put("accept", "*/*");
        //仅声明 HttpClient5 默认能解码的编码，勿加 br/zstd（无对应解码器会导致正文乱码）
        DEFAULT_HEADERS.put("accept-encoding", "gzip, deflate");
        DEFAULT_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36 Edg/135.0.0.0");
    }

    public static CloseableHttpClient client = null;
    private static PoolingHttpClientConnectionManager connectionManager;
    static {
        connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(SSLContexts.createSystemDefault())
                        //.setTlsVersions(TLS.V_1_2)
                        .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        // 与响应超时(30s)对齐，避免长响应被过短的读超时截断
                        .setSoTimeout(Timeout.ofSeconds(30))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setConnectionTimeToLive(TimeValue.ofMinutes(1L))
                .build();
        client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(30))
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                //设置全局下载代理
                //.setProxy(new HttpHost("127.0.0.1", 10808))
                .build();
    }

    /**
     * 为单个请求应用请求头：
     * 先用默认头，再用请求自定义头覆盖，最后处理 cookie。
     */
    protected static void applyHeaders(HttpMessage message, HttpRequest httpRequest) {
        for (Map.Entry<String, String> entry : DEFAULT_HEADERS.entrySet()) {
            message.setHeader(entry.getKey(), entry.getValue());
        }
        if (httpRequest.getHeaders() != null) {
            for (Map.Entry<String, String> entry : httpRequest.getHeaders().entrySet()) {
                message.setHeader(entry.getKey(), entry.getValue());
            }
        }
        String cookie = httpRequest.getCookie();
        if (cookie != null && !cookie.isEmpty()) {
            message.setHeader("cookie", cookie);
        }
    }
}
