package com.hzwyjxy;

import common.HttpGetDownloader;
import model.HttpRequest;
import model.HttpResponse;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        String url ="https://www.baidu.com";
        HttpRequest httpRequest = new HttpRequest("GET","");
        httpRequest.setUrl(url);
        HttpResponse httpResponse = HttpGetDownloader.get(httpRequest);
        System.out.println(httpResponse.getResultPage());
    }
}