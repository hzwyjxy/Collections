package database.config;

/**
 * 连接池配置类
 * 封装数据库连接池的配置参数
 * 
 * @author Collections Team
 * @since 1.0
 */
public class ConnectionPoolConfig {
    
    /**
     * 最大连接池大小
     */
    private int maxPoolSize = 10;
    
    /**
     * 最小连接池大小
     */
    private int minPoolSize = 2;
    
    /**
     * 连接超时时间（毫秒）
     */
    private long connectionTimeout = 30000; // 30秒
    
    /**
     * 连接空闲超时时间（毫秒）
     */
    private long idleTimeout = 600000; // 10分钟
    
    /**
     * 连接最大生命周期（毫秒）
     */
    private long maxLifetime = 1800000; // 30分钟
    
    /**
     * 连接泄露检测阈值（毫秒）
     */
    private long leakDetectionThreshold = 0; // 禁用泄露检测
    
    /**
     * 测试连接的SQL语句
     */
    private String connectionTestQuery;
    
    /**
     * 是否自动提交
     */
    private boolean autoCommit = true;
    
    /**
     * 默认构造函数
     */
    public ConnectionPoolConfig() {
        // 默认构造函数用于JSON/YAML反序列化
    }
    
    /**
     * 全参数构造函数
     * 
     * @param maxPoolSize 最大连接池大小
     * @param minPoolSize 最小连接池大小
     * @param connectionTimeout 连接超时时间
     * @param idleTimeout 连接空闲超时时间
     * @param maxLifetime 连接最大生命周期
     */
    public ConnectionPoolConfig(int maxPoolSize, int minPoolSize, long connectionTimeout, 
                               long idleTimeout, long maxLifetime) {
        this.maxPoolSize = maxPoolSize;
        this.minPoolSize = minPoolSize;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;
    }
    
