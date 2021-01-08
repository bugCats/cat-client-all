package com.bugcat.catclient.spi;

import com.bugcat.catclient.handler.CatHttpException;

import java.util.Map;


/**
 * http请求工具类，可替换成其他形式
 * 
 * 如果用其他http类，并且使用静态方法调用，必须手动指定CatHttp实现类
 * @link com.bugcat.catclient.utils.CatClientUtil#setDefaultCatHttp(com.bugcat.catclient.spi.CatHttp)
 *
 * @author bugcat
 */
public interface CatHttp {


    String doGet(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException;

    String doPost(String url, Map<String, Object> params, Map<String, String> headers, int... ints) throws CatHttpException;

    String jsonPost(String url, String jsonStr, Map<String, String> hearders, int... ints) throws CatHttpException;

}
