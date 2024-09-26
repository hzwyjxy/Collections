package index.all;

import index.Index;
import matrix.Election.*;

public class ElectionIndex extends Index {

    //index注册口
    public ElectionIndex() {
        register(new CnnSearchParser());
        register(new CnnDetailParser());
        register(new BBCListParser());
        register(new BBCDetailParser());
        register(new GuardianListParser());
        register(new GuardianDetailParser());
        register(new NytimesSearchParser());
        register(new NytimesDetailParser());
        register(new ApSearchParser());
        register(new ApDetailParser());
        register(new LaSearchParser());
        register(new LaDetailParser());
        register(new ReutersSearchParser());
        register(new ReutersDetailParser());
        register(new WpSearchParser());
        register(new WpDetailParser());
        register(new HuffPostSearchParser());
        register(new HuffPostDetailParser());
    }
}
