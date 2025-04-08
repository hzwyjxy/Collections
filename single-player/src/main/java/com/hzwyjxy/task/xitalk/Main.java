package com.hzwyjxy.task.xitalk;

import common.HttpGetDownloader;
import model.HttpRequest;
import model.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;


public class Main {
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
        while(true) {
            HttpRequest httpRequest = new HttpRequest("GET","");
            httpRequest.setUrl(url);
            HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
            String unicodeStr = httpResponse.getResultPage();
            if(httpResponse.getHttpCode() != 200) {
                System.out.println("list 下载失败 " + url);
                Thread.sleep(1000*60);
                continue;
            }
//        byte[] utf8Bytes = unicodeStr.getBytes("Unicode");
//        System.out.println(new String(utf8Bytes,"UTF-8"));
            JSONObject result =new JSONObject(unicodeStr);
            JSONArray list = result.optJSONArray("list");
            for(Object o : list) {
                JSONObject jo =new JSONObject(o.toString());
                String articleId = jo.optString("article_id");
                System.out.println(articleId);
                getArticle("https://jhsjk.people.cn/article/" + articleId);
                Thread.sleep(1000* 10);
            }
            return;
        }
    }

    public static void getArticle(String url) throws InterruptedException {
        System.out.println(url);
        while (true) {
            HttpRequest httpRequest = new HttpRequest("GET", "");
            httpRequest.setUrl(url);
            HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
            String unicodeStr = httpResponse.getResultPage();
            if (httpResponse.getHttpCode() != 200) {
                System.out.println("article 下载失败 " + url);
                Thread.sleep(1000 * 60);
                continue;
            }
            Document doc = new Document(unicodeStr);
            System.out.println(unicodeStr);
            String totalStr = doc.select("div[class=d2txt clearfix]").text();
            System.out.println(totalStr);
            return;
            }
    }



}
