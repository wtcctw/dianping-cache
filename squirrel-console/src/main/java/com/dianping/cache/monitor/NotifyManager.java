package com.dianping.cache.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.ba.es.qyweixin.adapter.api.dto.MessageDto;
import com.dianping.ba.es.qyweixin.adapter.api.dto.media.TextDto;
import com.dianping.ba.es.qyweixin.adapter.api.exception.QyWeixinAdaperException;
import com.dianping.ba.es.qyweixin.adapter.api.service.MessageService;
import com.dianping.cache.util.CollectionUtils;
import com.dianping.lion.Environment;
import com.dianping.mailremote.remote.MailService;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.sms.biz.SMSService;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class NotifyManager {

    private static Logger logger = LoggerFactory.getLogger(NotifyManager.class);
    
    private static class SingletonHolder {
        private static final NotifyManager INSTANCE = new NotifyManager();
    }
    
    public static NotifyManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private volatile boolean enableNotify;
    private int emailType;
    private String emailList;
    private int smsType;
    private String smsList;
    private int weixinType;
    private String weixinList;
    
    private MailService mailService;
    
    private SMSService smsService;
    
    private MessageService weixinService;
    
    private NotifyManager() {
        mailService = ServiceFactory.getService("http://service.dianping.com/mailService/mailService_1.0.0", MailService.class);
        smsService = ServiceFactory.getService("http://service.dianping.com/smsService/smsRemoteService_1.0.0", SMSService.class);
        weixinService = ServiceFactory.getService("http://service.dianping.com/ba/es/qyweixin/adapter/MessageService_1.0.0", MessageService.class);
        enableNotify = configManager.getBooleanValue(Constants.KEY_NOTIFY_ENABLE, Constants.DEFAULT_NOTIFY_ENABLE);
        emailType = configManager.getIntValue(Constants.KEY_NOTIFY_EMAIL_TYPE, Constants.DEFAULT_NOTIFY_EMAIL_TYPE);
        emailList = configManager.getStringValue(Constants.KEY_NOTIFY_EMAIL_LIST, Constants.DEFAULT_NOTIFY_EMAIL_LIST);
        smsType = configManager.getIntValue(Constants.KEY_NOTIFY_SMS_TYPE, Constants.DEFAULT_NOTIFY_SMS_TYPE);
        smsList = configManager.getStringValue(Constants.KEY_NOTIFY_SMS_LIST, Constants.DEFAULT_NOTIFY_SMS_LIST);
        weixinType = configManager.getIntValue(Constants.KEY_NOTIFY_WEIXIN_TYPE, Constants.DEFAULT_NOTIFY_WEIXIN_TYPE);
        weixinList = configManager.getStringValue(Constants.KEY_NOTIFY_WEIXIN_LIST, Constants.DEFAULT_NOTIFY_WEIXIN_LIST);
        
        try {
            configManager.registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(Constants.KEY_NOTIFY_ENABLE.equals(key)) {
                        enableNotify = Boolean.parseBoolean(value);
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
        
    }
    
    public void notify(String title, String message) {
        if(enableNotify) {
            notifyEmail(title, message);
            if(isProductEnv()) {
                notifySms(message);
                notifyWeixin(message);
            }
        }
    }

    private boolean isProductEnv() {
        return "product".equals(Environment.getEnv());
    }

    public boolean notifyEmail(String title, String message) {
        Map<String,String> subPair = new HashMap<String, String>();
        subPair.put("title", title);
        subPair.put("body", message);
        boolean result = mailService.send(emailType, CollectionUtils.toList(emailList, ","), subPair, "");
        if(!result) {
            logger.warn("failed to send email, content: " + message);
        }
        return result;
    }
    
    public boolean notifySms(String message) {
        Map<String,String> subPair = new HashMap<String, String>();
        subPair.put("body", message);
        String[] mobiles = smsList.split(",");
        boolean success = true;
        for(String mobile : mobiles) {
            int result = smsService.send(smsType, mobile, subPair);
            if(result != 200) {
                success = false;
                logger.warn("failed to send sms, content: " + message + ", error code: " + result);
            }
        }
        return success;
    }
    
    public void notifyWeixin(String message) {
        TextDto text = new TextDto();
        text.setContent(message);
        List<String> users = new ArrayList<String>();
        users.addAll(CollectionUtils.toList(weixinList, ","));
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

}
