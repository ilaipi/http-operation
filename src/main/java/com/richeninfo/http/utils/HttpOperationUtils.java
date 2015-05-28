package com.richeninfo.http.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

/**
 * 封装发送请求相关的方法
 * @author mz.yyam
 * @date 2013-5-5 14:07:25
 */
public class HttpOperationUtils {
    private static String RELATIVE = "\\.\\./";
    /**
     * 把提交参数键值对转换为NameValuePair的列表
     * @param nameValueMap key:提交参数名 value:提交参数值
     * @return 如果传入的键值对为空，那么返回空的list
     */
    public static List<NameValuePair> getNameValuePairList(Map<String, String> nameValueMap) {
        if(MapUtils.isEmpty(nameValueMap)){
            return new ArrayList<NameValuePair>(0);
        }
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> map : nameValueMap.entrySet()) {
			list.add(new BasicNameValuePair(map.getKey(), map.getValue()));
		}
		return list;
	}
    
    /**
     * 把请求头键值对转换为 Header[]
     * @param headerMap
     * @return 
     */
    public static Header[] getHeaders(Map<String, String> headerMap) {
        if(MapUtils.isEmpty(headerMap)){
            return new Header[0];
        }
		Header[] headers = new Header[headerMap.size()];
		int i = 0;
		for (Map.Entry<String, String> map : headerMap.entrySet()) {
			headers[i++] = new BasicHeader(map.getKey(), map.getValue());
		}
		return headers;
	}
    
    public static HttpEntity getEntity(Map<String, String> inputs, String charset) throws UnsupportedEncodingException{
        return new UrlEncodedFormEntity(getNameValuePairList(inputs), charset);
    }
    
    public static HttpEntity getEntity(String requestBody, String charset) throws UnsupportedEncodingException{
        return new StringEntity(requestBody, charset);
    }
    
    
	public static String getAbsoluteUrl(String refUrl, String targetUrl) {
		String prifix = "";
		if (refUrl.startsWith("http://")) {
			prifix = "http://";
		} else if (refUrl.startsWith("https://")) {
			prifix = "https://";
		}
		refUrl = refUrl.replaceAll("http://", "").replaceAll("https://", "").replaceAll("\\?.*$", "");
		String[] paths = refUrl.split("/");
		if (StringUtils.isEmpty(targetUrl)) {
			return prifix + refUrl;
		}
		if (targetUrl.startsWith("?")) {
			return prifix + refUrl + targetUrl;
		}
		if (targetUrl.startsWith("http")) {
			return targetUrl;
		}
		if (targetUrl.startsWith("/")) {
			return prifix + paths[0] + targetUrl;
		}
		if(targetUrl.startsWith("./")){
			return prifix+refUrl+targetUrl.substring(2);
		}
		if (targetUrl.startsWith("../")) {
			String currentUrl = "";
			Pattern p = Pattern.compile(RELATIVE);
			Matcher m = p.matcher(targetUrl);
			int length = 0;
			while (m.find()) {
				length++;
			}
			if (paths.length < length + 2) {
				// throw new RuntimeException("参照地址不符合获取绝对路径算法");
				length = 1;// 异常情况下特殊处理(针对中国金属网，这样处理后url可以正确拼接)
			}
			for (int i = 0; i < paths.length - length - 1; i++) {
				currentUrl += paths[i] + "/";
			}
			targetUrl = targetUrl.replaceAll(RELATIVE, "");
			return prifix + currentUrl + targetUrl;
		} else {
			String currentUrl = "";
			if(paths.length > 1){
                if (refUrl.endsWith("/")) {
                    for(int i = 0;i < paths.length;i++){
                        currentUrl += paths[i] + "/";
                    }
                } else {
                    for(int i = 0;i < paths.length - 1;i++){
                        currentUrl += paths[i] + "/";
                    }
                }
				return prifix + currentUrl + targetUrl;
			} else {
				return prifix + paths[0] + "/" + targetUrl;
			}
		}
	}
}
