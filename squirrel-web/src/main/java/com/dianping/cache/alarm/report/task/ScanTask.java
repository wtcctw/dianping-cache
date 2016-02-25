package com.dianping.cache.alarm.report.task;

import com.dianping.ba.hris.md.api.dto.EmployeeDto;
import com.dianping.ba.hris.md.api.service.EmployeeService;
import com.dianping.cache.alarm.email.SpringMailSender;
import com.dianping.cache.alarm.entity.ScanDetail;
import com.dianping.cache.alarm.report.scanService.ScanDetailService;
import com.dianping.cache.alarm.utils.DateUtil;
import com.dianping.cache.entity.CategoryToApp;
import com.dianping.cache.service.CategoryToAppService;
import com.dianping.ops.cmdb.CmdbManager;
import com.dianping.ops.cmdb.CmdbProject;
import com.dianping.ops.cmdb.CmdbResult;
import com.dianping.ops.http.HttpConfig;
import com.dianping.ops.http.HttpGetter;
import com.dianping.ops.http.HttpResult;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by lvshiyun on 16/1/12.
 */
@Component("scanTask")
@Scope("prototype")
public class ScanTask {
    private static final long MS_PER_HOUR = 1000 * 60 * 60;
    private static final long MS_PER_DAY = MS_PER_HOUR * 24;

    protected static Logger logger = LoggerFactory.getLogger(ScanTask.class);

    @Autowired
    ScanDetailService scanDetailService;

    @Autowired
    CategoryToAppService categoryToAppService;

    @Autowired
    private EmployeeService employeeService;


    public void run() throws InterruptedException, DocumentException, URISyntaxException, MessagingException {
        logger.info("ScanTask run");

        List<ScanDetail> scanDetails = AlarmScanDetails();

        saveToDb(scanDetails);

        Map<String, List<ScanDetail>> detail = dealScanDetal(scanDetails);

        String rdReceiver = "rdTeam";

        sendMail(detail.get("delayDetailLists"), detail.get("failDetailLists"), rdReceiver);


        //按项目将异常分类
        Map<String, List<ScanDetail>> diffScanDetails = splitScanDetails(scanDetails);

        logger.info("ScanTask SendEmail");
        for (Map.Entry<String, List<ScanDetail>> entry : diffScanDetails.entrySet()) {

            String receiverEmail;
            if("not found".equals(entry.getKey())){
                receiverEmail ="rdTeam";
            }else {

                CmdbResult<CmdbProject> result = CmdbManager.getProject(entry.getKey());
                String receiver;
                if (null == result || null == result.cmdbResult) {
                    receiver = "shiyun.lv";
                } else {
                    receiver = result.cmdbResult.getRd_duty();
                }
                List<EmployeeDto> userDtoList = employeeService.queryEmployeeByKeyword(receiver);

                if (0 == userDtoList.size()) {
                    receiverEmail = "shiyun.lv@dianping.com";
                } else {
                    receiverEmail = userDtoList.get(0).getEmail();
                }
            }
            Map<String, List<ScanDetail>> detailMap = dealScanDetal(entry.getValue());

            if ((detailMap.get("delayDetailLists").size() > 0) || (detailMap.get("failDetailLists").size() > 0)) {

                sendMail(detailMap.get("delayDetailLists"), detailMap.get("failDetailLists"), receiverEmail);
            }
        }
    }

