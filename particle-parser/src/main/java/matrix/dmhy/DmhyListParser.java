package matrix.dmhy;

import index.Category;
import matrix.BaseParticleParser;
import model.AbstractResponse;
import model.HttpRequest;
import model.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DmhyListParser extends BaseParticleParser {
    @Override
    public String getCategory() {
        return Category.DMHY_LIST_PAGE;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response) {
        System.out.println("达到dmhy");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System.out.println(doc);
        Elements trs=doc.select("table#topic_list").select("tbody").get(0).select("tr");
        for(Element tr: trs){
            String title=tr.select("td").get(2).text();
            System.out.println(title);
            break;
        }
        //System.out.println(body);
    }
}
