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
            //httpget.setHeader("User-Agent", "");
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
