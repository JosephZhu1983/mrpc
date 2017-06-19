package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * RPC客户端整合Spring的实现
 *
 * @author biezhi
 *         2017/4/25
 */
@Data
@Slf4j
public class RpcClient extends SimpleRpcClient implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext ctx;

    public RpcClient() {
        super();
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ClientBean> clientBeanMap = ctx.getBeansOfType(ClientBean.class);
        RpcClient rpcClient = ctx.getBean(RpcClient.class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ctx;
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) context.getBeanFactory();
        if (null != rpcClient && clientBeanMap != null && !clientBeanMap.isEmpty()) {
            clientBeanMap.values().forEach(bean -> {
                String id = bean.getId();
                String interfaceName = bean.getInterfaceName();
                try {
                    Class<?> clazz = Class.forName(interfaceName);
                    Object object = rpcClient.getProxyBean(clazz);
                    dbf.registerSingleton(id, object);
                    log.info("Bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            });
        }

        if (null != referers && !referers.isEmpty()) {
            referers.forEach(clazz -> {
                String interfaceName = clazz.getName();
                try {
                    Object object = rpcClient.getProxyBean(clazz);
                    dbf.registerSingleton(interfaceName, object);
                    log.info("Bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("Initializing rpc client.");
        ctx = applicationContext;
    }

}