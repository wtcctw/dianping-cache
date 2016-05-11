package com.dianping.squirrel.service;

import com.dianping.squirrel.vo.ScaleParams;
import com.dianping.squirrel.vo.hulk.ScaleResult;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public interface HulkApiService {
    int scaleOut(ScaleParams params);
    ScaleResult scaleOutResult(ScaleParams params,int rescode);

//    int scaleInInstance(ScaleParams params);
//    List<Machine> getMachines();
//    List<MachineStatusBean> getMachineStatus();
//    MachineStatusBean getMachineStatus(String machineIp);
}
