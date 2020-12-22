package com.bugcat.catclient.utils;

import com.bugcat.catclient.spi.CatHttp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * http请求工具类，可替换成其他形式
 * @author bugcat
 * */
public class CatHttpUtil implements CatHttp {
	
	private static final int socketTimeout = 10000;
	private static final int connectTimeout = 10000;

    public static String charset = "UTF-8";


    public final static Map<String, String> jsonpHeader = new HashMap<>();
    public final static Map<String, String> formHeader = new HashMap<>();
	static {
        jsonpHeader.put("Accept-Encoding", "gzip, deflate");
        jsonpHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
        jsonpHeader.put("Content-Type", "application/json");
        
        formHeader.put("Accept-Encoding", "gzip, deflate");
        formHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
        formHeader.put("Content-Type", "application/x-www-form-urlencoded");
    }

	public String doGet(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws Exception {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            if( params != null ){
                url = url + urlEncoded(params, "?", "");
            }
            HttpGet httpget = new HttpGet(new String(url.getBytes(), charset));
            
			headers.putAll(CatHttpUtil.formHeader);
			for (Map.Entry<String, String> en : headers.entrySet()) {
				httpget.setHeader(en.getKey(), en.getValue().toString());
			}
            
            httpget.setConfig(getRequestConfig(ints));
            httpclient = HttpClients.createDefault();
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(entity, charset);
            } else {
                throw new Exception("http请求异常！" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeAll(response, httpclient);
        }
    }
    
	public String doPost(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws Exception{
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		try {
			HttpPost httpPost = new HttpPost(url);

			headers.putAll(CatHttpUtil.formHeader);
			for(Map.Entry<String, String> map : headers.entrySet()){
				httpPost.setHeader(map.getKey(), map.getValue().toString());
			}
			if ( params != null && params.size() > 0 ) {
				List<BasicNameValuePair> datas = new ArrayList<>(params.size());
				for (Map.Entry<String, Object> map : params.entrySet()) {
					Object value = map.getValue();
					if( value != null && value instanceof List){
						for(Object val : ((List) value)){
							datas.add(new BasicNameValuePair(map.getKey(), val==null?"":val.toString()));
						}
					} else {
						datas.add(new BasicNameValuePair(map.getKey(), value==null?"":value.toString()));
					}
				}
				// 参数转码
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(datas, charset);
				httpPost.setEntity(entity);
			}
			HttpClientBuilder create = HttpClientBuilder.create();
			RequestConfig requestConfig = getRequestConfig(ints);
			if (requestConfig != null) {
				create.setDefaultRequestConfig(requestConfig);
			}
			httpclient = create.build();

			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode() / 100;
            if (status == 2) {
                return EntityUtils.toString(entity, charset);
            } else {
                throw new Exception("http请求异常！" + response.getStatusLine().toString());
            }
		} catch (Exception e) {
		    throw e;
		} finally {
			closeAll(response, httpclient);
		}
	}

	public String jsonPost(String url, String jsonStr, Map<String, String> hearders, int... ints) throws Exception {
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		try {
	
			HttpPost httpPost = new HttpPost(url);
			hearders.putAll(CatHttpUtil.jsonpHeader);
			for (Map.Entry<String, String> map : hearders.entrySet()) {
				httpPost.addHeader(map.getKey(), map.getValue());
			}
			StringEntity se = new StringEntity(jsonStr, charset);
			se.setContentEncoding(charset);
			httpPost.setEntity(se);
			HttpClientBuilder create = HttpClientBuilder.create();
			create.setDefaultRequestConfig(getRequestConfig(ints));
			httpclient = create.build();
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(entity, charset);
            } else {
                throw new Exception("http请求异常！" + response.getStatusLine().getStatusCode());
            }
		} catch (Exception e) {
			throw e;
		} finally {
			closeAll(response, httpclient);
		}
	}

	private static RequestConfig getRequestConfig(int[] ints) {
		if (ints == null || ints.length != 2) {
			ints = new int[] { socketTimeout, connectTimeout };
		}
        RequestConfig.Builder custom = RequestConfig.custom();
		if( ints[0] > 0){
            custom.setSocketTimeout(ints[0]);
        }
        if (ints[1] > 0) {
            custom.setSocketTimeout(ints[1]);
		}
        return custom.build();// 设置请求和传输超时时间
	}



	/**
	 * url编码
	 */
	private static String encodeUri(String url) {
		try {
			return URLEncoder.encode(url, charset);
		} catch (UnsupportedEncodingException e) {

		}
		return url;
	}
    

	/**
	 * 将键值对转换成get方式提交的url
	 *
	 * @param reqMap 键值对map
	 * @param pre 前缀
	 * @param suf 后缀
	 * @return
	 */
	private static String urlEncoded(Map<String, Object> reqMap, String pre, String suf) {
		StringBuilder url = new StringBuilder();
		if ( reqMap != null && reqMap.size() > 0 ) {
			for (String key : reqMap.keySet()) {
				Object value = reqMap.get(key);
				if( value != null && value instanceof List){
					for(Object val : ((List) value)){
						url.append("&" + key + "=" + encodeUri(val==null?"":val.toString()));
					}
				} else {
					url.append("&" + key + "=" + encodeUri(value==null?"":value.toString()));
				}
			}
			url.delete(0, 1);
		}
		return (pre != null ? pre.trim() : "") + url.append(suf != null ? suf : "").toString();
	}
	

	private static void closeAll(Closeable... clo){
		for (Closeable c : clo ){
			if(c != null){
				try {
					c.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}

}
