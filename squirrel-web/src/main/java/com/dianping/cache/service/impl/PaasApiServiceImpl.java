package com.dianping.cache.service.impl;

import com.dianping.cache.scale.instance.docker.paasbean.Machine;
import com.dianping.cache.scale.instance.docker.paasbean.MachineStatusBean;
import com.dianping.cache.service.PaasApiService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.JsonUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/2/17.
 */
@Service
public class PaasApiServiceImpl implements PaasApiService{

    private static final Logger logger = LoggerFactory.getLogger(PaasApiServiceImpl.class);

    private final String PASSMACHINE_STATUS_API_PATTERN = "machine/status?machine_ip=%s";

    private final String DEFAULT_PAASMACHINEURL = "http://10.3.21.21:8080/api/v1/machines/";

    private String paasUrl = ConfigManagerLoader.getConfigManager().getStringValue("squirrel-web.paas.machines.url",DEFAULT_PAASMACHINEURL);

    private volatile List<Machine> staticMachineList;

    private Map<String,MachineStatusBean> machineMap = new ConcurrentHashMap<String, MachineStatusBean>();

    private PaasMachineSyncThread syncThread = new PaasMachineSyncThread();

    @PostConstruct
    public void init() throws IOException, SQLException {
        List<String> ipList = new ArrayList<String>();

        staticMachineList = getStaticMachinesFromPaas();
        checkNotNull(staticMachineList);

        for (Machine machine : staticMachineList) {
            ipList.add(machine.getIp());
        }

        Map<String,MachineStatusBean> machineStatus = getMachineStatusFromPaas(StringUtils.join(ipList, ","));
        checkNotNull(machineStatus);

        for (MachineStatusBean entity : machineStatus.values()) {
            machineMap.put(entity.getIp(), entity);
        }

        syncThread.setName("paas_machine_syncthread");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private List<Machine> getStaticMachinesFromPaas() throws IOException {
        String jsonString = RequestUtil.sendGet(paasUrl+"/static",null);
        Machine[] machines = JsonUtils.fromStr(jsonString,Machine[].class);

        return Arrays.asList(machines);
    }

    private Map<String,MachineStatusBean> getMachineStatusFromPaas(String ip) throws IOException, IllegalArgumentException {
        String machineStatusUrl = String.format(paasUrl + PASSMACHINE_STATUS_API_PATTERN,ip);
        String jsonString = RequestUtil.sendGet(machineStatusUrl, null);
        checkArgument(jsonString != null && !"".equals(jsonString), "sendGet error url=" + machineStatusUrl);

        Map<String,MachineStatusBean> result = JsonUtils.fromStr(jsonString, new TypeReference<HashMap<String,MachineStatusBean>>(){});

        return result;
    }

    @Override
    public List<Machine> getMachines() {
        List<Machine> result = new ArrayList<Machine>();

        for(Machine machine : staticMachineList) {
            result.add(copyPaasMachine(machine));
        }

        return result;
    }

    @Override
    public List<MachineStatusBean> getMachineStatus() {
        List<MachineStatusBean> result = new ArrayList<MachineStatusBean>();

        for(MachineStatusBean entity : machineMap.values()) {
            result.add(entity);
        }

        return result;
    }

    @Override
    public MachineStatusBean getMachineStatus(String machineIp){
       return machineMap.get(machineIp);
    }

    private Machine copyPaasMachine(Machine bean){
        Machine machine = new Machine();
        try {
            BeanUtils.copyProperties(machine,bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return machine;
    }

    public class PaasMachineSyncThread extends Thread {

        @Override
        public void run() {
            while (!interrupted()) {
                // 每6秒进行一次同步
                try {
                    TimeUnit.SECONDS.sleep(6);
                } catch (InterruptedException e) {
                    return;
                }

                doSync();
            }
        }

        private void doSync() {
            List<String> ipList = new ArrayList<String>();

            try {
                List<Machine> newStaticMachineList = getStaticMachinesFromPaas();
                checkNotNull(newStaticMachineList);

                staticMachineList = newStaticMachineList;
                Set<String> newIpSet = new HashSet<String>();

                for(Machine machine : staticMachineList) {
                    ipList.add(machine.getIp());
                    newIpSet.add(machine.getIp());
                }

                Iterator<Map.Entry<String, MachineStatusBean>> iterator = machineMap.entrySet().iterator();

                while(iterator.hasNext()) {
                    Map.Entry<String, MachineStatusBean> entry = iterator.next();

                    if(!newIpSet.contains(entry.getKey())) {
                        iterator.remove();
                    }
                }
            } catch (Exception e) {
                logger.error("get static machine fail :", e);
                return;
            }

            try {
                Map<String,MachineStatusBean> machineStatus = getMachineStatusFromPaas(StringUtils.join(ipList, ","));
                checkNotNull(machineStatus);

                for(MachineStatusBean newNode : machineStatus.values()) {
                    machineMap.put(newNode.getIp(), newNode);
                }
             } catch (Exception e) {
                logger.error("get dynamic machine fail :", e);
            }
        }
    }
}
