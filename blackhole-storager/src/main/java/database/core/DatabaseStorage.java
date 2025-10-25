package database.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 统一数据库存储接口
 * 定义所有数据库操作的标准方法，支持多种数据库类型
 * 
 * @author Collections Team
 * @since 1.0
 */
public interface DatabaseStorage {
    
    /**
     * 初始化数据库连接
     * 
     * @throws StorageException 初始化失败时抛出异常
     */
    void initialize() throws StorageException;
    
    /**
     * 关闭数据库连接
     * 
     * @throws StorageException 关闭失败时抛出异常
     */
    void shutdown() throws StorageException;
    
    /**
     * 检查数据库连接是否健康
     * 
     * @return true 表示连接正常，false 表示连接异常
     */
    boolean isHealthy();
    
    /**
     * 单条记录查询
     * 
     * @param <T> 返回类型
     * @param sql SQL查询语句
     * @param mapper 结果集映射器
     * @param params 查询参数
     * @return 查询结果的Optional包装
     * @throws StorageException 查询失败时抛出异常
     */
    <T> Optional<T> querySingle(String sql, RowMapper<T> mapper, Object... params) throws StorageException;
    
    /**
     * 多条记录查询
     * 
     * @param <T> 返回类型
     * @param sql SQL查询语句
     * @param mapper 结果集映射器
     * @param params 查询参数
     * @return 查询结果列表
     * @throws StorageException 查询失败时抛出异常
     */
    <T> List<T> queryMultiple(String sql, RowMapper<T> mapper, Object... params) throws StorageException;
    
    /**
     * 分页查询
     * 
     * @param <T> 返回类型
     * @param sql SQL查询语句
     * @param mapper 结果集映射器
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 分页查询结果
     * @throws StorageException 查询失败时抛出异常
     */
    <T> Page<T> queryPage(String sql, RowMapper<T> mapper, int pageNum, int pageSize, Object... params) throws StorageException;
    
    /**
     * 单条记录插入
     * 
     * @param table 表名
     * @param data 要插入的数据（字段名-值映射）
     * @return 影响的行数
     * @throws StorageException 插入失败时抛出异常
     */
    int insert(String table, Map<String, Object> data) throws StorageException;
    
    /**
     * 批量插入
     * 
     * @param table 表名
     * @param dataList 要插入的数据列表
     * @return 每条记录影响的行数数组
     * @throws StorageException 插入失败时抛出异常
     */
    int[] insertBatch(String table, List<Map<String, Object>> dataList) throws StorageException;
    
    /**
     * 单条记录更新
     * 
     * @param table 表名
     * @param data 要更新的数据（字段名-值映射）
     * @param condition 更新条件
     * @param params 条件参数
     * @return 影响的行数
     * @throws StorageException 更新失败时抛出异常
     */
    int update(String table, Map<String, Object> data, String condition, Object... params) throws StorageException;
    
    /**
     * 批量更新
     * 
     * @param table 表名
     * @param dataList 要更新的数据列表
     * @param condition 更新条件模板
     * @return 每条记录影响的行数数组
     * @throws StorageException 更新失败时抛出异常
     */
    int[] updateBatch(String table, List<Map<String, Object>> dataList, String condition) throws StorageException;
    
    /**
     * 单条记录删除
     * 
     * @param table 表名
     * @param condition 删除条件
     * @param params 条件参数
     * @return 影响的行数
     * @throws StorageException 删除失败时抛出异常
     */
    int delete(String table, String condition, Object... params) throws StorageException;
    
    /**
     * 批量删除
     * 
     * @param table 表名
     * @param conditions 删除条件列表
     * @return 每条记录影响的行数数组
     * @throws StorageException 删除失败时抛出异常
     */
    int[] deleteBatch(String table, List<String> conditions) throws StorageException;
    
    /**
     * 开始事务
     * 
     * @throws StorageException 开始事务失败时抛出异常
     */
    void beginTransaction() throws StorageException;
    
    /**
     * 提交事务
     * 
     * @throws StorageException 提交事务失败时抛出异常
     */
    void commit() throws StorageException;
    
    /**
     * 回滚事务
     * 
     * @throws StorageException 回滚事务失败时抛出异常
     */
    void rollback() throws StorageException;
    
    /**
     * 检查是否处于事务中
     * 
     * @return true 表示处于事务中，false 表示不在事务中
     */
    boolean isInTransaction();
    
    /**
     * 获取数据库类型
     * 
     * @return 数据库类型名称
     */
    String getDatabaseType();
    
    /**
     * 获取连接信息（不包含敏感信息）
     * 
     * @return 连接信息描述
     */
    String getConnectionInfo();
}