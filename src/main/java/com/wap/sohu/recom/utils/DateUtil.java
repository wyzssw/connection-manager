package com.wap.sohu.recom.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

/**
 * 通用日期工具类
 * 
 * @author <a href="mailto:minni@sohu-inc.com">NiMin</a>
 * @version 1.0 2012-7-13 下午3:37:37
 * 
 */
public class DateUtil {

	public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 取当前日期的字符形式（yyyy年xx月xx日）
	 * 
	 * @return
	 */
	public static String getDateChStr() {
		Calendar calendar = new GregorianCalendar();
		return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";
	}

	/**
	 * 将指定格式的日期字符串转换成毫秒数
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static long paseDateLong(String date, String pattern) {
		long result = -1;
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		try {
			result = format.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 取当前时间的前一天时间
	 * 
	 * @return
	 */
	public static String getPreDay() {
		Date d = new Date(System.currentTimeMillis() - 24 * 3600 * 1000);
		SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
		return format.format(d);
	}

	/**
	 * 得到当前日期
	 * 
	 * @param format
	 *            默认 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static final String getDate(String format) {
		if (format.equals("")) {
			format = DEFAULT_DATETIME_FORMAT;
		}
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String cdate = sdf.format(cal.getTime());
		return cdate;
	}

	/**
	 * 把毫秒时间转换成指定格式的时间字符串
	 * 
	 * @param time
	 * @param format
	 * @return
	 */
	public static final String getDate(long time, String format) {
		if (format.equals("")) {
			format = DEFAULT_DATETIME_FORMAT;
		}
		Date d = new Date(time);
		SimpleDateFormat fm = new SimpleDateFormat(format);
		return fm.format(d);
	}

