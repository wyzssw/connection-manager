package com.wap.sohu.recom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.wap.sohu.recom.dao.BaseJdbcSupport;
import com.wap.sohu.recom.model.SubscriptionType;

/**
 * 类SubscriptionDao.java的实现描述：订阅信息查询
 *
 * @author yeyanchao 2012-10-18 下午3:55:24
 */
@Repository
public class SubscriptionDao extends BaseJdbcSupport {

    private static final Set<Integer> excludeSubIdSet = new HashSet<Integer>();

    {
        // 过滤欢乐快递
        excludeSubIdSet.add(181);
        // 八卦周刊
        excludeSubIdSet.add(183);
        // 专题快讯
        excludeSubIdSet.add(89);
    }

    /**
     * 获取运营刊物列表
     *
     * @return
     */
    public List<Integer> listOperationSubs() {
        String sql = "select s.id from p_subscription_info s,p_subscription_product p where p.productId=1 and p.isvalid =1 and p.subid=s.id and s.isHot =1";
        List<Integer> list = this.getJdbcTemplateMpaperCms2Slave().queryForList(sql, Integer.class);
        return list;
    }

    /**
     * 获取刊物对应类别
     *
     * @return
     */
    public List<SubscriptionType> listSubscriptionType(Integer subId) {
        if (subId == null || subId < 0) {
            return null;
        }
        String sql = "select r.subId,r.typeId from p_subscription_product p,p_subscription_type_relation r,p_subscription_type t where p.productId=1 and p.isvalid=1 and t.isValid =1 and t.productId=1 and t.id = r.typeId and p.subId = r.subId and p.subId=? GROUP BY r.subId,r.typeId";
        List<SubscriptionType> list = this.getJdbcTemplateMpaperCms2Slave().query(sql, new Object[] { subId },
                                                                             new RowMapper<SubscriptionType>() {

                                                                                 @Override
                                                                                 public SubscriptionType mapRow(ResultSet rs,
                                                                                                                int rowNum)
                                                                                                                           throws SQLException {
                                                                                     SubscriptionType type = new SubscriptionType();
                                                                                     type.setSubId(rs.getInt("subId"));
                                                                                     type.setTypeId(rs.getInt("typeId"));
                                                                                     return type;
                                                                                 }
                                                                             });

        return list;
    }

    public Map<Integer, String> listSubscibeInfo(List<Integer> subIdList) {
        String sql = "select s.name from p_subscription_info s,p_subscription_product p where p.productId=1 and p.isvalid =1 and p.subid=s.id and s.id = ?";
        Map<Integer, String> result = new HashMap<Integer, String>();
        if (subIdList != null) {
            for (Integer subId : subIdList) {
                if (excludeSubIdSet.contains(subId)) {
                    continue;
                }
                List<String> names = getJdbcTemplateMpaperCms2().queryForList(sql, new Object[] { subId }, String.class);
                if (names != null && !names.isEmpty()) {
                    result.put(subId, names.get(0));
                }
            }
        }
        return result;
    }

    /**
     * 查询订阅类型列表
     *
     * @return
     */
    public List<Integer> querySubTypeList() {
        String sql = "select id from p_subscription_type where productId = 1 and isvalid=1";
        List<Integer> subTypes = getJdbcTemplateMpaperNewSlave().queryForList(sql, Integer.class);
        return subTypes;
    }

    /**
     * 根据cid，productId查询用户订阅信息
     *
     * @param cid
     * @param productId
     * @return
     */
    public List<Integer> queryUserSubscribeList(long cid, int productId) {
        if (cid < 0 || productId < 0) {
            return null;
        }
        int tableIndex = getSubscribeTablet(cid);
        String sql = "select sub_id from tbl_client_subscribe_" + tableIndex
                     + " where client_id = ? and product_id = ? order by mtime desc";
        List<Integer> subIds = getJdbcTemplateSmcsubscribeSlave().queryForList(sql, new Object[] { cid, productId },
                                                                               Integer.class);
        return subIds;
    }

    /**
     * 获取分表ID
     *
     * @param cid
     * @return
     */
    private int getSubscribeTablet(long cid) {
        if (cid < 0) {
            cid = 0;
        }
        int tableIndex = (int) (cid % 64);
        return tableIndex;
    }

    /**
     * 查询默认订阅
     *
     * @return
     */
    public List<Integer> queryDefaultSubscriptionList() {
        String sql = "select subId from p_subscription_product where productId =1 and isvalid=1 and isDefault=1";
        List<Integer> defaultIdList = getJdbcTemplateMpaperCms2().queryForList(sql, Integer.class);
        return defaultIdList;
    }
    
    
//    public Set<Integer> getWemediaSubIds(){
//        String sql = "SELECT subId FROM `p_subscription_type_relation` WHERE typeId=120";
//        List<Integer> list = getJdbcTemplateMpaperCms2().queryForList(sql, Integer.class);
//        return new HashSet<Integer>(list);
//    }
    
    public Set<Integer> getWemediaSubIds(){
        String sql = "SELECT id FROM `p_subscription_info` WHERE belongingId=1 ;";
        List<Integer> list = getJdbcTemplateMpaperCms2().queryForList(sql, Integer.class);
        return new HashSet<Integer>(list);
    }
}
