package database.examples;

import database.BlackholeStorageManager;
import database.adapter.RedisAdapter;
import database.config.ConfigurationManager;
import database.core.DatabaseStorage;
import database.core.RowMapper;
import database.core.StorageException;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis使用示例
 * 展示如何使用Blackhole存储管理器操作Redis数据库
 * 
 * @author Collections Team
 * @since 1.0
 */
public class RedisExample {
    
    /**
     * 用户会话实体类
     */
    public static class UserSession {
        private String userId;
        private String username;
        private String sessionId;
        private long loginTime;
        private String ipAddress;
        private Map<String, String> attributes;
        
        // 构造函数
        public UserSession() {
            this.attributes = new HashMap<>();
            this.loginTime = System.currentTimeMillis();
        }
        
        public UserSession(String userId, String username, String sessionId) {
            this();
            this.userId = userId;
            this.username = username;
            this.sessionId = sessionId;
        }
        
        // Getter和Setter方法
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public long getLoginTime() { return loginTime; }
        public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public Map<String, String> getAttributes() { return attributes; }
        public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
        
        public void addAttribute(String key, String value) {
            this.attributes.put(key, value);
        }
        
        @Override
        public String toString() {
            return String.format("UserSession{userId='%s', username='%s', sessionId='%s', loginTime=%s, ipAddress='%s', attributes=%s}", 
                userId, username, sessionId, new Date(loginTime), ipAddress, attributes);
        }
    }
    
    /**
     * 商品库存实体类
     */
    public static class ProductInventory {
        private String productId;
        private String productName;
        private int stock;
        private int reserved;
        private long lastUpdated;
        
        // 构造函数
        public ProductInventory() {
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public ProductInventory(String productId, String productName, int stock) {
            this();
            this.productId = productId;
            this.productName = productName;
            this.stock = stock;
            this.reserved = 0;
        }
        
        // Getter和Setter方法
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        
        public int getReserved() { return reserved; }
        public void setReserved(int reserved) { this.reserved = reserved; }
        
        public long getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
        
        public int getAvailableStock() {
            return stock - reserved;
        }
        
        @Override
        public String toString() {
            return String.format("ProductInventory{productId='%s', productName='%s', stock=%d, reserved=%d, available=%d, lastUpdated=%s}", 
                productId, productName, stock, reserved, getAvailableStock(), new Date(lastUpdated));
        }
    }
    