    /**
     * 获取最大连接池大小
     * 
     * @return 最大连接数
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    /**
     * 设置最大连接池大小
     * 
     * @param maxPoolSize 最大连接数
     */
    public void setMaxPoolSize(int maxPoolSize) {
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("Max pool size must be greater than 0");
        }
        this.maxPoolSize = maxPoolSize;
    }
    
    /**
     * 获取最小连接池大小
     * 
     * @return 最小连接数
     */
    public int getMinPoolSize() {
        return minPoolSize;
    }
    
    /**
     * 设置最小连接池大小
     * 
     * @param minPoolSize 最小连接数
     */
    public void setMinPoolSize(int minPoolSize) {
        if (minPoolSize < 0) {
            throw new IllegalArgumentException("Min pool size must be non-negative");
        }
        if (minPoolSize > maxPoolSize) {
            throw new IllegalArgumentException("Min pool size cannot be greater than max pool size");
        }
        this.minPoolSize = minPoolSize;
    }
    
    /**
     * 获取连接超时时间
     * 
     * @return 连接超时时间（毫秒）
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    /**
     * 设置连接超时时间
     * 
     * @param connectionTimeout 连接超时时间（毫秒）
     */
    public void setConnectionTimeout(long connectionTimeout) {
        if (connectionTimeout < 1000) {
            throw new IllegalArgumentException("Connection timeout must be at least 1000ms");
        }
        this.connectionTimeout = connectionTimeout;
    }
    
    /**
     * 获取连接空闲超时时间
     * 
     * @return 连接空闲超时时间（毫秒）
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }
    
    /**
     * 设置连接空闲超时时间
     * 
     * @param idleTimeout 连接空闲超时时间（毫秒）
     */
    public void setIdleTimeout(long idleTimeout) {
        if (idleTimeout < 10000 && idleTimeout != 0) {
            throw new IllegalArgumentException("Idle timeout must be at least 10000ms or 0 (disabled)");
        }
        this.idleTimeout = idleTimeout;
    }
    
    /**
     * 获取连接最大生命周期
     * 
     * @return 连接最大生命周期（毫秒）
     */
    public long getMaxLifetime() {
        return maxLifetime;
    }
    
    /**
     * 设置连接最大生命周期
     * 
     * @param maxLifetime 连接最大生命周期（毫秒）
     */
    public void setMaxLifetime(long maxLifetime) {
        if (maxLifetime < 30000 && maxLifetime != 0) {
            throw new IllegalArgumentException("Max lifetime must be at least 30000ms or 0 (disabled)");
        }
        this.maxLifetime = maxLifetime;
    }
    
    /**
     * 获取连接泄露检测阈值
     * 
     * @return 连接泄露检测阈值（毫秒）
     */
    public long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }
    
    /**
     * 设置连接泄露检测阈值
     * 
     * @param leakDetectionThreshold 连接泄露检测阈值（毫秒）
     */
    public void setLeakDetectionThreshold(long leakDetectionThreshold) {
        if (leakDetectionThreshold < 2000 && leakDetectionThreshold != 0) {
            throw new IllegalArgumentException("Leak detection threshold must be at least 2000ms or 0 (disabled)");
        }
        this.leakDetectionThreshold = leakDetectionThreshold;
    }
    
    /**
     * 获取测试连接的SQL语句
     * 
     * @return 测试SQL语句
     */
    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }
    
    /**
     * 设置测试连接的SQL语句
     * 
     * @param connectionTestQuery 测试SQL语句
     */
    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }
    
    /**
     * 是否自动提交
     * 
     * @return true表示自动提交
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }
    
    /**
     * 设置是否自动提交
     * 
     * @param autoCommit 是否自动提交
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
    
    /**
     * 验证配置有效性
     * 
     * @throws IllegalArgumentException 配置无效时抛出异常
     */
    public void validate() throws IllegalArgumentException {
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("Max pool size must be greater than 0");
        }
        
        if (minPoolSize < 0) {
            throw new IllegalArgumentException("Min pool size must be non-negative");
        }
        
        if (minPoolSize > maxPoolSize) {
            throw new IllegalArgumentException("Min pool size cannot be greater than max pool size");
        }
        
        if (connectionTimeout < 1000) {
            throw new IllegalArgumentException("Connection timeout must be at least 1000ms");
        }
        
        if (idleTimeout != 0 && idleTimeout < 10000) {
            throw new IllegalArgumentException("Idle timeout must be at least 10000ms or 0 (disabled)");
        }
        
        if (maxLifetime != 0 && maxLifetime < 30000) {
            throw new IllegalArgumentException("Max lifetime must be at least 30000ms or 0 (disabled)");
        }
        
        if (leakDetectionThreshold != 0 && leakDetectionThreshold < 2000) {
            throw new IllegalArgumentException("Leak detection threshold must be at least 2000ms or 0 (disabled)");
        }
    }
    
    /**
     * 创建默认配置
     * 
     * @return 默认连接池配置
     */
    public static ConnectionPoolConfig defaultConfig() {
        return new ConnectionPoolConfig();
    }
    
    /**
     * 创建高性能配置
     * 
     * @return 高性能连接池配置
     */
    public static ConnectionPoolConfig highPerformanceConfig() {
        ConnectionPoolConfig config = new ConnectionPoolConfig();
        config.setMaxPoolSize(50);
        config.setMinPoolSize(10);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1800000);
        return config;
    }
    
    /**
     * 创建低资源配置
     * 
     * @return 低资源连接池配置
     */
    public static ConnectionPoolConfig lowResourceConfig() {
        ConnectionPoolConfig config = new ConnectionPoolConfig();
        config.setMaxPoolSize(5);
        config.setMinPoolSize(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1800000);
        return config;
    }
    
    @Override
    public String toString() {
        return String.format("ConnectionPoolConfig{maxPoolSize=%d, minPoolSize=%d, " +
                "connectionTimeout=%d, idleTimeout=%d, maxLifetime=%d, " +
                "leakDetectionThreshold=%d, autoCommit=%s}",
                maxPoolSize, minPoolSize, connectionTimeout, idleTimeout, 
                maxLifetime, leakDetectionThreshold, autoCommit);
    }
}