package database.adapter;

import database.config.DatabaseConfig;
import database.core.DatabaseStorage;
import database.core.Page;
import database.core.RowMapper;
import database.core.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础存储适配器
 * 提供通用的数据库操作实现，供具体数据库适配器继承
 * 
 * @author Collections Team
 * @since 1.0
 */
public abstract class BaseStorageAdapter implements DatabaseStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseStorageAdapter.class);
    
    /**
     * 数据库配置
     */
    protected final DatabaseConfig config;
    
    /**
     * 适配器状态
     */
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /**
     * 事务状态
     */
    protected final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    protected BaseStorageAdapter(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
    }
    
    @Override
    public void initialize() throws StorageException {
        if (initialized.compareAndSet(false, true)) {
            try {
                logger.info("Initializing {} adapter for database: {}", getDatabaseType(), config.getName());
                doInitialize();
                logger.info("Successfully initialized {} adapter", getDatabaseType());
            } catch (Exception e) {
                initialized.set(false);
                throw new StorageException("INIT_FAILED", "Failed to initialize " + getDatabaseType() + " adapter", e);
            }
        }
    }
    
    @Override
    public void shutdown() throws StorageException {
        if (initialized.compareAndSet(true, false)) {
            try {
                logger.info("Shutting down {} adapter for database: {}", getDatabaseType(), config.getName());
                doShutdown();
                logger.info("Successfully shut down {} adapter", getDatabaseType());
            } catch (Exception e) {
                throw new StorageException("SHUTDOWN_FAILED", "Failed to shut down " + getDatabaseType() + " adapter", e);
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
            logger.warn("Health check failed for {} adapter: {}", getDatabaseType(), e.getMessage());
            return false;
        }
    }
    
    @Override
    public <T> T querySingle(String sql, RowMapper<T> rowMapper, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing single query: {}", sql);
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T result = rowMapper.mapRow(rs, 1);
                    logSlowQuery(sql, startTime);
                    return result;
                }
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Single query failed: {}", sql, e);
            throw new StorageException("QUERY_FAILED", "Failed to execute single query", e);
        }
    }
    
    @Override
    public <T> List<T> queryMultiple(String sql, RowMapper<T> rowMapper, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing multiple query: {}", sql);
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 1;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                logSlowQuery(sql, startTime);
                return results;
            }
            
        } catch (SQLException e) {
            logger.error("Multiple query failed: {}", sql, e);
            throw new StorageException("QUERY_FAILED", "Failed to execute multiple query", e);
        }
    }
    
    @Override
    public <T> Page<T> queryPage(String sql, RowMapper<T> rowMapper, int pageNum, int pageSize, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing paged query: {} (page: {}, size: {})", sql, pageNum, pageSize);
        long startTime = System.currentTimeMillis();
        
        // 获取总记录数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS total";
        long totalElements = querySingle(countSql, rs -> rs.getLong(1), params);
        
        // 执行分页查询
        String pageSql = addPagination(sql, pageNum, pageSize);
        List<T> content = queryMultiple(pageSql, rowMapper, params);
        
        logSlowQuery(sql, startTime);
        return new Page<>(content, pageNum, pageSize, totalElements);
    }
    
    @Override
    public int insert(String sql, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing insert: {}", sql);
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            logSlowQuery(sql, startTime);
            return result;
            
        } catch (SQLException e) {
            logger.error("Insert failed: {}", sql, e);
            throw new StorageException("INSERT_FAILED", "Failed to execute insert", e);
        }
    }
    
    @Override
    public int[] batchInsert(String sql, List<Object[]> batchParams) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing batch insert: {} (batch size: {})", sql, batchParams.size());
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Object[] params : batchParams) {
                setParameters(stmt, params);
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            conn.commit();
            
            logSlowQuery(sql, startTime);
            return results;
            
        } catch (SQLException e) {
            logger.error("Batch insert failed: {}", sql, e);
            try {
                if (!getConnection().getAutoCommit()) {
                    getConnection().rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.error("Failed to rollback batch insert", rollbackEx);
            }
            throw new StorageException("BATCH_INSERT_FAILED", "Failed to execute batch insert", e);
        }
    }
    
    @Override
    public int update(String sql, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing update: {}", sql);
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            logSlowQuery(sql, startTime);
            return result;
            
        } catch (SQLException e) {
            logger.error("Update failed: {}", sql, e);
            throw new StorageException("UPDATE_FAILED", "Failed to execute update", e);
        }
    }
    
    @Override
    public int delete(String sql, Object... params) throws StorageException {
        checkInitialized();
        
        logger.debug("Executing delete: {}", sql);
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            logSlowQuery(sql, startTime);
            return result;
            
        } catch (SQLException e) {
            logger.error("Delete failed: {}", sql, e);
            throw new StorageException("DELETE_FAILED", "Failed to execute delete", e);
        }
    }
    
    @Override
    public void beginTransaction() throws StorageException {
        checkInitialized();
        
        if (transactionConnection.get() != null) {
            throw new StorageException("TRANSACTION_ACTIVE", "Transaction is already active");
        }
        
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            transactionConnection.set(conn);
            logger.debug("Transaction started");
        } catch (SQLException e) {
            throw new StorageException("TRANSACTION_START_FAILED", "Failed to start transaction", e);
        }
    }
    
    @Override
    public void commitTransaction() throws StorageException {
        checkInitialized();
        
        Connection conn = transactionConnection.get();
        if (conn == null) {
            throw new StorageException("NO_TRANSACTION", "No active transaction");
        }
        
        try {
            conn.commit();
            conn.setAutoCommit(true);
            logger.debug("Transaction committed");
        } catch (SQLException e) {
            throw new StorageException("TRANSACTION_COMMIT_FAILED", "Failed to commit transaction", e);
        } finally {
            closeQuietly(conn);
            transactionConnection.remove();
        }
    }
    
    @Override
    public void rollbackTransaction() throws StorageException {
        checkInitialized();
        
        Connection conn = transactionConnection.get();
        if (conn == null) {
            throw new StorageException("NO_TRANSACTION", "No active transaction");
        }
        
        try {
            conn.rollback();
            conn.setAutoCommit(true);
            logger.debug("Transaction rolled back");
        } catch (SQLException e) {
            throw new StorageException("TRANSACTION_ROLLBACK_FAILED", "Failed to rollback transaction", e);
        } finally {
            closeQuietly(conn);
            transactionConnection.remove();
        }
    }
    
    /**
     * 检查适配器是否已初始化
     * 
     * @throws StorageException 未初始化时抛出异常
     */
    protected void checkInitialized() throws StorageException {
        if (!initialized.get()) {
            throw new StorageException("NOT_INITIALIZED", "Adapter is not initialized");
        }
    }
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     * @throws SQLException 获取连接失败时抛出异常
     */
    protected Connection getConnection() throws SQLException {
        Connection conn = transactionConnection.get();
        if (conn != null) {
            return conn;
        }
        return doGetConnection();
    }
    
    /**
     * 设置参数
     * 
     * @param stmt 预编译语句
     * @param params 参数数组
     * @throws SQLException 设置参数失败时抛出异常
     */
    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * 记录慢查询
     * 
     * @param sql SQL语句
     * @param startTime 开始时间
     */
    protected void logSlowQuery(String sql, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 这里可以添加慢查询阈值配置
        long slowQueryThreshold = 1000; // 1秒
        
        if (executionTime > slowQueryThreshold) {
            logger.warn("Slow query detected: {} ({}ms)", sql, executionTime);
        } else {
            logger.debug("Query executed in {}ms: {}", executionTime, sql);
        }
    }
    
    /**
     * 关闭连接（静默模式）
     * 
     * @param conn 连接
     */
    protected void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.warn("Failed to close connection", e);
            }
        }
    }
    
    /**
     * 添加分页逻辑（由子类实现）
     * 
     * @param sql 原始SQL
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页SQL
     */
    protected abstract String addPagination(String sql, int pageNum, int pageSize);
    
    /**
     * 执行初始化操作（由子类实现）
     * 
     * @throws Exception 初始化失败时抛出异常
     */
    protected abstract void doInitialize() throws Exception;
    
    /**
     * 执行关闭操作（由子类实现）
     * 
     * @throws Exception 关闭失败时抛出异常
     */
    protected abstract void doShutdown() throws Exception;
    
    /**
     * 执行健康检查（由子类实现）
     * 
     * @return 健康检查结果
     * @throws Exception 健康检查失败时抛出异常
     */
    protected abstract boolean doHealthCheck() throws Exception;
    
    /**
     * 获取数据库连接（由子类实现）
     * 
     * @return 数据库连接
     * @throws SQLException 获取连接失败时抛出异常
     */
    protected abstract Connection doGetConnection() throws SQLException;
}