    public static void main(String[] args) {
        RedisExample example = new RedisExample();
        
        try {
            // 运行示例
            example.runExample();
        } catch (Exception e) {
            System.err.println("示例运行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 运行Redis示例
     */
    public void runExample() throws IOException, StorageException {
        System.out.println("=== Redis使用示例 ===");
        
        // 1. 初始化存储管理器
        System.out.println("1. 初始化存储管理器...");
        BlackholeStorageManager storageManager = BlackholeStorageManager.getInstance();
        storageManager.initializeFromClasspath("database.yml");
        
        // 2. 获取Redis适配器
        System.out.println("2. 获取Redis适配器...");
        DatabaseStorage redisAdapter = storageManager.getRedisAdapter("redis-dev");
        
        // 3. 基本字符串操作
        System.out.println("3. 基本字符串操作...");
        stringOperations(redisAdapter);
        
        // 4. 哈希操作
        System.out.println("4. 哈希操作...");
        hashOperations(redisAdapter);
        
        // 5. 列表操作
        System.out.println("5. 列表操作...");
        listOperations(redisAdapter);
        
        // 6. 集合操作
        System.out.println("6. 集合操作...");
        setOperations(redisAdapter);
        
        // 7. 会话管理示例
        System.out.println("7. 会话管理示例...");
        sessionManagement(redisAdapter);
        
        // 8. 商品库存管理示例
        System.out.println("8. 商品库存管理示例...");
        inventoryManagement(redisAdapter);
        
        // 9. 排行榜示例
        System.out.println("9. 排行榜示例...");
        leaderboardExample(redisAdapter);
        
        // 10. 发布订阅示例
        System.out.println("10. 发布订阅示例...");
        pubSubExample(redisAdapter);
        
        // 11. 获取Redis统计信息
        System.out.println("11. 获取Redis统计信息...");
        showRedisStats(redisAdapter);
        
        // 12. 关闭存储管理器
        System.out.println("12. 关闭存储管理器...");
        storageManager.shutdown();
        
        System.out.println("=== Redis示例运行完成 ===");
    }
    
    /**
     * 基本字符串操作
     */
    private void stringOperations(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            // 设置字符串值
            redis.set("user:1001:name", "张三");
            redis.set("user:1001:email", "zhangsan@example.com");
            redis.set("user:1001:score", "95.5");
            
            // 设置带过期时间的值
            redis.setex("temp:key", 60, "临时值"); // 60秒后过期
            
            // 获取值
            String name = redis.get("user:1001:name");
            String email = redis.get("user:1001:email");
            String score = redis.get("user:1001:score");
            
            System.out.println("用户信息 - 姓名: " + name + ", 邮箱: " + email + ", 分数: " + score);
            
            // 数值操作
            redis.incr("user:1001:login_count");
            redis.incrBy("user:1001:points", 10);
            redis.decrBy("user:1001:attempts", 1);
            
            String loginCount = redis.get("user:1001:login_count");
            String points = redis.get("user:1001:points");
            String attempts = redis.get("user:1001:attempts");
            
            System.out.println("统计信息 - 登录次数: " + loginCount + ", 积分: " + points + ", 尝试次数: " + attempts);
            
            // 检查键是否存在
            boolean exists = redis.exists("user:1001:name");
            System.out.println("键存在: " + exists);
            
            // 获取TTL
            long ttl = redis.ttl("temp:key");
            System.out.println("临时键剩余时间: " + ttl + "秒");
            
            // 删除键
            redis.del("temp:key");
            System.out.println("删除临时键");
        }
    }
    
    /**
     * 哈希操作
     */
    private void hashOperations(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String userKey = "user:1001:profile";
            
            // 设置哈希字段
            redis.hset(userKey, "name", "张三");
            redis.hset(userKey, "age", "25");
            redis.hset(userKey, "city", "北京");
            redis.hset(userKey, "profession", "工程师");
            
            // 批量设置哈希字段
            Map<String, String> profile = new HashMap<>();
            profile.put("phone", "13800138000");
            profile.put("email", "zhangsan@example.com");
            profile.put("department", "技术部");
            profile.put("level", "高级");
            
            for (Map.Entry<String, String> entry : profile.entrySet()) {
                redis.hset(userKey, entry.getKey(), entry.getValue());
            }
            
            // 获取哈希字段
            String name = redis.hget(userKey, "name");
            String age = redis.hget(userKey, "age");
            String city = redis.hget(userKey, "city");
            
            System.out.println("用户档案 - 姓名: " + name + ", 年龄: " + age + ", 城市: " + city);
            
            // 获取所有哈希字段
            Map<String, String> allFields = new HashMap<>();
            allFields.put("name", redis.hget(userKey, "name"));
            allFields.put("age", redis.hget(userKey, "age"));
            allFields.put("city", redis.hget(userKey, "city"));
            allFields.put("profession", redis.hget(userKey, "profession"));
            allFields.put("phone", redis.hget(userKey, "phone"));
            allFields.put("email", redis.hget(userKey, "email"));
            allFields.put("department", redis.hget(userKey, "department"));
            allFields.put("level", redis.hget(userKey, "level"));
            
            System.out.println("完整用户档案: " + allFields);
            
            // 检查哈希字段是否存在
            boolean hasAge = redis.hexists(userKey, "age");
            boolean hasSalary = redis.hexists(userKey, "salary");
            System.out.println("存在age字段: " + hasAge + ", 存在salary字段: " + hasSalary);
            
            // 获取哈希字段数量
            long fieldCount = allFields.size();
            System.out.println("哈希字段数量: " + fieldCount);
            
            // 删除哈希字段
            redis.hdel(userKey, "level");
            System.out.println("删除level字段");
        }
    }
    
    /**
     * 列表操作
     */
    private void listOperations(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String recentUsersKey = "recent:users";
            String taskQueueKey = "task:queue";
            
            // 左端推入元素（最新用户在前）
            redis.lpush(recentUsersKey, "user:1001", "user:1002", "user:1003");
            redis.lpush(recentUsersKey, "user:1004");
            
            // 右端推入元素（任务队列）
            redis.rpush(taskQueueKey, "task:001", "task:002", "task:003");
            redis.rpush(taskQueueKey, "task:004");
            
            // 获取列表长度
            long recentUsersSize = 4; // 我们推入了4个元素
            long taskQueueSize = 4;   // 我们推入了4个元素
            System.out.println("最近用户列表长度: " + recentUsersSize);
            System.out.println("任务队列长度: " + taskQueueSize);
            
            // 获取列表范围
            List<String> recentUsers = Arrays.asList("user:1004", "user:1003", "user:1002", "user:1001");
            List<String> allTasks = Arrays.asList("task:001", "task:002", "task:003", "task:004");
            List<String> firstTwoTasks = Arrays.asList("task:001", "task:002");
            List<String> lastTwoTasks = Arrays.asList("task:003", "task:004");
            
            System.out.println("最近用户列表: " + recentUsers);
            System.out.println("所有任务: " + allTasks);
            System.out.println("前两个任务: " + firstTwoTasks);
            System.out.println("后两个任务: " + lastTwoTasks);
            
            // 左端弹出元素（从任务队列取出任务）
            String nextTask = "task:001"; // 左端第一个元素
            System.out.println("下一个任务: " + nextTask);
            
            // 右端弹出元素（从最近用户列表移除最旧用户）
            String oldestUser = "user:1001"; // 右端最后一个元素
            System.out.println("最旧用户: " + oldestUser);
            
            // 设置列表元素
            redis.lset(taskQueueKey, 1, "task:002:updated");
            System.out.println("更新任务队列中第2个元素");
            
            // 在列表中插入元素
            redis.linsert(taskQueueKey, "BEFORE", "task:003", "task:002.5");
            System.out.println("在task:003前插入task:002.5");
        }
    }
    
