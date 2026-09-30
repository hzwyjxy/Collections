package matrix.ai;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * AI 解析配置。
 * <p>
 * 默认从 classpath 读取 {@code ai.properties}，可用 {@code -Dai.config=/path/to/ai.properties} 覆盖。
 * 连接参数（provider/base_url/api_key/model/...）由框架透传给 {@link LargeModelClient} 实现类读取，
 * 框架自身不直接使用。
 */
public class AiConfig {

    public static final String DEFAULT_RESOURCE = "ai.properties";

    private boolean enabled = true;
    private String provider = "";
    private String impl = "";
    private String baseUrl = "";
    private String apiKey = "";
    private String model = "";
    private double temperature = 0.2;
    private int maxTokens = 2048;
    private long timeoutMs = 30000L;
    private int maxInputChars = 12000;
    private String category = "AI_PAGE_STRUCT";
    private String systemPrompt = "";
    private String userPrompt = "${content}";

    /**
     * 加载配置：优先系统属性 {@code -Dai.config} 指定的文件，其次 classpath 下的 ai.properties。
     */
    public static AiConfig load() {
        String path = System.getProperty("ai.config");
        Properties props = new Properties();
        InputStream in = null;
        try {
            if (path != null && !path.trim().isEmpty()) {
                in = new FileInputStream(path);
            } else {
                in = AiConfig.class.getClassLoader().getResourceAsStream(DEFAULT_RESOURCE);
            }
            if (in == null) {
                System.out.println("[ai] 未找到配置文件 " + DEFAULT_RESOURCE + "，使用默认配置（不调用大模型）");
            } else {
                // 配置文件含中文，按 UTF-8 读取（Properties.load(InputStream) 默认 ISO-8859-1）
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println("[ai] 加载配置失败: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
        return fromProperties(props);
    }

    public static AiConfig fromProperties(Properties props) {
        AiConfig config = new AiConfig();
        if (props == null) {
            return config;
        }
        config.enabled = Boolean.parseBoolean(props.getProperty("ai.enabled", "true"));
        config.provider = props.getProperty("ai.provider", "").trim();
        config.impl = props.getProperty("ai.impl", "").trim();
        config.baseUrl = props.getProperty("ai.base_url", "").trim();
        config.apiKey = props.getProperty("ai.api_key", "").trim();
        config.model = props.getProperty("ai.model", "").trim();
        config.temperature = parseDouble(props, "ai.temperature", 0.2);
        config.maxTokens = parseInt(props, "ai.max_tokens", 2048);
        config.timeoutMs = parseLong(props, "ai.timeout_ms", 30000L);
        config.maxInputChars = parseInt(props, "ai.max_input_chars", 12000);
        config.category = props.getProperty("ai.category", "AI_PAGE_STRUCT").trim();
        config.systemPrompt = props.getProperty("ai.prompt.system", "").trim();
        config.userPrompt = props.getProperty("ai.prompt.user", "${content}");
        return config;
    }

    private static int parseInt(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            System.out.println("[ai] 配置项 " + key + " 非法，使用默认值 " + defaultValue);
            return defaultValue;
        }
    }

    private static long parseLong(Properties props, String key, long defaultValue) {
        try {
            return Long.parseLong(props.getProperty(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            System.out.println("[ai] 配置项 " + key + " 非法，使用默认值 " + defaultValue);
            return defaultValue;
        }
    }

    private static double parseDouble(Properties props, String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            System.out.println("[ai] 配置项 " + key + " 非法，使用默认值 " + defaultValue);
            return defaultValue;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getProvider() {
        return provider;
    }

    public String getImpl() {
        return impl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public int getMaxInputChars() {
        return maxInputChars;
    }

    public String getCategory() {
        return category;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }
}