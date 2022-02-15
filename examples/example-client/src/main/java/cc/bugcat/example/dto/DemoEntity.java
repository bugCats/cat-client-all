package cc.bugcat.example.dto;

import java.util.Arrays;
import java.util.List;

/**
 * 复杂对象，使用form表单提交方式
 * @author bugcat
 * */
public class DemoEntity {


	private Demo data;

	private String str;

	private List<Integer> ints;

	private List<Demo> list;

	public DemoEntity() {

	}


	public DemoEntity(Demo data) {
		this.str = "demo";
		this.ints = Arrays.asList(1,2,3);
		this.data = data;
		this.list = Arrays.asList(data);
	}



	public List<Integer> getInts() {
		return ints;
	}
	public void setInts(List<Integer> ints) {
		this.ints = ints;
	}

	public Demo getData() {
		return data;
	}
	public void setData(Demo data) {
		this.data = data;
	}

	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}

	public List<Demo> getList() {
		return list;
	}
	public void setList(List<Demo> list) {
		this.list = list;
	}

}
