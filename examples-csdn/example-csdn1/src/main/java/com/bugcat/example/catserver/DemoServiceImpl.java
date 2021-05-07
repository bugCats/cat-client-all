package com.bugcat.example.catserver;

import com.bugcat.example.dto.Demo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class DemoServiceImpl implements IDemoService {

    @Override
    public Demo demo1(Demo req) {
        return null;
    }

    @Override
    public List<Demo> demo3(Demo req) {
        return null;
    }

    @Override
    public ResponseEntity<Demo> demo4(String name, String mark) {
        return null;
    }

    @Override
    public Void demo6(Long userId, String name) {
        return null;
    }

    @Override
    public Demo demo7(String token) {
        return null;
    }
}
