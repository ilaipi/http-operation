package com.richeninfo.http;

import java.io.IOException;
import java.util.Map;

import com.richeninfo.http.utils.HttpOperationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import com.richeninfo.http.utils.ResponseHandler;

/**
 * 封装发送请求的方法。<br/>
 * 在创建对象时可以指定处理响应的编码
 * @author mz.yyam
 * @date 2013-5-5 13:28:10
 */
public abstract class HttpOperation {
    protected Log logger = LogFactory.getLog(this.getClass());
    /**
     * 网页编码。<br/>
     * 如果设置了，那么处理响应时统一使用此编码
     */
    protected String charset;
    
    protected int connectTimeout;
    
    protected int soTimeout;
    
    protected int socketBufferSize;
    
    protected HttpRequestRetryHandler requestRetryHandler;
    
    /**
     * 使用 org.apache.http.client.HttpClient 对象发送请求
     */
    protected HttpClient client;
    
    protected HttpRequestBase method;
    
    protected HttpResponse response;
    
    /**
     * 请求得到的响应源码
     */
    protected String responseBody;
    
    protected HttpHost host;
    
    /**
     * 发送get请求，得到响应的源代码
     * @param url
     * @param headers
     * @param inputs
     * @return 
     */
    public String get(String url, Map<String, String> headers, Map<String, String> inputs){
        responseBody = "";
        try{
            //before get request
            beforeGetRequest(url, headers, inputs);
            //get
            request();
            //after get request
            afterGetRequest(url, headers, inputs);
        } catch(Exception e){
            logger.warn("get request exception", e);
        } finally{
            release();
        }
        return responseBody;
    }
    
    protected void request() throws IOException{
        ResponseHandler responseHandler = null;
        if(StringUtils.isNotBlank(charset)){
            responseHandler = new ResponseHandler(charset);
        } else{
            responseHandler = new ResponseHandler();
        }
        responseBody = client.execute(host, method, responseHandler);
        charset = responseHandler.getNetCharset();
        response = responseHandler.getResponse();
    }
    
    protected abstract void beforeGetRequest(String url, Map<String, String> headers, Map<String, String> inputs) throws Exception;
    
    protected abstract void afterGetRequest(String url, Map<String, String> headers, Map<String, String> inputs) throws Exception;
    
    protected abstract void beforePostRequest(String url, Map<String, String> headers, HttpEntity entity) throws Exception;
    
    protected abstract void afterPostRequest(String url, Map<String, String> headers, HttpEntity entity) throws Exception;
    
    /**
     * 发送post请求，提交参数是标准的键值对形式
     * @param url
     * @param headers
     * @param inputs
     * @return 
     */
    public String post(String url, Map<String, String> headers, Map<String, String> inputs){
        responseBody = "";
        try{
            HttpEntity entity = HttpOperationUtils.getEntity(inputs, charset);
            beforePostRequest(url, headers, entity);
            
            request();
            
            afterPostRequest(url, headers, entity);
        } catch(Exception e){
            logger.warn("post(map) request exception", e);
        } finally{
            release();
        }
        return responseBody;
    }
    
    /**
     * 发送post请求，提交参数是字符串形式
     * @param url
     * @param headers
     * @param body
     * @return 
     */
    public String post(String url, Map<String, String> headers, String body){
        responseBody = "";
        try{
            HttpEntity entity = HttpOperationUtils.getEntity(body, charset);
            beforePostRequest(url, headers, entity);
            
            request();
            
            afterPostRequest(url, headers, entity);
        } catch(Exception e){
            logger.warn("post(string) request exception", e);
        } finally{
            release();
        }
        return responseBody;
    }
    
    public abstract String download(String url, String file, Map<String, String> headers) throws Exception;    
    
    protected void init() {
        client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);
		client.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
				Boolean.FALSE);
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		client.getParams().setParameter(
				CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset);
		client.getParams().setParameter(
				CoreProtocolPNames.HTTP_ELEMENT_CHARSET, charset);
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
		client.getParams().setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, socketBufferSize);
		client.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, Boolean.FALSE);
		client.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, Boolean.TRUE);
		//((DefaultHttpClient) client).setHttpRequestRetryHandler(DEFAULT_REQUEST_RETRY_HANDLER);

		//设置默认的 USER_AGENT
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
	}
    
    protected void release() {
		if (method != null) {
			method.abort();
		}
	}
    
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }
    
    public HttpRequestRetryHandler getRequestRetryHandler() {
        return requestRetryHandler;
    }

    public void setRequestRetryHandler(HttpRequestRetryHandler requestRetryHandler) {
        this.requestRetryHandler = requestRetryHandler;
    }

    public HttpClient getClient() {
        return client;
    }

    public void setClient(HttpClient client) {
        this.client = client;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
