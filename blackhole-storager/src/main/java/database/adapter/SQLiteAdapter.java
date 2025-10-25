package database.adapter;

import database.config.DatabaseConfig;
import database.core.StorageException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLite数据库适配器
 * 实现SQLite数据库的连接和操作
 * 
 * @author Collections Team
 * @since 1.0
 */
public class SQLiteAdapter extends BaseStorageAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLiteAdapter.class);
    
    /**
     * HikariCP数据源
     */
    private HikariDataSource dataSource;
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    public SQLiteAdapter(DatabaseConfig config) {
        super(config);
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLite";
    }
    
    @Override
    public String getConnectionInfo() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format("SQLite [%s] - Pool: %d/%d", 
                    config.getConnectionProperties().getOrDefault("url", "unknown"),
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections());
        }
        return "SQLite [Not Connected]";
    }
    
    @Override
    protected void doInitialize() throws Exception {
        logger.info("Initializing SQLite adapter");
        
        // 创建HikariCP配置
        HikariConfig hikariConfig = new HikariConfig();
        
        // 设置数据库URL
        String url = config.getConnectionProperties().get("url");
        if (url == null || url.trim().isEmpty()) {
            throw new StorageException("CONFIG_INVALID", "SQLite URL is required");
        }
        hikariConfig.setJdbcUrl(url);
        
        // 设置驱动类名
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        
        // 设置连接池配置
        if (config.getPoolConfig() != null) {
            hikariConfig.setMaximumPoolSize(config.getPoolConfig().getMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getPoolConfig().getMinPoolSize());
            hikariConfig.setConnectionTimeout(config.getPoolConfig().getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getPoolConfig().getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getPoolConfig().getMaxLifetime());
            hikariConfig.setLeakDetectionThreshold(config.getPoolConfig().getLeakDetectionThreshold());
            hikariConfig.setAutoCommit(config.getPoolConfig().isAutoCommit());
            
            // 设置连接测试查询
            String testQuery = config.getPoolConfig().getConnectionTestQuery();
            if (testQuery != null && !testQuery.trim().isEmpty()) {
                hikariConfig.setConnectionTestQuery(testQuery);
            } else {
                hikariConfig.setConnectionTestQuery("SELECT 1");
            }
        }
        
        // 设置SQLite特定属性
        hikariConfig.addDataSourceProperty("foreign_keys", "true");
        hikariConfig.addDataSourceProperty("journal_mode", "WAL"); // 使用WAL模式提高并发性能
        hikariConfig.addDataSourceProperty("synchronous", "normal");
        hikariConfig.addDataSourceProperty("cache_size", "10000");
        hikariConfig.addDataSourceProperty("temp_store", "memory");
        
        // 设置连接属性
        for (String key : config.getConnectionProperties().keySet()) {
            if (!"url".equals(key)) {
                hikariConfig.addDataSourceProperty(key, config.getConnectionProperties().get(key));
            }
        }
        
        // 设置池名称
        hikariConfig.setPoolName("SQLite-" + config.getName());
        
        // 创建数据源
        dataSource = new HikariDataSource(hikariConfig);
        
        // 测试连接
        try (Connection conn = dataSource.getConnection()) {
            logger.info("Successfully connected to SQLite database: {}", url);
        } catch (SQLException e) {
            throw new StorageException("CONNECTION_FAILED", "Failed to connect to SQLite database", e);
        }
    }
    
    @Override
    protected void doShutdown() throws Exception {
        logger.info("Shutting down SQLite adapter");
        
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("SQLite connection pool closed");
        }
    }
    
    @Override
    protected boolean doHealthCheck() throws Exception {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        
        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            logger.warn("SQLite health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected Connection doGetConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("SQLite connection pool is not available");
        }
        return dataSource.getConnection();
    }
    
    @Override
    protected String addPagination(String sql, int pageNum, int pageSize) {
        // SQLite分页使用LIMIT和OFFSET
        int offset = (pageNum - 1) * pageSize;
        return sql + " LIMIT " + pageSize + " OFFSET " + offset;
    }
    
    /**
     * 获取SQLite数据库文件大小
     * 
     * @return 数据库文件大小（字节）
     */
    public long getDatabaseFileSize() {
        String url = config.getConnectionProperties().get("url");
        if (url != null && url.startsWith("jdbc:sqlite:")) {
            String filePath = url.substring("jdbc:sqlite:".length());
            java.io.File dbFile = new java.io.File(filePath);
            if (dbFile.exists()) {
                return dbFile.length();
            }
        }
        return -1;
    }
    
    /**
     * 获取SQLite数据库版本信息
     * 
     * @return SQLite版本信息
     */
    public String getSQLiteVersion() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT sqlite_version()")) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.warn("Failed to get SQLite version", e);
        }
        return "Unknown";
    }
    
    /**
     * 执行SQLite特定的PRAGMA命令
     * 
     * @param pragma PRAGMA命令
     * @return 执行结果
     */
    public String executePragma(String pragma) {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("PRAGMA " + pragma)) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.warn("Failed to execute PRAGMA: {}", pragma, e);
        }
        return null;
    }
    
    /**
     * 获取SQLite数据库统计信息
     * 
     * @return 统计信息映射
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // 获取数据库大小
            long fileSize = getDatabaseFileSize();
            if (fileSize >= 0) {
                stats.put("fileSize", fileSize);
                stats.put("fileSizeMB", String.format("%.2f", fileSize / (1024.0 * 1024.0)));
            }
            
            // 获取SQLite版本
            stats.put("sqliteVersion", getSQLiteVersion());
            
            // 获取连接池统计信息
            if (dataSource != null && !dataSource.isClosed()) {
                stats.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
                stats.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
                stats.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
                stats.put("waitingForConnection", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            }
            
            // 获取PRAGMA信息
            stats.put("journalMode", executePragma("journal_mode"));
            stats.put("synchronous", executePragma("synchronous"));
            stats.put("cacheSize", executePragma("cache_size"));
            stats.put("tempStore", executePragma("temp_store"));
            
        } catch (Exception e) {
            logger.warn("Failed to get database stats", e);
        }
        
        return stats;
    }
}