package matrix.dmhy;

import index.Category;
import matrix.BaseParticleParser;

public class DmhyListParser extends BaseParticleParser {
    @Override
    public String getCategory() {
        return Category.DMHY_LIST_PAGE;
    }

    @Override
    public boolean checkSuccess() {
        return true;
    }

    @Override
    public void process() {
        System.out.println("达到dmhy");
    }
}