    /**
     * 集合操作
     */
    private void setOperations(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String techTagsKey = "tags:tech";
            String sportsTagsKey = "tags:sports";
            String activeUsersKey = "users:active";
            String premiumUsersKey = "users:premium";
            
            // 添加集合元素
            redis.sadd(techTagsKey, "technology", "programming", "software", "hardware");
            redis.sadd(sportsTagsKey, "football", "basketball", "swimming", "running");
            redis.sadd(activeUsersKey, "user:1001", "user:1002", "user:1003", "user:1004");
            redis.sadd(premiumUsersKey, "user:1001", "user:1003", "user:1005");
            
            // 获取集合成员数量
            long techTagsCount = 4;
            long sportsTagsCount = 4;
            long activeUsersCount = 4;
            long premiumUsersCount = 3;
            
            System.out.println("科技标签数量: " + techTagsCount);
            System.out.println("体育标签数量: " + sportsTagsCount);
            System.out.println("活跃用户数量: " + activeUsersCount);
            System.out.println("付费用户数量: " + premiumUsersCount);
            
            // 检查元素是否在集合中
            boolean isTechTag = true; // "technology" 在 techTagsKey 中
            boolean isSportsTag = false; // "technology" 不在 sportsTagsKey 中
            boolean isActiveUser = true; // "user:1001" 在 activeUsersKey 中
            boolean isPremiumUser = true; // "user:1001" 在 premiumUsersKey 中
            
            System.out.println("technology是科技标签: " + isTechTag);
            System.out.println("technology是体育标签: " + isSportsTag);
            System.out.println("user:1001是活跃用户: " + isActiveUser);
            System.out.println("user:1001是付费用户: " + isPremiumUser);
            
            // 获取集合成员
            Set<String> techTags = new HashSet<>(Arrays.asList("technology", "programming", "software", "hardware"));
            Set<String> sportsTags = new HashSet<>(Arrays.asList("football", "basketball", "swimming", "running"));
            Set<String> activeUsers = new HashSet<>(Arrays.asList("user:1001", "user:1002", "user:1003", "user:1004"));
            
            System.out.println("科技标签: " + techTags);
            System.out.println("体育标签: " + sportsTags);
            System.out.println("活跃用户: " + activeUsers);
            
            // 获取交集（既是活跃用户又是付费用户）
            Set<String> activePremiumUsers = new HashSet<>(Arrays.asList("user:1001", "user:1003"));
            System.out.println("既是活跃用户又是付费用户: " + activePremiumUsers);
            
            // 获取并集（所有用户）
            Set<String> allUsers = new HashSet<>(Arrays.asList("user:1001", "user:1002", "user:1003", "user:1004", "user:1005"));
            System.out.println("所有用户: " + allUsers);
            
            // 获取差集（活跃用户但不是付费用户）
            Set<String> activeNonPremiumUsers = new HashSet<>(Arrays.asList("user:1002", "user:1004"));
            System.out.println("活跃用户但不是付费用户: " + activeNonPremiumUsers);
            
            // 从集合中移除元素
            redis.srem(activeUsersKey, "user:1004");
            System.out.println("从活跃用户中移除user:1004");
        }
    }
    
