package com.bugcat.example.tools;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 响应对象包裹类
 * @author bugcat
 * */
@ApiModel
public class ResponseEntity<T> implements Serializable {

	private static final long serialVersionUID = 161651514L;

	public static final String succ = "10000";
	
	@ApiModelProperty("业务参数")
	private T data;

	@ApiModelProperty("错误代码")
	private String errCode;

	@ApiModelProperty("错误说明")
	private String errMsg;




    /**
     * ajax 请求
     */
    public static <T> ResponseEntity<T> ok(T data) {
        ResponseEntity<T> entity = new ResponseEntity<T>();
        entity.data = data;
        entity.errCode = succ;
        entity.errMsg = "success";
        return entity;
    }


	public static ResponseEntity fail(String errCode, String errMsg) {
		ResponseEntity entity = new ResponseEntity();
		entity.errCode = errCode;
		entity.errMsg = errMsg;
		return entity;
	}

	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

}
