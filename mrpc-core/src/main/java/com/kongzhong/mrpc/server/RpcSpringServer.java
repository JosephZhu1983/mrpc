package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.kongzhong.mrpc.Const.MRPC_SERVER_REGISTRY_PREFIX;

/**
 * RPC服务端Spring实现
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
@Data
@NoArgsConstructor
public class RpcSpringServer extends SimpleRpcServer implements ApplicationContextAware, InitializingBean {

    /**
     * ① 设置上下文
     *
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {

        Map<String, RegistryBean> registryBeanMap = ctx.getBeansOfType(RegistryBean.class);
        if (null != registryBeanMap) {
            registryBeanMap.values().forEach(registryBean -> serviceRegistryMap.put(MRPC_SERVER_REGISTRY_PREFIX + registryBean.getName(), parseRegistry(registryBean)));
        }

        if (StringUtils.isNotEmpty(this.interceptors)) {
            String[] inters = this.interceptors.split(",");
            for (String interceptorName : inters) {
                RpcServerInteceptor rpcServerInteceptor = (RpcServerInteceptor) ctx.getBean(interceptorName);
                rpcMapping.addInterceptor(rpcServerInteceptor);
            }
        }

        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        try {
            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object target : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(target);
                    RpcService rpcService = realBean.getClass().getAnnotation(RpcService.class);
                    String serviceName = rpcService.value().getName();
                    if (NoInterface.class.getName().equals(serviceName)) {
                        Class<?>[] intes = realBean.getClass().getInterfaces();
                        if (null == intes || intes.length != 1) {
                            serviceName = realBean.getClass().getName();
                        } else {
                            serviceName = intes[0].getName();
                        }
                    }
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setBean(realBean);
                    serviceBean.setServiceName(serviceName);
                    rpcMapping.addServiceBean(serviceBean);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * ② 后置操作
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.startServer();
    }

    protected ServiceRegistry parseRegistry(RegistryBean registryBean) {
        String type = registryBean.getType();
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            ServiceRegistry serviceRegistry = new DefaultRegistry();
            return serviceRegistry;
        }
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddr = registryBean.getAddress();
            log.info("RPC server connect zookeeper address: {}", zkAddr);
            try {
                Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddr);
                ServiceRegistry serviceRegistry = (ServiceRegistry) zookeeperServiceRegistry;
                return serviceRegistry;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }


}