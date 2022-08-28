package index.all;

import index.Index;
import matrix.dmhy.DmhyListParser;

public class DmhyIndex extends Index {

    //index注册口
    public DmhyIndex() {
        register(new DmhyListParser());
    }
}
