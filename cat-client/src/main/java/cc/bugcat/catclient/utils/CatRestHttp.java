package cc.bugcat.catclient.utils;

import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatHttpPoint;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;

import java.util.List;
import java.util.Map;

public class CatRestHttp implements CatHttp {

    /**
     * 默认http超时时间
     * */
    private static final int[] TIMEOUT = new int[]{3000, 5000};


    @Override
    public String doHttp(CatHttpPoint httpPoint, CatClientLogger catLog) throws CatHttpException {
        try {
            String respStr = null;
            switch ( httpPoint.getRequestType() ) {
                case GET:
                    respStr = doGet(httpPoint.getPath(), httpPoint.getKeyValueParam(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
                    break;
                case POST:
                    if( httpPoint.isPostString() ){
                        respStr = jsonPost(httpPoint.getPath(), httpPoint.getRequestBody(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
                    } else {
                        respStr = doPost(httpPoint.getPath(), httpPoint.getKeyValueParam(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
                    }
                    break;
                default:
                    throw new HttpClientErrorException(HttpStatus.NOT_IMPLEMENTED, "未实现的请求方式:" + httpPoint.getRequestType());
            }
            httpPoint.setResponseBody(respStr);
            return respStr;

        } catch ( HttpStatusCodeException ex ) {
            HttpStatus statusCode = ex.getStatusCode();
            throw new CatHttpException(statusCode.value(), statusCode.getReasonPhrase(), ex);
        } catch ( RestClientResponseException ex ) {
            throw new CatHttpException(ex.getRawStatusCode(), ex.getStatusText(), ex);
        } catch ( Exception ex ) {
            throw new CatHttpException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), ex);
        }
    }

    private String doGet(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException {
        return requestSend(url, HttpMethod.GET, headers, params, ints);
    }

    private String doPost(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException {
        return requestSend(url, HttpMethod.POST, headers, params, ints);
    }

    private String jsonPost(String url, String jsonStr, Map<String, String> headers, int... ints) throws CatHttpException {
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        HttpHeaders httpHeaders = getHeaders(headers);
        HttpEntity<String> request = new HttpEntity<>(jsonStr, httpHeaders);
        RestTemplate rest = getRestTemplate(ints);
        return rest.postForObject(url, request, String.class);
    }

    private String requestSend(String url, HttpMethod method, Map<String, String> headers, Map<String, Object> params, int... ints) {
        HttpHeaders httpHeaders = getHeaders(headers);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        for ( Map.Entry<String, Object> entry : params.entrySet() ) {
            paramMap.put(entry.getKey(), (List<Object>)entry.getValue());
        }
        RestTemplate rest = getRestTemplate(ints);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, httpHeaders);
        ResponseEntity<String> result = rest.exchange(url, method, requestEntity, String.class);
        return result.getBody();
    }


    private final RestTemplate getRestTemplate(int[] ints){
        if( ints == null || ints.length != 2 ){
            ints = TIMEOUT;
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(ints[0]);
        factory.setReadTimeout(ints[1]);
        RestTemplate rest = new RestTemplate(factory);
        return rest;
    }


    private final HttpHeaders getHeaders(Map<String, String> headers){
        HttpHeaders httpHeaders = new HttpHeaders();
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        return httpHeaders;
    }

}
