package com.dianping.squirrel.service;

import com.dianping.squirrel.vo.ScaleParams;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public interface CloudApiService {
    int scaleOut(ScaleParams params);
//    int scaleInInstance(ScaleParams params);
//    List<Machine> getMachines();
//    List<MachineStatusBean> getMachineStatus();
//    MachineStatusBean getMachineStatus(String machineIp);
}
