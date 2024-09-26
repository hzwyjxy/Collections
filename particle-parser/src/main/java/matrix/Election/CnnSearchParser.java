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

public class CnnSearchParser extends BaseParticleParser {
    public String getCategory() {
        return Category.ELECTION_CNN_SEARCH;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        return true;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        System.out.println("达到cnn");
        HttpResponse httpResponse= (HttpResponse)response;
        HttpRequest httpRequest =(HttpRequest)response.request;
        System.out.println(httpRequest.getUrl());
        Document doc = Jsoup.parse(httpResponse.getResultPage());
        //System System.out.println(doc);
        //System.out.println(body);
        JSONObject jo = new JSONObject(doc.body().text());
        System.out.println(jo);
        JSONArray result = jo.optJSONArray("result");
        for(Object o : result) {
            JSONObject joo = new JSONObject(o.toString());
            String detailUrl = joo.optString("path");
            System.out.println(detailUrl);

            HttpRequest request = new HttpRequest("GET", Category.ELECTION_CNN_DETAIL);
            request.setUrl(detailUrl);
            //universe.send(request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