	/**
	 * 判断一天是否为周末
	 * 
	 * @param sDate
	 * @return
	 */
	public static boolean isWeekEnd(String sDate) {
		if ("".equals(sDate)) {
			return false;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		Date date = null;
		try {
			date = df.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}

		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		int mydate = cd.get(Calendar.DAY_OF_WEEK);

		if (mydate == 1 || mydate == 7)
			return true;
		else
			return false;
	}

	/**
	 * 取两个时间间隔的分钟数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getMinutesNumberNew(Date date1, Date date2) {
		return (int) (date2.getTime() - date1.getTime()) / 60000;
	}

	public static Date getTomorrow() {
		return getDate(1);
	}

	// 获取next天后的日期，如果next为负数，则表示几天前的date
	public static Date getDate(int next) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, next);
		return calendar.getTime();
	}

	public static Date getToday() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static long todayLeftTimes() {
		Date now = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		System.out.println(calendar.getTime());

		return calendar.getTimeInMillis() - now.getTime();
	}

	public static Date dateAddDays(Date date, int addDays) {
		Date newDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, addDays);
			newDate = cal.getTime();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newDate;
	}

	public static String solarDateWeek() {
		Calendar calendar = new GregorianCalendar();
		return (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";
	}

	/**
	 * 根据当前日期生成简单密钥<BR>
	 * 规则： 年份后2位 + 月份 + 日期
	 * 
	 * @return
	 */
	public static int getDateCipher() {
		Calendar cal = Calendar.getInstance();// 使用日历类
		return cal.get(Calendar.YEAR) % 100 + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 返回当天的中文星期几
	 * 
	 * @return
	 */
	public static String dayInWeek() {
		return dayInWeek(new Date());
	}

	/**
	 * 返回中文星期几
	 * 
	 * @param d
	 *            传入的日期
	 * @return
	 */
	public static String dayInWeek(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int time = c.get(Calendar.DAY_OF_WEEK);
		String[] weekNames = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		String weekName = weekNames[time - 1];
		return weekName;
	}

	/**
	 * 将日期转换成字符串形式
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		return dateToString(date, DEFAULT_DATETIME_FORMAT, null);
	}

	/**
	 * 将日期转换成字符串形式
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		return dateToString(date, format, null);
	}

	/**
	 * 将日期转换成字符串形式
	 * 
	 * @param date
	 * @param format
	 * @param defVal
	 * @return
	 */
	public static String dateToString(Date date, String format, String defVal) {
		if (date == null || StringUtils.isEmpty(format)) {
			return defVal;
		}
		String result;
		try {
			result = new SimpleDateFormat(format).format(date);
		} catch (Exception e) {
			result = defVal;
		}
		return result;
	}

	/**
	 * 将时间转换成字符串形式
	 * 
	 * @param datetime
	 * @return
	 */
	public static String timeToString(long datetime) {
		return timeToString(datetime, DEFAULT_DATETIME_FORMAT, null);
	}

	/**
	 * 将时间转换成字符串形式
	 * 
	 * @param datetime
	 * @param format
	 * @return
	 */
	public static String timeToString(long datetime, String format) {
		return timeToString(datetime, format, null);
	}

	/**
	 * 将时间转换成字符串形式
	 * 
	 * @param datetime
	 * @param format
	 * @param defVal
	 * @return
	 */
	public static String timeToString(long datetime, String format, String defVal) {
		Date d = new Date(datetime);
		return new SimpleDateFormat(format).format(d);
	}

	/**
	 * 将字符串转换成日期对象
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date stringToDate(String strDate) {
		return stringToDate(strDate, DEFAULT_DATETIME_FORMAT, new Date());
	}

	/**
	 * 将字符串转换成日期对象
	 * 
	 * @param strDate
	 * @param format
	 * @return
	 */
	public static Date stringToDate(String strDate, String format) {
		return stringToDate(strDate, format, new Date());
	}

	/**
	 * 将字符串转换成日期对象
	 * 
	 * @param strDate
	 * @param format
	 * @param defVal
	 * @return
	 */
	public static Date stringToDate(String strDate, String format, Date defVal) {
		if (StringUtils.isEmpty(strDate) || StringUtils.isEmpty(format)) {
			return null;
		}
		Date d;
		try {
			d = new SimpleDateFormat(format).parse(strDate);
		} catch (ParseException e) {
			d = defVal;
		}
		return d;
	}

	/**
	 * 将字符串转换成时间
	 * 
	 * @param strDate
	 * @return
	 */
	public static long stringToTime(String strDate) {
		return stringToTime(strDate, DEFAULT_DATETIME_FORMAT, null);
	}

	/**
	 * 将字符串转换成时间
	 * 
	 * @param strDate
	 * @param format
	 * @return
	 */
	public static long stringToTime(String strDate, String format) {
		return stringToTime(strDate, format, null);
	}

	/**
	 * 将字符串转换成时间
	 * 
	 * @param strDate
	 * @param format
	 * @param defVal
	 * @return
	 */
	public static long stringToTime(String strDate, String format, Date defVal) {
		Date d = stringToDate(strDate, format, defVal);
		if (d == null) {
			return 0L;
		} else {
			return d.getTime();
		}
	}

	/**
	 * 将时间调整到当天的00：00：00
	 * 
	 * @param date
	 * @return
	 */
	public static Date setToDayStartTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获得两个日期之间相差的天数(返回值去掉了小数部分，即只返回)。（date1 - date2）
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int intervalDays(Date date1, Date date2) {
		long intervalMillSecond = setToDayStartTime(date1).getTime() - setToDayStartTime(date2).getTime();
		return (int) (intervalMillSecond / 24 * 3600 * 1000);
	}

	/**
	 * 获得两个日期之间相差的分钟数。（date1 - date2）
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int intervalMinutes(Date date1, Date date2) {
		long intervalMillSecond = date1.getTime() - date2.getTime();
		return (int) (intervalMillSecond / 60 * 1000 + (intervalMillSecond % 60 * 1000 > 0 ? 1 : 0));
	}

	/**
	 * 取两个日期（yyyy-MM-dd HH:mm:ss）字符串相差的分钟数
	 * 
	 * @param dateStr1
	 * @param dateStr2
	 * @return
	 */
	public static int intervalMinutes(String dateStr1, String dateStr2) {
		long minNumber = 0;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d1 = df.parse(dateStr1);
			Date d2 = df.parse(dateStr2);
			return intervalMinutes(d1, d2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (int) minNumber;
	}

	/**
	 * 获得两个日期之间相差的秒数。（date1 - date2）
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int intervalSeconds(Date date1, Date date2) {
		long intervalMillSecond = date1.getTime() - date2.getTime();
		return (int) (intervalMillSecond / 1000 + (intervalMillSecond % 1000 > 0 ? 1 : 0));
	}
	
    /**
     * 得到某个时刻的时间戳
     * @param offset
     * @param unit
     * @return
     */
	public static long getUnixTime(int offset,TimeUnit unit){
	        long seconds = unit.toSeconds(offset);
	        if (offset > 0 && seconds == 0) {
	            seconds = 1;
	        }
	        return System.currentTimeMillis()/1000+seconds;
	    }
	

}
