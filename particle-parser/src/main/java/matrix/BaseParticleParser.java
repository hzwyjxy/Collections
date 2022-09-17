package matrix;

import model.AbstractResponse;

public abstract class BaseParticleParser {

    public abstract String getCategory();

    public abstract boolean checkSuccess(AbstractResponse response);

    public abstract void process(AbstractResponse response);

}
