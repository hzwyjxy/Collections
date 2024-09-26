package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 路透社，详情有强浏览器js检测，只能拿到搜索结果，暂无法获取详情。思路：用selinium或者puppeteer应该能过（开发成本高）
 */
public class ReutersNews {
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
            String key = "trump trump"; //如有空格需转译
            //singleUniverse.send(getReutersSearch(key.replace(" ", "%20")));
            singleUniverse.send(getReutersDetail("https://www.reuters.com/world/biden-announces-8-billion-military-aid-ukraine-2024-09-26/"));
            Thread.sleep(1000);
        }
    }

    /**
     * 原搜索网页https://www.reuters.com/site-search/?query=trump+trump&offset=0
     * @return
     */
    public static HttpRequest getReutersSearch(String searchKey) {
        String url = "https://www.reuters.com/pf/api/v3/content/fetch/articles-by-search-v2?query=%7B%22keyword%22%3A%22" +
                searchKey +
                "%22%2C%22offset%22%3A0%2C%22orderby%22%3A%22display_date%3Adesc%22%2C%22size%22%3A20%2C%22website" +
                "%22%3A%22reuters%22%7D&d=216&_website=reuters";
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_REUTERS_SEARCH);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getReutersDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_REUTERS_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
