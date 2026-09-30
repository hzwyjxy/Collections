package factory;

import common.HttpGetDownloader;
import common.HttpPostDownloader;
import model.AbstractRequest;
import model.AbstractResponse;
import model.HttpRequest;
import model.RequestType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单机下载器
 */
public class SingleUniverse extends AbstractUniverse {
    private static int DEFAULT_THREAD_NUM = 10;
    private ConcurrentLinkedQueue<AbstractRequest> requestQueue;
    private ConcurrentLinkedQueue<AbstractResponse> responseQueue;

    public void create() {
        create(DEFAULT_THREAD_NUM);
    }

    public void create(int ThreadNum) {
        requestQueue = new ConcurrentLinkedQueue<>();
        responseQueue = new ConcurrentLinkedQueue<>();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(ThreadNum, new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                // 守护线程：main 结束后 JVM 可正常退出，避免 while(true) 线程挂住进程
                Thread t = new Thread(r, "universe-downloader-" + seq.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
        for (int i = 0; i < ThreadNum; i++) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            AbstractResponse response = downloadRequest(requestQueue.poll());
                            if (response != null) {
                                responseQueue.add(response);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    /**
     * 设置下载类型
     *
     * @param request
     * @return
     */
    private static AbstractResponse downloadRequest(AbstractRequest request) throws InterruptedException {
        if (request == null) {
            Thread.sleep(50);
            return null;
        }
        if (request.type == null || request.type.equals(RequestType.GET)) {
            return HttpGetDownloader.get((HttpRequest) request);
        } else if (request.type.equals(RequestType.POST)) {
            return HttpPostDownloader.post((HttpRequest) request);
        } else {
            return null;
        }
    }

    @Override
    public ConcurrentLinkedQueue<AbstractResponse> getResponseQueue() {
        return responseQueue;
    }

    @Override
    public void send(AbstractRequest request) {
        requestQueue.add(request);
    }

}
