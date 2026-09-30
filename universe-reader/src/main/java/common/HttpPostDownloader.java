package common;

import model.HttpRequest;
import model.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class HttpPostDownloader extends BaseHttpDownloader {

    public static HttpResponse post(HttpRequest httpRequest) {
        HttpResponse httpResponse = new HttpResponse();
        try {
            String url = httpRequest.getUrl();
            HttpPost httpPost = new HttpPost(url);
            //设置post内容（body 为空时发送空体，避免 NPE）
            String payload = httpRequest.getBody() == null ? "" : httpRequest.getBody().toString();
            StringEntity stringEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            applyHeaders(httpPost, httpRequest);
            CloseableHttpResponse response = client.execute(httpPost);
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
        HttpRequest httpRequest = new HttpRequest("POST","");
        httpRequest.setUrl(url);
        httpRequest.setBody(new org.json.JSONObject());
        HttpResponse httpResponse = HttpPostDownloader.post(httpRequest);
        System.out.println(httpResponse.getResultPage());
    }

}
