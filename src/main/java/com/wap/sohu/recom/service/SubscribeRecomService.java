package com.wap.sohu.recom.service;

import java.util.List;

/**
 * 类SubscriptionRecomService.java的实现描述：刊物推荐
 *
 * @author yeyanchao 2012-10-17 下午5:16:52
 */
public interface SubscribeRecomService {

    /**
     * 用户刊物推荐
     *
     * @param cid
     * @param moreCount TODO
     * @param excludes TODO
     * @param hasSubs TODO
     * @return
     */
    public List<Integer> getSubscriptionRecom(long cid, int moreCount, String[] excludes, String[] hasSubs);


    /**
     *
     * @param cid
     * @param moreCount
     * @param excludes
     * @param hasSubs TODO
     * @param times TODO
     * @return
     */
    public List<Integer> getSubscriptionRecomByRandom(long cid, int moreCount, String[] excludes, String[] hasSubs, int times);

    /**
     * 刊物详情页 推荐接口
     * @param cid
     * @param subId
     * @param moreCount
     * @param excludes
     * @param hasSubs TODO
     * @return
     */
    public List<Integer> getSubscriptionRecomInDesc(long cid,int subId, int moreCount, String[] excludes, String[] hasSubs);
}
