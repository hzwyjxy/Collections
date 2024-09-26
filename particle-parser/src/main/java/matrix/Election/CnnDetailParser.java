package matrix.Election;

import factory.AbstractUniverse;
import index.Category;
import matrix.BaseParticleParser;
import model.AbstractResponse;
import model.HttpRequest;
import model.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CnnDetailParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_CNN_DETAIL;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到cnn detail");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        System.out.println("透传消息：" + httpRequest.getTransport());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System.out.println(doc);
        try {
            //cnn文章详情文本位置
            Element ele = doc.select("script[type=application/ld+json]").get(0);
            System.out.println(ele.html());
            //存储结果

        }catch (Exception e) {
            System.out.println("无数据 " + httpRequest.getUrl());
        }

    }
}
