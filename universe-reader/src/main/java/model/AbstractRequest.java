package model;

public abstract class AbstractRequest {

    public String type;
    public String category;

    /**
     * 框架级复投计数：解析器 checkSuccess=false 时由 ParticleParser 累加，用于限制重试次数。
     */
    public int retryCount = 0;

}
