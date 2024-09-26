package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import matrix.election.seed.GenerateSeeds;
import model.HttpRequest;
import org.json.JSONObject;

import java.util.List;

/**
 * 洛杉矶时报
 */
public class LaNews {
    static SingleUniverse singleUniverse;

    static {
        //生成下载器
        singleUniverse = new SingleUniverse();
        singleUniverse.create();
        //生成解析器
        new ParticleParser(singleUniverse, new ElectionIndex());
    }

    public final static void main(final String[] args) throws Exception {

        List<String> seeds = GenerateSeeds.getAllSeeds();
        for(String seed : seeds) {
            String key = seed; //"trump trump"; //如有空格需转译
            singleUniverse.send(getLaSearch(key.replace(" ", "+"), 1));
            //singleUniverse.send(getLaDetail("https://www.latimes.com/politics/story/2024-09-25/2024-election-trump-mental-acuity"));
            Thread.sleep(1000 * 10);
        }
    }

    /**
     * https://www.latimes.com/search?q=trump+trump&s=0
     * la搜索，可翻页
     * @return
     */
    public static HttpRequest getLaSearch(String searchKey, int page) {
        String url = "https://www.latimes.com/search?q=" + searchKey + "&p=" + page;
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_LA_SEARCH);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", searchKey));
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getLaDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_LA_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
