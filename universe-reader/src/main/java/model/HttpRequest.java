package model;

import org.json.JSONObject;

public class HttpRequest extends AbstractRequest {
    String url;
    String cookie;
    JSONObject body;
    JSONObject transport;

    public HttpRequest(String type, String categeory) {
        this.type = type;
        this.category =categeory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public JSONObject getBody() {
        return body;
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    public JSONObject getTransport() {
        return transport;
    }

    public void setTransport(JSONObject transport) {
        this.transport = transport;
    }

}
