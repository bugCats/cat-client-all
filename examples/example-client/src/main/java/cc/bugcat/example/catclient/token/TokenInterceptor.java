package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.example.tools.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class TokenInterceptor implements CatMethodSendInterceptor {


    /**
     * 使用拦截器修改参数
     * */
    @Override
    public void executeVariable(CatSendContextHolder context, CatSendProcessor sendHandler) {

        JSONObject notes = sendHandler.getNotes();
        CatHttpPoint httpPoint = sendHandler.getHttpPoint();

        //使用note，标记是否需要添加签名
        String need = notes.getString("needToken");

        if( CatToosUtil.isNotBlank(need)){
            String token = TokenInfo.getToken();
            httpPoint.getHeaderMap().put("token", token);

            System.out.println(token);
        }

        sendHandler.postVariableResolver(context);
        sendHandler.afterVariableResolver(context);
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

        /**
         * 使用继承CatSendProcessor形式修改参数
         * */
        @Override
        public void postVariableResolver(CatSendContextHolder context){
            String pwd = notes.getString("pwd");
            String username = notes.getString("username");

            MultiValueMap<String, Object> keyValueParam = this.getHttpPoint().getKeyValueParam();
            keyValueParam.add("username", username);
            keyValueParam.add("pwd", pwd);
        }
    }
}
