package model;

public abstract class AbstractResponse {
    public String category;
    public AbstractRequest request; //保存request，用于fail retry
}