    private void saveToDb(List<ScanDetail> scanDetails) {
        Map<String, ScanDetail> dealedDetails = new HashMap<String, ScanDetail>();


        for (ScanDetail scanDetail : scanDetails) {
            String name = scanDetail.getCacheName() + scanDetail.getProject();

            if (null != dealedDetails.get(name)) {
                int total = dealedDetails.get(name).getTotalCount() + scanDetail.getTotalCount();
                int failure = dealedDetails.get(name).getFailCount() + scanDetail.getFailCount();
                double failurePercent = failure / total;
                double min = Math.min(dealedDetails.get(name).getMinVal(), scanDetail.getMinVal());
                double max = Math.max(dealedDetails.get(name).getMaxVal(), scanDetail.getMaxVal());
                double avg = (dealedDetails.get(name).getAvgVal() * dealedDetails.get(name).getTotalCount() +
                        scanDetail.getAvgVal() * scanDetail.getTotalCount()) / (dealedDetails.get(name).getTotalCount()
                        + scanDetail.getTotalCount());

                dealedDetails.get(name).setTotalCount(total)
                        .setFailCount(failure)
                        .setFailPercent(failurePercent)
                        .setMinVal(min)
                        .setMaxVal(max)
                        .setAvgVal(avg);

            } else {
                dealedDetails.put(name, scanDetail);
            }
        }

        for (Map.Entry<String, ScanDetail> entry : dealedDetails.entrySet()) {
            scanDetailService.insert(entry.getValue());
        }
    }

    private Map<String, List<ScanDetail>> splitScanDetails(List<ScanDetail> scanDetails) {
        Map<String, List<ScanDetail>> diffScanDetailMap = new HashMap<String, List<ScanDetail>>();

        for(ScanDetail scanDetail:scanDetails){
            scanDetail.setRowspan(0);
        }

        for (ScanDetail scanDetail : scanDetails) {
            String category = scanDetail.getProject().split(":")[0];

            String appName;
            List<CategoryToApp> categoryToAppList = categoryToAppService.findByCategory(category);
            if (0 != categoryToAppList.size()) {
                appName = categoryToAppService.findByCategory(category).get(0).getApplication();
            } else {
                appName = "not found";
            }
            if (null != diffScanDetailMap.get(appName)) {
                diffScanDetailMap.get(appName).add(scanDetail);
            } else {
                List<ScanDetail> list = new ArrayList<ScanDetail>();
                list.add(scanDetail);
                diffScanDetailMap.put(appName, list);
            }
        }

        return diffScanDetailMap;
    }

    Map<String, List<ScanDetail>> dealScanDetal(List<ScanDetail> scanDetails) {
        Map<String, ScanDetail> failDetails = new HashMap<String, ScanDetail>();
        Map<String, ScanDetail> delayDetails = new HashMap<String, ScanDetail>();

        for (ScanDetail scanDetail : scanDetails) {
            String name = scanDetail.getCacheName() + scanDetail.getProject();
            if (scanDetail.getAvgVal() > 10) {
                if (null != delayDetails.get(name)) {
                    int total = delayDetails.get(name).getTotalCount() + scanDetail.getTotalCount();
                    int failure = delayDetails.get(name).getFailCount() + scanDetail.getFailCount();
                    double failurePercent = failure / total;
                    double min = Math.min(delayDetails.get(name).getMinVal(), scanDetail.getMinVal());
                    double max = Math.max(delayDetails.get(name).getMaxVal(), scanDetail.getMaxVal());
                    double avg = (delayDetails.get(name).getAvgVal() * delayDetails.get(name).getTotalCount() +
                            scanDetail.getAvgVal() * scanDetail.getTotalCount()) / (delayDetails.get(name).getTotalCount()
                            + scanDetail.getTotalCount());

                    delayDetails.get(name).setTotalCount(total)
                            .setFailCount(failure)
                            .setFailPercent(failurePercent)
                            .setMinVal(min)
                            .setMaxVal(max)
                            .setAvgVal(avg);

                } else {
                    delayDetails.put(name, scanDetail);
                }

            } else if (scanDetail.getFailPercent() > 0.1) {

                if (null != failDetails.get(name)) {
                    int total = failDetails.get(name).getTotalCount() + scanDetail.getTotalCount();
                    int failure = failDetails.get(name).getFailCount() + scanDetail.getFailCount();
                    double failurePercent = failure / total;
                    double min = Math.min(failDetails.get(name).getMinVal(), scanDetail.getMinVal());
                    double max = Math.max(failDetails.get(name).getMaxVal(), scanDetail.getMaxVal());
                    double avg = (failDetails.get(name).getAvgVal() * failDetails.get(name).getTotalCount() +
                            scanDetail.getAvgVal() * scanDetail.getTotalCount()) / (failDetails.get(name).getTotalCount()
                            + scanDetail.getTotalCount());

                    failDetails.get(name).setTotalCount(total)
                            .setFailCount(failure)
                            .setFailPercent(failurePercent)
                            .setMinVal(min)
                            .setMaxVal(max)
                            .setAvgVal(avg);

                } else {
                    failDetails.put(name, scanDetail);
                }
            }
        }

        List<ScanDetail> failDetailList = new ArrayList<ScanDetail>();
        List<ScanDetail> delayDetailList = new ArrayList<ScanDetail>();

        for (Map.Entry<String, ScanDetail> entry : failDetails.entrySet()) {
            ScanDetail detail = failDetails.get(entry.getKey());

            failDetailList.add(detail);
        }

        for (Map.Entry<String, ScanDetail> entry : delayDetails.entrySet()) {
            ScanDetail detail = delayDetails.get(entry.getKey());
            delayDetailList.add(detail);
        }


        List<ScanDetail> failDetailLists = dealRowSpan(failDetailList);
        List<ScanDetail> delayDetailLists = dealRowSpan(delayDetailList);

        Map<String, List<ScanDetail>> detailMap = new HashMap<String, List<ScanDetail>>();
        detailMap.put("failDetailLists", failDetailLists);
        detailMap.put("delayDetailLists", delayDetailLists);

        return detailMap;
    }

