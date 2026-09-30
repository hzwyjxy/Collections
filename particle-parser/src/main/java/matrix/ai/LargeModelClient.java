package matrix.ai;

import org.json.JSONObject;

/**
 * 大模型客户端接口。
 * <p>
 * particle-parser 只依赖此接口做“页面解读 + 结构化”，具体走哪家大模型由实现类决定，
 * 实现类全限定名通过配置文件 ai.properties 的 {@code ai.impl} 指定。
 * 框架本身不直接调用任何模型服务。
 */
public interface LargeModelClient {

    /**
     * 是否已配置可用（未配置的实现应返回 false，解析器据此跳过）。
     */
    boolean isAvailable();

    /**
     * 文本补全：返回模型原始输出文本。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词（通常含页面内容）
     * @return 模型输出文本，失败返回 null
     */
    String complete(String systemPrompt, String userPrompt);

    /**
     * 解读并结构化：调用 {@link #complete} 后从输出中提取 JSON。
     * 实现类一般无需重写，只要按提示词约定输出 JSON 即可。
     *
     * @return 结构化结果，无法解析时返回 null
     */
    default JSONObject interpretJson(String systemPrompt, String userPrompt) {
        return extractJson(complete(systemPrompt, userPrompt));
    }

    /**
     * 从模型输出里提取 JSON 对象：容忍 ```json 代码围栏与前后多余说明。
     */
    static JSONObject extractJson(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            int fence = text.lastIndexOf("```");
            if (fence >= 0) {
                text = text.substring(0, fence);
            }
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            return new JSONObject(text.substring(start, end + 1));
        } catch (Exception e) {
            return null;
        }
    }
}