package com.dianping.cache.alarm.event;


import com.dianping.ba.es.qyweixin.adapter.api.dto.MessageDto;
import com.dianping.ba.es.qyweixin.adapter.api.dto.media.TextDto;
import com.dianping.ba.es.qyweixin.adapter.api.exception.QyWeixinAdaperException;
import com.dianping.ba.es.qyweixin.adapter.api.service.MessageService;
import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.event.alarmDelayCache.EventCache;
import com.dianping.cache.alarm.receiver.ReceiverService;
import com.dianping.cache.config.ConfigChangeListener;
import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.monitor.Constants;
import com.dianping.cache.util.RequestUtil;
import com.dianping.lion.Environment;
import com.dianping.mailremote.remote.MailService;
import com.dianping.pigeon.remoting.ServiceFactory;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by lvshiyun on 15/11/27.
 */
public abstract class Event {
    protected static Logger logger = LoggerFactory.getLogger(Event.class);


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

        InetAddress inetAddress = null;

        try {
            inetAddress = inetAddress.getLocalHost();

            String localip = inetAddress.getHostAddress();
            //线上、beta环境机器
            if ("10.1.14.104".equals(localip)||"192.168.227.113".equals(localip)) {

                if (isAlarm(alarmDetail)) {
                    notify(alarmDetail);
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
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

    private boolean isProductEnv() {

        return "product".equals(Environment.getEnv());

    }

    public boolean notifyEmail(String title, String message, String receiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {

        Map<String, String> subPair = new HashMap<String, String>();
        subPair.put("title", title);
        subPair.put("body", message);

        List<String> emailList = receiverService.getMailReceiver(receiver, domain, sendToBusiness);

        boolean result = mailService.send(emailType, emailList, subPair, "");

        if (!result) {
            logger.warn("fail to send email, content:" + message);
        }
        return result;
    }

    public boolean notifySms(String message, String receiver, String domain, boolean sendToBusiness) throws DocumentException, InterruptedException, URISyntaxException {
        Map<String, String> subPair = new HashMap<String, String>();
        subPair.put("body", message);
//        String[] mobiles = smsList.split(",");
        List<String> mobiles = receiverService.getSmsReceiver(receiver, domain, sendToBusiness);

        boolean success = true;
        for (String mobile : mobiles) {
            String smsparam = "mobile=" + mobile + "&body=" + message;
            String result = RequestUtil.sendGet("http://web.paas.dp/sms/send", smsparam);

            if (!result.contains("200")) {
                success = false;
                logger.warn("failed to send sms, content:" + message + ",error code:" + result);
            }
        }
        return success;
    }

    public void notifyWeixin(String message, String receiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {
        TextDto text = new TextDto();
        text.setContent(message);
        List<String> users = new ArrayList<String>();
//        users.addAll(CollectionUtils.toList(weixinList, ","));
        users.addAll(receiverService.getWeiXinReceiver(receiver, domain, sendToBusiness));
        MessageDto messageDto = new MessageDto();
        messageDto.setAgentid(weixinType);
        messageDto.setMediaDto(text);
        messageDto.setTouser(users);
        messageDto.setSafe(0);
        try {
            weixinService.sendMessage(messageDto);
        } catch (QyWeixinAdaperException e) {
            logger.error("failed to send weixin, content: " + message, e);
        }
    }


    private boolean isAlarm(AlarmDetail alarmDetail) {

        EventCache eventCache = EventCache.getInstance();

        eventCache.put(alarmDetail);

        return eventCache.check(alarmDetail);
    }
}