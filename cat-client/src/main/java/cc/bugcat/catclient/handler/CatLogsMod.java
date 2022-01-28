package cc.bugcat.catclient.handler;


/**
 * api记录日志方案
 * @author bugcat
 * */
public enum CatLogsMod {

    Def,    //默认，跟随全局配置

    In,     //仅输入

    Out,    //仅输出

    All,    //输入、输出

    None,   //不记录


    In2,     //如果出现异常，仅输入

    Out2,    //如果出现异常，仅输出

    All2,    //如果出现异常，输入、输出

}

