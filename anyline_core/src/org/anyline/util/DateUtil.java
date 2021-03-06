/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	private static int MaxDate;// 一月最大天数
	private static int MaxYear;// 一年最大天数
	
	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.ms";
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final int DATE_PART_YEAR = Calendar.YEAR;
	public static final int DATE_PART_MONTH = Calendar.MONTH;
	public static final int DATE_PART_DATE = Calendar.DATE;
	public static final int DATE_PART_DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
	public static final int DATE_PART_HOUR = Calendar.HOUR;
	public static final int DATE_PART_MINUTE = Calendar.MINUTE;
	public static final int DATE_PART_SECOND = Calendar.SECOND;
	public static final int DATE_PART_MILLISECOND = Calendar.MILLISECOND;
	
	private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINESE);
	public static boolean between( Date cur,Date fr, Date to){
		if(cur.getTime() >= fr.getTime() && cur.getTime() <= to.getTime()){
			return true;
		}
		return false;
	}
	/**
	 * cur是否在fr和to之内
	 * @param cur
	 * @param fr
	 * @param to
	 * @return
	 */
	public static boolean between(String cur, String fr, String to){
		return between(parse(cur), parse(fr), parse(to));
	}
	/**
	 * 时间差
	 * @param part
	 * @param fr
	 * @param to
	 * @return
	 */
	public static long diff(int part, Date fr, Date to) {
		long result = 0;
		if (Calendar.YEAR == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.YEAR);
		}
		if (Calendar.MONTH == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR) * 12;
			calendar.setTime(fr);
			time -= calendar.get(Calendar.YEAR) * 12;
			calendar.setTime(to);
			time += calendar.get(Calendar.MONTH);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.MONTH);
		}
		if (Calendar.WEEK_OF_YEAR == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(fr);
			time -= calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(to);
			time += calendar.get(Calendar.WEEK_OF_YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.WEEK_OF_YEAR);
		}
		long ms = to.getTime() - fr.getTime();
		if (Calendar.DAY_OF_YEAR == part) {
			result = ms / 1000 / 60 / 60 / 24;
		}else if (Calendar.HOUR == part) {
			result = ms / 1000 / 60 / 60;
		}else if (Calendar.MINUTE == part) {
			result = ms / 1000 / 60;
		}else if (Calendar.SECOND == part) {
			result = ms / 1000;
		}else if (Calendar.MILLISECOND == part) {
			result = ms;
		}
		return result;
	}

	public static long diff(int part, String fr, String to) {
		return diff(part, parse(fr), parse(to));
	}

	public static long diff(int part, Date fr) {
		return diff(part, fr, new Date());
	}

	public static long diff(int part, String fr) {
		return diff(part, parse(fr));
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format(Date date, String format) {
		if (null == date || null == format)
			return "";
		return new java.text.SimpleDateFormat(format).format(date);
	}

	public static String format() {
		return format(new Date(), FORMAT_DATE_TIME);
	}

	public static String format(String format) {
		return format(new Date(), format);
	}

	public static String format(Date date) {
		return format(date, FORMAT_FULL);
	}

	public static String format(String date, String format) {
		Date d = parse(date);
		return format(d, format);
	}

	public static String dateformat(Date date){
		return format(date, FORMAT_DATE);
	}

	public static String dateformat(){
		return format(new Date(), FORMAT_DATE);
	}
	
	/**
	 * 时间转换成分钟
	 * 
	 * @param hm
	 * @return
	 */
	public static int convertMinute(String hm) {
		int minute = -1;
		if(!hm.contains(":")){
			return minute;
		}
		String sps[] = hm.split(":");
		int h = BasicUtil.parseInt(sps[0], 0);
		int m = BasicUtil.parseInt(sps[1], 0);
		minute = h * 60 + m;
		return minute;
	}

	public static int convertMinute() {
		String hm = format("hh:mm");
		return convertMinute(hm);
	}
	/**
	 * 分钟转换成时间
	 * @param minute
	 * @return
	 */
	public static String convertMinute(int minute) {
		String time = "";
		int h = minute / 60;
		int m = minute % 60;
		if(h < 10){
			time += "0";
		}
		time += h + ":";
		if(m < 10){
			time += "0";
		}
		time += m;
		return time;
	}
	/**
	 * 根据一个日期，返回是星期几的字符串
	 * 
	 * @param sdate
	 * @return
	 */
	public static String getWeek(Date date) {
		// 再转换为时间
		calendar.setTime(date);
		// int hour=c.get(Calendar.DAY_OF_WEEK);
		// hour中存的就是星期几了，其范围 1~7
		// 1=星期日 7=星期六，其他类推
		return new SimpleDateFormat("EEEE").format(calendar.getTime());
	}

	/**
	 * 当月第一天
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设为当前月的1号
		return calendar.getTime();
	}

	public static Date getFirstDayOfMonth(String date) {
		return getFirstDayOfMonth(parse(date));
	}

	public static Date getFirstDayOfMonth() {
		return getFirstDayOfMonth(new Date());
	}

	/**
	 * 下个月第一天
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfNextMonth(Date date) {
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		return calendar.getTime();
	}

	public static Date getFirstDayOfNextMonth(String date) {
		return getFirstDayOfNextMonth(parse(date));
	}

	public static Date getFirstDayOfNextMonth() {
		return getFirstDayOfNextMonth(new Date());
	}
	/**
	 * 上个月第一天
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfPreviousMonth(Date date) {
		calendar.setTime(date);
		calendar.set(Calendar.DATE, 1);// 设为当前月的1号
		calendar.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		return calendar.getTime();
	}
	public static Date getFirstDayOfPreviousMonth(String date) {
		return getFirstDayOfPreviousMonth(parse(date));
	}

	public static Date getFirstDayOfPreviousMonth() {
		return getFirstDayOfPreviousMonth(new Date());
	}

	/**
	 * 当月最后一天
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("ETC/GMT-8"), Locale.CHINESE);
		calendar.setTimeInMillis(date.getTime()+100000);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设为当前月的1号
		calendar.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		calendar.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfMonth(String date) {
		return getLastDayOfMonth(parse(date));
	}

	public static Date getLastDayOfMonth() {
		return getLastDayOfMonth(new Date());
	}

	/**
	 * 上月最后一天
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfPreviousMonth(Date date) {
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfPreviousMonth(String date) {
		return getLastDayOfPreviousMonth(parse(date));
	}

	public static Date getLastDayOfPreviousMonth() {
		return getLastDayOfPreviousMonth(new Date());
	}

	/**
	 * 下月最后一天
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfNextMonth(Date date) {
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 加一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfNextMonth(String date) {
		return getLastDayOfNextMonth(parse(date));
	}

	public static Date getLastDayOfNextMonth() {
		return getLastDayOfNextMonth(new Date());
	}

	
	// 获得本周星期日的日期
	public static Date getCurrentWeekday(Date date) {
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 6);
		return calendar.getTime();
	}

	public static Date getCurrentWeekday(String date) {
		return getCurrentWeekday(parse(date));
	}

	public static Date getCurrentWeekday() {
		return getCurrentWeekday(new Date());
	}
	// 获得当前日期与本周日相差的天数
	public static int getMondayPlus(Date date) {
		calendar.setTime(date);
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}
	public static int getMondayPlus() {
		return getMondayPlus(new Date());
	}

	// 获得本周一的日期
	public static Date getMondayOFWeek(Date date) {
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus);
		return calendar.getTime();
	}

	public static Date getMondayOFWeek(String date) {
		return getMondayOFWeek(parse(date));
	}

	public static Date getMondayOFWeek() {
		return getMondayOFWeek(new Date(0));
	}
	

	// 获得下周星期一的日期
	public static Date getNextMonday(Date date) {
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 7);
		return calendar.getTime();
	}
	public static Date getNextMonday(String date) {
		return getNextMonday(parse(date));
	}
	public static Date getNextMonday() {
		return getNextMonday(new Date());
	}

	// 获得下周星期日的日期
	public static Date getNextSunday(Date date) {
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 7 + 6);
		return calendar.getTime();
	}
	public static Date getNextSunday(String date) {
		return getNextSunday(parse(date));
	}

	public static Date getNextSunday() {
		return getNextSunday(new Date());
	}
	
	//当前日期与本周日相差几天
	public static int getMonthPlus(Date date) {
		calendar.setTime(date);
		int monthOfNumber = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
		MaxDate = calendar.get(Calendar.DATE);
		if (monthOfNumber == 1) {
			return -MaxDate;
		} else {
			return 1 - monthOfNumber;
		}
	}

	// 获得明年最后一天的日期
	public static Date getNextYearEnd(Date date) {
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, 1);// 加一个年
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime();
	}
	public static Date getNextYearEnd(String date) {
		return getNextYearEnd(parse(date));
	}
	public static Date getNextYearEnd() {
		return getNextYearEnd(new Date());
	}

	// 获得明年第一天的日期
	public static Date getNextYearFirst(Date date) {
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, 1);// 加一个年
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		return calendar.getTime();
	}
	public static Date getNextYearFirst(String date) {
		return getNextYearFirst(parse(date));
	}
	public static Date getNextYearFirst() {
		return getNextYearFirst(new Date());
	}

	/**
	 * 一年多少天
	 * @return
	 */
	public static int getDaysOfYear(Date date) {
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		calendar.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = calendar.get(Calendar.DAY_OF_YEAR);
		return MaxYear;
	}
	public static int getDaysOfYear(){
		return getDaysOfYear(new Date());
	}
	
	private static int getYearPlus(Date date) {
		calendar.setTime(date);
		int yearOfNumber = calendar.get(Calendar.DAY_OF_YEAR);// 获得当天是一年中的第几天
		calendar.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		calendar.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = calendar.get(Calendar.DAY_OF_YEAR);
		if (yearOfNumber == 1) {
			return -MaxYear;
		} else {
			return 1 - yearOfNumber;
		}
	}

	// 获得本年第一天的日期
	public static Date getFirstDayOfYear(Date date) {
		int yearPlus = getYearPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, yearPlus);
		return calendar.getTime();
	}
	public static Date getFirstDayOfYear(String date) {
		return getFirstDayOfYear(parse(date));
	}
	public static Date getFirstDayOfYear() {
		return getFirstDayOfYear(new Date());
	}

	// 获得本年最后一天的日期 *
	public static String getCurrentYearEnd(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	public static String getCurrentYearEnd(String date) {
		return getCurrentYearEnd(parse(date));
	}
	public static String getCurrentYearEnd() {
		return getCurrentYearEnd(new Date());
	}
	// 获得上年第一天的日期 *
	public static String getPreviousYearFirst(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-01--1";
	}
	public static String getPreviousYearFirst(String date) {
		return getPreviousYearFirst(parse(date));
	}
	public static String getPreviousYearFirst() {
		return getPreviousYearFirst(new Date());
	}

	/**
	 * 获取某年某月的最后一天
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return 最后一天
	 */
	public static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;
	}

	/**
	 * 是否闰年
	 * 
	 * @param year
	 *            年
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * 转换成日期
	 * 
	 * @param dateString
	 * @param formatString
	 * @return
	 */
	public static Date parse(String date, String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		try {
			return df.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 转换成日期(使用默认格式)
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parse(String dateString) {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_FULL);
		try {
			date = sdf.parse(dateString);
		} catch (Exception e) {
			try {
				sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
				date = sdf.parse(dateString);
			} catch (Exception ex) {
				try {
					sdf = new SimpleDateFormat(FORMAT_DATE);
					date = sdf.parse(dateString);
				} catch (Exception exc) {
					try {
						sdf = new SimpleDateFormat("yyyyMMdd");
						date = sdf.parse(dateString);
					} catch (Exception exce) {
						try {
							sdf = new SimpleDateFormat();
							date = sdf.parse(dateString);
						} catch (Exception excep) {
							date = null;
						}
					}
				}
			}
		}
		return date;
	}

	/**
	 * 昨天
	 * 
	 * @return
	 */
	public static Date yesterday() {
		return addDay(-1);
	}

	/**
	 * 明天
	 * 
	 * @return
	 */
	public static Date tomorrow() {
		return addDay(1);
	}

	/**
	 * 现在
	 * 
	 * @return
	 */
	public static Date now() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * 按日加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addDay(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按日加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addDay(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按月加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addMonth(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}

	/**
	 * 按月加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addMonth(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}

	/**
	 * 按年加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addYear(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 当前日期所在周的第idx天 第1天：星期日 第7天：星期六
	 * 
	 * @param idx
	 * @param date
	 * @return
	 */
	public static Date getDateOfWeek(int idx, Date date) {
		Date result = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - idx;
		cal.add(Calendar.DATE, -day_of_week);
		result = cal.getTime();
		return result;
	}

	public static Date getDateOfWeek(int idx) {
		return getDateOfWeek(idx, new Date());
	}

	/**
	 * 星期几(礼拜几)
	 * 
	 * @return
	 */
	public static int getDayOfWeek(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK) - 1;
	}
	public static int getDayOfWeek() {
		return getDayOfWeek(new Date());
	}

	/**
	 * 一年中的第几个星期
	 * 
	 * @return
	 */
	public static int getWeekOfYear(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
	public static int getWeekOfYear() {
		return getWeekOfYear(new Date());
	}
	/**
	 * 按年加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addYear(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按小时加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addHour(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}

	/**
	 * 按小时加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addHour(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}

	/**
	 * 按分钟加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addMinute(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}

	/**
	 * 按分钟加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addMinute(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}

	/**
	 * 年份
	 * 
	 * @return
	 */
	public static int year(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}
	public static int year() {
		return year(new Date());
	}


	/**
	 * 月份
	 * 
	 * @return
	 */
	public static int month(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}
	public static int month() {
		return month(new Date());
	}

	/**
	 * 日(号)
	 * 
	 * @return
	 */
	public static int day(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	public static int day() {
		return day(new Date());
	}

	/**
	 * 小时(点)
	 * 
	 * @return
	 */
	public static int hour(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR);
	}
	public static int hour() {
		return hour(new Date());
	}

	/**
	 * 分钟
	 * 
	 * @return
	 */
	public static int minute(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	public static int minute() {
		return minute(new Date());
	}

	/**
	 * 秒
	 * 
	 * @return
	 */
	public static int second(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.SECOND);
	}
	public static int second() {
		return second(new Date());
	}

	/**
	 * fr > to返回 1 
	 * @param fr
	 * @param to
	 * @return
	 */
	public static int compare(Date fr, Date to){
		int result = 0;
		if(fr.getTime() > to.getTime()){
			result = 1;
		}else{
			result = -1;
		}
		return result;
	}
	public static int compare(String fr, String to){
		return compare(parse(fr), parse(to));
	}
	/**
	 * 是上午吗?
	 * 
	 * @return
	 */
	public static boolean isAm(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.AM_PM) == 0;
	}
	public static boolean isAm() {
		return isAm(new Date());
	}

	/**
	 * 是下午吗?
	 * 
	 * @return
	 */
	public static boolean isPm(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.AM_PM) == 1;
	}
	public static boolean isPm() {
		return isPm(new Date());
	}
}