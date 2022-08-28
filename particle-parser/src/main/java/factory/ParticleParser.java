package factory;

import index.Index;
import matrix.BaseParticleParser;
import model.AbstractResponse;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleParser {
    private ConcurrentLinkedQueue<AbstractResponse> responseQueue;
    private static int DEFAULT_THREAD_NUM = 10;
    private int threadNum;
    private Index index;

    public ParticleParser(ConcurrentLinkedQueue<AbstractResponse> responseQueue, Index index) {
        this.responseQueue = responseQueue;
        threadNum = DEFAULT_THREAD_NUM;
        this.index = index;
        startParser();
    }

    public ParticleParser(ConcurrentLinkedQueue<AbstractResponse> responseQueue, Index index, int threadNum) {
        this.responseQueue = responseQueue;
        this.threadNum = threadNum;
        this.index = index;
        startParser();
    }

    public void startParser() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
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
        if(response == null){
            Thread.sleep(50);
            return;
        }
        BaseParticleParser parser = index.getIndexParser(response.category);
        if(parser !=null && parser.checkSuccess()) {
            parser.process();
        }
    }


}
