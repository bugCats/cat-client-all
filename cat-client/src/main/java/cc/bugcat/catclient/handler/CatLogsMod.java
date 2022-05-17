package cc.bugcat.catclient.handler;


/**
 * api记录日志方案
 *
 * @author bugcat
 * */
public enum CatLogsMod {

    Off(false, false),    //关闭

    Def(true, true),    //默认，跟随全局配置

    In(true, false),     //仅输入

    Out(false, true),    //仅输出

    All(true, true),    //输入、输出

    None(false, true),   //不记录输入、输出


    In2(true, false),     //如果出现异常，仅输入

    Out2(false, true),    //如果出现异常，仅输出

    All2(true, true);    //如果出现异常，输入、输出

    
    public final boolean printIn;
    public final boolean printOut;

    CatLogsMod(boolean printIn, boolean printOut) {
        this.printIn = printIn;
        this.printOut = printOut;
    }
}

