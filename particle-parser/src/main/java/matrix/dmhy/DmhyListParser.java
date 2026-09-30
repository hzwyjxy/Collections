package matrix.dmhy;

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
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

/**
 * dmhy 列表页解析器。
 * 从列表页每一行直接提取磁力链接与元数据，归一化 infohash 后写入 SQLite，
 * 按 infohash 去重，无需进入详情页。
 */
public class DmhyListParser extends BaseParticleParser {

    private static final String BASE_URL = "https://share.dmhy.org";
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    //单个页面的最大重试次数，重试由框架在 checkSuccess=false 时复投实现
    private static final int MAX_RETRY = Integer.getInteger("dmhy.retry.max", 3);
    private static final AtomicBoolean EXHAUSTED = new AtomicBoolean(false);

    /**
     * 是否已翻到末页（最后一页无“下一頁”链接）。生产者据此结束分页。
     */
    public static boolean isExhausted() {
        return EXHAUSTED.get();
    }

    /**
     * 重置状态，便于同一 JVM 内重复运行
     */
    public static void reset() {
        EXHAUSTED.set(false);
    }

    @Override
    public String getCategory() {
        return Category.DMHY_LIST_PAGE;
    }

    @Override
    public boolean checkSuccess(AbstractResponse response) {
        HttpResponse httpResponse = (HttpResponse) response;
        HttpRequest httpRequest = (HttpRequest) response.request;
        String body = httpResponse.getResultPage();
        //正常列表页含 topic_list；越界/末页含站点页脚“動漫花園”。二者皆无视为失败，交框架重投
        boolean ok = httpResponse.getHttpCode() == 200 && body != null
                && (body.contains("id=\"topic_list\"") || body.contains("動漫花園"));
        if (ok) {
            return true;
        }
        int retry = incRetry(httpRequest);
        if (retry <= MAX_RETRY) {
            System.out.println("[dmhy] 异常响应(状态码=" + httpResponse.getHttpCode() + ")，第 "
                    + retry + "/" + MAX_RETRY + " 次重试: " + httpRequest.getUrl());
            return false; // 交由框架复投
        }
        System.out.println("[dmhy] 重试 " + MAX_RETRY + " 次仍失败，放弃该页: " + httpRequest.getUrl());
        return true; // 不再复投，交给 process 跳过
    }

    /**
     * 通过 transport 记录重试次数（请求对象随复投复用）
     */
    private static int incRetry(HttpRequest httpRequest) {
        JSONObject transport = httpRequest.getTransport();
        if (transport == null) {
            transport = new JSONObject();
            httpRequest.setTransport(transport);
        }
        int retry = transport.optInt("retry", 0) + 1;
        transport.put("retry", retry);
        return retry;
    }

    @Override
    public void process(AbstractResponse response, AbstractUniverse universe) {
        HttpResponse httpResponse = (HttpResponse) response;
        HttpRequest httpRequest = (HttpRequest) response.request;
        String html = httpResponse.getResultPage();
        if (html == null || html.isEmpty()) {
            System.out.println("[dmhy] 空响应，跳过: " + httpRequest.getUrl());
            return;
        }
        Document doc = Jsoup.parse(html);

        //末页判定：站点底部导航无“下一頁”链接即为最后一页（真末页有内容但无下一頁，越界页同理）
        boolean hasNext = doc.selectFirst("a:contains(下一頁)") != null;

        Elements trs = doc.select("table#topic_list tbody tr");
        if (trs.isEmpty()) {
            if (!hasNext) {
                EXHAUSTED.set(true);
                System.out.println("[dmhy] " + httpRequest.getUrl() + " -> 无数据且无“下一頁”，判定已到末页");
            } else {
                System.out.println("[dmhy] " + httpRequest.getUrl() + " -> 无数据行");
            }
            return;
        }

        MagnetStore store = MagnetStore.getInstance();

        int saved = 0;
        for (Element tr : trs) {
            Element titleA = tr.selectFirst("td.title > a");
            if (titleA == null) {
                titleA = tr.selectFirst("td.title a[href^=/topics/view/]");
            }
            if (titleA == null) {
                continue;
            }
            String title = titleA.text();
            String topicUrl = BASE_URL + titleA.attr("href");

            String magnet = extractMagnet(tr);
            if (magnet == null) {
                continue;
            }
            String infohash = normalizeInfohash(extractBtih(magnet));
            if (infohash == null) {
                continue;
            }
            magnet = fillDisplayName(magnet, title);

            Elements tds = tr.select("td");
            String category = tds.size() > 1 ? tds.get(1).text() : null;
            String size = tds.size() > 4 ? tds.get(4).text() : null;
            String pubDate = extractPubDate(tr);

            if (store.save(infohash, title, magnet, topicUrl, category, size, pubDate)) {
                saved++;
            }
        }
        System.out.println("[dmhy] " + httpRequest.getUrl() + " -> 新增 " + saved + " 条，累计 " + store.count());
        if (!hasNext) {
            EXHAUSTED.set(true);
            System.out.println("[dmhy] 已到最后一页（无“下一頁”链接），停止翻页");
        }
    }

