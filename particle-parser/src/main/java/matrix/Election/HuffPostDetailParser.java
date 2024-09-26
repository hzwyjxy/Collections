package matrix.Election;

import factory.AbstractUniverse;
import index.Category;
import matrix.BaseParticleParser;
import model.AbstractResponse;
import model.HttpRequest;
import model.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HuffPostDetailParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_HUFFPOST_DETAIL;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到HP detail");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        System.out.println("透传消息：" + httpRequest.getTransport());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System.out.println(doc);
        try {
            //标题
            String title = doc.select("h1[class=headline]").text();
            System.out.println(title);
            //文章详情文本位置
            Elements ele = doc.select("section[id=entry-body]").select("p");
            String article = ele.text();
            System.out.println(article);
            //存储结果

        }catch (Exception e) {
            System.out.println("无数据 " + httpRequest.getUrl());
        }

    }
}