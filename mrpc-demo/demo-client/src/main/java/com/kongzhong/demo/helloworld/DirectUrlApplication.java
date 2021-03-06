package com.kongzhong.demo.helloworld;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * 直连客户端测试，需先启动 {@link com.kongzhong.mrpc.demo.helloworld.ServerApplication}
 *
 * @author biezhi
 *         2017/4/19
 */
public class DirectUrlApplication {

    public static void main(String[] args) throws Exception {
        RpcClient rpcClient = new RpcClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        final UserService userService = rpcClient.getProxyReferer(UserService.class);
        String msg = userService.hello("direct url");
        System.out.println(msg);
        rpcClient.stop();
    }

}
