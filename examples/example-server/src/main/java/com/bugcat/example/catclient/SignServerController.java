package com.bugcat.example.catclient;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.TreeMap;


/**
 * 供 example-server 模块调用
 * */
@Api(tags = "服务端API - 签名")
@RestController
public class SignServerController {

    
    /**
     * 
     * 参数名字典排序，参数值为空按空字符串处理
     * 
     * */

    @PostMapping(value = "/cat/sign1")
    public ResponseEntity<String> demo11(HttpServletRequest request){

        TreeMap<String, String> treeMap = new TreeMap<>();

        request.getParameterMap().forEach((key, values) -> {
            treeMap.put(key, values != null && values.length > 0 ? values[0] : "");
        });
        
        System.out.println("sign1 >>> req: " + JSONObject.toJSONString(treeMap));
        
        return ResponseEntity.ok("");
    }
    
}
