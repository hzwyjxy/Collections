package database.examples;

import database.BlackholeStorageManager;
import database.config.ConfigurationManager;
import database.core.DatabaseStorage;
import database.core.StorageException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 快速入门示例
 * 展示如何使用Blackhole存储管理器快速开始使用各种数据库
 * 
 * @author Collections Team
 * @since 1.0
 */
public class QuickStartExample {
    
    public static void main(String[] args) {
        QuickStartExample example = new QuickStartExample();
        
        try {
            // 运行快速入门示例
            example.runQuickStart();
            
            // 运行配置管理示例
            example.runConfigurationExample();
            
            // 运行多数据库示例
            example.runMultiDatabaseExample();
            
        } catch (Exception e) {
            System.err.println("快速入门示例运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 快速入门 - 最简单的方式开始使用
     */
    public void runQuickStart() throws IOException, StorageException {
        System.out.println("=== Blackhole存储管理器 - 快速入门 ===");
        
        // 1. 使用默认配置快速启动
        System.out.println("1. 使用默认配置快速启动...");
        BlackholeStorageManager storageManager = BlackholeStorageManager.quickStart();
        
        // 2. 获取默认的SQLite适配器
        System.out.println("2. 获取默认SQLite适配器...");
        DatabaseStorage sqliteAdapter = storageManager.getDefaultAdapter();
        System.out.println("默认适配器类型: " + sqliteAdapter.getDatabaseType());
        
        // 3. 执行简单的SQL操作
        System.out.println("3. 执行简单SQL操作...");
        
        // 创建表
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS quick_start_demo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(100) NOT NULL,
                value VARCHAR(200),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        sqliteAdapter.update(createTableSql);
        System.out.println("创建表成功");
        
        // 插入数据
        String insertSql = "INSERT INTO quick_start_demo (name, value) VALUES (?, ?)";
        sqliteAdapter.insert(insertSql, "测试数据1", "这是第一条测试数据");
        sqliteAdapter.insert(insertSql, "测试数据2", "这是第二条测试数据");
        sqliteAdapter.insert(insertSql, "测试数据3", "这是第三条测试数据");
        System.out.println("插入数据成功");
        
        // 查询数据
        String querySql = "SELECT * FROM quick_start_demo ORDER BY id";
        List<Map<String, Object>> results = sqliteAdapter.queryMultiple(querySql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("name", rs.getString("name"));
            row.put("value", rs.getString("value"));
            row.put("created_at", rs.getTimestamp("created_at"));
            return row;
        });
        
        System.out.println("查询结果:");
        results.forEach(row -> {
            System.out.println("  ID: " + row.get("id") + ", 名称: " + row.get("name") + ", 值: " + row.get("value"));
        });
        
        // 4. 关闭存储管理器
        System.out.println("4. 关闭存储管理器...");
        storageManager.shutdown();
        
        System.out.println("=== 快速入门完成 ===");
    }
    
    /**
     * 配置管理示例
     */
    public void runConfigurationExample() throws IOException, StorageException {
        System.out.println("\n=== 配置管理示例 ===");
        
        // 1. 创建配置管理器
        System.out.println("1. 创建配置管理器...");
        ConfigurationManager configManager = new ConfigurationManager();
        
        // 2. 从classpath加载配置
        System.out.println("2. 从classpath加载配置...");
        configManager.loadFromClasspath("database.yml");
        
        // 3. 获取特定数据库配置
        System.out.println("3. 获取数据库配置...");
        var sqliteConfig = configManager.getConfiguration("sqlite-dev");
        var mysqlConfig = configManager.getConfiguration("mysql-dev");
        var redisConfig = configManager.getConfiguration("redis-dev");
        
        System.out.println("SQLite配置 - 类型: " + sqliteConfig.getType() + ", 名称: " + sqliteConfig.getName());
        System.out.println("MySQL配置 - 类型: " + mysqlConfig.getType() + ", 名称: " + mysqlConfig.getName());
        System.out.println("Redis配置 - 类型: " + redisConfig.getType() + ", 名称: " + redisConfig.getName());
        
        // 4. 获取默认配置
        var defaultConfig = configManager.getDefaultConfiguration();
        System.out.println("默认配置: " + defaultConfig.getName() + " (" + defaultConfig.getType() + ")");
        
        // 5. 获取所有配置
        var allConfigs = configManager.getAllConfigurations();
        System.out.println("所有配置数量: " + allConfigs.size());
        allConfigs.forEach((name, config) -> {
            System.out.println("  - " + name + ": " + config.getType());
        });
        
        // 6. 重新加载配置
        System.out.println("6. 重新加载配置...");
        configManager.reload();
        System.out.println("配置重新加载完成");
        
        System.out.println("=== 配置管理示例完成 ===");
    }
    
    /**
     * 多数据库示例
     */
    public void runMultiDatabaseExample() throws IOException, StorageException {
        System.out.println("\n=== 多数据库示例 ===");
        
        // 1. 初始化存储管理器
        System.out.println("1. 初始化存储管理器...");
        BlackholeStorageManager storageManager = BlackholeStorageManager.getInstance();
        storageManager.initializeFromClasspath("database.yml");
        
        // 2. 获取不同类型的数据库适配器
        System.out.println("2. 获取不同数据库适配器...");
        DatabaseStorage sqliteAdapter = storageManager.getSQLiteAdapter("sqlite-dev");
        DatabaseStorage mysqlAdapter = storageManager.getMySQLAdapter("mysql-dev");
        DatabaseStorage redisAdapter = storageManager.getRedisAdapter("redis-dev");
        
        System.out.println("成功获取适配器:");
        System.out.println("  - SQLite: " + sqliteAdapter.getDatabaseType());
        System.out.println("  - MySQL: " + mysqlAdapter.getDatabaseType());
        System.out.println("  - Redis: " + redisAdapter.getDatabaseType());
        
        // 3. SQLite操作
        System.out.println("3. SQLite操作...");
        sqliteOperations(sqliteAdapter);
        
        // 4. MySQL操作
        System.out.println("4. MySQL操作...");
        mysqlOperations(mysqlAdapter);
        
        // 5. Redis操作
        System.out.println("5. Redis操作...");
        redisOperations(redisAdapter);
        
        // 6. 健康检查
        System.out.println("6. 健康检查...");
        checkHealth(sqliteAdapter, "SQLite");
        checkHealth(mysqlAdapter, "MySQL");
        checkHealth(redisAdapter, "Redis");
        
        // 7. 关闭存储管理器
        System.out.println("7. 关闭存储管理器...");
        storageManager.shutdown();
        
        System.out.println("=== 多数据库示例完成 ===");
    }
    
    /**
     * SQLite操作
     */
    private void sqliteOperations(DatabaseStorage adapter) throws StorageException {
        System.out.println("  SQLite - 创建表并插入数据...");
        
        // 创建表
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS sqlite_demo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data_type VARCHAR(50) NOT NULL,
                content TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        adapter.update(createTableSql);
        
        // 插入数据
        String insertSql = "INSERT INTO sqlite_demo (data_type, content) VALUES (?, ?)";
        adapter.insert(insertSql, "sqlite", "这是SQLite中的数据");
        
        // 查询数据
        String querySql = "SELECT * FROM sqlite_demo WHERE data_type = ?";
        List<Map<String, Object>> results = adapter.queryMultiple(querySql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("data_type", rs.getString("data_type"));
            row.put("content", rs.getString("content"));
            row.put("created_at", rs.getTimestamp("created_at"));
            return row;
        }, "sqlite");
        
        System.out.println("  SQLite查询结果: " + results.size() + "条记录");
        results.forEach(row -> {
            System.out.println("    - " + row.get("data_type") + ": " + row.get("content"));
        });
    }
    
    /**
     * MySQL操作
     */
    private void mysqlOperations(DatabaseStorage adapter) throws StorageException {
        System.out.println("  MySQL - 创建表并插入数据...");
        
        // 创建表
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS mysql_demo (
                id INT AUTO_INCREMENT PRIMARY KEY,
                data_type VARCHAR(50) NOT NULL,
                content TEXT,
                status VARCHAR(20) DEFAULT 'active',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        adapter.update(createTableSql);
        
        // 插入数据
        String insertSql = "INSERT INTO mysql_demo (data_type, content) VALUES (?, ?)";
        adapter.insert(insertSql, "mysql", "这是MySQL中的数据");
        
        // 查询数据
        String querySql = "SELECT * FROM mysql_demo WHERE data_type = ?";
        List<Map<String, Object>> results = adapter.queryMultiple(querySql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("data_type", rs.getString("data_type"));
            row.put("content", rs.getString("content"));
            row.put("status", rs.getString("status"));
            row.put("created_at", rs.getTimestamp("created_at"));
            return row;
        }, "mysql");
        
        System.out.println("  MySQL查询结果: " + results.size() + "条记录");
        results.forEach(row -> {
            System.out.println("    - " + row.get("data_type") + ": " + row.get("content") + " (状态: " + row.get("status") + ")");
        });
    }
    
    /**
     * Redis操作
     */
    private void redisOperations(DatabaseStorage adapter) throws StorageException {
        System.out.println("  Redis - 设置键值对...");
        
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            // 字符串操作
            redis.set("demo:string", "这是Redis中的字符串数据");
            redis.setex("demo:temp", 60, "这是60秒后过期的临时数据");
            
            // 哈希操作
            redis.hset("demo:hash", "type", "redis");
            redis.hset("demo:hash", "content", "这是Redis中的哈希数据");
            redis.hset("demo:hash", "timestamp", String.valueOf(System.currentTimeMillis()));
            
            // 列表操作
            redis.lpush("demo:list", "item1", "item2", "item3");
            
            // 集合操作
            redis.sadd("demo:set", "member1", "member2", "member3");
            
            // 读取数据
            String stringValue = redis.get("demo:string");
            String hashType = redis.hget("demo:hash", "type");
            String hashContent = redis.hget("demo:hash", "content");
            long listSize = 3; // lpush了3个元素
            long setSize = 3;  // sadd了3个元素
            
            System.out.println("  Redis查询结果:");
            System.out.println("    - 字符串: " + stringValue);
            System.out.println("    - 哈希: type=" + hashType + ", content=" + hashContent);
            System.out.println("    - 列表大小: " + listSize);
            System.out.println("    - 集合大小: " + setSize);
            
            // 清理演示数据
            redis.del("demo:string", "demo:temp", "demo:hash", "demo:list", "demo:set");
            System.out.println("  清理演示数据完成");
        }
    }
    
    /**
     * 健康检查
     */
    private void checkHealth(DatabaseStorage adapter, String databaseType) {
        try {
            boolean isHealthy = adapter.isHealthy();
            System.out.println("  " + databaseType + "健康状态: " + (isHealthy ? "健康" : "不健康"));
            
            if (isHealthy) {
                String connectionInfo = adapter.getConnectionInfo();
                System.out.println("  " + databaseType + "连接信息: " + connectionInfo);
            }
        } catch (Exception e) {
            System.out.println("  " + databaseType + "健康检查失败: " + e.getMessage());
        }
    }
}