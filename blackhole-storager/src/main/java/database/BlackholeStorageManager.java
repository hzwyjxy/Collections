package database;

import database.adapter.BaseStorageAdapter;
import database.config.ConfigurationManager;
import database.config.DatabaseConfig;
import database.core.DatabaseStorage;
import database.core.StorageException;
import database.factory.StorageAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blackhole存储管理器
 * 提供统一的数据库操作API，支持多种数据库类型
 * 
 * @author Collections Team
 * @since 1.0
 */
public class BlackholeStorageManager {
    
    private static final Logger logger = LoggerFactory.getLogger(BlackholeStorageManager.class);
    
    /**
     * 单例实例
     */
    private static volatile BlackholeStorageManager instance;
    
    /**
     * 存储适配器映射
     */
    private final Map<String, DatabaseStorage> adapters = new ConcurrentHashMap<>();
    
    /**
     * 初始化状态
     */
    private volatile boolean initialized = false;
    
    /**
     * 私有构造函数
     */
    private BlackholeStorageManager() {
        // 私有构造函数，防止直接实例化
    }
    
    /**
     * 获取单例实例
     * 
     * @return Blackhole存储管理器实例
     */
    public static BlackholeStorageManager getInstance() {
        if (instance == null) {
            synchronized (BlackholeStorageManager.class) {
                if (instance == null) {
                    instance = new BlackholeStorageManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化存储管理器
     * 从配置文件加载数据库配置并创建适配器
     * 
     * @param configFilePath 配置文件路径
     * @throws IOException 配置文件加载失败时抛出异常
     * @throws StorageException 适配器创建失败时抛出异常
     */
    public void initialize(String configFilePath) throws IOException, StorageException {
        if (initialized) {
            logger.warn("BlackholeStorageManager is already initialized");
            return;
        }
        
        logger.info("Initializing BlackholeStorageManager from config file: {}", configFilePath);
        
        try {
            // 加载配置文件
            Map<String, DatabaseConfig> configs = ConfigurationManager.loadFromFile(configFilePath);
            
            // 创建存储适配器
            for (Map.Entry<String, DatabaseConfig> entry : configs.entrySet()) {
                String name = entry.getKey();
                DatabaseConfig config = entry.getValue();
                
                if (config.isEnabled()) {
                    try {
                        DatabaseStorage adapter = StorageAdapterFactory.createAdapter(config);
                        adapters.put(name, adapter);
                        logger.info("Created adapter for database: {} (type: {})", name, config.getType());
                    } catch (StorageException e) {
                        logger.error("Failed to create adapter for database: {} (type: {})", name, config.getType(), e);
                        throw e;
                    }
                } else {
                    logger.info("Skipping disabled database: {} (type: {})", name, config.getType());
                }
            }
            
            initialized = true;
            logger.info("BlackholeStorageManager initialized successfully with {} adapters", adapters.size());
            
        } catch (IOException e) {
            logger.error("Failed to load configuration from file: {}", configFilePath, e);
            throw e;
        }
    }
    
    /**
     * 初始化存储管理器（从类路径加载配置）
     * 
     * @param resourcePath 类路径中的配置文件
     * @throws IOException 配置文件加载失败时抛出异常
     * @throws StorageException 适配器创建失败时抛出异常
     */
    public void initializeFromClasspath(String resourcePath) throws IOException, StorageException {
        if (initialized) {
            logger.warn("BlackholeStorageManager is already initialized");
            return;
        }
        
        logger.info("Initializing BlackholeStorageManager from classpath resource: {}", resourcePath);
        
        try {
            // 加载配置文件
            Map<String, DatabaseConfig> configs = ConfigurationManager.loadFromClasspath(resourcePath);
            
            // 创建存储适配器
            for (Map.Entry<String, DatabaseConfig> entry : configs.entrySet()) {
                String name = entry.getKey();
                DatabaseConfig config = entry.getValue();
                
                if (config.isEnabled()) {
                    try {
                        DatabaseStorage adapter = StorageAdapterFactory.createAdapter(config);
                        adapters.put(name, adapter);
                        logger.info("Created adapter for database: {} (type: {})", name, config.getType());
                    } catch (StorageException e) {
                        logger.error("Failed to create adapter for database: {} (type: {})", name, config.getType(), e);
                        throw e;
                    }
                } else {
                    logger.info("Skipping disabled database: {} (type: {})", name, config.getType());
                }
            }
            
            initialized = true;
            logger.info("BlackholeStorageManager initialized successfully with {} adapters", adapters.size());
            
        } catch (IOException e) {
            logger.error("Failed to load configuration from classpath: {}", resourcePath, e);
            throw e;
        }
    }
    
    /**
     * 获取指定名称的存储适配器
     * 
     * @param name 数据库名称
     * @return 存储适配器实例，不存在时抛出异常
     * @throws StorageException 未初始化或适配器不存在时抛出异常
     */
    public DatabaseStorage getAdapter(String name) throws StorageException {
        checkInitialized();
        
        DatabaseStorage adapter = adapters.get(name);
        if (adapter == null) {
            throw new StorageException("ADAPTER_NOT_FOUND", "Database adapter not found: " + name);
        }
        
        return adapter;
    }
    
    /**
     * 获取默认存储适配器
     * 
     * @return 默认存储适配器实例
     * @throws StorageException 未初始化或默认适配器不存在时抛出异常
     */
    public DatabaseStorage getDefaultAdapter() throws StorageException {
        checkInitialized();
        
        DatabaseConfig defaultConfig = ConfigurationManager.getDefaultConfig();
        if (defaultConfig == null) {
            throw new StorageException("NO_DEFAULT_ADAPTER", "No default database adapter configured");
        }
        
        return getAdapter(defaultConfig.getName());
    }
    
    /**
     * 获取SQLite适配器
     * 
     * @param name 数据库名称
     * @return SQLite适配器实例
     * @throws StorageException 未初始化或适配器不存在时抛出异常
     */
    public DatabaseStorage getSQLiteAdapter(String name) throws StorageException {
        return getAdapter(name);
    }
    
    /**
     * 获取MySQL适配器
     * 
     * @param name 数据库名称
     * @return MySQL适配器实例
     * @throws StorageException 未初始化或适配器不存在时抛出异常
     */
    public DatabaseStorage getMySQLAdapter(String name) throws StorageException {
        return getAdapter(name);
    }
    
    /**
     * 获取Redis适配器
     * 
     * @param name 数据库名称
     * @return Redis适配器实例
     * @throws StorageException 未初始化或适配器不存在时抛出异常
     */
    public DatabaseStorage getRedisAdapter(String name) throws StorageException {
        return getAdapter(name);
    }
    
    /**
     * 添加存储适配器
     * 
     * @param name 数据库名称
     * @param adapter 存储适配器实例
     * @throws StorageException 已存在同名适配器时抛出异常
     */
    public void addAdapter(String name, DatabaseStorage adapter) throws StorageException {
        checkInitialized();
        
        if (adapters.containsKey(name)) {
            throw new StorageException("ADAPTER_EXISTS", "Database adapter already exists: " + name);
        }
        
        adapters.put(name, adapter);
        logger.info("Added adapter for database: {} (type: {})", name, adapter.getDatabaseType());
    }
    
    /**
     * 移除存储适配器
     * 
     * @param name 数据库名称
     * @return 被移除的适配器实例，不存在时返回null
     * @throws StorageException 未初始化时抛出异常
     */
    public DatabaseStorage removeAdapter(String name) throws StorageException {
        checkInitialized();
        
        DatabaseStorage adapter = adapters.remove(name);
        if (adapter != null) {
            try {
                adapter.shutdown();
                logger.info("Removed and shut down adapter for database: {} (type: {})", 
                        name, adapter.getDatabaseType());
            } catch (StorageException e) {
                logger.warn("Failed to shut down adapter for database: {}", name, e);
            }
        }
        
        return adapter;
    }
    
    /**
     * 检查适配器是否存在
     * 
     * @param name 数据库名称
     * @return true表示存在
     */
    public boolean hasAdapter(String name) {
        return adapters.containsKey(name);
    }
    
    /**
     * 获取所有适配器名称
     * 
     * @return 适配器名称数组
     */
    public String[] getAdapterNames() {
        return adapters.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取适配器数量
     * 
     * @return 适配器数量
     */
    public int getAdapterCount() {
        return adapters.size();
    }
    
    /**
     * 关闭存储管理器
     * 关闭所有适配器并清理资源
     */
    public void shutdown() {
        if (!initialized) {
            logger.warn("BlackholeStorageManager is not initialized");
            return;
        }
        
        logger.info("Shutting down BlackholeStorageManager");
        
        // 关闭所有适配器
        for (Map.Entry<String, DatabaseStorage> entry : adapters.entrySet()) {
            String name = entry.getKey();
            DatabaseStorage adapter = entry.getValue();
            
            try {
                adapter.shutdown();
                logger.info("Successfully shut down adapter for database: {} (type: {})", 
                        name, adapter.getDatabaseType());
            } catch (StorageException e) {
                logger.warn("Failed to shut down adapter for database: {}", name, e);
            }
        }
        
        adapters.clear();
        initialized = false;
        logger.info("BlackholeStorageManager shut down successfully");
    }
    
    /**
     * 获取管理器状态
     * 
     * @return 状态信息映射
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("initialized", initialized);
        status.put("adapterCount", adapters.size());
        status.put("adapterNames", getAdapterNames());
        
        // 获取每个适配器的状态
        Map<String, String> adapterStatus = new ConcurrentHashMap<>();
        for (Map.Entry<String, DatabaseStorage> entry : adapters.entrySet()) {
            String name = entry.getKey();
            DatabaseStorage adapter = entry.getValue();
            
            try {
                String health = adapter.isHealthy() ? "Healthy" : "Unhealthy";
                String info = adapter.getConnectionInfo();
                adapterStatus.put(name, String.format("%s (%s)", health, info));
            } catch (Exception e) {
                adapterStatus.put(name, "Error: " + e.getMessage());
            }
        }
        status.put("adapterStatus", adapterStatus);
        
        return status;
    }
    
    /**
     * 检查是否已初始化
     * 
     * @throws StorageException 未初始化时抛出异常
     */
    private void checkInitialized() throws StorageException {
        if (!initialized) {
            throw new StorageException("NOT_INITIALIZED", "BlackholeStorageManager is not initialized");
        }
    }
    
    /**
     * 获取实例（静态方法）
     * 
     * @return Blackhole存储管理器实例
     */
    public static BlackholeStorageManager getStorageManager() {
        return getInstance();
    }
    
    /**
     * 快速初始化方法（从默认配置文件）
     * 
     * @return Blackhole存储管理器实例
     * @throws IOException 配置文件加载失败时抛出异常
     * @throws StorageException 适配器创建失败时抛出异常
     */
    public static BlackholeStorageManager quickStart() throws IOException, StorageException {
        BlackholeStorageManager manager = getInstance();
        manager.initializeFromClasspath("database.yml");
        return manager;
    }
}