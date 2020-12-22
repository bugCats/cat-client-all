package com.bugcat.example.dto;

import java.util.List;


/**
 * 分页对象
 * @author bugcat
 */
public class PageInfo<T> {
	/**
	 * 总记录数
	 */
	private long total;
	
	/**
	 * 起始条数
	 */
	private int pageNum;

	/**
	 * 每页显示的数量
	 */
	private int count;

	/**
	 * 当前结果集合
	 */
	private List<T> list;

	/**
	 * 总页数
	 */
	private Integer pages;

	
	public PageInfo () {
		super();
	}
	
	
	public PageInfo (int pageNum, int count, int totel) {
        this.pageNum = pageNum;
        this.count = count;
        this.total = count;
    }


    public long getTotal () {
        return total;
    }

    public void setTotal (long total) {
        this.total = total;
    }

    public int getPageNum () {
        return pageNum;
    }

    public void setPageNum (int pageNum) {
        this.pageNum = pageNum;
    }

    public int getCount () {
        return count;
    }

    public void setCount (int count) {
        this.count = count;
    }

    public List<T> getList () {
        return list;
    }

    public void setList (List<T> list) {
        this.list = list;
    }

    public Integer getPages () {
        return pages;
    }

    public void setPages (Integer pages) {
        this.pages = pages;
    }
}
