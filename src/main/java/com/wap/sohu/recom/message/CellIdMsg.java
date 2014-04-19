package com.wap.sohu.recom.message;

/**
 * 类CellIdMsg.java的实现描述：TODO 类实现描述
 *
 * @author yeyanchao 2013-1-7 下午4:50:03
 */
public class CellIdMsg {

    private long cid;

    private int  cellid;

    private int  lac;

    public CellIdMsg(){
        super();
    }

    public CellIdMsg(long cid, int cellid, int lac){
        super();
        this.cid = cid;
        this.cellid = cellid;
        this.lac = lac;
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
     * @return the cellid
     */
    public int getCellid() {
        return cellid;
    }

    /**
     * @param cellid the cellid to set
     */
    public void setCellid(int cellid) {
        this.cellid = cellid;
    }

    /**
     * @return the lac
     */
    public int getLac() {
        return lac;
    }

    /**
     * @param lac the lac to set
     */
    public void setLac(int lac) {
        this.lac = lac;
    }

}
