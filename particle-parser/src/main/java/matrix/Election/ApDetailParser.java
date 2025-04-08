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

public class ApDetailParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_AP_DETAIL;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到ap detail");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        System.out.println("透传消息：" + httpRequest.getTransport());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System.out.println(doc);
        try {
            String title = doc.select("h1[class=Page-headline]").text();
            System.out.println(title);
            //文章详情文本位置
            Elements ele = doc.select("div[class=RichTextStoryBody RichTextBody]");
            System.out.println(ele.text());
            //存储结果
            //样例参考,建议存到数据库
            //String searchKey = httpRequest.getTransport().optString("searchKey");
            //FileUtils.appendLine("xxxx.txt", searchKey+"\t\t\t"+title+"\t"+ele);

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("无数据 " + httpRequest.getUrl());
        }

    }
}
