package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 华盛顿邮报, tip：ttl设置成30s才能下载，可能是网不好
 */
public class WpNews {
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
            //singleUniverse.send(getWpSearch(key));
            singleUniverse.send(getWpDetail("https://www.washingtonpost.com/elections/2024/09/25/trump-women-voters-harris/"));
            Thread.sleep(1000);
        }
    }

    /**
     * https://www.washingtonpost.com/search/?query=trump+trump
     * wp搜索
     * @return
     */
    public static HttpRequest getWpSearch(String searchKey) {
        String url = "https://www.washingtonpost.com/search/api/search/";
        JSONObject body = new JSONObject("{\n" +
                "  \"searchTerm\": \""+ searchKey +"\",\n" +
                "  \"filters\": {\n" +
                "    \"sortBy\": \"relevancy\",\n" +
                "    \"dateRestrict\": \"\",\n" +
                "    \"start\": 0,\n" +
                "    \"author\": \"\",\n" +
                "    \"section\": \"\",\n" +
                "    \"nextPageToken\": \"\"\n" +
                "  }\n" +
                "}");
        HttpRequest httpRequest = new HttpRequest("POST", Category.ELECTION_WP_SEARCH);
        httpRequest.setUrl(url);
        httpRequest.setBody(body);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getWpDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_WP_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
