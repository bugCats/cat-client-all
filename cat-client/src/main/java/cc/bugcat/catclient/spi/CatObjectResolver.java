package cc.bugcat.catclient.spi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * 如果使用键值对方式发送复杂对象
 * 将复杂对象转换成form表单模式
 * 不建议使用，可以采用post发送json字符串模式
 *
 * @see RequestMethod#POST
 * @see RequestBody
 *
 * @author bugcat
 * */
public interface CatObjectResolver {


    MultiValueMap<String, Object> resolver(Object requestModel);




    /**
     * 默认使用fastjson工具类
     * @deprecated 不建议使用，可以采用post发送json字符串模式
     * */
    public static class DefaultResolver implements CatObjectResolver {

        /**
         * 复杂对象，转form表单形式
         * */
        @Override
        public MultiValueMap<String, Object> resolver(Object requestModel) {
            if( requestModel == null ){
                return new LinkedMultiValueMap<>();
            }
            Object value = JSON.toJSON(requestModel);
            MultiValueMap<String, Object> result = transform(value);
            return result;
        }



        /**
         * 复杂对象，转form表单形式
         * */
        protected MultiValueMap<String, Object> transform(Object value){
            MultiValueMap<String, Object> result = new LinkedMultiValueMap<>();
            for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
                transform(result, entry.getKey(), entry.getValue());
            }
            return result;
        }


        protected void transform(MultiValueMap<String, Object> result, String parName, Object value){
            if( value == null ) {
                result.add(parName, null);
                return;
            }
            if( value instanceof JSONObject ){
                for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
                    transform(result, parName + "." + entry.getKey(), entry.getValue());
                }
            } else if ( value instanceof JSONArray ){
                int count = 0;
                for(Object entry : (List<Object>) value){
                    transform(result, parName + "[" + (count ++) + "]", entry);
                }
            } else {
                result.add(parName, value == null ? null : value.toString());
            }
        }

    }

}
