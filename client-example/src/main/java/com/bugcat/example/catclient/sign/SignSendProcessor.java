package com.bugcat.example.catclient.sign;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catface.utils.CatToosUtil;

import java.util.Map;
import java.util.TreeMap;

public class SignSendProcessor extends SendProcessor {

    private String mk;
    
    @Override
    public void setSendVariable(CatParameter param) {
        
        super.setSendVariable(param);

        //使用note，标记是否需要添加签名
        String need = notes.getString("needSign");
        if( CatToosUtil.isNotBlank(need) && keyValueParam != null && keyValueParam.size() > 0 ){

            TreeMap<String, Object> treeMap = new TreeMap<>(keyValueParam);
            
            StringBuffer sbr = new StringBuffer();
            for( Map.Entry<String, Object> entry : treeMap.entrySet()  ){
                String key = entry.getKey();
                String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                sbr.append(key + "=" + value + "&");
            }
            
            // 密钥
            String apikey = notes.getString("apikey");
            
            String md5 = sbr.append("[" + apikey + "]").toString(); //没有引入加密工具类，假设已经加密了
            treeMap.put("sign", md5);

            keyValueParam = treeMap;
            System.out.println(md5);
            
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
