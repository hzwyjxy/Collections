package matrix.ai;

/**
 * 大模型客户端工厂：按配置实例化 {@link LargeModelClient} 实现类。
 * <p>
 * 约定：实现类无参构造，或提供 {@code (AiConfig)} 单参构造以读取连接参数。
 * 未配置实现类或实例化失败时，回退到 {@link NoopLargeModelClient}。
 */
public class AiClientFactory {

    private AiClientFactory() {
    }

    public static LargeModelClient create(AiConfig config) {
        if (config == null) {
            return new NoopLargeModelClient();
        }
        String impl = config.getImpl();
        if (impl == null || impl.trim().isEmpty()) {
            return new NoopLargeModelClient();
        }
        try {
            Class<?> clazz = Class.forName(impl);
            if (!LargeModelClient.class.isAssignableFrom(clazz)) {
                System.out.println("[ai] " + impl + " 未实现 LargeModelClient 接口，回退空实现");
                return new NoopLargeModelClient();
            }
            try {
                return (LargeModelClient) clazz.getConstructor(AiConfig.class).newInstance(config);
            } catch (NoSuchMethodException noConfigCtor) {
                return (LargeModelClient) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            System.out.println("[ai] 实例化大模型实现失败 " + impl + ": " + e.getMessage() + "，回退空实现");
            return new NoopLargeModelClient();
        }
    }
}