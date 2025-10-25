package database.adapter;

import database.config.DatabaseConfig;
import database.core.StorageException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HBase数据库适配器
 * Apache HBase数据库的存储适配器（待实现）
 * 
 * @author Collections Team
 * @since 1.0
 */
public class HBaseAdapter extends BaseStorageAdapter {
    
    /**
     * 构造函数
     * 
     * @param config 数据库配置
     */
    public HBaseAdapter(DatabaseConfig config) {
        super(config);
    }
    
    @Override
    public String getDatabaseType() {
        return "HBase";
    }
    
    @Override
    public String getConnectionInfo() {
        return "HBase [Not Implemented]";
    }
    
    @Override
    protected void doInitialize() throws Exception {
        throw new StorageException("NOT_IMPLEMENTED", "HBase adapter is not implemented yet");
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
        throw new SQLException("HBase adapter is not implemented yet");
    }
    
    @Override
    protected String addPagination(String sql, int pageNum, int pageSize) {
        // HBase分页逻辑（待实现）
        return sql; // 暂时返回原SQL
    }
}