    /**
     * 优先取列表页磁链列的 magnet，其次取迅雷按钮上的 data-magnet
     */
    private static String extractMagnet(Element tr) {
        Element a = tr.selectFirst("a[href^='magnet:']");
        if (a != null) {
            return a.attr("href");
        }
        Element xl = tr.selectFirst("a[data-magnet]");
        return xl != null ? xl.attr("data-magnet") : null;
    }

    /**
     * 从 magnet 里取出 btih 值（可能是 40 位 hex 或 32 位 base32）
     */
    private static String extractBtih(String magnet) {
        int i = magnet.indexOf("btih:");
        if (i < 0) {
            return null;
        }
        String rest = magnet.substring(i + "btih:".length());
        int end = rest.indexOf('&');
        return (end >= 0 ? rest.substring(0, end) : rest).trim();
    }

    /**
     * 统一 infohash 为小写 hex，保证 base32 / hex 两种写法去重一致
     */
    private static String normalizeInfohash(String btih) {
        if (btih == null) {
            return null;
        }
        if (btih.matches("(?i)[0-9a-f]{40}")) {
            return btih.toLowerCase();
        }
        if (btih.matches("(?i)[a-z2-7]{32}")) {
            return base32ToHex(btih);
        }
        return null;
    }

    private static String base32ToHex(String base32) {
        StringBuilder bits = new StringBuilder();
        for (char c : base32.toUpperCase().toCharArray()) {
            int idx = BASE32_ALPHABET.indexOf(c);
            if (idx < 0) {
                return null;
            }
            String bin = Integer.toBinaryString(idx);
            for (int p = bin.length(); p < 5; p++) {
                bits.append('0');
            }
            bits.append(bin);
        }
        if (bits.length() < 160) {
            return null;
        }
        StringBuilder hex = new StringBuilder(40);
        for (int i = 0; i + 4 <= 160; i += 4) {
            hex.append(Character.forDigit(Integer.parseInt(bits.substring(i, i + 4), 2), 16));
        }
        return hex.toString();
    }

    /**
     * 列表页磁力的 dn 为空，用标题补上便于识别
     */
    private static String fillDisplayName(String magnet, String title) {
        if (title == null || title.isEmpty()) {
            return magnet;
        }
        String encoded = URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8);
        Matcher m = java.util.regex.Pattern.compile("&dn=[^&]*").matcher(magnet);
        if (m.find()) {
            return m.replaceFirst("&dn=" + Matcher.quoteReplacement(encoded));
        }
        return magnet + "&dn=" + encoded;
    }

    /**
     * 取隐藏的绝对时间，取不到则用可见文本
     */
    private static String extractPubDate(Element tr) {
        Element span = tr.selectFirst("td span");
        if (span != null && !span.text().trim().isEmpty()) {
            return span.text().trim();
        }
        Elements tds = tr.select("td");
        return tds.isEmpty() ? null : tds.get(0).text().trim();
    }
}