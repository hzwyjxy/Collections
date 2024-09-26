package matrix.election;

import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.ElectionIndex;
import model.HttpRequest;
import org.json.JSONObject;

public class CNNNews {
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
        for(int i=1;i<2;i++) {
            String key = "123 456"; //空格需转译
            singleUniverse.send(getCnnSearch(key.replace(" ", "%20")));
            //singleUniverse.send(getCnnDetail("https://edition.cnn.com/2024/08/01/tech/china-temu-suppliers-protest-hnk-intl/index.html"));
            Thread.sleep(500);
        }
    }

    /**
     * cnn搜索接口
     * @return
     */
    public static HttpRequest getCnnSearch(String searchKey) {
        String url = "https://search.prod.di.api.cnn.io/content?q=" + searchKey +
                "&size=20&from=0&page=1&sort=newest&request_id=pdx-search-1a782dac-ee9a-49fb-a0fb-69070df402ds";
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_CNN_SEARCH);
        httpRequest.setUrl(url);
        return httpRequest;
    }

    //测试详情页
    public static HttpRequest getCnnDetail(String url) {
        HttpRequest httpRequest = new HttpRequest("GET", Category.ELECTION_CNN_DETAIL);
        httpRequest.setUrl(url);
        httpRequest.setTransport(new JSONObject().put("searchKey", "searchKey"));
        return httpRequest;
    }
}
