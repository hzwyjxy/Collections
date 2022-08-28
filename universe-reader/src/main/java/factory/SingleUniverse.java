package factory;

import common.HttpGetDownloader;
import model.AbstractRequest;
import model.AbstractResponse;
import model.HttpRequest;
import model.RequestType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单机下载器
 */
public class SingleUniverse {
    private static int DEFAULT_THREAD_NUM = 10;
    private ConcurrentLinkedQueue<AbstractRequest> requestQueue;
    private ConcurrentLinkedQueue<AbstractResponse> responseQueue;

    public void create() {
        create(DEFAULT_THREAD_NUM);
    }

    public void create(int ThreadNum) {
        requestQueue = new ConcurrentLinkedQueue<>();
        responseQueue = new ConcurrentLinkedQueue<>();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(ThreadNum);
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
            return HttpGetDownloader.get((HttpRequest) request);
        } else {
            return null;
        }
    }

    public ConcurrentLinkedQueue<AbstractResponse> getResponseQueue() {
        return responseQueue;
    }

    public void send(AbstractRequest request) {
        requestQueue.add(request);
    }

}
