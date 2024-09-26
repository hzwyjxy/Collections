package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 美联社
 */
public class ApNews {
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
            String key = "trump"; //如有空格需转译
            //singleUniverse.send(getApSearch(key.replace(" ", "+")));
            singleUniverse.send(getApDetail("https://apnews.com/article/trump-iran-assassination-election-harris-09648f806d7a7f52965782e0edc67e96"));
            Thread.sleep(1000);
        }
    }

    /**
     * ap搜索
     * @return
     */
    public static HttpRequest getApSearch(String searchKey) {
        String url = "https://apnews.com/search?q=" + searchKey + "&s=0";
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_AP_SEARCH);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getApDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_AP_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
