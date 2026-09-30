package matrix.dmhy;


import factory.ParticleParser;
import factory.SingleUniverse;
import index.Category;
import index.all.DmhyIndex;
import model.HttpRequest;

public class CrawlAllAnimations {
    //每页访问间隔，避免触发站点限流，可用 -Ddmhy.page.interval.ms 覆盖
    private static final long PAGE_INTERVAL_MILLIS = Long.getLong("dmhy.page.interval.ms", 800L);
    //安全上限，动态终止条件失效时兜底（動畫分类实测约 5600+ 页）
    private static final int MAX_PAGE = Integer.getInteger("dmhy.max.page", 20000);

    static SingleUniverse singleUniverse;

    static {
        //生成下载器
        singleUniverse = new SingleUniverse();
        singleUniverse.create();
        //生成解析器
        new ParticleParser(singleUniverse, new DmhyIndex());
    }

    public final static void main(final String[] args) throws Exception {
        DmhyListParser.reset();
        int page = 1;
        //动态分页：解析器遇到末尾页（无“下一頁”链接）置为 exhausted，即停止
        while (!DmhyListParser.isExhausted() && page <= MAX_PAGE) {
            singleUniverse.send(getDmhyCompleteList(page));
            page++;
            Thread.sleep(PAGE_INTERVAL_MILLIS);
        }
        //稍作等待，让在途请求完成解析落库
        Thread.sleep(3000);
        System.out.println("[dmhy] 采集结束，共发送 " + (page - 1) + " 页，累计 "
                + MagnetStore.getInstance().count() + " 条");
    }

    /**
     * 動畫分类（sort_id/2），含季度全集等全部子分类
     */
    public static HttpRequest getDmhyCompleteList(int page) {
        String url = "https://share.dmhy.org/topics/list/sort_id/2/page/" + page;
        HttpRequest httpRequest = new HttpRequest("GET", Category.DMHY_LIST_PAGE);
        httpRequest.setUrl(url);
        return httpRequest;
    }
}
