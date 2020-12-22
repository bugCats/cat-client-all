package com.bugcat.example.catclient.remote;

import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;

/**
 * 呆毛2，异常plus版
 * 
 * @author: bugcat
 * */
public class ApiRemote2Error implements ApiRemote2 {

    /**
     * 将req序列化成json，再使用post发送json字符串。@RequestBody 不能少
     *
     * @param req 入参
     */
    @Override
    public ResponseEntity<Demo> demo1(Demo req) {
        return ResponseEntity.fail("demo1", "demo1 异常哦");
    }

    /**
     * 仅将req转换成键值对，再使用post发送键值对。
     *
     * @param send 请求协助类，必须是SendProcessor的子类；可无、位置可任意
     * @param req   对象的属性，不能有Map
     */
    @Override
    public String demo2(SendProcessor send, Demo req) {
        return "demo2 异常";
    }
    

    /**
     * 模拟发生http异常
     */
    @Override
    public ResponseEntity<String> demo9(PageInfo<Demo> pageInfo) {
        return ResponseEntity.fail("demo9", "这是真的异常了");
    }
}
