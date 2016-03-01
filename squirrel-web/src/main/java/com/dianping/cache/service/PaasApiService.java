package com.dianping.cache.service;

import com.dianping.cache.scale.instance.docker.paasbean.Machine;
import com.dianping.cache.scale.instance.docker.paasbean.MachineStatusBean;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/2/17.
 */
public interface PaasApiService {
    List<Machine> getMachines();
    List<MachineStatusBean> getMachineStatus();
    MachineStatusBean getMachineStatus(String machineIp);
}
