package matrix.ai;

import factory.AbstractUniverse;
import matrix.BaseParticleParser;
import model.AbstractResponse;
import model.HttpRequest;
import model.HttpResponse;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * AI 页面解析器：把页面交给大模型解读，产出结构化 JSON。
 * <p>
 * 与大模型的实际交互由 {@link LargeModelClient} 负责，本类只做：取正文 → 拼提示词 →
 * 调用模型 → 结构化输出。是否启用、用哪个实现、提示词等均由 ai.properties 配置。
 */
public class AiPageParser extends BaseParticleParser {

    private final AiConfig config;
    private final LargeModelClient client;

    public AiPageParser() {
        this(AiConfig.load());
    }

    public AiPageParser(AiConfig config) {
        this.config = config;
        this.client = AiClientFactory.create(config);
    }

    @Override
    public String getCategory() {
        return config.getCategory();
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        // 框架对 checkSuccess=false 会无上限复投，这里统一返回 true，由 process 判断并跳过，
        // 避免不可恢复的响应（如 4xx）造成无限重投。
        return response instanceof HttpResponse;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        HttpResponse httpResponse = (HttpResponse) response;
        HttpRequest httpRequest = (HttpRequest) response.request;

        if (!config.isEnabled()) {
            System.out.println("[ai] 已禁用，跳过: " + httpRequest.getUrl());
            return;
        }
        if (httpResponse.getHttpCode() != 200 || httpResponse.getResultPage() == null
                || httpResponse.getResultPage().isEmpty()) {
            System.out.println("[ai] 响应无效(状态码=" + httpResponse.getHttpCode() + ")，跳过: " + httpRequest.getUrl());
            return;
        }
        if (!client.isAvailable()) {
            return;
        }

        String url = httpRequest.getUrl();
        String searchKey = httpRequest.getTransport() == null
                ? "" : httpRequest.getTransport().optString("searchKey", "");
        String content = extractText(httpResponse.getResultPage());

        String systemPrompt = config.getSystemPrompt();
        String userPrompt = config.getUserPrompt()
                .replace("${url}", url == null ? "" : url)
                .replace("${searchKey}", searchKey)
                .replace("${content}", content);

        JSONObject structured = client.interpretJson(systemPrompt, userPrompt);
        if (structured == null) {
            System.out.println("[ai] 结构化失败: " + url);
            return;
        }
        System.out.println("[ai] " + url + " -> " + structured.toString(2));
    }

    /**
     * 取页面可见文本并截断，控制送入模型的上下文长度。
     */
    private String extractText(String html) {
        if (html == null) {
            return "";
        }
        String text = Jsoup.parse(html).text().trim();
        int max = config.getMaxInputChars();
        if (max > 0 && text.length() > max) {
            return text.substring(0, max);
        }
        return text;
    }
}