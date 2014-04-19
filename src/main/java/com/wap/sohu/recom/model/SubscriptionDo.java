package com.wap.sohu.recom.model;

/**
 * 类SubscriptionDo.java的实现描述：订阅缓存数据
 * 
 * @author yeyanchao 2012-10-15 下午3:39:58
 */
public class SubscriptionDo implements Comparable<SubscriptionDo> {

    private int    subId;

    private double score;

    public SubscriptionDo(){
        super();
    }

    public SubscriptionDo(int subId, double score){
        super();
        this.subId = subId;
        this.score = score;
    }

    /**
     * @return the subId
     */
    public int getSubId() {
        return subId;
    }

    /**
     * @param subId the subId to set
     */
    public void setSubId(int subId) {
        this.subId = subId;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * 逆序排列: 由大到小排序
     */
    @Override
    public int compareTo(SubscriptionDo o) {
        if (score < o.score) {
            return 1;
        }
        if (score > o.score) {
            return -1;
        }
        return 0;
    }
}
