package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

/**
 * 卫报
 */
public class GuardianNews {
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
            //singleUniverse.send(getGuardianSearch(i));
            singleUniverse.send(getGuardianDetail("https://www.theguardian.com/us-news/2024/sep/22/national-security-officials-endorse-harris"));
            Thread.sleep(1000 * 10);
        }
    }

    /**
     * https://www.theguardian.com/us-news/us-elections-2024?page=1
     * Guardian大选列表页api，页数范围建议1-100页以上
     * @param page
     * @return
     */
    public static HttpRequest getGuardianSearch(int page) {
        String url = "https://www.theguardian.com/us-news/us-elections-2024?page=" + page;
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_GUARDIAN_ELECTION_LIST);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getGuardianDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_GUARDIAN_ELECTION_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
