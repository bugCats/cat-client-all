package com.bugcat.example.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author bugcat
 */
@ApiModel
public class Demo {
	
	@ApiModelProperty("主键")
	private Long id;  // 主键

	@ApiModelProperty("姓名")
	private String name;  // 姓名

	@ApiModelProperty("")
	private String password;  // 密码。32位小写MD5

	@ApiModelProperty("备注")
	private String mark;  // 备注

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
	
	


}