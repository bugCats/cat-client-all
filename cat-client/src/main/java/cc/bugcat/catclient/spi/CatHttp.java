package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.exception.CatHttpException;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.utils.CatRestHttp;


/**
 * http请求工具类，默认使用{@code RestTemplate}。
 * 
 * 可替换成其他形式。
 *
 * @see CatRestHttp
 * @author bugcat
 * */
public interface CatHttp {

    String doHttp(CatHttpPoint httpPoint, CatClientLogger catLog) throws CatHttpException;

}
