package database.examples;

import database.BlackholeStorageManager;
import database.adapter.MySQLAdapter;
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
 * MySQL使用示例
 * 展示如何使用Blackhole存储管理器操作MySQL数据库
 * 
 * @author Collections Team
 * @since 1.0
 */
public class MySQLExample {
    
    /**
     * 产品实体类
     */
    public static class Product {
        private Long id;
        private String name;
        private String category;
        private Double price;
        private Integer stock;
        private String description;
        
        // 构造函数
        public Product() {}
        
        public Product(Long id, String name, String category, Double price, Integer stock, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.description = description;
        }
        
        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        @Override
        public String toString() {
            return String.format("Product{id=%d, name='%s', category='%s', price=%.2f, stock=%d, description='%s'}", 
                id, name, category, price, stock, description);
        }
    }
    
    /**
     * 产品RowMapper
     */
    public static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setDescription(rs.getString("description"));
            return product;
        }
    }
    
    public static void main(String[] args) {
        MySQLExample example = new MySQLExample();
        
        try {
            // 运行示例
            example.runExample();
        } catch (Exception e) {
            System.err.println("示例运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 运行MySQL示例
     */
    public void runExample() throws IOException, StorageException {
        System.out.println("=== MySQL使用示例 ===");
        
        // 1. 初始化存储管理器
        System.out.println("1. 初始化存储管理器...");
        BlackholeStorageManager storageManager = BlackholeStorageManager.getInstance();
        storageManager.initializeFromClasspath("database.yml");
        
        // 2. 获取MySQL适配器
        System.out.println("2. 获取MySQL适配器...");
        DatabaseStorage mysqlAdapter = storageManager.getMySQLAdapter("mysql-dev");
        
        // 3. 创建表
        System.out.println("3. 创建产品表...");
        createProductTable(mysqlAdapter);
        
        // 4. 插入数据
        System.out.println("4. 插入产品数据...");
        insertProducts(mysqlAdapter);
        
        // 5. 查询数据
        System.out.println("5. 查询产品数据...");
        queryProducts(mysqlAdapter);
        
        // 6. 更新数据
        System.out.println("6. 更新产品数据...");
        updateProduct(mysqlAdapter);
        
        // 7. 删除数据
        System.out.println("7. 删除产品数据...");
        deleteProduct(mysqlAdapter);
        
        // 8. 复杂查询
        System.out.println("8. 复杂查询示例...");
        complexQueries(mysqlAdapter);
        
        // 9. 分页查询
        System.out.println("9. 分页查询示例...");
        paginatedQuery(mysqlAdapter);
        
        // 10. 事务操作
        System.out.println("10. 事务操作示例...");
        transactionExample(mysqlAdapter);
        
        // 11. 获取数据库统计信息
        System.out.println("11. 获取数据库统计信息...");
        showDatabaseStats(mysqlAdapter);
        
        // 12. 关闭存储管理器
        System.out.println("12. 关闭存储管理器...");
        storageManager.shutdown();
        
        System.out.println("=== MySQL示例运行完成 ===");
    }
    
    /**
     * 创建产品表
     */
    private void createProductTable(DatabaseStorage adapter) throws StorageException {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS products (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                category VARCHAR(100) NOT NULL,
                price DECIMAL(10,2) NOT NULL,
                stock INT DEFAULT 0,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_category (category),
                INDEX idx_name (name),
                INDEX idx_price (price)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        
        adapter.update(createTableSql);
        System.out.println("产品表创建成功");
    }
    
    /**
     * 插入产品数据
     */
    private void insertProducts(DatabaseStorage adapter) throws StorageException {
        // 单条插入
        String insertSql = """
            INSERT INTO products (name, category, price, stock, description) 
            VALUES (?, ?, ?, ?, ?)
        """;
        
        adapter.insert(insertSql, "iPhone 15 Pro", "手机", 7999.00, 100, "苹果最新旗舰手机");
        adapter.insert(insertSql, "MacBook Pro 14", "笔记本", 14999.00, 50, "苹果专业级笔记本电脑");
        adapter.insert(insertSql, "AirPods Pro", "耳机", 1999.00, 200, "苹果无线降噪耳机");
        adapter.insert(insertSql, "iPad Air", "平板", 4399.00, 80, "苹果轻薄平板电脑");
        adapter.insert(insertSql, "Apple Watch", "手表", 2999.00, 150, "苹果智能手表");
        
        System.out.println("插入5条产品数据成功");
        
        // 批量插入
        String batchInsertSql = """
            INSERT INTO products (name, category, price, stock, description) 
            VALUES (?, ?, ?, ?, ?)
        """;
        List<Object[]> batchParams = Arrays.asList(
            new Object[]{"Samsung Galaxy S24", "手机", 5999.00, 120, "三星最新旗舰手机"},
            new Object[]{"Dell XPS 13", "笔记本", 8999.00, 30, "戴尔超薄笔记本电脑"},
            new Object[]{"Sony WH-1000XM5", "耳机", 2299.00, 80, "索尼顶级降噪耳机"},
            new Object[]{"Surface Pro 9", "平板", 7888.00, 60, "微软二合一平板电脑"},
            new Object[]{"Garmin Fenix 7", "手表", 4680.00, 40, "佳明户外运动手表"}
        );
        
        int[] results = adapter.batchInsert(batchInsertSql, batchParams);
        System.out.println("批量插入" + results.length + "条产品数据成功");
    }
    
    /**
     * 查询产品数据
     */
    private void queryProducts(DatabaseStorage adapter) throws StorageException {
        ProductRowMapper rowMapper = new ProductRowMapper();
        
        // 单条查询
        String singleQuerySql = "SELECT * FROM products WHERE name = ?";
        Product product = adapter.querySingle(singleQuerySql, rowMapper, "iPhone 15 Pro");
        System.out.println("单条查询结果: " + product);
        
        // 多条查询 - 按类别
        String categoryQuerySql = "SELECT * FROM products WHERE category = ? ORDER BY price DESC";
        List<Product> phones = adapter.queryMultiple(categoryQuerySql, rowMapper, "手机");
        System.out.println("手机类产品（共" + phones.size() + "条）:");
        phones.forEach(System.out::println);
        
        // 查询库存不足的产品
        String lowStockQuerySql = "SELECT * FROM products WHERE stock < ? ORDER BY stock ASC";
        List<Product> lowStockProducts = adapter.queryMultiple(lowStockQuerySql, rowMapper, 50);
        System.out.println("库存不足的产品（库存<50）:");
        lowStockProducts.forEach(System.out::println);
    }
    
    /**
     * 更新产品数据
     */
    private void updateProduct(DatabaseStorage adapter) throws StorageException {
        // 更新价格
        String updatePriceSql = "UPDATE products SET price = ? WHERE name = ?";
        int updatedRows = adapter.update(updatePriceSql, 7599.00, "iPhone 15 Pro");
        System.out.println("更新了" + updatedRows + "条产品价格");
        
        // 更新库存
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE name = ?";
        int updatedStockRows = adapter.update(updateStockSql, 5, "iPhone 15 Pro");
        System.out.println("更新了" + updatedStockRows + "条产品库存");
        
        // 验证更新结果
        ProductRowMapper rowMapper = new ProductRowMapper();
        String verifySql = "SELECT * FROM products WHERE name = ?";
        Product updatedProduct = adapter.querySingle(verifySql, rowMapper, "iPhone 15 Pro");
        System.out.println("更新后的产品: " + updatedProduct);
    }
    
    /**
     * 删除产品数据
     */
    private void deleteProduct(DatabaseStorage adapter) throws StorageException {
        // 删除库存为0的产品（假设）
        String deleteSql = "DELETE FROM products WHERE stock = 0";
        int deletedRows = adapter.delete(deleteSql);
        System.out.println("删除了" + deletedRows + "条库存为0的产品数据");
    }
    
    /**
     * 复杂查询示例
     */
    private void complexQueries(DatabaseStorage adapter) throws StorageException {
        ProductRowMapper rowMapper = new ProductRowMapper();
        
        // 价格范围查询
        String priceRangeSql = """
            SELECT * FROM products 
            WHERE price BETWEEN ? AND ? 
            ORDER BY price ASC
        """;
        List<Product> midRangeProducts = adapter.queryMultiple(priceRangeSql, rowMapper, 2000.00, 5000.00);
        System.out.println("价格区间2000-5000的产品（共" + midRangeProducts.size() + "条）:");
        midRangeProducts.forEach(System.out::println);
        
        // 多条件查询
        String multiConditionSql = """
            SELECT * FROM products 
            WHERE category = ? AND price < ? AND stock > ?
            ORDER BY price DESC
        """;
        List<Product> affordablePhones = adapter.queryMultiple(multiConditionSql, rowMapper, "手机", 7000.00, 50);
        System.out.println("价格低于7000且库存充足（>50）的手机产品:");
        affordablePhones.forEach(System.out::println);
        
        // 模糊查询
        String fuzzyQuerySql = "SELECT * FROM products WHERE description LIKE ? ORDER BY name";
        List<Product> wirelessProducts = adapter.queryMultiple(fuzzyQuerySql, rowMapper, "%无线%");
        System.out.println("描述中包含'无线'的产品:");
        wirelessProducts.forEach(System.out::println);
    }
    
    /**
     * 分页查询示例
     */
    private void paginatedQuery(DatabaseStorage adapter) throws StorageException {
        ProductRowMapper rowMapper = new ProductRowMapper();
        String querySql = "SELECT * FROM products ORDER BY price DESC";
        
        // 查询第1页，每页4条
        var page1 = adapter.queryPage(querySql, rowMapper, 1, 4);
        System.out.println("第1页结果（共" + page1.getTotalElements() + "条，" + page1.getTotalPages() + "页）:");
        page1.getContent().forEach(System.out::println);
        
        // 查询第2页，每页4条
        if (page1.hasNext()) {
            var page2 = adapter.queryPage(querySql, rowMapper, 2, 4);
            System.out.println("第2页结果:");
            page2.getContent().forEach(System.out::println);
        }
        
        // 查询最后一页
        var lastPage = adapter.queryPage(querySql, rowMapper, page1.getTotalPages(), 4);
        System.out.println("最后一页（第" + page1.getTotalPages() + "页）:");
        lastPage.getContent().forEach(System.out::println);
    }
    
    /**
     * 事务操作示例
     */
    private void transactionExample(DatabaseStorage adapter) throws StorageException {
        try {
            // 开始事务
            adapter.beginTransaction();
            
            System.out.println("开始事务操作...");
            
            // 在事务中执行多个操作
            String insertSql = "INSERT INTO products (name, category, price, stock, description) VALUES (?, ?, ?, ?, ?)";
            adapter.insert(insertSql, "事务产品1", "测试类别", 999.00, 10, "事务测试产品1");
            adapter.insert(insertSql, "事务产品2", "测试类别", 888.00, 20, "事务测试产品2");
            
            // 更新操作
            String updateSql = "UPDATE products SET stock = stock - ? WHERE name = ?";
            int updatedRows = adapter.update(updateSql, 5, "iPhone 15 Pro");
            System.out.println("事务中更新库存: " + updatedRows + " 条记录");
            
            // 删除操作
            String deleteSql = "DELETE FROM products WHERE category = ? AND price < ?";
            int deletedRows = adapter.delete(deleteSql, "测试类别", 100.00);
            System.out.println("事务中删除低价测试产品: " + deletedRows + " 条记录");
            
            // 提交事务
            adapter.commitTransaction();
            System.out.println("事务提交成功");
            
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
        if (adapter instanceof MySQLAdapter) {
            MySQLAdapter mysqlAdapter = (MySQLAdapter) adapter;
            
            // 获取MySQL版本
            String version = mysqlAdapter.getMySQLVersion();
            System.out.println("MySQL版本: " + version);
            
            // 获取服务器状态
            String status = mysqlAdapter.getServerStatus();
            System.out.println("服务器状态: " + status);
            
            // 获取数据库统计信息
            Map<String, Object> stats = mysqlAdapter.getDatabaseStats();
            System.out.println("数据库统计信息:");
            stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            
            // 获取连接信息
            String connectionInfo = mysqlAdapter.getConnectionInfo();
            System.out.println("连接信息: " + connectionInfo);
            
            // 健康检查
            boolean isHealthy = mysqlAdapter.isHealthy();
            System.out.println("健康状态: " + (isHealthy ? "健康" : "不健康"));
            
            // 执行SHOW命令
            String showDatabases = "SHOW DATABASES";
            var databases = mysqlAdapter.queryMultiple(showDatabases, (rs, rowNum) -> rs.getString(1));
            System.out.println("可用数据库: " + databases);
        }
    }
}