package cc.bugcat.catclient.utils;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.CatHttp;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 *
 * 基于RestTemplate http请求
 *
 * @author bugcat
 * */
public class CatRestHttp implements CatHttp {

    /**
     * 默认http超时时间
     * */
    private static final int[] TIMEOUT = new int[]{3000, 5000};


    @Override
    public String doHttp(CatHttpPoint httpPoint, CatClientLogger catLog) throws CatHttpException {
        try {
            String path = httpPoint.getHost() + httpPoint.getUrl();
            String respStr = null;
            switch ( httpPoint.getRequestType() ) {
                case GET:
                    respStr = doGet(path, httpPoint.getKeyValueParam(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
                    break;
                case POST:
                    if( httpPoint.isPostString() ){
                        respStr = jsonPost(path, httpPoint.getRequestBody(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
                    } else {
                        respStr = doPost(path, httpPoint.getKeyValueParam(), httpPoint.getHeaderMap(), httpPoint.getSocket(), httpPoint.getConnect());
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

    private String doGet(String url, MultiValueMap<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        if ( params != null && params.size() > 0 ) {
            params.forEach((key, value) -> {
                if( value != null){
                    for(Object val : value){
                        uriBuilder.queryParam(key, val==null ? null : val.toString());
                    }
                } else {
                    uriBuilder.queryParam(key, null);
                }
            });
        }
        url = uriBuilder.build().toUriString();
        return requestSend(url, HttpMethod.GET, headers, null, ints);
    }

    private String doPost(String url, MultiValueMap<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException {
        return requestSend(url, HttpMethod.POST, headers, params, ints);
    }

    private String jsonPost(String url, String jsonStr, Map<String, String> headers, int... ints) throws CatHttpException {
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        HttpHeaders httpHeaders = getHeaders(headers);
        HttpEntity<String> request = new HttpEntity<>(jsonStr, httpHeaders);
        RestTemplate rest = createRestTemplate(ints);
        return rest.postForObject(url, request, String.class);
    }

    private String requestSend(String url, HttpMethod method, Map<String, String> headers, MultiValueMap<String, Object> params, int... ints) {
        if( params == null ){
            params = new LinkedMultiValueMap<>();
        }
        HttpHeaders httpHeaders = getHeaders(headers);
        RestTemplate rest = createRestTemplate(ints);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> result = rest.exchange(url, method, requestEntity, String.class);
        return result.getBody();
    }


    private final RestTemplate createRestTemplate(int[] ints){
        if( ints == null || ints.length != 2 ){
            ints = TIMEOUT;
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(ints[0]);
        factory.setReadTimeout(ints[1]);
        RestTemplate rest = new RestTemplate(factory);
        
        // 如果有自定义 RestTemplate
        RestTemplate restTemplate = getRestTemplate();
        if( restTemplate != null ){
            rest.setUriTemplateHandler(restTemplate.getUriTemplateHandler());
            rest.setInterceptors(restTemplate.getInterceptors());
            rest.setMessageConverters(restTemplate.getMessageConverters());
            rest.setErrorHandler(restTemplate.getErrorHandler());
        } else {
            rest.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        }
        return rest;
    }


    private final HttpHeaders getHeaders(Map<String, String> headers){
        HttpHeaders httpHeaders = new HttpHeaders();
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        return httpHeaders;
    }


    /**
     * 自定义RestTemplate情况：
     * <pre>
     *    @Bean
     *    @LoadBalanced
     *    public RestTemplate restTemplate() {
     *        return new RestTemplate();
     *    }
     * </pre>
     * */
    protected RestTemplate getRestTemplate(){
        return CatClientUtil.getBean(RestTemplate.class);
    }
            
}
