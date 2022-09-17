package factory;

import model.AbstractRequest;
import model.AbstractResponse;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractUniverse {

    abstract ConcurrentLinkedQueue<AbstractResponse> getResponseQueue();
    abstract void send(AbstractRequest request);
}
