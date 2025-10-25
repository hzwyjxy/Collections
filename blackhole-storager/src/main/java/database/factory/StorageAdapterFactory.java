package database.factory;

import database.adapter.*;
import database.config.DatabaseConfig;
import database.core.DatabaseStorage;
import database.core.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储适配器工厂
 * 负责创建和管理不同类型的数据库适配器实例
 * 
 * @author Collections Team
 * @since 1.0
 */
public class StorageAdapterFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageAdapterFactory.class);
    
    /**
     * 适配器实例缓存
     */
    private static final Map<String, DatabaseStorage> adapterCache = new ConcurrentHashMap<>();
    
    /**
     * 支持的适配器类型
     */
    public enum AdapterType {
        SQLITE("SQLite", SQLiteAdapter.class),
        MYSQL("MySQL", MySQLAdapter.class),
        REDIS("Redis", RedisAdapter.class),
        HIVE("Hive", HiveAdapter.class),
        HBASE("HBase", HBaseAdapter.class);
        
        private final String typeName;
        private final Class<? extends DatabaseStorage> adapterClass;
        
        AdapterType(String typeName, Class<? extends DatabaseStorage> adapterClass) {
            this.typeName = typeName;
            this.adapterClass = adapterClass;
        }
        
        public String getTypeName() {
            return typeName;
        }
        
        public Class<? extends DatabaseStorage> getAdapterClass() {
            return adapterClass;
        }
        
        public static AdapterType fromString(String type) {
            for (AdapterType adapterType : values()) {
                if (adapterType.typeName.equalsIgnoreCase(type)) {
                    return adapterType;
                }
            }
            throw new IllegalArgumentException("Unsupported adapter type: " + type);
        }
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private StorageAdapterFactory() {
        // 工具类，不需要实例化
    }
    
    /**
     * 创建存储适配器
     * 
     * @param config 数据库配置
     * @return 存储适配器实例
     * @throws StorageException 创建失败时抛出异常
     */
    public static DatabaseStorage createAdapter(DatabaseConfig config) throws StorageException {
        if (config == null) {
            throw new StorageException("CONFIG_INVALID", "Database configuration cannot be null");
        }
        
        String type = config.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new StorageException("CONFIG_INVALID", "Database type cannot be null or empty");
        }
        
        try {
            AdapterType adapterType = AdapterType.fromString(type);
            return createAdapterInstance(adapterType, config);
        } catch (IllegalArgumentException e) {
            throw new StorageException("UNSUPPORTED_TYPE", "Unsupported database type: " + type, e);
        }
    }
    
    /**
     * 创建存储适配器（带缓存）
     * 
     * @param config 数据库配置
     * @param useCache 是否使用缓存
     * @return 存储适配器实例
     * @throws StorageException 创建失败时抛出异常
     */
    public static DatabaseStorage createAdapter(DatabaseConfig config, boolean useCache) throws StorageException {
        if (!useCache) {
            return createAdapter(config);
        }
        
        String cacheKey = buildCacheKey(config);
        DatabaseStorage adapter = adapterCache.get(cacheKey);
        
        if (adapter == null) {
            synchronized (adapterCache) {
                adapter = adapterCache.get(cacheKey);
                if (adapter == null) {
                    adapter = createAdapter(config);
                    adapterCache.put(cacheKey, adapter);
                    logger.info("Created and cached adapter for database: {}", config.getName());
                }
            }
        }
        
        return adapter;
    }
    
    /**
     * 获取已创建的适配器
     * 
     * @param name 数据库名称
     * @return 存储适配器实例，不存在时返回null
     */
    public static DatabaseStorage getAdapter(String name) {
        return adapterCache.get(name);
    }
    
    /**
     * 移除适配器缓存
     * 
     * @param name 数据库名称
     * @return 被移除的适配器实例，不存在时返回null
     */
    public static DatabaseStorage removeAdapter(String name) {
        DatabaseStorage adapter = adapterCache.remove(name);
        if (adapter != null) {
            try {
                adapter.shutdown();
                logger.info("Removed and shut down adapter for database: {}", name);
            } catch (StorageException e) {
                logger.warn("Failed to shut down adapter for database: {}", name, e);
            }
        }
        return adapter;
    }
    
    /**
     * 关闭所有适配器
     */
    public static void shutdownAll() {
        logger.info("Shutting down all adapters");
        
        for (Map.Entry<String, DatabaseStorage> entry : adapterCache.entrySet()) {
            String name = entry.getKey();
            DatabaseStorage adapter = entry.getValue();
            
            try {
                adapter.shutdown();
                logger.info("Successfully shut down adapter for database: {}", name);
            } catch (StorageException e) {
                logger.warn("Failed to shut down adapter for database: {}", name, e);
            }
        }
        
        adapterCache.clear();
        logger.info("All adapters shut down successfully");
    }
    
    /**
     * 获取支持的适配器类型
     * 
     * @return 支持的适配器类型数组
     */
    public static String[] getSupportedTypes() {
        AdapterType[] types = AdapterType.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            typeNames[i] = types[i].getTypeName();
        }
        return typeNames;
    }
    
    /**
     * 检查是否支持指定的适配器类型
     * 
     * @param type 适配器类型
     * @return true表示支持
     */
    public static boolean isSupported(String type) {
        try {
            AdapterType.fromString(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 创建适配器实例
     * 
     * @param adapterType 适配器类型
     * @param config 数据库配置
     * @return 适配器实例
     * @throws StorageException 创建失败时抛出异常
     */
    private static DatabaseStorage createAdapterInstance(AdapterType adapterType, DatabaseConfig config) throws StorageException {
        try {
            Class<? extends DatabaseStorage> adapterClass = adapterType.getAdapterClass();
            
            // 检查适配器类是否已实现
            if (adapterClass == HiveAdapter.class || adapterClass == HBaseAdapter.class) {
                throw new StorageException("NOT_IMPLEMENTED", 
                        adapterType.getTypeName() + " adapter is not implemented yet");
            }
            
            // 创建适配器实例
            DatabaseStorage adapter = adapterClass.getDeclaredConstructor(DatabaseConfig.class).newInstance(config);
            
            // 初始化适配器
            adapter.initialize();
            
            logger.info("Successfully created {} adapter for database: {}", adapterType.getTypeName(), config.getName());
            return adapter;
            
        } catch (InstantiationException | IllegalAccessException | 
                 java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
            throw new StorageException("ADAPTER_CREATION_FAILED", 
                    "Failed to create " + adapterType.getTypeName() + " adapter", e);
        }
    }
    
    /**
     * 构建缓存键
     * 
     * @param config 数据库配置
     * @return 缓存键
     */
    private static String buildCacheKey(DatabaseConfig config) {
        return config.getName() + "_" + config.getType();
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息映射
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", adapterCache.size());
        stats.put("cachedAdapters", new ArrayList<>(adapterCache.keySet()));
        
        // 获取每个适配器的状态
        Map<String, String> adapterStatus = new ConcurrentHashMap<>();
        for (Map.Entry<String, DatabaseStorage> entry : adapterCache.entrySet()) {
            String name = entry.getKey();
            DatabaseStorage adapter = entry.getValue();
            
            try {
                String status = adapter.isHealthy() ? "Healthy" : "Unhealthy";
                String info = adapter.getConnectionInfo();
                adapterStatus.put(name, String.format("%s (%s)", status, info));
            } catch (Exception e) {
                adapterStatus.put(name, "Error: " + e.getMessage());
            }
        }
        stats.put("adapterStatus", adapterStatus);
        
        return stats;
    }
    
    /**
     * 清除缓存
     */
    public static void clearCache() {
        logger.info("Clearing adapter cache");
        adapterCache.clear();
    }
}