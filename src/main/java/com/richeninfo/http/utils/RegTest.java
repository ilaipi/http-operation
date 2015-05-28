package com.richeninfo.http.utils;

/**
 * 匹配<meta http-equiv='Content-Type' content='text/html; charset=GB18030'/>标签中的字符编码
 * 的正则表达式
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Title: RegTest正则表达式测试类
 * </p>
 * <p>
 * Description: 匹配<meta标签中的字符编码
 * </p>
 * <p>
 * Copyright: Copyright (c) 2010
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author <a href="hpjianhua@163.com">hpjianhua</a>
 * @version 1.0
 * @created 2010-10-04
 */
public class RegTest {

	public static void main(String[] args) {
		// 要匹配的字符串
		String source = "<meta content='text/html; charset=GB18030' http-equiv='Content-Type'/>";
		// 将上面要匹配的字符串转换成小写
		source = source.toLowerCase();
		// 匹配的字符串的正则表达式
		String reg_charset = "<meta[^>]*?charset=([a-z|A-Z|0-9]*[\\-]*[0-9]*)[\\s|\\S]*";

		Pattern p = Pattern.compile(reg_charset);
		Matcher m = p.matcher(source);

		while (m.find()) {
			System.out.println(m.group(0));
			System.out.println(m.group(1));
		}

	}
}

// outout:
// <meta http-equiv='content-type' content='text/html; charset=gb18030'/>
// gb18030
