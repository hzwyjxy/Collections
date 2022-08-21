package factory;

import common.HttpGetDownloader;
import model.AbstractRequest;
import model.AbstractResponse;
import model.HttpRequest;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Single {
    private static int DEFAULT_THREAD_NUM = 10;
    private static ConcurrentLinkedQueue<AbstractRequest> requestQueue;
    private static ConcurrentLinkedQueue<AbstractResponse> responseQueue;

    public static void create() {
        create(DEFAULT_THREAD_NUM);
    }

    public static void create(int ThreadNum) {
        requestQueue = new ConcurrentLinkedQueue<>();
        responseQueue = new ConcurrentLinkedQueue<>();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(ThreadNum);
        for (int i = 0; i < ThreadNum; i++) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        AbstractResponse response = downloadRequest(requestQueue.poll());
                        if (response != null) {
                            responseQueue.add(response);
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
    private static AbstractResponse downloadRequest(AbstractRequest request) {
        if (request.type.equals("get")) {
            return HttpGetDownloader.get((HttpRequest) request);
        } else if (request.type.equals("post")) {
            return HttpGetDownloader.get((HttpRequest) request);
        } else {
            return null;
        }
    }

    public static ConcurrentLinkedQueue<AbstractResponse> getResponseQueue() {
        return responseQueue;
    }

}
