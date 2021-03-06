package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class ServiceBean {

    private String serviceName;
    private Object bean;
    private String address;
    private boolean direct;
    private String appId;
    private String registry = "default";

}
