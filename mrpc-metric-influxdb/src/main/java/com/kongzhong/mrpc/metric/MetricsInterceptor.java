package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.model.RpcContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控拦截器
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class MetricsInterceptor implements RpcInteceptor {

    private MetricsClient metricsClient;
    private MetricsUtils metricsUtils;

    public MetricsInterceptor(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
        this.metricsClient.init();
        this.metricsUtils = new MetricsUtils(metricsClient);
    }

    @Override
    public Object execute(Invocation invocation) throws Exception {

        long begin = System.currentTimeMillis();

        log.debug("metrics execute [{}]-[{}]", invocation.getRequest().getRequestId(), begin);

        Class<?> clazz = invocation.getTarget().getClass();
        String method = invocation.getFastMethod().getName();
        String appId = RpcContext.get().getRpcRequest().getAppId();

        try {
            Object bean = invocation.next();
            metricsUtils.success(clazz, method, metricsClient.getName(), begin);
            return bean;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                metricsUtils.systemFail(clazz, method, metricsClient.getName(), begin);
            } else {
                metricsUtils.serviceFail(clazz, method, metricsClient.getName(), begin);
            }
            throw e;
        }
    }

}
