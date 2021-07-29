package cc.bugcat.example.api.vi;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@ApiModel
public class UserPageVi {

    @ApiModelProperty("查询页数")
    private int pageNum = 1;

    @ApiModelProperty("每页条数")
    private int count = 15;
    

    @ApiModelProperty("用户id")
    private String uid;

    @NotBlank(message = "姓名不能为空")
    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("email")
    private String email; 
    
    
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
