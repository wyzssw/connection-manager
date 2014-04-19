package com.wap.sohu.recom.model;

import java.io.Serializable;

public class Area  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8440400938413343908L;
	
	private int id;
	private int code;
	private String area;
	private String province;
	private String city;
	private String gbcode; //国标码
	private String firstSpell; //城市拼音首字母
	
	public int getId() {
		return id;
	}
	public void setId(int code) {
		this.id = code;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getGbcode() {
		return gbcode;
	}
	public void setGbcode(String gbcode) {
		this.gbcode = gbcode;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getFirstSpell() {
		return firstSpell;
	}
	public void setFirstSpell(String firstSpell) {
		this.firstSpell = firstSpell;
	}
	
//	public String toString(){
//		return "\n"+"{" + "\n"+
//				"\"city\":\""+city+"\"," +"\n"+
//				"\"code\":\""+code+"\"," +"\n"+
//				"\"province\":\""+province+"\"," +"\n"+
//				"\"gbcode\":\""+gbcode+"\"," +"\n"+
//				"\"index\":\""+firstSpell+"\"" +"\n"+
//				"}"+"\n";
//	}
	
}
