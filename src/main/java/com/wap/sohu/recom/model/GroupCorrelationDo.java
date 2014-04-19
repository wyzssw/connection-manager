/**
 * 
 */
package com.wap.sohu.recom.model;

import java.io.Serializable;
import java.util.Date;

/**
 * group tag 相似度持久化对象
 * 
 * @author yeyanchao
 * 
 */
public class GroupCorrelationDo implements Comparable<GroupCorrelationDo>,
		Serializable {
	private int gid;

	private double score;

	private Date time;

	private GroupCorrelationDo() {
	}

	public GroupCorrelationDo(int gid, double score, Date time) {
		this.gid = gid;
		this.score = score;
		this.time = time;
	}

	/**
	 * @return the gid
	 */
	public int getGid() {
		return gid;
	}

	/**
	 * @param gid
	 *            the gid to set
	 */
	public void setGid(int gid) {
		this.gid = gid;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/*
	 * 逆序排列: 根据相似度score 和 图片时间 两维排序
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GroupCorrelationDo o) {
		if (score < o.score) {
			return 1;
		}
		if (score > o.score) {
			return -1;
		}
		if (time.before(o.time)) {
			return 1;
		}
		if (time.after(o.time)) {
			return -1;
		}
		return 0;
	}
}