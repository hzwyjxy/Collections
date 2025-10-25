package database.config;

import java.util.Map;
import java.util.Objects;

/**
 * 数据库配置类
 * 封装单个数据库的连接配置信息
 * 
 * @author Collections Team
 * @since 1.0
 */
public class DatabaseConfig {
    
    /**
     * 数据库类型（sqlite, mysql, redis, hive, hbase）
     */
    private String type;
    
    /**
     * 数据库名称标识
     */
    private String name;
    
    /**
     * 是否启用该数据库
     */
    private boolean enabled = true;
    
    /**
     * 数据库连接属性
     */
    private Map<String, Object> properties;
    
    /**
     * 连接池配置
     */
    private ConnectionPoolConfig pool;
    
    /**
     * 默认构造函数
     */
    public DatabaseConfig() {
        // 默认构造函数用于JSON/YAML反序列化
    }
    
    /**
     * 全参数构造函数
     * 
     * @param type 数据库类型
     * @param name 数据库名称
     * @param enabled 是否启用
     * @param properties 连接属性
     * @param pool 连接池配置
     */
    public DatabaseConfig(String type, String name, boolean enabled, 
                         Map<String, Object> properties, ConnectionPoolConfig pool) {
        this.type = type;
        this.name = name;
        this.enabled = enabled;
        this.properties = properties;
        this.pool = pool;
    }
    
    /**
     * 获取数据库类型
     * 
     * @return 数据库类型
     */
    public String getType() {
        return type;
    }
    
    /**
     * 设置数据库类型
     * 
     * @param type 数据库类型
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * 获取数据库名称
     * 
     * @return 数据库名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置数据库名称
     * 
     * @param name 数据库名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 检查是否启用
     * 
     * @return true表示启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置启用状态
     * 
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 获取连接属性
     * 
     * @return 连接属性映射
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    /**
     * 设置连接属性
     * 
     * @param properties 连接属性映射
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    /**
     * 获取连接池配置
     * 
     * @return 连接池配置
     */
    public ConnectionPoolConfig getPool() {
        return pool;
    }
    
    /**
     * 设置连接池配置
     * 
     * @param pool 连接池配置
     */
    public void setPool(ConnectionPoolConfig pool) {
        this.pool = pool;
    }
    
    /**
     * 获取连接属性值
     * 
     * @param key 属性键
     * @return 属性值，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return (T) properties.get(key);
    }
    
    /**
     * 获取连接属性值，如果不存在则返回默认值
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        T value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 验证配置有效性
     * 
     * @throws IllegalArgumentException 配置无效时抛出异常
     */
    public void validate() throws IllegalArgumentException {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Database properties cannot be null or empty");
        }
        
        // 验证必需属性
        switch (type.toLowerCase()) {
            case "sqlite":
                validateSQLiteProperties();
                break;
            case "mysql":
                validateMySQLProperties();
                break;
            case "redis":
                validateRedisProperties();
                break;
            case "hive":
                validateHiveProperties();
                break;
            case "hbase":
                validateHBaseProperties();
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        
        if (pool != null) {
            pool.validate();
        }
    }
    
    private void validateSQLiteProperties() {
        if (!properties.containsKey("url")) {
            throw new IllegalArgumentException("SQLite database requires 'url' property");
        }
    }
    
    private void validateMySQLProperties() {
        if (!properties.containsKey("host")) {
            throw new IllegalArgumentException("MySQL database requires 'host' property");
        }
        if (!properties.containsKey("port")) {
            throw new IllegalArgumentException("MySQL database requires 'port' property");
        }
        if (!properties.containsKey("database")) {
            throw new IllegalArgumentException("MySQL database requires 'database' property");
        }
        if (!properties.containsKey("username")) {
            throw new IllegalArgumentException("MySQL database requires 'username' property");
        }
    }
    
    private void validateRedisProperties() {
        if (!properties.containsKey("host")) {
            throw new IllegalArgumentException("Redis database requires 'host' property");
        }
        if (!properties.containsKey("port")) {
            throw new IllegalArgumentException("Redis database requires 'port' property");
        }
    }
    
    private void validateHiveProperties() {
        if (!properties.containsKey("host")) {
            throw new IllegalArgumentException("Hive database requires 'host' property");
        }
        if (!properties.containsKey("port")) {
            throw new IllegalArgumentException("Hive database requires 'port' property");
        }
    }
    
    private void validateHBaseProperties() {
        if (!properties.containsKey("zookeeper_quorum")) {
            throw new IllegalArgumentException("HBase database requires 'zookeeper_quorum' property");
        }
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseConfig{type='%s', name='%s', enabled=%s, properties=%s, pool=%s}",
                type, name, enabled, properties != null ? properties.size() + " items" : null, pool);
    }
}