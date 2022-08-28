package index;

import matrix.BaseParticleParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Index {
    public static Map<String, BaseParticleParser> indexMap = new ConcurrentHashMap<>();

    public static void register(BaseParticleParser baseParticleParser) {
        indexMap.put(baseParticleParser.getCategory(), baseParticleParser);
    }

    public static BaseParticleParser getIndexParser(String category) {
        return indexMap.get(category);
    }
}
