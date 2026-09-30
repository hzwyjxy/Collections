package index;

import matrix.BaseParticleParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Index {
    public static Map<String, BaseParticleParser> indexMap = new ConcurrentHashMap<>();

    public static void register(BaseParticleParser baseParticleParser) {
        BaseParticleParser existing = indexMap.put(baseParticleParser.getCategory(), baseParticleParser);
        if (existing != null && existing != baseParticleParser) {
            System.out.println("[index] 覆盖已注册的解析器: " + baseParticleParser.getCategory());
        }
    }

    public static BaseParticleParser getIndexParser(String category) {
        return indexMap.get(category);
    }
}
