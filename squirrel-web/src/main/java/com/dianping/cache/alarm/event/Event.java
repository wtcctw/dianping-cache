package com.dianping.cache.alarm.event;


import com.dianping.ba.es.qyweixin.adapter.api.service.MessageService;
import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.event.alarmDelayCache.EventCache;
import com.dianping.cache.alarm.receiver.ReceiverService;
import com.dianping.cache.config.ConfigChangeListener;
import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.monitor.Constants;
import com.dianping.cache.util.NetUtil;
import com.dianping.cache.util.RequestUtil;
import com.dianping.mailremote.remote.MailService;
import com.dianping.pigeon.remoting.ServiceFactory;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by lvshiyun on 15/11/27.
 */
public abstract class Event {
    protected static Logger logger = LoggerFactory.getLogger(Event.class);

    private static final ArrayList<String> IPLIST = new ArrayList<String>(){{
        add("10.1.14.104");//线上
        add("10.2.7.129");//ppe
        add("192.168.227.113");//beta
        add("10.128.121.42");//my host
    }};



    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private volatile boolean enableNotify;
    private int emailType;
    private String emailList;
    private int smsType;
    private String smsList;
    private int weixinType;
    private String weixinList;

    private MailService mailService;

    private MessageService weixinService;

    protected ReceiverService receiverService;

    public abstract void setReceiverService(ReceiverService receiverService);

    protected Event() {
        mailService = ServiceFactory.getService("http://service.dianping.com/mailService/mailService_1.0.0", MailService.class);
        //smsService = ServiceFactory.getService("http://service.dianping.com/smsService/smsRemoteService_1.0.0", SMSService.class);
        weixinService = ServiceFactory.getService("http://service.dianping.com/ba/es/qyweixin/adapter/MessageService_1.0.0", MessageService.class);
        enableNotify = configManager.getBooleanValue(Constants.KEY_NOTIFY_ENABLE, Constants.DEFAULT_NOTIFY_ENABLE);
        emailType = configManager.getIntValue(Constants.KEY_NOTIFY_EMAIL_TYPE, Constants.DEFAULT_NOTIFY_EMAIL_TYPE);
        emailList = configManager.getStringValue(Constants.KEY_NOTIFY_EMAIL_LIST, Constants.DEFAULT_NOTIFY_EMAIL_LIST);
        smsType = configManager.getIntValue(Constants.KEY_NOTIFY_SMS_TYPE, Constants.DEFAULT_NOTIFY_SMS_TYPE);
//        smsList = configManager.getStringValue(Constants.KEY_NOTIFY_SMS_LIST, Constants.DEFAULT_NOTIFY_SMS_LIST);
        smsList = configManager.getStringValue("17802118895", "17802118895");
        weixinType = configManager.getIntValue(Constants.KEY_NOTIFY_WEIXIN_TYPE, Constants.DEFAULT_NOTIFY_WEIXIN_TYPE);
//        weixinList = configManager.getStringValue(Constants.KEY_NOTIFY_WEIXIN_LIST, Constants.DEFAULT_NOTIFY_WEIXIN_LIST);
        weixinList = configManager.getStringValue("0021910", "0021910");


        try {
            configManager.registerConfigChangeListener(new ConfigChangeListener() {
                @Override
                public void onChange(String key, String value) {
                    if (Constants.KEY_NOTIFY_ENABLE.equals(key)) {
                        enableNotify = Boolean.parseBoolean(value);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("fail to register config change listener", e);
        }

    }


    protected long checkInterval = 30 * 1000;

    private Date createTime;

    private AlarmType alarmType;

    private EventType eventType;

    public Date getCreateTime() {
        return createTime;
    }

    public Event setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public Event setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Event setEventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public abstract void alarm() throws InterruptedException, URISyntaxException, DocumentException;

    public void sendMessage(AlarmDetail alarmDetail) throws InterruptedException, URISyntaxException, DocumentException {
        logger.info("[sendMessage] AlarmType {}", alarmType);

        try {

            if (isMaster()) {

                if (isAlarm(alarmDetail)) {
                    notify(alarmDetail);
                }
            }

        } catch (Exception e) {
            logger.error(this.getClass().getSimpleName() + e);
        }


    }

    //    public void notify(String title, String message, String receiver) {
    public void notify(AlarmDetail alarmDetail) throws DocumentException, InterruptedException, URISyntaxException {
        String title = alarmDetail.getAlarmTitle();
        String message = alarmDetail.getAlarmDetail();

        String domain = alarmDetail.getClusterName();

        String receiver = alarmDetail.getReceiver();

        boolean sendToBusiness = alarmDetail.isToBusiness();

        if (enableNotify) {
            if (alarmDetail.isMailMode()) {
                notifyEmail(title, message, receiver, domain, sendToBusiness);
            }
//            notifySms(message, receiver);
//            if (isProductEnv()) {
            if (alarmDetail.isSmsMode()) {
                notifySms(message, receiver, domain, sendToBusiness);
            }
            if (alarmDetail.isWeixinMode()) {
                notifyWeixin(message, receiver, domain, sendToBusiness);
            }
//            }
        }
    }

//    private boolean isProductEnv() {
//
//        return "product".equals(Environment.getEnv());
//
//    }

    public boolean notifyEmail(String title, String message, String receiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {

        List<String> emailList = receiverService.getMailReceiver(receiver, domain, sendToBusiness);
        boolean success = true;
        for (String email : emailList) {
            String mailparam = "title=" + title + "&body=" + message + "&recipients=" + email;

            String result = RequestUtil.sendGet("http://web.paas.dp/mail/send", mailparam);

            if (!result.contains("true")) {
                success = false;
                logger.warn("failed to send mail, content:" + message + ",error code:" + result);
            }

        }
        return success;

    }

    public boolean notifySms(String message, String receiver, String domain, boolean sendToBusiness) throws DocumentException, InterruptedException, URISyntaxException {
        Map<String, String> subPair = new HashMap<String, String>();
        subPair.put("body", message);
//        String[] mobiles = smsList.split(",");
        List<String> mobiles = receiverService.getSmsReceiver(receiver, domain, sendToBusiness);

        boolean success = true;
        for (String mobile : mobiles) {
            String smsparam = "mobile=" + mobile + "&body=" + message;

            logger.info("sendsms:" + smsparam);
            String result = RequestUtil.sendGet("http://web.paas.dp/sms/send", smsparam);

            if (!result.contains("200")) {
                success = false;
                logger.warn("failed to send sms, content:" + message + ",error code:" + result);
            }
        }
        return success;
    }

    public boolean notifyWeixin(String message, String receiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {

        List<String> employeeIds = receiverService.getWeiXinReceiver(receiver, domain, sendToBusiness);

        boolean success = true;

        for (String employeeId : employeeIds) {
            String wechatparam = "keyword=" + employeeId + "&title=" + message + "&content=" + message;

            String result = RequestUtil.sendGet("http://web.paas.dp/wechat/send", wechatparam);

            if (!result.contains("true")) {
                success = false;
                logger.warn("failed to send weixin, content:" + message + ",error code:" + result);
            }
        }
        return success;

    }


    private boolean isAlarm(AlarmDetail alarmDetail) {

        EventCache eventCache = EventCache.getInstance();

        eventCache.put(alarmDetail);

        return eventCache.check(alarmDetail);
    }


    public boolean isMaster(){
        boolean isMaster = false;
        try {
            List<String> ip= NetUtil.getAllLocalIp();
            ip.retainAll(IPLIST);
            if(ip.size() > 0)
                isMaster = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isMaster;
    }
}