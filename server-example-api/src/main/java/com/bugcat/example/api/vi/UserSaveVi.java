package com.bugcat.example.api.vi;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class UserSaveVi {

    @ApiModelProperty("用户id，不为空时，表示更新")
    private String uid;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("email")
    private String email;

    @ApiModelProperty("描述")
    private String remark;
    
    
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
}
