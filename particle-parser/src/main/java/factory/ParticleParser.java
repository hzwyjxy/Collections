package factory;

import index.Index;
import matrix.BaseParticleParser;
import model.AbstractRequest;
import model.AbstractResponse;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ParticleParser {

    private AbstractUniverse universe;
    private ConcurrentLinkedQueue<AbstractResponse> responseQueue;
    private static int DEFAULT_THREAD_NUM = 10;
    /**
     * 框架级复投上限：避免解析器 checkSuccess=false 时无限重投。可用 -Dparser.retry.max 覆盖。
     */
    private static final int MAX_RETRY = Integer.getInteger("parser.retry.max", 3);
    private int threadNum;
    private Index index;

    public ParticleParser(AbstractUniverse universe, Index index) {
        this.universe = universe;
        this.responseQueue = universe.getResponseQueue();
        threadNum = DEFAULT_THREAD_NUM;
        this.index = index;
        startParser();
    }

    public ParticleParser(AbstractUniverse universe, Index index, int threadNum) {
        this.universe = universe;
        this.responseQueue = universe.getResponseQueue();
        this.threadNum = threadNum;
        this.index = index;
        startParser();
    }

    public void startParser() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum, new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                // 守护线程：main 结束后 JVM 可正常退出，避免 while(true) 线程挂住进程
                Thread t = new Thread(r, "particle-parser-" + seq.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
        for (int i = 0; i < threadNum; i++) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            parsePresonse(responseQueue.poll());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void parsePresonse(AbstractResponse response) throws InterruptedException {
        if (response == null) {
            Thread.sleep(50);
            return;
        }
        BaseParticleParser parser = index.getIndexParser(response.category);
        if (parser == null) {
            System.out.println("no parser: " + response.category);
            return;
        }
        if (parser.checkSuccess(response)) {
            parser.process(response, universe);
        } else {
            AbstractRequest request = response.request;
            if (request == null) {
                return;
            }
            request.retryCount++;
            if (request.retryCount <= MAX_RETRY) {
                universe.send(request);
            } else {
                System.out.println("retry exhausted(" + MAX_RETRY + "): " + response.category);
            }
        }
    }


}
