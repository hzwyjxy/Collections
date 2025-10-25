package database.adapter;

import database.config.DatabaseConfig;
import database.core.Page;
import database.core.RowMapper;
import database.core.StorageException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis数据库适配器
 * 实现Redis数据库的连接和操作
 * 
 * @author Collections Team
 * @since 1.0
 */
public class RedisAdapter implements DatabaseStorage {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RedisAdapter.class);
    
    /**
     * Redis连接池
     */
    private JedisPool jedisPool;
    
    /**
     * 数据库配置
     */
    private final DatabaseConfig config;
    
    /**
     * 适配器状态
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    public RedisAdapter(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
    }
    
    @Override
    public String getDatabaseType() {
        return "Redis";
    }
    
    @Override
    public String getConnectionInfo() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            return String.format("Redis [%s:%s] - Pool: %d/%d", 
                    config.getConnectionProperties().getOrDefault("host", "localhost"),
                    config.getConnectionProperties().getOrDefault("port", "6379"),
                    getActiveConnections(),
                    getMaxConnections());
        }
        return "Redis [Not Connected]";
    }
    
    @Override
    public void initialize() throws StorageException {
        if (initialized.compareAndSet(false, true)) {
            try {
                logger.info("Initializing Redis adapter for database: {}", config.getName());
                doInitialize();
                logger.info("Successfully initialized Redis adapter");
            } catch (Exception e) {
                initialized.set(false);
                throw new StorageException("INIT_FAILED", "Failed to initialize Redis adapter", e);
            }
        }
    }
    
    @Override
    public void shutdown() throws StorageException {
        if (initialized.compareAndSet(true, false)) {
            try {
                logger.info("Shutting down Redis adapter for database: {}", config.getName());
                doShutdown();
                logger.info("Successfully shut down Redis adapter");
            } catch (Exception e) {
                throw new StorageException("SHUTDOWN_FAILED", "Failed to shut down Redis adapter", e);
            }
        }
    }
    
    @Override
    public boolean isHealthy() {
        if (!initialized.get()) {
            return false;
        }
        
        try {
            return doHealthCheck();
        } catch (Exception e) {
            logger.warn("Health check failed for Redis adapter: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public <T> T querySingle(String sql, RowMapper<T> rowMapper, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL queries");
    }
    
    @Override
    public <T> List<T> queryMultiple(String sql, RowMapper<T> rowMapper, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL queries");
    }
    
    @Override
    public <T> Page<T> queryPage(String sql, RowMapper<T> rowMapper, int pageNum, int pageSize, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL queries");
    }
    
    @Override
    public int insert(String sql, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL insert");
    }
    
    @Override
    public int[] batchInsert(String sql, List<Object[]> batchParams) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL batch insert");
    }
    
    @Override
    public int update(String sql, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL update");
    }
    
    @Override
    public int delete(String sql, Object... params) throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support SQL delete");
    }
    
    @Override
    public void beginTransaction() throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support transactions in this adapter");
    }
    
    @Override
    public void commitTransaction() throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support transactions in this adapter");
    }
    
    @Override
    public void rollbackTransaction() throws StorageException {
        throw new StorageException("UNSUPPORTED_OPERATION", "Redis does not support transactions in this adapter");
    }
    
    /**
     * Redis特定的操作方法
     */
    
    /**
     * 设置字符串值
     * 
     * @param key 键
     * @param value 值
     * @return 成功返回OK，失败返回null
     */
    public String set(String key, String value) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        } catch (JedisException e) {
            logger.error("Redis SET operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to set value", e);
        }
    }
    
    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 值，不存在时返回null
     */
    public String get(String key) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (JedisException e) {
            logger.error("Redis GET operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get value", e);
        }
    }
    
    /**
     * 删除键
     * 
     * @param keys 要删除的键
     * @return 删除的键数量
     */
    public long del(String... keys) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        } catch (JedisException e) {
            logger.error("Redis DEL operation failed for keys: {}", Arrays.toString(keys), e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to delete keys", e);
        }
    }
    
    /**
     * 检查键是否存在
     * 
     * @param key 键
     * @return true表示存在
     */
    public boolean exists(String key) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (JedisException e) {
            logger.error("Redis EXISTS operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to check key existence", e);
        }
    }
    
    /**
     * 设置哈希字段
     * 
     * @param key 哈希键
     * @param field 字段名
     * @param value 字段值
     * @return 成功返回1，失败返回0
     */
    public long hset(String key, String field, String value) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        } catch (JedisException e) {
            logger.error("Redis HSET operation failed for key: {}, field: {}", key, field, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to set hash field", e);
        }
    }
    
    /**
     * 获取哈希字段值
     * 
     * @param key 哈希键
     * @param field 字段名
     * @return 字段值，不存在时返回null
     */
    public String hget(String key, String field) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        } catch (JedisException e) {
            logger.error("Redis HGET operation failed for key: {}, field: {}", key, field, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get hash field", e);
        }
    }
    
    /**
     * 获取所有哈希字段
     * 
     * @param key 哈希键
     * @return 所有字段映射
     */
    public Map<String, String> hgetAll(String key) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        } catch (JedisException e) {
            logger.error("Redis HGETALL operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get all hash fields", e);
        }
    }
    
    /**
     * 添加到列表
     * 
     * @param key 列表键
     * @param values 要添加的值
     * @return 列表长度
     */
    public long lpush(String key, String... values) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(key, values);
        } catch (JedisException e) {
            logger.error("Redis LPUSH operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to push to list", e);
        }
    }
    
    /**
     * 从列表获取元素
     * 
     * @param key 列表键
     * @param start 开始索引
     * @param end 结束索引
     * @return 元素列表
     */
    public List<String> lrange(String key, long start, long end) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(key, start, end);
        } catch (JedisException e) {
            logger.error("Redis LRANGE operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get list range", e);
        }
    }
    
    /**
     * 添加到集合
     * 
     * @param key 集合键
     * @param members 要添加的成员
     * @return 添加成功的成员数量
     */
    public long sadd(String key, String... members) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sadd(key, members);
        } catch (JedisException e) {
            logger.error("Redis SADD operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to add to set", e);
        }
    }
    
    /**
     * 获取集合成员
     * 
     * @param key 集合键
     * @return 成员集合
     */
    public Set<String> smembers(String key) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        } catch (JedisException e) {
            logger.error("Redis SMEMBERS operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get set members", e);
        }
    }
    
    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param seconds 过期时间（秒）
     * @return 成功返回1，失败返回0
     */
    public long expire(String key, int seconds) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        } catch (JedisException e) {
            logger.error("Redis EXPIRE operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to set expiration", e);
        }
    }
    
    /**
     * 获取剩余过期时间
     * 
     * @param key 键
     * @return 剩余时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long ttl(String key) throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        } catch (JedisException e) {
            logger.error("Redis TTL operation failed for key: {}", key, e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get TTL", e);
        }
    }
    
    /**
     * 获取Redis服务器信息
     * 
     * @return 服务器信息
     */
    public String getServerInfo() throws StorageException {
        checkInitialized();
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.info();
        } catch (JedisException e) {
            logger.error("Redis INFO operation failed", e);
            throw new StorageException("REDIS_OPERATION_FAILED", "Failed to get server info", e);
        }
    }
    
    /**
     * 获取Redis统计信息
     * 
     * @return 统计信息映射
     */
    public Map<String, Object> getRedisStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取连接池统计信息
            if (jedisPool != null && !jedisPool.isClosed()) {
                stats.put("activeConnections", getActiveConnections());
                stats.put("idleConnections", getIdleConnections());
                stats.put("maxConnections", getMaxConnections());
                stats.put("waitingForConnection", getWaitingForConnection());
            }
            
            // 获取服务器信息
            try (Jedis jedis = jedisPool.getResource()) {
                String info = jedis.info();
                String[] lines = info.split("\\n");
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        stats.put(parts[0], parts[1]);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to get Redis stats", e);
        }
        
        return stats;
    }
    
    /**
     * 检查适配器是否已初始化
     * 
     * @throws StorageException 未初始化时抛出异常
     */
    private void checkInitialized() throws StorageException {
        if (!initialized.get()) {
            throw new StorageException("NOT_INITIALIZED", "Redis adapter is not initialized");
        }
    }
    
    /**
     * 执行初始化操作
     * 
     * @throws Exception 初始化失败时抛出异常
     */
    private void doInitialize() throws Exception {
        logger.info("Initializing Redis adapter");
        
        // 获取连接配置
        String host = config.getConnectionProperties().getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getConnectionProperties().getOrDefault("port", "6379"));
        String password = config.getConnectionProperties().get("password");
        int database = Integer.parseInt(config.getConnectionProperties().getOrDefault("database", "0"));
        int timeout = Integer.parseInt(config.getConnectionProperties().getOrDefault("timeout", "2000"));
        
        // 创建连接池配置
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        
        // 设置连接池参数
        if (config.getPoolConfig() != null) {
            poolConfig.setMaxTotal(config.getPoolConfig().getMaxPoolSize());
            poolConfig.setMinIdle(config.getPoolConfig().getMinPoolSize());
            poolConfig.setMaxWaitMillis(config.getPoolConfig().getConnectionTimeout());
            poolConfig.setMinEvictableIdleTimeMillis(config.getPoolConfig().getIdleTimeout());
            poolConfig.setTimeBetweenEvictionRunsMillis(30000);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
        } else {
            // 默认配置
            poolConfig.setMaxTotal(10);
            poolConfig.setMinIdle(2);
            poolConfig.setMaxWaitMillis(30000);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
        }
        
        // 创建连接池
        if (password != null && !password.trim().isEmpty()) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
        }
        
        // 测试连接
        try (Jedis jedis = jedisPool.getResource()) {
            String ping = jedis.ping();
            if (!"PONG".equals(ping)) {
                throw new StorageException("CONNECTION_FAILED", "Redis ping failed: " + ping);
            }
            logger.info("Successfully connected to Redis server: {}:{}", host, port);
        } catch (JedisException e) {
            throw new StorageException("CONNECTION_FAILED", "Failed to connect to Redis server", e);
        }
    }
    
    /**
     * 执行关闭操作
     * 
     * @throws Exception 关闭失败时抛出异常
     */
    private void doShutdown() throws Exception {
        logger.info("Shutting down Redis adapter");
        
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }
    
    /**
     * 执行健康检查
     * 
     * @return 健康检查结果
     * @throws Exception 健康检查失败时抛出异常
     */
    private boolean doHealthCheck() throws Exception {
        if (jedisPool == null || jedisPool.isClosed()) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ping = jedis.ping();
            return "PONG".equals(ping);
        } catch (JedisException e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取活跃连接数
     * 
     * @return 活跃连接数
     */
    private int getActiveConnections() {
        if (jedisPool != null) {
            return jedisPool.getNumActive();
        }
        return 0;
    }
    
    /**
     * 获取空闲连接数
     * 
     * @return 空闲连接数
     */
    private int getIdleConnections() {
        if (jedisPool != null) {
            return jedisPool.getNumIdle();
        }
        return 0;
    }
    
    /**
     * 获取最大连接数
     * 
     * @return 最大连接数
     */
    private int getMaxConnections() {
        if (jedisPool != null) {
            return jedisPool.getMaxTotal();
        }
        return 0;
    }
    
    /**
     * 获取等待连接的线程数
     * 
     * @return 等待连接的线程数
     */
    private int getWaitingForConnection() {
        if (jedisPool != null) {
            return jedisPool.getNumWaiters();
        }
        return 0;
    }
}