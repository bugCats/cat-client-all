package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;

/**
 *
 * 呆毛4，继承
 *
 *
 * 单元测试类 cc.bugcat.example.catclient.remote.ApiRemote4Test
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}")
public interface ApiRemoteService4Plus extends ApiRemoteService4 {



}
