package com.dianping.squirrel.cluster.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class ReplicationInfo extends AbstractInfo{
    private String role;
    //master
    private int connected_slaves;
    private long master_repl_offset;
    private List<Slave> slaves;

    //slave
    private String master_host;
    private int master_port;

    //common
    private int repl_backlog_active;
    private long repl_backlog_size;
    private long repl_backlog_first_byte_offset;
    private long repl_backlog_histlen;

    public ReplicationInfo(Map<String,String> infoMap){
        role = infoMap.get("role");
        repl_backlog_active = Integer.parseInt(infoMap.get("repl_backlog_active"));
        repl_backlog_size = Long.parseLong(infoMap.get("repl_backlog_size"));
        repl_backlog_first_byte_offset = Long.parseLong(infoMap.get("repl_backlog_first_byte_offset"));
        repl_backlog_histlen = Long.parseLong(infoMap.get("repl_backlog_histlen"));

        if("master".equalsIgnoreCase(role)){
            connected_slaves = Integer.parseInt(infoMap.get("connected_slaves"));
            master_repl_offset = Long.parseLong(infoMap.get("master_repl_offset"));

            for(int i = 0; i < connected_slaves; i++){
                String slaveStr = infoMap.get("slave"+i);
                if(slaveStr != null){
                    String[] infoArr = slaveStr.split(",");
                    Map<String, String> data = new HashMap<String, String>();
                    for(String infoPair : infoArr){
                        String[] pair = infoPair.split("=");
                        if(pair.length > 1){
                            data.put(pair[0],pair[1]);
                        }
                    }
                    if(slaves == null){
                        slaves = new ArrayList<Slave>();
                    }
                    Slave slave = new Slave();
                    slave.setIp(data.get("ip"));
                    slave.setLag(Integer.parseInt(data.get("lag")));
                    slave.setOffset(Long.parseLong(data.get("offset")));
                    slave.setPort(Integer.parseInt(data.get("port")));
                    slave.setState(data.get("state"));
                    slaves.add(slave);
                }
            }
        }else {
            master_host = infoMap.get("master_host");
            master_port = Integer.parseInt(infoMap.get("master_port"));
        }


    }
    public String getRole() {
        return role;
    }

    public int getConnected_slaves() {
        return connected_slaves;
    }

    public long getMaster_repl_offset() {
        return master_repl_offset;
    }

    public int getRepl_backlog_active() {
        return repl_backlog_active;
    }

    public long getRepl_backlog_size() {
        return repl_backlog_size;
    }

    public long getRepl_backlog_first_byte_offset() {
        return repl_backlog_first_byte_offset;
    }

    public long getRepl_backlog_histlen() {
        return repl_backlog_histlen;
    }

    public List<Slave> getSlaves() {
        return slaves;
    }

    public String getMaster_host() {
        return master_host;
    }

    public int getMaster_port() {
        return master_port;
    }

    @Override
    public String toString() {
        return "ReplicationInfo{" +
                "role='" + role + '\'' +
                ", connected_slaves=" + connected_slaves +
                ", master_repl_offset=" + master_repl_offset +
                ", slaves=" + slaves +
                ", master_host='" + master_host + '\'' +
                ", master_port=" + master_port +
                ", repl_backlog_active=" + repl_backlog_active +
                ", repl_backlog_size=" + repl_backlog_size +
                ", repl_backlog_first_byte_offset=" + repl_backlog_first_byte_offset +
                ", repl_backlog_histlen=" + repl_backlog_histlen +
                '}';
    }
}
