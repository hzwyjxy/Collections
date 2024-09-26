package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 赫芬顿邮报。无搜索，取政治新闻列表
 */
public class HuffPostNews {
    static SingleUniverse singleUniverse;

    static {
        //生成下载器
        singleUniverse = new SingleUniverse();
        singleUniverse.create();
        //生成解析器
        new ParticleParser(singleUniverse, new ElectionIndex());
    }

    public final static void main(final String[] args) throws Exception {

//        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
//        System.out.println(httpResponse.getResultPage());
        for(int i=0;i<1;i++) {
            //singleUniverse.send(getHuffPostSearch(1));
            singleUniverse.send(getHuffPostDetail("https://www.huffpost.com/entry/civil-fraud-verdict-trump-lawyers-appeals-court_n_66f1de67e4b0451ba129e9c8"));
            Thread.sleep(1000);
        }
    }

    /**
     * https://www.latimes.com/search?q=trump+trump&s=0
     * la搜索，可翻页
     * @return
     */
    public static HttpRequest getHuffPostSearch(int page) {
        String url = "https://www.huffpost.com/news/politics?page=1" + page;
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_HUFFPOST_SEARCH);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getHuffPostDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_HUFFPOST_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
