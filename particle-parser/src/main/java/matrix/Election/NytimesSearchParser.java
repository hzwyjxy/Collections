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
import org.jsoup.select.Elements;

public class NytimesSearchParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_NYTIMES_SEARCH;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到nytimes");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System System.out.println(doc);
        //System.out.println(body);
        Elements as = doc.select("li[data-testid=search-bodega-result]").select("a");


        for(Element ele : as) {
            String detailUrl = ele.attr("href");
            if(!detailUrl.startsWith("/")) {
                continue;
            }
            detailUrl = detailUrl.substring(0, detailUrl.indexOf("?searchResultPosition"));
            System.out.println(detailUrl);
            HttpRequest request = new HttpRequest("GET", Category.ELECTION_NYTIMES_DETAIL);
            request.setUrl("https://www.nytimes.com" + detailUrl);
            //universe.send(request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
