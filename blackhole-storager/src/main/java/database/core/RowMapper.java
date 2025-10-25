package database.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射器接口
 * 用于将数据库结果集的行数据映射为Java对象
 * 
 * @param <T> 目标类型
 * @author Collections Team
 * @since 1.0
 */
@FunctionalInterface
public interface RowMapper<T> {
    
    /**
     * 将结果集的当前行映射为目标对象
     * 
     * @param rs 结果集
     * @param rowNum 行号（从1开始）
     * @return 映射后的对象
     * @throws SQLException 映射失败时抛出异常
     */
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}