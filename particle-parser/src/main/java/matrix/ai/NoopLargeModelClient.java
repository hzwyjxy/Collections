package matrix.ai;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认空实现：不调用任何大模型。
 * 未在配置文件中指定 {@code ai.impl} 时使用，保证模块可独立编译运行。
 */
public class NoopLargeModelClient implements LargeModelClient {

    private static final AtomicBoolean WARNED = new AtomicBoolean(false);

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (WARNED.compareAndSet(false, true)) {
            System.out.println("[ai] 未配置大模型实现（ai.impl 为空），跳过 AI 解析");
        }
        return null;
    }
}