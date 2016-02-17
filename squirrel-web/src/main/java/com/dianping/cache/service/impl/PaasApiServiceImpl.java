package com.dianping.cache.service.impl;

import com.dianping.cache.scale.instance.docker.paasbean.Machine;
import com.dianping.cache.scale.instance.docker.paasbean.MachineStatusBean;
import com.dianping.cache.service.PaasApiService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/2/17.
 */
@Service
public class PaasApiServiceImpl implements PaasApiService{
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final String DEFAULT_PAASMACHINEURL = "http://10.3.21.21:8080/api/v1/machines/";
    private String paasUrl = configManager.getStringValue("squirrel-web.paas.machines.url",DEFAULT_PAASMACHINEURL);
    private static final Logger logger = LoggerFactory.getLogger(PaasApiServiceImpl.class);

    @Override
    public List<Machine> getMachines() {
        String result = RequestUtil.sendGet(paasUrl+"/static",null);
        Machine[] machines = null;
        try {
            machines = JsonUtils.fromStr(result,Machine[].class);
        } catch (IOException e) {
            logger.error("get static machines info error.",e);
        }
        return Arrays.asList(machines);
    }

    @Override
    public List<MachineStatusBean> getMachineStatus() {
        List<MachineStatusBean> machineStatusBeans = new ArrayList<MachineStatusBean>();
        String result = RequestUtil.sendGet(paasUrl+"static",null);
        Machine[] machines = null;
        try {
            machines = JsonUtils.fromStr(result,Machine[].class);
            for(Machine machine : machines){
                result = RequestUtil.sendGet(paasUrl+ machine.getIp()+"/status",null);
                MachineStatusBean bean = JsonUtils.fromStr(result,MachineStatusBean.class);
                machineStatusBeans.add(bean);
            }
        } catch (IOException e) {
            logger.error("get dynamic machines status error.",e);
        }
        return machineStatusBeans;
    }

    @Override
    public MachineStatusBean getMachineStatus(String machineIp){
        String result = RequestUtil.sendGet(paasUrl+ machineIp+"/status",null);
        MachineStatusBean bean = null;
        try {
            bean = JsonUtils.fromStr(result,MachineStatusBean.class);
        } catch (IOException e) {
            logger.error("get {} dynamic machine status error.",machineIp,e);
        }
        return bean;
    }
}
