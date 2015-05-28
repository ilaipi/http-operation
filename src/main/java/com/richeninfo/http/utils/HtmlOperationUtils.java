package com.richeninfo.http.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 封装操作html文档的方法
 * @author mz.yyam
 * @date 2013-5-6 21:11:48
 */
public class HtmlOperationUtils {
    public static final String META_CHARSET_REG = "<meta[^>]*?charset=([a-z|A-Z|0-9]*[\\-]*[0-9]*)[\\s|\\S]*";
    public static String getMetaCharset(String html){
        Pattern p = Pattern.compile(META_CHARSET_REG);
		Matcher m = p.matcher(html);
        if(m.find()){
            return m.group(1);
        } else{
            return "";
        }
    }
}
