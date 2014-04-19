/**
 *
 */
package com.wap.sohu.recom.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wap.sohu.recom.utils.CommonUtils;



/**
 * 跟踪访问推荐服务数据
 *
 * @author yeyanchao
 */
public class AccessTraceLogData {

    private static final String host = CommonUtils.getLocalHost(0);

    public static String getLocalHost(){
        return host;
    }
    /**
     * 访问跟踪日志数据类别:日志类别名称、接受的日志数据键值(categoryname，keys..)
     *
     * @author yeyanchao
     */
    public static enum LogCategoryEnum {
        GroupRec("groupRecomStatistic", LogKeyEnum.Total, LogKeyEnum.UserRec, LogKeyEnum.HotRec, LogKeyEnum.TagRec,
                 LogKeyEnum.GroupCorrelation, LogKeyEnum.Gid, LogKeyEnum.ItemBased, LogKeyEnum.PicType, LogKeyEnum.Cid,
                 LogKeyEnum.NewsId, LogKeyEnum.NewsRec),

        UserLikeAction("userLike",LogKeyEnum.Cid,LogKeyEnum.LikeType,LogKeyEnum.TidClick,LogKeyEnum.TidUnclick,LogKeyEnum.ItemIdClick
                       ,LogKeyEnum.ItemIdUnClick),

        LocationInfo("userlocation",LogKeyEnum.Cid,LogKeyEnum.CITYGBCODE,LogKeyEnum.TimeStamp,LogKeyEnum.LOCATIONTYPE),
        NewsRec("newsRecomStatistic",LogKeyEnum.Cid,LogKeyEnum.NewsId,LogKeyEnum.RecCount,LogKeyEnum.RecNewsIds,LogKeyEnum.TimeStamp,LogKeyEnum.IP),
        topNewsRec("topNewsStatistic",LogKeyEnum.Cid,LogKeyEnum.RecCount,LogKeyEnum.RecNewsIds,LogKeyEnum.TimeStamp),
        mChannelNewRec("mChannelNews",LogKeyEnum.Cid,LogKeyEnum.RecCount,LogKeyEnum.RecNewsIds,LogKeyEnum.CHANNELID,LogKeyEnum.TimeStamp),
        taoClickStat("taoClickStatistic",LogKeyEnum.Cid,LogKeyEnum.PID,LogKeyEnum.NewsId,LogKeyEnum.RecType,LogKeyEnum.TimeStamp);

        private String          category;
        private Set<LogKeyEnum> keySet = new HashSet<LogKeyEnum>();


        private LogCategoryEnum(String category, LogKeyEnum... keyEnums){
            this.category = category;
            for (LogKeyEnum logKeyEnum : keyEnums) {
                keySet.add(logKeyEnum);
            }
        }

        /**
         * @return the category
         */
        public String getCategory() {
            return category;
        }

        /**
         * @param category the category to set
         */
        public void setCategory(String category) {
            this.category = category;
        }

        public boolean contains(LogKeyEnum key) {
            return keySet.contains(key);
        }

    }

    /**
     * 访问跟踪日志数据键值:
     *
     * @author yeyanchao
     */
    public static enum LogKeyEnum {
        Total("total"), UserRec("userRec"), HotRec("hotRec"), TagRec("tagRec"), GroupCorrelation("groupCorr"),
        Gid("gid"), ItemBased("itembase"), PicType("picType"), Cid("cid"), NewsId("newsId"), NewsRec("newsRec"),
        TidClick("tidClick"),TidUnclick("tidUnClick"),LikeType("likeType"),ItemIdClick("itemIdClick"),ItemIdUnClick("itemIdUnClick")
        ,TimeStamp("ts"),CITYGBCODE("gb"),LOCATIONTYPE("locType"),RecNewsIds("recNewsIds"), RecCount("recCount"), IP("ip"),PID("pid"),
        RecType("recType"),CHANNELID("channelId");

        private String name;

        private LogKeyEnum(String name){
            this.name = name;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

    }

    private LogCategoryEnum   category;
    private Map<String, Object> message = new HashMap<String, Object>();

    public AccessTraceLogData(LogCategoryEnum category){
        this.category = category;
    }

    public void add(LogKeyEnum key, long count) {
        if (category.contains(key)) {
            message.put(key.name, count);
        }
    }

    public void add(LogKeyEnum key, int count) {
        if (category.contains(key)) {
            message.put(key.name, Long.valueOf(count));
        }
    }

    public void add(LogKeyEnum key,String value){
        if (category.contains(key)) {
            message.put(key.name, value);
        }
    }

    public void add(LogKeyEnum key,Iterable<?> iterable){
        if (category.contains(key)) {
            message.put(key.name, iterable);
        }
    }



    public long get(LogKeyEnum key) {
        Object obj = message.get(key.name);
        if (obj==null||!(obj instanceof Long)) {
            return 0L;
        }
        return  Long.valueOf(obj.toString());
    }

    public LogCategoryEnum getCategory() {
        return category;
    }

    public Map<String, Object> getMessage() {
        return message;
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AccessTraceLogData [category=" + category + ", message=" + message + "]";
    }

    public static void main(String[] args) {
//        AccessTraceLogData logData = new AccessTraceLogData(LogCategoryEnum.GroupRec);
//        logData.add(LogKeyEnum.Total, 100);
//        logData.add(LogKeyEnum.Total, "100");
//        JSONObject jso = new JSONObject(logData.getMessage());
//        System.out.println(jso);
        Set<Integer> set = new HashSet<Integer>();
        set.remove(null);
        System.out.println(set);
    }

}
