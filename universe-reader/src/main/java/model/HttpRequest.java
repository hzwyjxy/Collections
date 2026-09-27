package model;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest extends AbstractRequest {
    String url;
    String cookie;
    JSONObject body;
    JSONObject transport;
    Map<String, String> headers = new LinkedHashMap<>();

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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers == null ? new LinkedHashMap<>() : headers;
    }

    /**
     * 添加单个自定义 header
     */
    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * 批量添加自定义 header
     */
    public void addHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

}
