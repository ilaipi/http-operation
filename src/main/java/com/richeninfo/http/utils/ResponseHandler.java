package com.richeninfo.http.utils;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author mz.yyam
 * @date 2013-5-6 21:45:17
 */
public class ResponseHandler implements org.apache.http.client.ResponseHandler<String>{
    private String charset;
    protected HttpResponse response;
    
    public ResponseHandler(){
        
    }

    public ResponseHandler(String charset) {
        this.charset = charset;
    }
    
    /**
     * 返回最终处理响应使用的编码（主要用于未知编码的处理）
     * @return 
     */
    public String getNetCharset(){
        return charset;
    }
    
    /**
     * 返回本次请求的响应对象
     * @return 
     */
    public HttpResponse getResponse(){
        return this.response;
    }

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        this.response = response;
        HttpEntity entity = this.response.getEntity();
        if (entity != null) {
            if(StringUtils.isNotBlank(charset)){
                return EntityUtils.toString(entity, charset);
            } else{
                //1.EntityUtils.getContentCharSet
                charset = EntityUtils.getContentCharSet(entity);
                if(StringUtils.isNotBlank(charset)){
                    return EntityUtils.toString(entity, charset);
                } else{
                    //2.匹配meta中的charset
                    charset = HtmlOperationUtils.getMetaCharset(new String(EntityUtils.toByteArray(entity), Charset.ISO_8859_1.getCharset()));
                    if(StringUtils.isNotBlank(charset)){
                        return EntityUtils.toString(entity, charset);
                    } else{
                        //3.根据Charset定义的字符集进行匹配
                        for(Charset c : Charset.values()){
                            charset = c.getCharset();
                            String html = EntityUtils.toString(entity, charset);
                            if(java.nio.charset.Charset.forName(charset).newEncoder().canEncode(html)){
                                return EntityUtils.toString(entity, charset);
                            } else{
                                charset = "";
                            }
                        }
                    }
                }
            }
        }
        return "";
    }
}
