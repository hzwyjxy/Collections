package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

public class BBCNews {
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
        for(int i=0;i<12;i++) {
            //singleUniverse.send(getBBCSearch(i));
            singleUniverse.send(getBBCDetail("https://www.bbc.com/news/articles/c23k0d09d4do"));
            Thread.sleep(1000 * 10);
        }
    }

    /**
     * https://www.bbc.com/news/topics/cj3ergr8209t
     * BBC大选列表页api，页数范围0-11
     * @param page
     * @return
     */
    public static HttpRequest getBBCSearch(int page) {
        String url = "https://web-cdn.api.bbci.co.uk/xd/content-collection/" +
                "37cc4793-d90a-4e83-993d-36c4144c8d5f?country=jp&page=" + page + "&size=9";
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_BBC_ELECTION_LIST);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getBBCDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_BBC_ELECTION_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
