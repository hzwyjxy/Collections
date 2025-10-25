package database.adapter;

import database.config.DatabaseConfig;
import database.core.StorageException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hive数据库适配器
 * Apache Hive数据库的存储适配器（待实现）
 * 
 * @author Collections Team
 * @since 1.0
 */
public class HiveAdapter extends BaseStorageAdapter {
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    public HiveAdapter(DatabaseConfig config) {
        super(config);
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
    
    @Override
    public String getConnectionInfo() {
        return "Hive [Not Implemented]";
    }
    
    @Override
    protected void doInitialize() throws Exception {
        throw new StorageException("NOT_IMPLEMENTED", "Hive adapter is not implemented yet");
    }
    
    @Override
    protected void doShutdown() throws Exception {
        // 不需要实现
    }
    
    @Override
    protected boolean doHealthCheck() throws Exception {
        return false;
    }
    
    @Override
    protected Connection doGetConnection() throws SQLException {
        throw new SQLException("Hive adapter is not implemented yet");
    }
    
    @Override
    protected String addPagination(String sql, int pageNum, int pageSize) {
        // Hive分页逻辑（待实现）
        return sql; // 暂时返回原SQL
    }
}