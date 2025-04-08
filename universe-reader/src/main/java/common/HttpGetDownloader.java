package common;

import model.HttpRequest;
import model.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class HttpGetDownloader extends BaseHttpDownloader {

    public static HttpResponse get(HttpRequest httpRequest) {
        HttpResponse httpResponse = new HttpResponse();
        try {
            String url = httpRequest.getUrl();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("accept","*/*");
            httpget.setHeader("accept-encoding","gzip, deflate, br, zstd");
            httpget.setHeader("cookie","__jsluid_s=0fb816c92e84cb6097aa222b4f1f2713; ci_session=5kgd6e2u68oh7sv74a55a1lo8s88f7kn");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36 Edg/135.0.0.0");
            CloseableHttpResponse response = client.execute(httpget);
            httpResponse.setHttpCode(response.getCode());
            String result = EntityUtils.toString(response.getEntity(),"utf-8");
            httpResponse.setResultPage(result);
            httpResponse.category =httpRequest.category;
            httpResponse.request = httpRequest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpResponse;
    }

    public final static void main(final String[] args) throws Exception {
        String url ="https://www.baidu.com";
        HttpRequest httpRequest = new HttpRequest("GET","");
        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
        System.out.println(httpResponse.getResultPage());
    }

}
