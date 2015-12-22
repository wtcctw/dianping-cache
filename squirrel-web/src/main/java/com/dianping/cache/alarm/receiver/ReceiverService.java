package com.dianping.cache.alarm.receiver;

import org.dom4j.DocumentException;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by lvshiyun on 15/12/12.
 */
public interface ReceiverService {

    public List<String> getSmsReceiver(String smsReceiver,String domain) throws URISyntaxException, DocumentException, InterruptedException;

    public List<String> getWeiXinReceiver(String weiXinReceiver, String domain) throws InterruptedException, DocumentException, URISyntaxException;

    public List<String> getMailReceiver(String mailReceiver, String domain) throws InterruptedException, DocumentException, URISyntaxException;
}
