package com.bugcat.example.catserver;

import com.bugcat.example.annotation.ApiCtrl;
import com.bugcat.example.dto.Demo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiCtrl
public interface IDemoService{

    @RequestMapping(value = "/server/demo41", method = RequestMethod.POST)
    Demo demo1(@RequestBody Demo req);

    @RequestMapping(value = "/server/demo43", method = RequestMethod.GET)
    List<Demo> demo3(@ModelAttribute Demo req);

    @RequestMapping(value = "/server/demo44", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("userName") String name, @RequestParam("userMark") String mark);

    @RequestMapping(value = "/server/demo46/{uid}", method = RequestMethod.GET)
    Void demo6(@PathVariable("uid") Long userId, @RequestParam("userName") String name);

    @RequestMapping(value = "/server/demo47", method = RequestMethod.GET)
    Demo demo7(@RequestHeader("token") String token);

}