    private List<ScanDetail> dealRowSpan(List<ScanDetail> detailList) {

        Map<String, List<ScanDetail>> detailMap = new HashMap<String, List<ScanDetail>>();

        List<ScanDetail> scanDetailList = new ArrayList<ScanDetail>();

        for (ScanDetail scanDetail : detailList) {
            if (null != detailMap.get(scanDetail.getCacheName())) {
                detailMap.get(scanDetail.getCacheName()).add(scanDetail);
            } else {
                List<ScanDetail> list = new ArrayList<ScanDetail>();
                list.add(scanDetail);
                detailMap.put(scanDetail.getCacheName(), list);
            }
        }

        for (Map.Entry<String, List<ScanDetail>> entry : detailMap.entrySet()) {
            List<ScanDetail> list = detailMap.get(entry.getKey());
            list.get(0).setRowspan(list.size());
            scanDetailList.addAll(list);
        }

        return scanDetailList;

    }


    /**
     *  * 使用Velocity模板发送邮件
     *  *
     *  * @throws MessagingException
     *  
     */

    private void sendMail(List<ScanDetail> delayDetails, List<ScanDetail> failDetails, String receiver) throws MessagingException {

        SpringMailSender mailSender = new SpringMailSender();

        Properties props = System.getProperties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Session session = Session.getInstance(props);

        VelocityEngineFactoryBean v = new VelocityEngineFactoryBean();
        v.setVelocityProperties(props);
        try {
            VelocityEngine velocityEngine = v.createVelocityEngine();

            // 声明Map对象，并填入用来填充模板文件的键值对
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("delayDetails", delayDetails);
            model.put("failDetails", failDetails);
            // Spring提供的VelocityEngineUtils将模板进行数据填充，并转换成普通的String对象
            String emailText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "report.vm", "utf-8", model);
            MimeMessage msg = new MimeMessage(session);

            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(mailSender.getMailSender().getUsername());
            String[] receiverList;
            if ("rdTeam".equals(receiver)) {
                receiverList = new String[]{"shiyun.lv@dianping.com", "xiaoxiong.dai@dianping.com", "dp.wang@dianping.com", "enlight.chen@dianping.com", "xiang.wu@dianping.com", "faping.miao@dianping.com"};
//                receiverList = new String[]{"shiyun.lv@dianping.com"};
            } else {
                receiverList = new String[]{"shiyun.lv@dianping.com", receiver};
//                receiverList = new String[]{"shiyun.lv@dianping.com"};
            }

            helper.setTo(receiverList);
            helper.setSubject("KV红黑榜日报表");

            msg.setContent(emailText, "text/html; charset=UTF-8");

            mailSender.getMailSender().send(msg);
        } catch (VelocityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<ScanDetail> AlarmScanDetails() throws URISyntaxException, InterruptedException, DocumentException {

        List<ScanDetail> scanDetailList = new ArrayList<ScanDetail>();

        Date now = new Date();
        String nowText = DateUtil.getCatDayString(now);

        Date yesterday = new Date(now.getTime() - MS_PER_DAY);

        String yesterdayText = DateUtil.getCatDayString(yesterday);

        URI uri = new URIBuilder().setPath("http://cat.dianpingoa.com/cat/r/t")
                .setParameter("op", "history").setParameter("domain", "All")
                .setParameter("date", yesterdayText).setParameter("forceDownload", "xml").build();
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setRedirect(false);
        httpConfig.setTimeout(100000);
        HttpGetter HTTP_GETTER = HttpGetter.create(httpConfig);

        HttpResult httpResult = HTTP_GETTER.getWithoutException(uri);
        int count = 0;
        while ((!httpResult.isSuccess || httpResult.response == null) && (count < 5)) {
            count++;
            Thread.sleep(100000);
            httpResult = HTTP_GETTER.getWithoutException(uri);
        }
        if (httpResult.response.trim().length() == 0) {
            return null;
        }
        Document document = DocumentHelper.parseText(httpResult.response);
        org.dom4j.Element rootElement = document.getRootElement();

        org.dom4j.Element reportElement = rootElement.element("report");
        if (null == reportElement) {
            return null;
        }
        List<Element> machineElement = reportElement.elements("machine");
        if (null == machineElement) {
            return null;
        }

        for (int i = 0; i < machineElement.size(); i++) {

            List<Element> typeElements = machineElement.get(i).elements("type");
            for (int j = 0; j < typeElements.size(); j++) {
                Element element = typeElements.get(j);
                Attribute attr = element.attribute("id");
                if (attr.getStringValue().contains("Cache.") || attr.getStringValue().contains("Squirrel")) {
                    List<Element> projectElements = typeElements.get(j).elements("name");
                    for (int k = 0; k < projectElements.size(); k++) {
                        Element e = projectElements.get(k);

                        if ((Double.parseDouble(e.attribute("failPercent").getValue()) > 0.1) || (Double.parseDouble(e.attribute("avg").getValue())) > 10) {

                            ScanDetail scanDetail = new ScanDetail();
                            scanDetail.setCacheName(attr.getStringValue())
                                    .setProject(e.attribute("id").getStringValue())
                                    .setTotalCount(Integer.parseInt(e.attribute("totalCount").getValue()))
                                    .setFailCount(Integer.parseInt(e.attribute("failCount").getValue()))
                                    .setFailPercent(Double.parseDouble(e.attribute("failPercent").getValue()))
                                    .setMinVal(Double.parseDouble(e.attribute("min").getValue()))
                                    .setMaxVal(Double.parseDouble(e.attribute("max").getValue()))
                                    .setAvgVal(Double.parseDouble(e.attribute("avg").getValue()))
                                    .setSumVal(Double.parseDouble(e.attribute("sum").getValue()))
                                    .setSum2(Double.parseDouble(e.attribute("sum2").getValue()))
                                    .setStd(Double.parseDouble(e.attribute("std").getValue()))
                                    .setTps(Double.parseDouble(e.attribute("tps").getValue()))
                                    .setLine95Value(Double.parseDouble(e.attribute("line95Value").getValue()))
                                    .setLine99Value(Double.parseDouble(e.attribute("line99Value").getValue()))
                                    .setCreateTime(yesterdayText)
                                    .setUpdateTime(nowText);

                            scanDetailList.add(scanDetail);

//                            scanDetailService.insert(scanDetail);

                        }
                    }
                }
            }

        }

        return scanDetailList;
    }

    public static void main(String[] args) throws InterruptedException, DocumentException, URISyntaxException, MessagingException {
        ScanTask scanTask = new ScanTask();

        scanTask.run();
    }


}


