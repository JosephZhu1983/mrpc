package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class TcpClientHandler extends SimpleClientHandler<RpcResponse> {

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    public RpcCallbackFuture sendRequest(RpcRequest request) {
        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(request);
        mapCallBack.put(request.getRequestId(), rpcCallbackFuture);
        log.debug("request: {}", request);
        channel.writeAndFlush(request);
        return rpcCallbackFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (response.getSuccess()) {
            log.debug("response: {}", response);
        }
        String messageId = response.getRequestId();
        RpcCallbackFuture rpcCallbackFuture = mapCallBack.get(messageId);
        if (rpcCallbackFuture != null) {
            mapCallBack.remove(messageId);
            rpcCallbackFuture.done(response);
        }
    }

}