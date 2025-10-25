package database.examples;

import database.BlackholeStorageManager;
import database.adapter.SQLiteAdapter;
import database.config.ConfigurationManager;
import database.core.DatabaseStorage;
import database.core.RowMapper;
import database.core.StorageException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SQLite使用示例
 * 展示如何使用Blackhole存储管理器操作SQLite数据库
 * 
 * @author Collections Team
 * @since 1.0
 */
public class SQLiteExample {
    
    /**
     * 用户实体类
     */
    public static class User {
        private Long id;
        private String name;
        private String email;
        private Integer age;
        
        // 构造函数
        public User() {}
        
        public User(Long id, String name, String email, Integer age) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        
        @Override
        public String toString() {
            return String.format("User{id=%d, name='%s', email='%s', age=%d}", id, name, email, age);
        }
    }
    
    /**
     * 用户RowMapper
     */
    public static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setAge(rs.getInt("age"));
            return user;
        }
    }
    
    public static void main(String[] args) {
        SQLiteExample example = new SQLiteExample();
        
        try {
            // 运行示例
            example.runExample();
        } catch (Exception e) {
            System.err.println("示例运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 运行SQLite示例
     */
    public void runExample() throws IOException, StorageException {
        System.out.println("=== SQLite使用示例 ===");
        
        // 1. 初始化存储管理器
        System.out.println("1. 初始化存储管理器...");
        BlackholeStorageManager storageManager = BlackholeStorageManager.getInstance();
        storageManager.initializeFromClasspath("database.yml");
        
        // 2. 获取SQLite适配器
        System.out.println("2. 获取SQLite适配器...");
        DatabaseStorage sqliteAdapter = storageManager.getSQLiteAdapter("sqlite-dev");
        
        // 3. 创建表
        System.out.println("3. 创建用户表...");
        createUserTable(sqliteAdapter);
        
        // 4. 插入数据
        System.out.println("4. 插入用户数据...");
        insertUsers(sqliteAdapter);
        
        // 5. 查询数据
        System.out.println("5. 查询用户数据...");
        queryUsers(sqliteAdapter);
        
        // 6. 更新数据
        System.out.println("6. 更新用户数据...");
        updateUser(sqliteAdapter);
        
        // 7. 删除数据
        System.out.println("7. 删除用户数据...");
        deleteUser(sqliteAdapter);
        
        // 8. 批量操作
        System.out.println("8. 批量操作示例...");
        batchOperations(sqliteAdapter);
        
        // 9. 分页查询
        System.out.println("9. 分页查询示例...");
        paginatedQuery(sqliteAdapter);
        
        // 10. 事务操作
        System.out.println("10. 事务操作示例...");
        transactionExample(sqliteAdapter);
        
        // 11. 获取数据库统计信息
        System.out.println("11. 获取数据库统计信息...");
        showDatabaseStats(sqliteAdapter);
        
        // 12. 关闭存储管理器
        System.out.println("12. 关闭存储管理器...");
        storageManager.shutdown();
        
        System.out.println("=== SQLite示例运行完成 ===");
    }
    
    /**
     * 创建用户表
     */
    private void createUserTable(DatabaseStorage adapter) throws StorageException {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                age INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        adapter.update(createTableSql);
        System.out.println("用户表创建成功");
    }
    
    /**
     * 插入用户数据
     */
    private void insertUsers(DatabaseStorage adapter) throws StorageException {
        // 单条插入
        String insertSql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        
        adapter.insert(insertSql, "张三", "zhangsan@example.com", 25);
        adapter.insert(insertSql, "李四", "lisi@example.com", 30);
        adapter.insert(insertSql, "王五", "wangwu@example.com", 28);
        
        System.out.println("插入3条用户数据成功");
        
        // 批量插入
        String batchInsertSql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        List<Object[]> batchParams = Arrays.asList(
            new Object[]{"赵六", "zhaoliu@example.com", 32},
            new Object[]{"钱七", "qianqi@example.com", 27},
            new Object[]{"孙八", "sunba@example.com", 29}
        );
        
        int[] results = adapter.batchInsert(batchInsertSql, batchParams);
        System.out.println("批量插入" + results.length + "条用户数据成功");
    }
    
    /**
     * 查询用户数据
     */
    private void queryUsers(DatabaseStorage adapter) throws StorageException {
        UserRowMapper rowMapper = new UserRowMapper();
        
        // 单条查询
        String singleQuerySql = "SELECT * FROM users WHERE email = ?";
        User user = adapter.querySingle(singleQuerySql, rowMapper, "zhangsan@example.com");
        System.out.println("单条查询结果: " + user);
        
        // 多条查询
        String multipleQuerySql = "SELECT * FROM users WHERE age > ? ORDER BY name";
        List<User> users = adapter.queryMultiple(multipleQuerySql, rowMapper, 26);
        System.out.println("多条查询结果（年龄>26）:");
        users.forEach(System.out::println);
        
        // 查询所有用户
        String allUsersSql = "SELECT * FROM users ORDER BY id";
        List<User> allUsers = adapter.queryMultiple(allUsersSql, rowMapper);
        System.out.println("所有用户（共" + allUsers.size() + "条）:");
        allUsers.forEach(System.out::println);
    }
    
    /**
     * 更新用户数据
     */
    private void updateUser(DatabaseStorage adapter) throws StorageException {
        String updateSql = "UPDATE users SET age = ? WHERE email = ?";
        int updatedRows = adapter.update(updateSql, 26, "zhangsan@example.com");
        System.out.println("更新了" + updatedRows + "条用户数据");
        
        // 验证更新结果
        UserRowMapper rowMapper = new UserRowMapper();
        String verifySql = "SELECT * FROM users WHERE email = ?";
        User updatedUser = adapter.querySingle(verifySql, rowMapper, "zhangsan@example.com");
        System.out.println("更新后的用户: " + updatedUser);
    }
    
    /**
     * 删除用户数据
     */
    private void deleteUser(DatabaseStorage adapter) throws StorageException {
        String deleteSql = "DELETE FROM users WHERE email = ?";
        int deletedRows = adapter.delete(deleteSql, "sunba@example.com");
        System.out.println("删除了" + deletedRows + "条用户数据");
    }
    
    /**
     * 批量操作示例
     */
    private void batchOperations(DatabaseStorage adapter) throws StorageException {
        // 批量更新年龄
        String batchUpdateSql = "UPDATE users SET age = age + 1 WHERE name = ?";
        List<Object[]> batchParams = Arrays.asList(
            new Object[]{"张三"},
            new Object[]{"李四"},
            new Object[]{"王五"}
        );
        
        // 执行批量更新（需要手动实现，因为BaseStorageAdapter只支持批量插入）
        for (Object[] params : batchParams) {
            adapter.update(batchUpdateSql, params);
        }
        System.out.println("批量更新年龄完成");
    }
    
    /**
     * 分页查询示例
     */
    private void paginatedQuery(DatabaseStorage adapter) throws StorageException {
        UserRowMapper rowMapper = new UserRowMapper();
        String querySql = "SELECT * FROM users ORDER BY id";
        
        // 查询第1页，每页3条
        var page1 = adapter.queryPage(querySql, rowMapper, 1, 3);
        System.out.println("第1页结果（共" + page1.getTotalElements() + "条，" + page1.getTotalPages() + "页）:");
        page1.getContent().forEach(System.out::println);
        
        // 查询第2页，每页3条
        if (page1.hasNext()) {
            var page2 = adapter.queryPage(querySql, rowMapper, 2, 3);
            System.out.println("第2页结果:");
            page2.getContent().forEach(System.out::println);
        }
    }
    
    /**
     * 事务操作示例
     */
    private void transactionExample(DatabaseStorage adapter) throws StorageException {
        try {
            // 开始事务
            adapter.beginTransaction();
            
            // 在事务中执行多个操作
            String insertSql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
            adapter.insert(insertSql, "事务用户1", "transaction1@example.com", 35);
            adapter.insert(insertSql, "事务用户2", "transaction2@example.com", 36);
            
            // 更新操作
            String updateSql = "UPDATE users SET age = ? WHERE email = ?";
            adapter.update(updateSql, 37, "transaction1@example.com");
            
            // 提交事务
            adapter.commitTransaction();
            System.out.println("事务操作成功完成");
            
        } catch (Exception e) {
            // 回滚事务
            try {
                adapter.rollbackTransaction();
                System.out.println("事务已回滚");
            } catch (StorageException rollbackEx) {
                System.err.println("事务回滚失败: " + rollbackEx.getMessage());
            }
            throw new StorageException("TRANSACTION_FAILED", "事务操作失败", e);
        }
    }
    
    /**
     * 显示数据库统计信息
     */
    private void showDatabaseStats(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof SQLiteAdapter) {
            SQLiteAdapter sqliteAdapter = (SQLiteAdapter) adapter;
            
            // 获取数据库文件大小
            long fileSize = sqliteAdapter.getDatabaseFileSize();
            System.out.println("数据库文件大小: " + (fileSize / 1024) + " KB");
            
            // 获取SQLite版本
            String version = sqliteAdapter.getSQLiteVersion();
            System.out.println("SQLite版本: " + version);
            
            // 获取数据库统计信息
            Map<String, Object> stats = sqliteAdapter.getDatabaseStats();
            System.out.println("数据库统计信息:");
            stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            
            // 获取连接信息
            String connectionInfo = sqliteAdapter.getConnectionInfo();
            System.out.println("连接信息: " + connectionInfo);
            
            // 健康检查
            boolean isHealthy = sqliteAdapter.isHealthy();
            System.out.println("健康状态: " + (isHealthy ? "健康" : "不健康"));
        }
    }
}