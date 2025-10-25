package database.adapter;

import database.config.DatabaseConfig;
import database.core.StorageException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL数据库适配器
 * 实现MySQL数据库的连接和操作
 * 
 * @author Collections Team
 * @since 1.0
 */
public class MySQLAdapter extends BaseStorageAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLAdapter.class);
    
    /**
     * HikariCP数据源
     */
    private HikariDataSource dataSource;
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    public MySQLAdapter(DatabaseConfig config) {
        super(config);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public String getConnectionInfo() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format("MySQL [%s@%s] - Pool: %d/%d", 
                    config.getConnectionProperties().getOrDefault("username", "unknown"),
                    config.getConnectionProperties().getOrDefault("host", "unknown"),
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections());
        }
        return "MySQL [Not Connected]";
    }
    
    @Override
    protected void doInitialize() throws Exception {
        logger.info("Initializing MySQL adapter");
        
        // 创建HikariCP配置
        HikariConfig hikariConfig = new HikariConfig();
        
        // 构建JDBC URL
        String host = config.getConnectionProperties().getOrDefault("host", "localhost");
        String port = config.getConnectionProperties().getOrDefault("port", "3306");
        String database = config.getConnectionProperties().get("database");
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&characterEncoding=utf8", 
                host, port, database);
        hikariConfig.setJdbcUrl(url);
        
        // 设置认证信息
        String username = config.getConnectionProperties().get("username");
        String password = config.getConnectionProperties().get("password");
        if (username == null || username.trim().isEmpty()) {
            throw new StorageException("CONFIG_INVALID", "MySQL username is required");
        }
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password != null ? password : "");
        
        // 设置驱动类名
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
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
        
        // 设置MySQL特定属性
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        
        // 设置连接属性
        for (String key : config.getConnectionProperties().keySet()) {
            if (!"host".equals(key) && !"port".equals(key) && !"database".equals(key) && 
                !"username".equals(key) && !"password".equals(key)) {
                hikariConfig.addDataSourceProperty(key, config.getConnectionProperties().get(key));
            }
        }
        
        // 设置池名称
        hikariConfig.setPoolName("MySQL-" + config.getName());
        
        // 创建数据源
        dataSource = new HikariDataSource(hikariConfig);
        
        // 测试连接
        try (Connection conn = dataSource.getConnection()) {
            logger.info("Successfully connected to MySQL database: {}@{}:{}", username, host, port);
        } catch (SQLException e) {
            throw new StorageException("CONNECTION_FAILED", "Failed to connect to MySQL database", e);
        }
    }
    
    @Override
    protected void doShutdown() throws Exception {
        logger.info("Shutting down MySQL adapter");
        
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("MySQL connection pool closed");
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
            logger.warn("MySQL health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected Connection doGetConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("MySQL connection pool is not available");
        }
        return dataSource.getConnection();
    }
    
    @Override
    protected String addPagination(String sql, int pageNum, int pageSize) {
        // MySQL分页使用LIMIT和OFFSET
        int offset = (pageNum - 1) * pageSize;
        return sql + " LIMIT " + pageSize + " OFFSET " + offset;
    }
    
    /**
     * 获取MySQL服务器版本信息
     * 
     * @return MySQL版本信息
     */
    public String getMySQLVersion() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.warn("Failed to get MySQL version", e);
        }
        return "Unknown";
    }
    
    /**
     * 获取MySQL服务器状态信息
     * 
     * @return 状态信息映射
     */
    public Map<String, String> getServerStatus() {
        Map<String, String> status = new HashMap<>();
        
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SHOW STATUS")) {
            
            while (rs.next()) {
                String variableName = rs.getString("Variable_name");
                String variableValue = rs.getString("Value");
                status.put(variableName, variableValue);
            }
            
        } catch (SQLException e) {
            logger.warn("Failed to get MySQL server status", e);
        }
        
        return status;
    }
    
    /**
     * 获取MySQL数据库统计信息
     * 
     * @return 统计信息映射
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取MySQL版本
            stats.put("mysqlVersion", getMySQLVersion());
            
            // 获取连接池统计信息
            if (dataSource != null && !dataSource.isClosed()) {
                stats.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
                stats.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
                stats.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
                stats.put("waitingForConnection", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            }
            
            // 获取数据库大小信息
            try (Connection conn = getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT table_schema, SUM(data_length + index_length) as size " +
                     "FROM information_schema.tables " +
                     "WHERE table_schema = DATABASE() " +
                     "GROUP BY table_schema")) {
                
                if (rs.next()) {
                    long size = rs.getLong("size");
                    stats.put("databaseSize", size);
                    stats.put("databaseSizeMB", String.format("%.2f", size / (1024.0 * 1024.0)));
                }
            }
            
            // 获取表数量
            try (Connection conn = getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as table_count FROM information_schema.tables " +
                     "WHERE table_schema = DATABASE()")) {
                
                if (rs.next()) {
                    stats.put("tableCount", rs.getInt("table_count"));
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to get database stats", e);
        }
        
        return stats;
    }
    
    /**
     * 执行MySQL特定的SHOW命令
     * 
     * @param command SHOW命令
     * @return 执行结果
     */
    public Map<String, String> executeShowCommand(String command) {
        Map<String, String> result = new HashMap<>();
        
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SHOW " + command)) {
            
            if (rs.next()) {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(i);
                    result.put(columnName, columnValue);
                }
            }
            
        } catch (SQLException e) {
            logger.warn("Failed to execute SHOW command: {}", command, e);
        }
        
        return result;
    }
}