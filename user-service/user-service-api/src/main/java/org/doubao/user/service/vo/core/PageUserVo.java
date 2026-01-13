package org.doubao.user.service.vo.core;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class PageUserVo<T> {
	private List<T> records;
	private long total;
	private long size;
	private long current;

	public static <T> PageUserVo<T> from(Page<T> page) {
		PageUserVo<T> vo = new PageUserVo<>();
		vo.setRecords(page.getRecords());
		vo.setTotal(page.getTotal());
		vo.setSize(page.getSize());
		vo.setCurrent(page.getCurrent());
		return vo;
	}

	public List<T> getRecords() {
		return records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}
}