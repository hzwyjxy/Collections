package com.hzwyjxy.task.xitalk;

import common.HttpGetDownloader;
import model.HttpRequest;
import model.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;


public class Main {

    private static final int MAX_RETRY = 3;

    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
        for(int i =1;i<=12;i++){
            String url =
                    "https://jhsjk.people.cn/testnew/result?keywords=&isFuzzy=0&searchArea=0" +
                            "&year=0&form=706&type=0&page="+i+"&sortType=2&origin=3&source=2";
            getList(url);
            Thread.sleep(1000*10);
        }
    }

    public static void getList(String url) throws InterruptedException {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            HttpRequest httpRequest = new HttpRequest("GET","");
            httpRequest.setUrl(url);
            HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
            String unicodeStr = httpResponse.getResultPage();
            if(httpResponse.getHttpCode() != 200) {
                System.out.println("list 下载失败 " + url + " (" + (attempt + 1) + "/" + MAX_RETRY + ")");
                Thread.sleep(1000*60);
                continue;
            }
//        byte[] utf8Bytes = unicodeStr.getBytes("Unicode");
//        System.out.println(new String(utf8Bytes,"UTF-8"));
            JSONObject result =new JSONObject(unicodeStr);
            JSONArray list = result.optJSONArray("list");
            if (list == null) {
                System.out.println("list 为空 " + url);
                return;
            }
            for(Object o : list) {
                JSONObject jo =new JSONObject(o.toString());
                String articleId = jo.optString("article_id");
                System.out.println(articleId);
                getArticle("https://jhsjk.people.cn/article/" + articleId);
                Thread.sleep(1000* 10);
            }
            return;
        }
        System.out.println("list 重试 " + MAX_RETRY + " 次仍失败 " + url);
    }

    public static void getArticle(String url) throws InterruptedException {
        System.out.println(url);
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            HttpRequest httpRequest = new HttpRequest("GET", "");
            httpRequest.setUrl(url);
            HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
            String unicodeStr = httpResponse.getResultPage();
            if (httpResponse.getHttpCode() != 200) {
                System.out.println("article 下载失败 " + url + " (" + (attempt + 1) + "/" + MAX_RETRY + ")");
                Thread.sleep(1000 * 60);
                continue;
            }
            // 修复：new Document(str) 会把整段 HTML 当作 baseUri，正文为空，应使用 Jsoup.parse
            Document doc = Jsoup.parse(unicodeStr);
            String totalStr = doc.select("div[class=d2txt clearfix]").text();
            System.out.println(totalStr);
            return;
        }
        System.out.println("article 重试 " + MAX_RETRY + " 次仍失败 " + url);
    }

}
