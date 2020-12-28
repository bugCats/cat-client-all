package com.bugcat.example.api;


import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vi.UserSaveVi;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


public interface UserService2 {
    
    ResponseEntity<PageInfo<UserInfo>> userPage(@ModelAttribute UserPageVi vi);
    
    UserInfo userInfo(@PathVariable("uid") @RequestBody @RequestParam("status") String uid);

    ResponseEntity<Void> userSave(@RequestBody UserSaveVi vi);

    Void status(@RequestParam("uid") String userId, @RequestParam("status") String status);

}
