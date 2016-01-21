package com.dianping.squirrel.monitor.collector;

import com.dianping.squirrel.monitor.data.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ZabbixCollector extends AbstractCollector {

    private Map<String,String> hostMap = new HashMap<String, String>();

    private final String ZABBIX_COLLECTOR_API = "http://z.dp/api_jsonrpc.php";

    private final String AUTH_USERNAME = "dp.wang";

    private final String AUTH_PASSWORD = "vivi520882";

    @Scheduled(cron = "10/40 * * * * *")
    public void scheduled(){
        if(isLeader() && isProductEnv()){
//            Data data = collectData();
//            dataManager.addData(data);
        }
    }

    private Data collectData(){
        Data data = new Data();
        data.setType(Data.DataType.ZabbixStats);

        return data;
    }

    @Scheduled(cron = "0 0 1 * * *")
    private void refreshHostMap(){

    }

}
