package com.bugcat.example.catclient.token;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.example.tools.ResponseEntity;

public class TokenSendProcessor extends SendProcessor {

    
    
    @Override
    public void setSendVariable(CatParameter param) {
        
        super.setSendVariable(param);


        //使用note，标记是否需要添加签名
        String need = notes.getString("needToken");
        
        if( CatToosUtil.isNotBlank(need)){
            String token = TokenInfo.getToken();
            headerMap.put("token", token);
            
            System.out.println(token);
        }
    }
    
    
    private static class TokenInfo {

        private static TokenInfo info = new TokenInfo();
        
        public static String getToken(){
            return info.getToken(System.currentTimeMillis());
        }
        
        private TokenRemote tokenRemote = CatClientUtil.getBean(TokenRemote.class);
        private long keepTime;
        private String value;
        
        private String getToken(long now){
            if( now > keepTime ){
                TokenSend sender = new TokenSend();
                ResponseEntity<String> bean = tokenRemote.getToken(sender, "", "");
                keepTime = System.currentTimeMillis() + 3600;
                value = bean.getData();
                return value;
            } else {
                return value;
            }
        }
    }

    private static class TokenSend extends SendProcessor {
        
        @Override
        public void setSendVariable(CatParameter param) {
            super.setSendVariable(param);
            String pwd = notes.getString("pwd");
            String username = notes.getString("username");
            keyValueParam.put("username", username);
            keyValueParam.put("pwd", pwd);
        }
    }
}
