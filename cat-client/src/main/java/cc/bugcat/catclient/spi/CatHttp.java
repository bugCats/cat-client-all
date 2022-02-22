package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatHttpPoint;


/**
 * http请求工具类，可替换成其他形式
 *
 * @author bugcat
 * */
public interface CatHttp {

    String doHttp(CatHttpPoint httpPoint, CatClientLogger catLog) throws CatHttpException;

}
