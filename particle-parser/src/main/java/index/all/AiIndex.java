package index.all;

import index.Index;
import matrix.ai.AiPageParser;

/**
 * AI 解析器注册口。按 ai.properties 的 ai.category 注册到对应 Category。
 */
public class AiIndex extends Index {

    public AiIndex() {
        register(new AiPageParser());
    }
}