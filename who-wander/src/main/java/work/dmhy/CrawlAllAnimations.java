package work.dmhy;


import common.HttpGetDownloader;
import model.HttpRequest;
import model.HttpResponse;

public class CrawlAllAnimations {
    public final static void main(final String[] args) throws Exception {
        String url ="https://www.baidu.com";
        HttpRequest httpRequest = new HttpRequest(url);
        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
        System.out.println(httpResponse.getResultPage());
    }
}
