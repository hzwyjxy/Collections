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

public class BBCListParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_BBC_ELECTION_LIST;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到bbc");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System System.out.println(doc);
        //System.out.println(body);
        JSONObject jo = new JSONObject(doc.body().text());
        System.out.println(jo);
        JSONArray result = jo.optJSONArray("data");
        for(Object o : result) {
            JSONObject joo = new JSONObject(o.toString());
            String detailUrl = joo.optString("path");
            System.out.println(detailUrl);

            HttpRequest request = new HttpRequest("GET", Category.ELECTION_BBC_ELECTION_DETAIL);
            request.setUrl("https://www.bbc.com" + detailUrl);
            //universe.send(request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
