package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/10.
 */
public class AlarmConfig {

    private int id = -1;

    private String clusterType;

    private String clusterName;

    private String alarmTemplate;

    private String receiver;

    private boolean toBusiness;

    private Date createTime;

    private Date updateTime;

    @Autowired
    MemcacheAlarmTemplateService memcacheAlarmTemplateService;

    @Autowired
    RedisAlarmTemplateService redisAlarmTemplateService;

    public AlarmConfig(){

    }

    public AlarmConfig(String clusterType, String clusterName){

        this.setId(0);

        this.setClusterType(clusterType)
                .setClusterName(clusterName);
        if("Memcache".equals(clusterType)){
            this.setAlarmTemplate("Default");
        }else if("Redis".equals(clusterType)){
            this.setAlarmTemplate("Default");
        }
        this.setReceiver("shiyun.lv,xiaoxiong.dai")
                .setToBusiness(false)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());

    }

    public int getId() {
        return id;
    }

    public AlarmConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getClusterType() {
        return clusterType;
    }

    public AlarmConfig setClusterType(String clusterType) {
        this.clusterType = clusterType;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public AlarmConfig setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getAlarmTemplate() {
        return alarmTemplate;
    }

    public void setAlarmTemplate(String alarmTemplate) {
        this.alarmTemplate = alarmTemplate;
    }

    public String getReceiver() {
        return receiver;
    }

    public AlarmConfig setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public boolean isToBusiness() {
        return toBusiness;
    }

    public AlarmConfig setToBusiness(boolean toBusiness) {
        this.toBusiness = toBusiness;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public AlarmConfig setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public AlarmConfig setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
