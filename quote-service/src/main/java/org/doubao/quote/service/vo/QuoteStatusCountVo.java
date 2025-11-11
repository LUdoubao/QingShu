package org.doubao.quote.service.vo;

public class QuoteStatusCountVo {
	/**
	 * 统计总数
	 */
	private int allCount;
	/**
	 * 审核通过
	 */
	private int auditPassCount;
	/**
	 * 未通过
	 */
	private int auditRejectCount;
	/**
	 * 等待审核
	 */
	private int auditWaitCount;
	/**
	 * 下架数
	 */
	private int withdrawCount;
	/**
	 * 屏蔽数
	 */
	private int blockCount;
	/**
	 * 草稿数
	 */
	private int draftCount;

	public int getAllCount() {
		return allCount;
	}

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	public int getAuditPassCount() {
		return auditPassCount;
	}

	public void setAuditPassCount(int auditPassCount) {
		this.auditPassCount = auditPassCount;
	}

	public int getAuditWaitCount() {
		return auditWaitCount;
	}

	public void setAuditWaitCount(int auditWaitCount) {
		this.auditWaitCount = auditWaitCount;
	}

	public int getAuditRejectCount() {
		return auditRejectCount;
	}

	public void setAuditRejectCount(int auditRejectCount) {
		this.auditRejectCount = auditRejectCount;
	}

	public int getWithdrawCount() {
		return withdrawCount;
	}

	public void setWithdrawCount(int withdrawCount) {
		this.withdrawCount = withdrawCount;
	}

	public int getBlockCount() {
		return blockCount;
	}

	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}

	public int getDraftCount() {
		return draftCount;
	}

	public void setDraftCount(int draftCount) {
		this.draftCount = draftCount;
	}
}
