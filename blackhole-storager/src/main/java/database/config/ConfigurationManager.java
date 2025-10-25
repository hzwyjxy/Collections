package database.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 * 负责加载和管理数据库配置文件
 * 
 * @author Collections Team
 * @since 1.0
 */
public class ConfigurationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    
    /**
     * YAML文件映射器
     */
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * 配置文件缓存
     */
    private static final Map<String, DatabaseConfig> configCache = new HashMap<>();
    
    /**
     * 全局配置实例
     */
    private static GlobalConfig globalConfig;
    
    /**
     * 私有构造函数，防止实例化
     */
    private ConfigurationManager() {
        // 工具类，不需要实例化
    }
    
    /**
     * 从文件系统加载配置文件
     * 
     * @param configFilePath 配置文件路径
     * @return 数据库配置映射
     * @throws IOException 加载失败时抛出异常
     */
    public static Map<String, DatabaseConfig> loadFromFile(String configFilePath) throws IOException {
        logger.info("Loading database configuration from file: {}", configFilePath);
        
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFilePath);
        }
        
        try {
            ConfigWrapper wrapper = yamlMapper.readValue(configFile, ConfigWrapper.class);
            Map<String, DatabaseConfig> configs = wrapper.getDatabases();
            globalConfig = wrapper.getGlobal();
            
            // 验证配置
            validateConfigs(configs);
            
            // 缓存配置
            configCache.putAll(configs);
            
            logger.info("Successfully loaded {} database configurations", configs.size());
            return configs;
            
        } catch (IOException e) {
            logger.error("Failed to load configuration from file: {}", configFilePath, e);
            throw new IOException("Failed to load configuration from file: " + configFilePath, e);
        }
    }
    
    /**
     * 从类路径加载配置文件
     * 
     * @param resourcePath 类路径中的配置文件
     * @return 数据库配置映射
     * @throws IOException 加载失败时抛出异常
     */
    public static Map<String, DatabaseConfig> loadFromClasspath(String resourcePath) throws IOException {
        logger.info("Loading database configuration from classpath: {}", resourcePath);
        
        try (InputStream inputStream = ConfigurationManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Configuration resource not found in classpath: " + resourcePath);
            }
            
            ConfigWrapper wrapper = yamlMapper.readValue(inputStream, ConfigWrapper.class);
            Map<String, DatabaseConfig> configs = wrapper.getDatabases();
            globalConfig = wrapper.getGlobal();
            
            // 验证配置
            validateConfigs(configs);
            
            // 缓存配置
            configCache.putAll(configs);
            
            logger.info("Successfully loaded {} database configurations from classpath", configs.size());
            return configs;
            
        } catch (IOException e) {
            logger.error("Failed to load configuration from classpath: {}", resourcePath, e);
            throw new IOException("Failed to load configuration from classpath: " + resourcePath, e);
        }
    }
    
    /**
     * 获取指定名称的数据库配置
     * 
     * @param name 数据库配置名称
     * @return 数据库配置，不存在时返回null
     */
    public static DatabaseConfig getConfig(String name) {
        return configCache.get(name);
    }
    
    /**
     * 获取默认数据库配置
     * 
     * @return 默认数据库配置，不存在时返回null
     */
    public static DatabaseConfig getDefaultConfig() {
        if (globalConfig != null && globalConfig.getDefaultDatabase() != null) {
            return configCache.get(globalConfig.getDefaultDatabase());
        }
        
        // 如果没有设置默认数据库，返回第一个启用的数据库配置
        return configCache.values().stream()
                .filter(DatabaseConfig::isEnabled)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取全局配置
     * 
     * @return 全局配置
     */
    public static GlobalConfig getGlobalConfig() {
        return globalConfig;
    }
    
    /**
     * 获取所有数据库配置
     * 
     * @return 所有数据库配置
     */
    public static Map<String, DatabaseConfig> getAllConfigs() {
        return new HashMap<>(configCache);
    }
    
    /**
     * 重新加载配置
     * 
     * @param configFilePath 配置文件路径
     * @throws IOException 重新加载失败时抛出异常
     */
    public static void reload(String configFilePath) throws IOException {
        logger.info("Reloading database configuration from file: {}", configFilePath);
        
        // 清空缓存
        configCache.clear();
        globalConfig = null;
        
        // 重新加载
        loadFromFile(configFilePath);
        
        logger.info("Configuration reloaded successfully");
    }
    
    /**
     * 验证配置有效性
     * 
     * @param configs 配置映射
     * @throws IllegalArgumentException 配置无效时抛出异常
     */
    private static void validateConfigs(Map<String, DatabaseConfig> configs) throws IllegalArgumentException {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("No database configurations found");
        }
        
        for (Map.Entry<String, DatabaseConfig> entry : configs.entrySet()) {
            String name = entry.getKey();
            DatabaseConfig config = entry.getValue();
            
            if (config == null) {
                throw new IllegalArgumentException("Database configuration '" + name + "' is null");
            }
            
            // 只验证启用的配置
            if (config.isEnabled()) {
                try {
                    config.validate();
                    logger.debug("Database configuration '{}' is valid", name);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid database configuration '" + name + "': " + e.getMessage(), e);
                }
            } else {
                logger.debug("Database configuration '{}' is disabled, skipping validation", name);
            }
        }
        
        // 验证全局配置
        if (globalConfig != null && globalConfig.getDefaultDatabase() != null) {
            if (!configs.containsKey(globalConfig.getDefaultDatabase())) {
                throw new IllegalArgumentException("Default database '" + globalConfig.getDefaultDatabase() + "' not found in configurations");
            }
            
            DatabaseConfig defaultConfig = configs.get(globalConfig.getDefaultDatabase());
            if (!defaultConfig.isEnabled()) {
                throw new IllegalArgumentException("Default database '" + globalConfig.getDefaultDatabase() + "' is disabled");
            }
        }
    }
    
    /**
     * 配置包装器类
     * 用于YAML文件的根结构
     */
    private static class ConfigWrapper {
        private Map<String, DatabaseConfig> databases;
        private GlobalConfig global;
        
        public Map<String, DatabaseConfig> getDatabases() {
            return databases != null ? databases : new HashMap<>();
        }
        
        public void setDatabases(Map<String, DatabaseConfig> databases) {
            this.databases = databases;
        }
        
        public GlobalConfig getGlobal() {
            return global != null ? global : new GlobalConfig();
        }
        
        public void setGlobal(GlobalConfig global) {
            this.global = global;
        }
    }
    
    /**
     * 全局配置类
     */
    private static class GlobalConfig {
        private String defaultDatabase;
        private boolean healthCheckEnabled = true;
        private long healthCheckInterval = 30000; // 30秒
        private long slowQueryThreshold = 1000; // 1秒
        private boolean sqlLoggingEnabled = false;
        private int maxRetries = 3;
        private long retryDelay = 1000; // 1秒
        
        public String getDefaultDatabase() {
            return defaultDatabase;
        }
        
        public void setDefaultDatabase(String defaultDatabase) {
            this.defaultDatabase = defaultDatabase;
        }
        
        public boolean isHealthCheckEnabled() {
            return healthCheckEnabled;
        }
        
        public void setHealthCheckEnabled(boolean healthCheckEnabled) {
            this.healthCheckEnabled = healthCheckEnabled;
        }
        
        public long getHealthCheckInterval() {
            return healthCheckInterval;
        }
        
        public void setHealthCheckInterval(long healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
        }
        
        public long getSlowQueryThreshold() {
            return slowQueryThreshold;
        }
        
        public void setSlowQueryThreshold(long slowQueryThreshold) {
            this.slowQueryThreshold = slowQueryThreshold;
        }
        
        public boolean isSqlLoggingEnabled() {
            return sqlLoggingEnabled;
        }
        
        public void setSqlLoggingEnabled(boolean sqlLoggingEnabled) {
            this.sqlLoggingEnabled = sqlLoggingEnabled;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public long getRetryDelay() {
            return retryDelay;
        }
        
        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }
    }
}