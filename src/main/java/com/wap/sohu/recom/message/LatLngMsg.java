package com.wap.sohu.recom.message;

/**
 * 类LatLngMsg.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao 2013-1-7 下午4:48:47
 */
public class LatLngMsg {

    private long   cid;

    private double lat;

    private double lng;

    public LatLngMsg(){
        super();
    }

    public LatLngMsg(long cid, double lat, double lng){
        super();
        this.cid = cid;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * @return the cid
     */
    public long getCid() {
        return cid;
    }

    /**
     * @param cid the cid to set
     */
    public void setCid(long cid) {
        this.cid = cid;
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the lng
     */
    public double getLng() {
        return lng;
    }

    /**
     * @param lng the lng to set
     */
    public void setLng(double lng) {
        this.lng = lng;
    }

}
