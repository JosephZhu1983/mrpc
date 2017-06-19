package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.client.cluster.ha.HaStrategy;
import com.kongzhong.mrpc.client.cluster.loadblance.LBStrategy;
import com.kongzhong.mrpc.client.proxy.SimpleClientProxy;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * rpc客户端
 */
@Data
@Slf4j
public class SimpleRpcClient {

    /**
     * 序列化类型，默认protostuff
     */
    protected RpcSerialize serialize;

    /**
     * 传输协议，默认tcp协议
     */
    protected String transport;

    /**
     * 服务发现
     */
    protected ServiceDiscovery serviceDiscovery;

    /**
     * 客户端是否已经初始化
     */
    protected boolean isInit;

    /**
     * 负载均衡策略，默认轮询
     */
    protected LBStrategy lbStrategy;

    /**
     * 高可用策略，默认failover
     */
    protected HaStrategy haStrategy;

    /**
     * appId
     */
    protected String appId;

    /**
     * 引用类名
     */
    protected List<Class<?>> referers = Lists.newArrayList();

    protected List<RpcInteceptor> inteceptors = Lists.newArrayList();

    public SimpleRpcClient() {
    }

    public SimpleRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public void stop() {
        Connections.me().shutdown();
    }

    /***
     * 动态代理,获得代理后的对象
     *
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyBean(Class<T> rpcInterface) {
        if (!isInit) {
            this.init();
        }
        return (T) Reflection.newProxy(rpcInterface, new SimpleClientProxy<T>(inteceptors));
    }

    private void init() {
        synchronized (Connections.class) {
            Connections connections = Connections.me();
            ClientConfig clientConfig = ClientConfig.me();

            if (null == serialize) {
                serialize = DefaultConfig.serialize();
            }

            if (null == transport) {
                transport = DefaultConfig.transport();
            }

            if (null == lbStrategy) {
                lbStrategy = DefaultConfig.lbStrategy();
            }
            if (null == haStrategy) {
                haStrategy = DefaultConfig.haStrategy();
            }

            if (null == serialize) {
                throw new InitializeException("Serialize not is null.");
            }
            TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
            if (null == transportEnum) {
                throw new InitializeException("Transport type [" + transport + "] error.");
            }
            if (transportEnum.equals(TransportEnum.HTTP)) {
                clientConfig.setHttp(true);
            }

            clientConfig.setRpcSerialize(serialize);
            clientConfig.setLbStrategy(lbStrategy);
            if (StringUtils.isNotEmpty(appId)) {
                clientConfig.setAppId(appId);
            }
            clientConfig.setHaStrategy(haStrategy);
            clientConfig.setTransport(transportEnum);
            clientConfig.setReferers(referers);

            if (null == serviceDiscovery) {
                serviceDiscovery = new DefaultDiscovery();
            }
            serviceDiscovery.discover();
            isInit = true;
        }
    }

    public void bindReferer(Class<?>... interfaces) {
        if (null != interfaces) {
            referers.addAll(Arrays.asList(interfaces));
        }
    }

    public void bindReferer(String... interfaces) {
        if (null != interfaces) {
            for (String type : interfaces) {
                referers.add(ReflectUtils.from(type));
            }
        }
    }

    public List<RpcInteceptor> getInteceptors() {
        return inteceptors;
    }

    public void setInteceptors(List<RpcInteceptor> inteceptors) {
        this.inteceptors = inteceptors;
    }

    public void addInterceptor(RpcInteceptor inteceptor) {
        if (null == inteceptor) {
            throw new RpcException("Inteceptor not is null");
        }
        log.info("Add interceptor {}", inteceptor.toString());
        this.inteceptors.add(inteceptor);
    }
}