package matrix.dmhy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * dmhy 磁力链接存储（SQLite）。
 * 以 infohash 为主键去重，重复抓取同一资源不会产生重复记录。
 * 单连接 + 方法同步，供多线程解析器安全写入。
 */
public class MagnetStore {

    private static final String DB_PATH_PROPERTY = "dmhy.db.path";
    private static final String DEFAULT_DB_PATH = "dmhy.db";

    private static MagnetStore instance;

    private final Connection connection;

    private MagnetStore(String dbPath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("CREATE TABLE IF NOT EXISTS dmhy_magnet ("
                    + "infohash  TEXT PRIMARY KEY,"
                    + "title     TEXT,"
                    + "magnet    TEXT,"
                    + "topic_url TEXT,"
                    + "category  TEXT,"
                    + "size      TEXT,"
                    + "pub_date  TEXT,"
                    + "created_at INTEGER"
                    + ")");
        }
    }

    /**
     * 获取单例，数据库路径可用 -Ddmhy.db.path=/path/to/dmhy.db 指定
     */
    public static synchronized MagnetStore getInstance() {
        if (instance == null) {
            String dbPath = System.getProperty(DB_PATH_PROPERTY, DEFAULT_DB_PATH);
            try {
                instance = new MagnetStore(dbPath);
            } catch (SQLException e) {
                throw new RuntimeException("初始化 SQLite 失败: " + dbPath, e);
            }
        }
        return instance;
    }

    /**
     * 保存一条磁力记录
     *
     * @return true 表示新插入，false 表示 infohash 已存在被忽略
     */
    public synchronized boolean save(String infohash, String title, String magnet,
                                     String topicUrl, String category, String size, String pubDate) {
        String sql = "INSERT OR IGNORE INTO dmhy_magnet "
                + "(infohash, title, magnet, topic_url, category, size, pub_date, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, infohash);
            ps.setString(2, title);
            ps.setString(3, magnet);
            ps.setString(4, topicUrl);
            ps.setString(5, category);
            ps.setString(6, size);
            ps.setString(7, pubDate);
            ps.setLong(8, System.currentTimeMillis());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 当前记录总数
     */
    public synchronized long count() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dmhy_magnet")) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}