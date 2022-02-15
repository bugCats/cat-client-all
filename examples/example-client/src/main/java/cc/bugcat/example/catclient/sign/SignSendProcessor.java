package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Set;
import java.util.TreeSet;

public class SignSendProcessor extends CatSendProcessor {

    private String mk;

    @Override
    public void postVariableResolver(CatSendContextHolder context){

        CatHttpPoint httpPoint = super.getHttpPoint();

        //使用note，标记是否需要添加签名
        String need = notes.getString("needSign");

        MultiValueMap<String, Object> keyValueParam = httpPoint.getKeyValueParam();
        if( CatToosUtil.isNotBlank(need) && keyValueParam != null && keyValueParam.size() > 0 ){

            Set<String> keys = new TreeSet<>(keyValueParam.keySet());
            MultiValueMap<String, Object> treeMap = new LinkedMultiValueMap<>();

            StringBuffer sbr = new StringBuffer();
            keys.forEach(key -> {
                Object value = keyValueParam.getFirst(key);
                treeMap.add(key, value);
                sbr.append("&" + key + "=" + ( value != null ? value : "" ));
            });

            /**
             * 密钥
             * demo11 从环境配置中获取
             * demo12 从入参中获取
             * */
            String apikey = notes.getString("apikey"); //

            String md5 = "@md5{" + apikey + "#" + sbr.deleteCharAt(0).toString() + "}"; //没有引入加密工具类，假设已经加密了
            treeMap.add("sign", md5);

            System.out.println("sign=>" + md5);

            httpPoint.setKeyValueParam(treeMap);

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
