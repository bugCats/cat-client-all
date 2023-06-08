package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;

public class SignSendProcessor extends CatSendProcessor {

    private String mk;

    @Override
    public void postVariableResolver(CatClientContextHolder context){

        CatHttpPoint httpPoint = super.getHttpPoint();

        //使用note，标记是否需要添加签名
        String need = notes.getString("needSign");
        if( CatToosUtil.isNotBlank(need) ){
            /**
             * 密钥
             * demo11 从环境配置中获取
             * demo12 从入参中获取
             * demo13 从入参中获取
             * */
            String spring = notes.getString("spring"); //
            System.out.println("SignSendProcessor.spring=>" + spring);

            String spEL = notes.getString("spEL"); //
            System.out.println("SignSendProcessor.spEL=>" + spEL);
            
            // 还可以使用 ThreadLocal、或者SendProcessor本身 传递密钥
        }
    }


    public String getMk() {
        return mk;
    }
    public void setMk(String mk) {
        this.mk = mk;
    }
}
