package cc.bugcat.catclient.beanInfos;


import cc.bugcat.catface.utils.CatToosUtil;

/**
 * 解析方法上的入参信息
 *
 * @author bugcat
 * */
public class CatMethodParamInfo {

    /**
     * 参数列表的索引值
     * */
    private final int index;

    /**
     * 是否为String、基本数据类型、包装类
     * */
    private final boolean simple;

    /**
     * 是否为主要参数？只能容许有一个被@RequestBody、@ModelAttribute 标记的入参
     * */
    private final boolean primary;


    private CatMethodParamInfo(Builder builder) {
        this.index = builder.index;
        this.simple = builder.simple;
        this.primary = builder.primary;
    }


    public int getIndex() {
        return index;
    }
    public boolean isSimple() {
        return simple;
    }
    public boolean isPrimary() {
        return primary;
    }



    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private int index;
        private boolean simple;
        private boolean primary;

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder parameterType(Class parameterType) {
            this.simple = CatToosUtil.isSimpleClass(parameterType);
            return this;
        }

        public Builder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public CatMethodParamInfo build(){
            return new CatMethodParamInfo(this);
        }
    }


}
