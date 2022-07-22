package common;

import model.HttpRequest;
import model.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class HttpPostDownloader extends BaseHttpDownloader {

    public static HttpResponse get(HttpRequest httpRequest) {
        HttpResponse httpResponse = new HttpResponse();
        try {
            String url = httpRequest.getUrl();
            HttpPost httpPost = new HttpPost(url);
            //设置post内容
            StringEntity stringEntity = new StringEntity(httpRequest.getBody().toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse response = client.execute(httpPost);
            httpResponse.setHttpCode(response.getCode());
            String result = EntityUtils.toString(response.getEntity(),"utf-8");
            httpResponse.setResultPage(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpResponse;
    }

    public final static void main(final String[] args) throws Exception {
        String url ="https://www.baidu.com";
        HttpRequest httpRequest = new HttpRequest(url);
        HttpResponse httpResponse = HttpPostDownloader.get(httpRequest);
        System.out.println(httpResponse.getResultPage());
    }

}
