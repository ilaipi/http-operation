package com.richeninfo.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException;

import com.richeninfo.http.utils.HttpOperationUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * HttpOperation的默认实现
 * @author mz.yyam
 * @date 2013-5-5 13:34:29
 */
public class DefaultHttpOperation extends HttpOperation{
    public static final Map<String, String> contentTypes;
    static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("application/msword", ".doc");
		map.put("application/pdf", ".pdf");
		map.put("application/msexcel", ".xls");
		map.put("application/vnd.ms-excel", ".xls");
		map.put("application/vnd.ms-powerpoint", ".ppt");
		map.put("application/x-javascript", ".js");
		map.put("application/x-shockwave-flash", ".swf");
		map.put("application/xhtml+xml", ".xhtml");
		map.put("application/zip", ".zip");
		map.put("audio/midi", ".mid");
		map.put("audio/mpeg", ".mp3");
		map.put("audio/x-pn-realaudio", ".rm");
		map.put("audio/x-wav", ".wav");
		map.put("image/bmp", ".bmp");
		map.put("image/gif", ".gif");
		map.put("image/jpeg", ".jpg");
		map.put("image/png", ".png");
		map.put("text/css", ".css");
		map.put("text/html", ".html");
		map.put("text/plain", ".txt");
		map.put("text/xml", ".xml");
		map.put("video/mpeg", ".mpeg");
		map.put("video/x-msvideo", ".avi");
		map.put("video/x-sgi-movie", ".movie");
		contentTypes = Collections.unmodifiableMap(map);

	}
    
    public DefaultHttpOperation(){
        this.connectTimeout = 50000;
        this.soTimeout = 300000;
        this.socketBufferSize = 8 * 1024;
        super.init();
        
        ((DefaultHttpClient) client).setHttpRequestRetryHandler(DEFAULT_REQUEST_RETRY_HANDLER);
    }
    
    public DefaultHttpOperation(String charset){
        this();
        this.charset = charset;
    }

    @Override
    protected void beforeGetRequest(String url, Map<String, String> headers, Map<String, String> inputs) throws MalformedURLException {
        if (StringUtils.isBlank(url)) {
			throw new IllegalArgumentException("get request:url cannot be blank.");
		}
        URL u = new URL(url);
        host = new HttpHost(u.getHost(), 80);
        String formatParamString = URLEncodedUtils.format(HttpOperationUtils.getNameValuePairList(inputs), charset);
        if(StringUtils.isNotBlank(formatParamString)){
            url = (url.indexOf("?") < 0) ? (url + "?" + formatParamString) : (url.substring(0, url.indexOf("?") + 1) + formatParamString);
        }
        method = new HttpGet(url);
        if(MapUtils.isNotEmpty(headers)){
            method.setHeaders(HttpOperationUtils.getHeaders(headers));
        }
    }

    @Override
    public String post(String url, Map<String, String> headers, String body) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private final static HttpRequestRetryHandler DEFAULT_REQUEST_RETRY_HANDLER = new HttpRequestRetryHandler() {

        @Override
		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {
			if (executionCount >= 3) {
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			return !(request instanceof HttpEntityEnclosingRequest);
		}

	};

    /**
     * do nothing
     * @param url
     * @param headers
     * @param inputs 
     */
    @Override
    protected void afterGetRequest(String url, Map<String, String> headers, Map<String, String> inputs) {
        //Nothing to do
    }

    @Override
    protected void beforePostRequest(String url, Map<String, String> headers, HttpEntity entity) throws Exception {
        if (StringUtils.isBlank(url)) {
			throw new IllegalArgumentException("post request:url cannot be blank.");
		}
        URL u = new URL(url);
        host = new HttpHost(u.getHost(), 80);
        method = new HttpPost(url);
        if(entity != null){
            ((HttpPost) method).setEntity(entity);
        }
        if(MapUtils.isNotEmpty(headers)){
            method.setHeaders(HttpOperationUtils.getHeaders(headers));
        }
    }

    @Override
    protected void afterPostRequest(String url, Map<String, String> headers, HttpEntity entity) throws Exception {
        //处理跳转
        int statusCode = response.getStatusLine().getStatusCode();
        if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
				|| (statusCode == HttpStatus.SC_SEE_OTHER)
				|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            Header header = response.getFirstHeader("location");
            if (header == null) {
				throw new UnsupportedOperationException("Invalid redirect,header information does not contain location property.");
			}
            String location = header.getValue();
            if(StringUtils.isBlank(location)){
                location = url.substring(0, url.indexOf("/"));//默认访问网站host
            }
            release();
            location = HttpOperationUtils.getAbsoluteUrl(url, location);
            responseBody = get(location, headers, null);
        }
    }

    @Override
    public String download(String url, String file, Map<String, String> headers) throws Exception {
        try{
            beforeGetRequest(url, headers, null);
            response = client.execute(host, method);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if ((entity != null) && (entity.isStreaming())) {
					String suffix = FilenameUtils.getExtension(file);
					if (StringUtils.isEmpty(suffix)) {
						Header header = entity.getContentType();
						if (header != null) {
							String key = header.getValue();
							int index = key.indexOf(";");
							if (index > 0) {
								key = key.substring(0, index);
							}
							key = key.toLowerCase();
							if (contentTypes.containsKey(key)) {
								file += contentTypes.get(key);
							}
						}
					}
                    File storeFile = new File(file);
					FileOutputStream output = new FileOutputStream(storeFile);
					InputStream input = entity.getContent();
					byte[] bytes = new byte[1024];
					int i = 0;
					while ((i = input.read(bytes)) != -1) {
						output.write(bytes, 0, i);
					}
					output.flush();
					output.close();
				}
				EntityUtils.consume(entity);
			}
        } catch(Exception e){
            logger.warn("download exception", e);
        }
        return "";
    }
}
