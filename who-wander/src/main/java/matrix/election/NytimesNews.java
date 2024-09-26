package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 纽约时报,正文详情页有IP级风控，只能显示一半文本，暂未处理
 */
public class NytimesNews {
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
            //singleUniverse.send(getNytimesSearch(key.replace(" ", "%20")));
            singleUniverse.send(getNytimesDetail("https://www.nytimes.com/2024/09/19/business/trump-media-lockup-expires.html"));
            Thread.sleep(1000);
        }
    }

    /**
     * 纽约时报搜索，默认10条结果，10条后有加密暂未获取
     * @return
     */
    public static HttpRequest getNytimesSearch(String searchKey) {
        String url = "https://www.nytimes.com/search?query=" + searchKey;
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_NYTIMES_SEARCH);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getNytimesDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_NYTIMES_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
