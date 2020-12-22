package com.bugcat.example.catclient.token;

import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.utils.CatToosUtil;
import com.bugcat.example.dto.ResponseEntity;

public class TokenSendProcessor extends SendProcessor {

    
    
    @Override
    public void setSendVariable(CatParameter param) {
        
        super.setSendVariable(param);


        //使用note，标记是否需要添加签名
        String need = (String) notes.getOrDefault("needToken", "true");
        
        if("true".equals(need)){
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
        
        private TokenRemote tokenRemote = CatToosUtil.getBean(TokenRemote.class);
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
