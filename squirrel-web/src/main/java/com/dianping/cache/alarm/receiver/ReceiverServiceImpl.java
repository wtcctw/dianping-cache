package com.dianping.cache.alarm.receiver;

import com.dianping.ba.hris.md.api.dto.EmployeeDto;
import com.dianping.ba.hris.md.api.service.EmployeeService;
import com.dianping.cache.alarm.utils.DateUtil;
import com.dianping.cache.util.CollectionUtils;
import com.dianping.ops.cmdb.CmdbManager;
import com.dianping.ops.cmdb.CmdbProject;
import com.dianping.ops.cmdb.CmdbResult;
import com.dianping.ops.http.HttpConfig;
import com.dianping.ops.http.HttpGetter;
import com.dianping.ops.http.HttpResult;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lvshiyun on 15/12/11.
 */
@Component("receiverService")
public class ReceiverServiceImpl implements ReceiverService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EmployeeService employeeService;

    public List<String> getSmsReceiver(String smsReceiver, String domain, boolean sendToBusiness) throws URISyntaxException, DocumentException, InterruptedException {

        List<String> defalutReceiver = null;
        if (sendToBusiness) {
            defalutReceiver = getDefaultReceiver(domain);
        }

        List<String> adReceiverList = CollectionUtils.toList(smsReceiver, ",");
        if ((null != defalutReceiver)&&sendToBusiness) {
            adReceiverList.addAll(defalutReceiver);
        }
        List<String> smsReceiverList = new ArrayList<String>();

        for (String receiver : adReceiverList) {
            try {
                List<EmployeeDto> userDtoList = employeeService.queryEmployeeByKeyword(receiver);
                smsReceiverList.add(userDtoList.get(0).getMobileNo());
            } catch (Exception e) {
                logger.error("sms receiver " + receiver + "not found" + e);
            }

        }


        return smsReceiverList;

    }

    public List<String> getWeiXinReceiver(String weiXinReceiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {

        List<String> defalutReceiver = null;
        if (sendToBusiness) {
            defalutReceiver = getDefaultReceiver(domain);
        }

        List<String> adReceiverList = CollectionUtils.toList(weiXinReceiver, ",");

        if ((null != defalutReceiver)&&sendToBusiness) {
            adReceiverList.addAll(defalutReceiver);
        }
        List<String> weiXinReceiverList = new ArrayList<String>();

        for (String receiver : adReceiverList) {
            try {
                List<EmployeeDto> userDtoList = employeeService.queryEmployeeByKeyword(receiver);
                weiXinReceiverList.add(userDtoList.get(0).getEmployeeId());
            } catch (Exception e) {
                logger.error("weixin receiver " + receiver + "not found" + e);
            }

        }

        return weiXinReceiverList;

    }

    public List<String> getMailReceiver(String mailReceiver, String domain, boolean sendToBusiness) throws InterruptedException, DocumentException, URISyntaxException {

        List<String> defalutReceiver = null;
        if (sendToBusiness) {
            defalutReceiver = getDefaultReceiver(domain);
        }

        List<String> adReceiverList = CollectionUtils.toList(mailReceiver, ",");

        if ((null != defalutReceiver)&&sendToBusiness) {
            adReceiverList.addAll(defalutReceiver);
        }
        List<String> mailReceiverList = new ArrayList<String>();

        for (String receiver : adReceiverList) {
            try {
                List<EmployeeDto> userDtoList = employeeService.queryEmployeeByKeyword(receiver);
                mailReceiverList.add(userDtoList.get(0).getEmail());
            } catch (Exception e) {
                logger.error("mail receiver " + receiver + "not found" + e);
            }

        }

        return mailReceiverList;

    }

    List<String> getDefaultReceiver(String domain) throws URISyntaxException, InterruptedException, DocumentException {
        List<String> defaultReceiverList = new ArrayList<String>();
        List<String> projectList = new ArrayList<String>();

        Date now = new Date();
        String nowText = DateUtil.getCatHourString(now);

        URI uri = new URIBuilder().setPath("http://cat.dianpingoa.com/cat/r/storage")
                .setParameter("op", "view").setParameter("type", "Cache")
                .setParameter("domain", "cat").setParameter("id", domain)
                .setParameter("date", nowText).setParameter("forceDownload", "xml").build();

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setRedirect(false);
        httpConfig.setTimeout(10000);
        HttpGetter HTTP_GETTER = HttpGetter.create(httpConfig);

        HttpResult httpResult = HTTP_GETTER.getWithoutException(uri);
        int count = 0;
        while ((!httpResult.isSuccess || httpResult.response == null) && (count < 5)) {
            count++;
            Thread.sleep(10000);
            httpResult = HTTP_GETTER.getWithoutException(uri);
        }
        if (httpResult.response.trim().length() == 0) {
            return null;
        }


        Document document = DocumentHelper.parseText(httpResult.response);
        org.dom4j.Element rootElement = document.getRootElement();

        org.dom4j.Element reportElement = rootElement.element("originalReport");
        if (null == reportElement) {
            return null;
        }
        List<org.dom4j.Element> machineElement = reportElement.elements("machine");
        if (null == machineElement) {
            return null;
        }
        for (int i = 0; i < machineElement.size(); i++) {
            List<org.dom4j.Element> elements = machineElement.get(i).elements();
            if (null == elements) {
                return null;
            }

            for (int j = 0; j < elements.size(); j++) {
                org.dom4j.Element element = elements.get(j);

                Attribute e = element.attribute("id");

                projectList.add(e.getValue());
            }

            for (String project : projectList) {

                CmdbResult<CmdbProject> result = CmdbManager.getProject(project);
                if (null != result.cmdbResult) {
                    String receiver = result.cmdbResult.getRd_duty();
                    if (!defaultReceiverList.contains(receiver)) {
                        defaultReceiverList.add(receiver);
                    }
                }


            }
        }
        return defaultReceiverList;
    }


    public EmployeeService getEmployeeService() {
        return employeeService;
    }

    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
}
