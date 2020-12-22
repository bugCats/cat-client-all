package com.bugcat.example.catclient.server;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 模拟远程服务端API接口
 * 使用单元测试类
 * */
@Api(tags = "服务端API - 签名")
@RestController
public class SignServerController {

    
    /**
     * 
     * 参数名字典排序，参数值为空按空字符串处理
     * 
     * */

    @PostMapping(value = "/server/sign1")
    public ResponseEntity<String> demo11(HttpServletRequest request){

        TreeMap<String, String> treeMap = new TreeMap<>();

        request.getParameterMap().forEach((key, values) -> {
            treeMap.put(key, values != null && values.length > 0 ? values[0] : "");
        });
        
        System.out.println("sign1 >>> req: " + JSONObject.toJSONString(treeMap));
        
        return ResponseEntity.ok("");
    }
    
}
