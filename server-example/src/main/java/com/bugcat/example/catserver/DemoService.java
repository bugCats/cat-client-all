package com.bugcat.example.catserver;

import com.bugcat.example.api.UserService;
import com.bugcat.example.dto.Demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * 一个普通的服务类
 * @author bugcat
 * 
 * */
@Service
public class DemoService {

    /**
     * 也可以作为普通组件自动注入
     * */
    @Autowired
    private UserService userService;
    
    
    public Demo creart(){
        Demo demo = new Demo();
        demo.setId(2L);
        demo.setName("bugcat");
        demo.setMark("服务端");
        return demo;
    }

    
}
