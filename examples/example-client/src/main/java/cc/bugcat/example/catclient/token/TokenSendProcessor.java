package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.example.tools.ResponseEntity;

import java.util.Map;

public class TokenSendProcessor extends CatSendProcessor {


    @Override
    public void afterVariableResolver(CatSendContextHolder context, CatHttpPoint httpPoint){
        //使用note，标记是否需要添加签名
        String need = notes.getString("needToken");

        if( CatToosUtil.isNotBlank(need)){
            String token = TokenInfo.getToken();
            httpPoint.getHeaderMap().put("token", token);

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

    private static class TokenSend extends CatSendProcessor {

        @Override
        public void afterVariableResolver(CatSendContextHolder context, CatHttpPoint httpPoint){
            String pwd = notes.getString("pwd");
            String username = notes.getString("username");

            Map<String, Object> keyValueParam = httpPoint.getKeyValueParam();
            keyValueParam.put("username", username);
            keyValueParam.put("pwd", pwd);
        }
    }
}
