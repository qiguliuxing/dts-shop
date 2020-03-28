package com.qiguliuxing.dts.core.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化工具类
 */
public class DateTimeUtil {

	/**
	 * 格式 yyyy年MM月dd日 HH:mm:ss
	 *
	 * @param dateTime
	 * @return
	 */
	public static String getDateTimeDisplayString(LocalDateTime dateTime) {
		DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
		String strDate2 = dtf2.format(dateTime);

		return strDate2;
	}

	public static String getPrevMonthEndDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = new Date();
			Calendar c = Calendar.getInstance();
			// 设置为指定日期
			c.setTime(date);
			// 指定日期月份减去一
			c.add(Calendar.MONTH, -1);
			// 指定日期月份减去一后的 最大天数
			c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
			// 获取最终的时间
			Date lastDateOfPrevMonth = c.getTime();
			return sdf.format(lastDateOfPrevMonth);
		} catch (Exception e) {
			return null;
		}
	}
}
