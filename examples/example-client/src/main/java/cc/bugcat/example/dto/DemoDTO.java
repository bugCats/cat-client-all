package cc.bugcat.example.dto;


import com.alibaba.fastjson.annotation.JSONField;

/**
 *
 * @author bugcat
 */
public class DemoDTO extends Demo {
	

	/**
	 * 禁止序列化成json
	 * */
	@JSONField(serialize = false)   //fastjson 忽略转换
	private String userkey;			//密钥


	public String getUserkey() {
		return userkey;
	}

	public void setUserkey(String userkey) {
		this.userkey = userkey;
	}
}