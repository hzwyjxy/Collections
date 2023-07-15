package matrix.dmhy;


import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.DmhyIndex;
import model.HttpRequest;

public class CrawlAllAnimations {
    static SingleUniverse singleUniverse;

    static {
        //生成下载器
        singleUniverse = new SingleUniverse();
        singleUniverse.create();
        //生成解析器
        new ParticleParser(singleUniverse, new DmhyIndex());
    }

    public final static void main(final String[] args) throws Exception {

//        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
//        System.out.println(httpResponse.getResultPage());
        for(int i=1;i<320;i++) {
            singleUniverse.send(getDmhyCompleteList(i));
            Thread.sleep(500);
        }
    }

    public static HttpRequest getDmhyCompleteList(int page) {
        String url = "https://share.dmhy.org/topics/list/sort_id/31/page/" + page;
        HttpRequest httpRequest = new HttpRequest("GET", Category.DMHY_LIST_PAGE);
        httpRequest.setUrl(url);
        return httpRequest;
    }
}
