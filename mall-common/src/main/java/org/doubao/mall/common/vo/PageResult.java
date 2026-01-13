package org.doubao.mall.common.vo;


import java.io.Serializable;
import java.util.List;

/**
 * 分页结果通用VO
 */
public class PageResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private int page; // 当前页码
	private int size; // 每页条数
	private long total; // 总条数
	private List<T> list; // 数据列表

	public PageResult() {
	}

	public PageResult(int page, int size, long total, List<T> list) {
		this.page = page;
		this.size = size;
		this.total = total;
		this.list = list;
	}

	public static <T> PageResult<T> of(int page, int size, long total, List<T> list) {
		return new PageResult<>(page, size, total, list);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
}