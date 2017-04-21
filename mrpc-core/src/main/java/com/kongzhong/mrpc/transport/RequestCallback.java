package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.client.RpcServerLoader;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

/**
 * @author biezhi
 *         2017/4/19
 */
public class RequestCallback implements Callable<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(RequestCallback.class);

    private EventLoopGroup eventLoopGroup = null;
    private InetSocketAddress serverAddress = null;
    private RpcSerialize rpcSerialize;

    public RequestCallback(EventLoopGroup eventLoopGroup, InetSocketAddress serverAddress, RpcSerialize rpcSerialize) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    public Boolean call() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new TcpClientChannelInitializer(rpcSerialize));

        // 和服务端建立连接,然后异步获取运行结果
        ChannelFuture channelFuture = b.connect(serverAddress);

        // 给结果绑定 Listener,
        channelFuture.addListener(new ChannelFutureListener() {
            /**
             * 当ChannelFuture 执行完毕之后(也就是异步IO结束之后) 会调用该函数
             * @param channelFuture
             * @throws Exception
             */
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    log.debug("client connect success");
                    //和服务器连接成功后, 获取MessageSendHandler对象
                    RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                    RpcServerLoader.me().setMessageSendHandler(handler);
                }
            }
        });
        return Boolean.TRUE;
    }
}