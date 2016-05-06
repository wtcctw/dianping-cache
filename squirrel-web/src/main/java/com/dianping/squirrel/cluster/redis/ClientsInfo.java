package com.dianping.squirrel.cluster.redis;

import org.apache.commons.lang.math.NumberUtils;

import java.util.List;
import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class ClientsInfo extends AbstractInfo {

    public ClientsInfo() {
        this.infoSegmentName = "clients";
    }

    public ClientsInfo(Map<String,String> infoMap){
        this.blocked_clients = NumberUtils.toInt(infoMap.get("blocked_clients"),0);
        this.client_biggest_input_buf = NumberUtils.toInt(infoMap.get("client_biggest_input_buf"),0);
        this.client_longest_output_list = NumberUtils.toInt(infoMap.get("client_longest_output_list"),0);
        this.connected_clients = NumberUtils.toInt(infoMap.get("connected_clients"),0);
    }

    private int connected_clients;
    private int client_longest_output_list;
    private int client_biggest_input_buf;
    private int blocked_clients;
    private List<Client> clientList;

    public int getConnected_clients() {
        return connected_clients;
    }

    public void setConnected_clients(int connected_clients) {
        this.connected_clients = connected_clients;
    }

    public int getClient_longest_output_list() {
        return client_longest_output_list;
    }

    public void setClient_longest_output_list(int client_longest_output_list) {
        this.client_longest_output_list = client_longest_output_list;
    }

    public int getClient_biggest_input_buf() {
        return client_biggest_input_buf;
    }

    public void setClient_biggest_input_buf(int client_biggest_input_buf) {
        this.client_biggest_input_buf = client_biggest_input_buf;
    }

    public int getBlocked_clients() {
        return blocked_clients;
    }

    public void setBlocked_clients(int blocked_clients) {
        this.blocked_clients = blocked_clients;
    }

    public List<Client> getClientList() {
        return clientList;
    }

    public void setClientList(List<Client> clientList) {
        this.clientList = clientList;
    }

    @Override
    public String toString() {
        return "ClientsInfo{" +
                "connected_clients=" + connected_clients +
                ", client_longest_output_list=" + client_longest_output_list +
                ", client_biggest_input_buf=" + client_biggest_input_buf +
                ", blocked_clients=" + blocked_clients +
                ", clientList=" + clientList +
                '}';
    }
}

