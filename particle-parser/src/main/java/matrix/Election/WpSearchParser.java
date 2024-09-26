package matrix.Election;

import factory.AbstractUniverse;
import index.Category;
import matrix.BaseParticleParser;
import model.AbstractResponse;
import model.HttpRequest;
import model.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WpSearchParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_WP_SEARCH;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到REUTERS");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        System.out.println(doc);
        //System.out.println(body);
        JSONObject jo = new JSONObject(doc.body().text());
        //System.out.println(jo);
        JSONArray result = jo.optJSONObject("body").optJSONArray("items");
        for(Object o : result) {
            JSONObject joo = new JSONObject(o.toString());
            String detailUrl = joo.optString("link");
            System.out.println(detailUrl);

            HttpRequest request = new HttpRequest("GET", Category.ELECTION_WP_DETAIL);
            request.setUrl(detailUrl);
            request.setTransport(request.getTransport());
            universe.send(request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
