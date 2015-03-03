package cn.ict.carc.christine.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.ict.carc.christine.Preprocesser.EncodeConverter;

/**
 * 
 * @author Stefanie Liu
 *
 *The DateHelper provides methods for formatting Date, get Current Date, etc. 
 */
public class DateHelper {
	public final static Logger logger = LogManager.getLogger(DateHelper.class);
	
	private static DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat dayFormat2 = new SimpleDateFormat("yyyyMMdd");
	private static DateFormat secondsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Date Now() {
		return new Date();
	}
	
	public static Date MinDate() {
		return new Date(Integer.MIN_VALUE);
	}
	
	public static Date MaxDate() {
		return new Date(Integer.MAX_VALUE);
	}
	
	
	public static String format2Day(Date date) {
		if(date==null)
			return null;
		return dayFormat.format(date);
	}
	
	public static String format2Seconds(Date date) {
		if(date==null)
			return null;
		return secondsFormat.format(date);
	}
	
	public static Date parser4Day(String day) {
		if(day==null) {
			return null;
		}
		try {
			return dayFormat.parse(day);
		} catch (ParseException e) {
			try {
				return dayFormat2.parse(day);
			} catch (ParseException e1) {
				return null;
			}
		}
	}
	
	public static Date parser4Seconds(String seconds) {
		if(seconds==null) {
			return null;
		}
		try {
			return secondsFormat.parse(seconds);
		} catch (ParseException e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	public static Timestamp getTimestampOrNull(Date date) {
	  return date == null? null:new Timestamp(date.getTime());
	}
}