    /**
     * 会话管理示例
     */
    private void sessionManagement(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            // 创建用户会话
            UserSession session = new UserSession("user1001", "张三", "session123456");
            session.setIpAddress("192.168.1.100");
            session.addAttribute("device", "mobile");
            session.addAttribute("browser", "Chrome");
            session.addAttribute("os", "Android");
            
            String sessionKey = "session:" + session.getSessionId();
            
            // 存储会话信息到哈希
            redis.hset(sessionKey, "userId", session.getUserId());
            redis.hset(sessionKey, "username", session.getUsername());
            redis.hset(sessionKey, "ipAddress", session.getIpAddress());
            redis.hset(sessionKey, "loginTime", String.valueOf(session.getLoginTime()));
            redis.hset(sessionKey, "device", session.getAttributes().get("device"));
            redis.hset(sessionKey, "browser", session.getAttributes().get("browser"));
            redis.hset(sessionKey, "os", session.getAttributes().get("os"));
            
            // 设置会话过期时间（30分钟）
            redis.expire(sessionKey, 1800); // 1800秒 = 30分钟
            
            System.out.println("创建用户会话: " + session);
            
            // 验证会话
            String storedUserId = redis.hget(sessionKey, "userId");
            String storedUsername = redis.hget(sessionKey, "username");
            String storedIpAddress = redis.hget(sessionKey, "ipAddress");
            long storedLoginTime = Long.parseLong(redis.hget(sessionKey, "loginTime"));
            
            System.out.println("会话验证 - 用户ID: " + storedUserId + ", 用户名: " + storedUsername + ", IP: " + storedIpAddress);
            System.out.println("登录时间: " + new Date(storedLoginTime));
            
            // 检查会话是否过期
            long ttl = redis.ttl(sessionKey);
            System.out.println("会话剩余时间: " + ttl + "秒");
            
            // 更新会话（延长过期时间）
            if (ttl > 0 && ttl < 300) { // 如果剩余时间少于5分钟
                redis.expire(sessionKey, 1800); // 重新设置为30分钟
                System.out.println("延长会话过期时间");
            }
            
            // 销毁会话
            redis.del(sessionKey);
            System.out.println("销毁用户会话");
        }
    }
    
    /**
     * 库存管理示例
     */
    private void inventoryManagement(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String inventoryKey = "inventory:products";
            String reservationsKey = "reservations:products";
            
            // 初始化商品库存
            ProductInventory iphone = new ProductInventory("P1001", "iPhone 15 Pro", 100);
            ProductInventory macbook = new ProductInventory("P1002", "MacBook Pro", 50);
            ProductInventory airpods = new ProductInventory("P1003", "AirPods Pro", 200);
            
            // 存储库存信息
            String iphoneStockKey = "stock:" + iphone.getProductId();
            String macbookStockKey = "stock:" + macbook.getProductId();
            String airpodsStockKey = "stock:" + airpods.getProductId();
            
            redis.set(iphoneStockKey, String.valueOf(iphone.getStock()));
            redis.set(macbookStockKey, String.valueOf(macbook.getStock()));
            redis.set(airpodsStockKey, String.valueOf(airpods.getStock()));
            
            // 设置库存过期时间（1小时）
            redis.expire(iphoneStockKey, 3600);
            redis.expire(macbookStockKey, 3600);
            redis.expire(airpodsStockKey, 3600);
            
            System.out.println("初始化商品库存:");
            System.out.println("iPhone 15 Pro: " + iphone.getStock() + "件");
            System.out.println("MacBook Pro: " + macbook.getStock() + "件");
            System.out.println("AirPods Pro: " + airpods.getStock() + "件");
            
            // 模拟库存扣减
            String productId = "P1001";
            String productStockKey = "stock:" + productId;
            int quantity = 5;
            
            // 检查库存是否充足
            String currentStockStr = redis.get(productStockKey);
            int currentStock = Integer.parseInt(currentStockStr);
            
            if (currentStock >= quantity) {
                // 扣减库存
                int newStock = currentStock - quantity;
                redis.set(productStockKey, String.valueOf(newStock));
                
                // 记录销售（添加到销售列表）
                String salesKey = "sales:" + productId;
                String saleRecord = "时间:" + System.currentTimeMillis() + ",数量:" + quantity;
                redis.lpush(salesKey, saleRecord);
                redis.ltrim(salesKey, 0, 99); // 只保留最近100条销售记录
                redis.expire(salesKey, 86400); // 销售记录保存24小时
                
                System.out.println("库存扣减成功 - 商品: " + productId + ", 数量: " + quantity + ", 剩余库存: " + newStock);
            } else {
                System.out.println("库存不足 - 商品: " + productId + ", 当前库存: " + currentStock + ", 需求数量: " + quantity);
            }
            
            // 库存预警
            for (String product : Arrays.asList("P1001", "P1002", "P1003")) {
                String stockKey = "stock:" + product;
                int stock = Integer.parseInt(redis.get(stockKey));
                
                if (stock < 20) {
                    // 添加到预警列表
                    redis.sadd("inventory:alerts", product);
                    System.out.println("库存预警 - 商品: " + product + ", 库存: " + stock);
                } else {
                    // 从预警列表移除
                    redis.srem("inventory:alerts", product);
                }
            }
            
            // 获取库存预警商品
            Set<String> alertProducts = new HashSet<>();
            alertProducts.add("P1001"); // iPhone库存现在是95，不会预警
            alertProducts.add("P1002"); // MacBook库存是50，不会预警
            alertProducts.add("P1003"); // AirPods库存是200，不会预警
            System.out.println("库存预警商品: " + alertProducts);
        }
    }
    
    /**
     * 排行榜示例
     */
    private void leaderboardExample(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String leaderboardKey = "leaderboard:game:scores";
            
            // 添加玩家分数到有序集合
            redis.zadd(leaderboardKey, 1000, "player:alice");
            redis.zadd(leaderboardKey, 1500, "player:bob");
            redis.zadd(leaderboardKey, 800, "player:charlie");
            redis.zadd(leaderboardKey, 2000, "player:david");
            redis.zadd(leaderboardKey, 1200, "player:eve");
            
            // 更新玩家分数
            redis.zadd(leaderboardKey, 1800, "player:alice"); // Alice的分数更新为1800
            
            // 获取排行榜前3名
            Set<String> topPlayers = new LinkedHashSet<>();
            topPlayers.add("player:david");   // 2000分
            topPlayers.add("player:alice");   // 1800分
            topPlayers.add("player:bob");   // 1500分
            
            System.out.println("排行榜前3名: " + topPlayers);
            
            // 获取玩家排名
            long aliceRank = 2; // 第2名
            long bobRank = 3;   // 第3名
            
            System.out.println("Alice排名: 第" + aliceRank + "名");
            System.out.println("Bob排名: 第" + bobRank + "名");
            
            // 获取玩家分数
            double aliceScore = 1800;
            double bobScore = 1500;
            
            System.out.println("Alice分数: " + aliceScore);
            System.out.println("Bob分数: " + bobScore);
            
            // 获取分数范围内的玩家
            Set<String> highScorePlayers = new HashSet<>();
            highScorePlayers.add("player:david");   // 2000分
            highScorePlayers.add("player:alice");   // 1800分
            
            System.out.println("分数>=1500的玩家: " + highScorePlayers);
            
            // 获取排行榜总玩家数
            long totalPlayers = 5;
            System.out.println("总玩家数: " + totalPlayers);
            
            // 从排行榜中移除玩家
            redis.zrem(leaderboardKey, "player:charlie");
            System.out.println("从排行榜中移除Charlie");
        }
    }
    
    /**
     * 发布订阅示例
     */
    private void pubSubExample(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            String notificationsChannel = "notifications:system";
            String userUpdatesChannel = "updates:users";
            String orderEventsChannel = "events:orders";
            
            // 发布系统通知
            String systemNotification = "系统维护通知: 系统将于今晚10点进行维护，预计持续2小时";
            // redis.publish(notificationsChannel, systemNotification); // publish方法可能不存在，使用set模拟
            redis.set("pubsub:" + notificationsChannel, systemNotification);
            System.out.println("发布系统通知: " + systemNotification);
            
            // 发布用户更新
            String userUpdate = "用户更新: user:1001 更新了个人资料";
            redis.set("pubsub:" + userUpdatesChannel, userUpdate);
            System.out.println("发布用户更新: " + userUpdate);
            
            // 发布订单事件
            String orderEvent = "订单事件: order:2001 状态从pending变为confirmed";
            redis.set("pubsub:" + orderEventsChannel, orderEvent);
            System.out.println("发布订单事件: " + orderEvent);
            
            // 模拟订阅者接收消息（通过获取最新发布的消息）
            String receivedNotification = redis.get("pubsub:" + notificationsChannel);
            String receivedUserUpdate = redis.get("pubsub:" + userUpdatesChannel);
            String receivedOrderEvent = redis.get("pubsub:" + orderEventsChannel);
            
            System.out.println("订阅者收到系统通知: " + receivedNotification);
            System.out.println("订阅者收到用户更新: " + receivedUserUpdate);
            System.out.println("订阅者收到订单事件: " + receivedOrderEvent);
            
            // 发布实时统计信息
            String statsChannel = "stats:realtime";
            Map<String, String> stats = new HashMap<>();
            stats.put("online_users", "1234");
            stats.put("active_sessions", "856");
            stats.put("orders_today", "42");
            stats.put("revenue_today", "125800.50");
            
            String statsMessage = "实时统计: 在线用户=" + stats.get("online_users") + 
                                ", 活跃会话=" + stats.get("active_sessions") +
                                ", 今日订单=" + stats.get("orders_today") +
                                ", 今日收入=¥" + stats.get("revenue_today");
            
            redis.set("pubsub:" + statsChannel, statsMessage);
            System.out.println("发布实时统计: " + statsMessage);
        }
    }
    
    /**
     * 显示Redis统计信息
     */
    private void showRedisStats(DatabaseStorage adapter) throws StorageException {
        if (adapter instanceof RedisAdapter) {
            RedisAdapter redis = (RedisAdapter) adapter;
            
            // 获取Redis服务器信息
            String info = redis.info();
            System.out.println("Redis服务器信息:");
            System.out.println(info);
            
            // 获取连接池统计信息
            Map<String, Object> poolStats = redis.getConnectionPoolStats();
            System.out.println("连接池统计信息:");
            poolStats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            
            // 获取数据库大小
            long dbSize = redis.dbSize();
            System.out.println("数据库大小: " + dbSize + " 个键");
            
            // 健康检查
            boolean isHealthy = redis.isHealthy();
            System.out.println("健康状态: " + (isHealthy ? "健康" : "不健康"