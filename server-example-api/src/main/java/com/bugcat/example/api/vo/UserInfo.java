package com.bugcat.example.api.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class UserInfo {

    @ApiModelProperty("用户id")
    private String uid;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("email")
    private String email;

    
    @ApiModelProperty("描述")
    private String remark;


    @ApiModelProperty("用户状态")
    private String status;
    
    
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
