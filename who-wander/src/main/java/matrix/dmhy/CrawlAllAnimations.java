package matrix.dmhy;


import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.DmhyIndex;
import model.HttpRequest;

public class CrawlAllAnimations {
    public final static void main(final String[] args) throws Exception {
        String url ="https://www.baidu.com";
        HttpRequest httpRequest = new HttpRequest("GET", Category.DMHY_LIST_PAGE);
        httpRequest.setUrl(url);
//        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
//        System.out.println(httpResponse.getResultPage());
        SingleUniverse singleUniverse = new SingleUniverse();
        singleUniverse.create();
        new ParticleParser(singleUniverse.getResponseQueue(),new DmhyIndex());

        singleUniverse.send(httpRequest);
    }
}
