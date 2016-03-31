package com.dianping.cache.monitor;

public interface Constants {
    
    String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";

    String SERVICE_PATH = "/dp/cache/service";
    String MEMBER_PATH = "/dp/cache/cache-server";
    String MONITOR_PATH = "/dp/cache/monitor";

    String KEY_MONITOR_ENABLE = "avatar-cache.monitor.enable";
    boolean DEFAULT_MONITOR_ENABLE = true;
    
    String KEY_MONITOR_MEMBER_MINIMUM = "avatar-cache.monitor.member.minimum";
    int DEFAULT_MONITOR_MEMBER_MINIMUM = 2;
    String KEY_MONITOR_MEMBER_PERCENT = "avatar-cache.monitor.member.percent";
    float DEFAULT_MONITOR_MEMBER_PERCENT = 1.0F;
    String KEY_MONITOR_ENABLE_OFFLINE = "avatar-cache.monitor.enable.offline";
    boolean DEFAULT_MONITOR_ENABLE_OFFLINE = false;
    String KEY_MONITOR_ENABLE_ONLINE = "avatar-cache.monitor.enable.online";
    boolean DEFAULT_MONITOR_ENABLE_ONLINE = false;
    
    String KEY_DEAD_THRESHOLD = "avatar-cache.monitor.dead.threshold";
    int DEFAULT_DEAD_THRESHOLD = 5;
    String KEY_LIVE_THRESHOLD = "avatar-cache.monitor.live.threshold";
    int DEFAULT_LIVE_THRESHOLD = 5;
    
    String KEY_MONITOR_SERVER_MINIMUM = "avatar-cache.monitor.server.minimum";
    int DEFAULT_MONITOR_SERVER_MINIMUM = 1;
    String KEY_MONITOR_SERVER_PERCENT = "avatar-cache.monitor.server.percent";
    float DEFAULT_MONITOR_SERVER_PERCENT = 0.6F;

    String KEY_MONITOR_INTERVAL = "avatar-cache.monitor.interval";
    int DEFAULT_MONITOR_INTERVAL = 5000; // milliseconds
    String KEY_MONITOR_ENABLE_STATS = "avatar-cache.monitor.stats.enble";
    boolean DEFAULT_MONITOR_ENABLE_STATS = false;
    
    String KEY_NOTIFY_ENABLE = "avatar-cache.notify.enable";
    boolean DEFAULT_NOTIFY_ENABLE = true;
    String KEY_NOTIFY_EMAIL_TYPE = "avatar-cache.notify.email.type";
    int DEFAULT_NOTIFY_EMAIL_TYPE = 15;// 架构中间件异常告警邮件
    String KEY_NOTIFY_EMAIL_LIST = "avatar-cache.notify.email.list";
    String DEFAULT_NOTIFY_EMAIL_LIST = "dp.wang@dianping.com,faping.miao@dianping.com,xiaoxiong.dai@dianping.com," +
    		"xiang.wu@dianping.com,enlight.chen@dianping.com,shiyun.lv@dianping.com";
    String KEY_NOTIFY_SMS_TYPE = "avatar-cache.notify.sms.type";
    int DEFAULT_NOTIFY_SMS_TYPE = 801;// 架构监控通知短信$$body#$
    String KEY_NOTIFY_SMS_LIST = "avatar-cache.notify.sms.list";
    String DEFAULT_NOTIFY_SMS_LIST = "18721794573,13501702948,18918371840,18616210562,18917002059,17802118895";
    String KEY_NOTIFY_WEIXIN_TYPE = "avatar-cache.notify.weixin.type";
    int DEFAULT_NOTIFY_WEIXIN_TYPE = 54;// 运维平台
    String KEY_NOTIFY_WEIXIN_LIST = "avatar-cache.notify.weixin.list";
    String DEFAULT_NOTIFY_WEIXIN_LIST = "0023415,0000558,0014852,0005503,0003769,0021910";

}
