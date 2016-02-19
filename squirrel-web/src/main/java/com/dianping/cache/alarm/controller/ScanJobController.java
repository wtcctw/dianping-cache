package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.dataanalyse.thread.BaselineComputeThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineMapGetThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineThreadFactory;
import com.dianping.cache.alarm.entity.ScanDetail;
import com.dianping.cache.alarm.report.scanService.ScanDetailService;
import com.dianping.cache.alarm.report.thread.ScanThread;
import com.dianping.cache.alarm.report.thread.ScanThreadFactory;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import com.dianping.cache.alarm.utils.DateUtil;
import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.cache.util.NetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Controller
public class ScanJobController extends AbstractSidebarController {
    private static final long MS_PER_HOUR = 1000 * 60 * 60;
    private static final long MS_PER_DAY = MS_PER_HOUR * 24;

    private static final ArrayList<String> IPLIST = new ArrayList<String>() {{
        add("10.1.14.104");//线上
        add("10.2.7.129");//ppe
        add("192.168.227.113");//beta
        add("172.24.121.42");//my host
    }};


    @Autowired
    ScanDetailService scanDetailService;

    @Autowired
    ScanThreadFactory scanThreadFactory;

    @Autowired
    BaselineThreadFactory baselineThreadFactory;


    @RequestMapping(value = "/report")
    public ModelAndView topicSetting(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("alarm/report", createViewMap());
    }




    @RequestMapping(value = "/report/list", method = RequestMethod.GET)
    @ResponseBody
    public Object scanJobList() {
        Date now = new Date();
        String nowText = DateUtil.getCatDayString(now);

        Date yesterday = new Date(now.getTime() - MS_PER_DAY);
        String yesterdayText = DateUtil.getCatDayString(yesterday);

        List<ScanDetail> scanDetails = scanDetailService.findByCreateTime(yesterdayText);

        Map<String,List<ScanDetail>>failDetails = new HashMap<String, List<ScanDetail>>();
        Map<String,List<ScanDetail>>delayDetails = new HashMap<String, List<ScanDetail>>();

        for(ScanDetail scanDetail:scanDetails){
            if(scanDetail.getAvgVal()>10){
                if(null != delayDetails.get(scanDetail.getCacheName())){
                    delayDetails.get(scanDetail.getCacheName()).add(scanDetail);
                }else {
                    List<ScanDetail> list = new ArrayList<ScanDetail>();
                    list.add(scanDetail);
                    delayDetails.put(scanDetail.getCacheName(),list);
                }

            }else if(scanDetail.getFailPercent()>0.1){
                if(null != failDetails.get(scanDetail.getCacheName())){
                    failDetails.get(scanDetail.getCacheName()).add(scanDetail);
                }else {
                    List<ScanDetail> list = new ArrayList<ScanDetail>();
                    list.add(scanDetail);
                    failDetails.put(scanDetail.getCacheName(), list);
                }
            }
        }

        List<ScanDetail>failDetailList = new ArrayList<ScanDetail>();
        List<ScanDetail>delayDetailList = new ArrayList<ScanDetail>();

        for(Map.Entry<String, List<ScanDetail>> entry:failDetails.entrySet()){
            List<ScanDetail> list =failDetails.get(entry.getKey());
            list.get(0).setRowspan(list.size());
            failDetailList.addAll(list);
        }

        for(Map.Entry<String, List<ScanDetail>> entry:delayDetails.entrySet()){
            List<ScanDetail> list =delayDetails.get(entry.getKey());
            list.get(0).setRowspan(list.size());
            delayDetailList.addAll(list);
        }



        Map<String, Object> result = new HashMap<String, Object>();

        result.put("size", scanDetails.size());
        result.put("failDetails", failDetailList);
        result.put("delayDetails", delayDetailList);
        return result;
    }

    @RequestMapping(value = "/report/search", method = RequestMethod.GET)
    @ResponseBody
    public Object getScanListByDay(String createTime) {

        List<ScanDetail> scanDetails = scanDetailService.findByCreateTime(createTime);

        Map<String,List<ScanDetail>>failDetails = new HashMap<String, List<ScanDetail>>();
        Map<String,List<ScanDetail>>delayDetails = new HashMap<String, List<ScanDetail>>();

        for(ScanDetail scanDetail:scanDetails){
            if(scanDetail.getAvgVal()>10){
                if(null != delayDetails.get(scanDetail.getCacheName())){
                    delayDetails.get(scanDetail.getCacheName()).add(scanDetail);
                }else {
                    List<ScanDetail> list = new ArrayList<ScanDetail>();
                    list.add(scanDetail);
                    delayDetails.put(scanDetail.getCacheName(),list);
                }

            }else if(scanDetail.getFailPercent()>0.1){
                if(null != failDetails.get(scanDetail.getCacheName())){
                    failDetails.get(scanDetail.getCacheName()).add(scanDetail);
                }else {
                    List<ScanDetail> list = new ArrayList<ScanDetail>();
                    list.add(scanDetail);
                    failDetails.put(scanDetail.getCacheName(), list);
                }
            }
        }

        List<ScanDetail>failDetailList = new ArrayList<ScanDetail>();
        List<ScanDetail>delayDetailList = new ArrayList<ScanDetail>();

        for(Map.Entry<String, List<ScanDetail>> entry:failDetails.entrySet()){
            List<ScanDetail> list =failDetails.get(entry.getKey());
            list.get(0).setRowspan(list.size());
            failDetailList.addAll(list);
        }

        for(Map.Entry<String, List<ScanDetail>> entry:delayDetails.entrySet()){
            List<ScanDetail> list =delayDetails.get(entry.getKey());
            list.get(0).setRowspan(list.size());
            delayDetailList.addAll(list);
        }



        Map<String, Object> result = new HashMap<String, Object>();

        result.put("size", scanDetails.size());
        result.put("failDetails", failDetailList);
        result.put("delayDetails", delayDetailList);
        return result;
    }

    @RequestMapping(value = "/report/getWeekList", method = RequestMethod.GET)
    @ResponseBody
    public Object getWeekList() {
        Date now = new Date();
        String nowText = DateUtil.getCatDayString(now);

        List<String>lastWeekList = new ArrayList<String>();

        for(int i=1;i<=7;i++) {

            Date yesterday = new Date(now.getTime() - (MS_PER_DAY)*i);
            String yesterdayText = DateUtil.getCatDayString(yesterday);
            lastWeekList.add(yesterdayText);

        }

        return lastWeekList;
    }



    @RequestMapping(value = "/report/scanjob")
    @ResponseBody
    public void scanJob() {

        if(isMaster()) {
            logger.info(getClass()+"ScanJob Start...");

            ScanThread scanThread = scanThreadFactory.createScanThread();

            ThreadManager.getInstance().execute(scanThread);
        }
    }

    @RequestMapping(value = "/report/baselineComputeJob")
    @ResponseBody
    public void baselineCompute() {

        if(isMaster()) {
            logger.info(getClass()+"baselineCompute Start...");
            BaselineComputeThread baselineComputeThread = baselineThreadFactory.createBaselineComputeThread();

            ThreadManager.getInstance().execute(baselineComputeThread);
        }
    }

    @RequestMapping(value = "/report/baselineMapGetJob")
    @ResponseBody
    public void baselineMapGet() {

        if(isMaster()) {
            logger.info(getClass()+"baselineMapGet Start...");
            BaselineMapGetThread baselineMapGetThread = baselineThreadFactory.createBaselineMapGetThread();

            ThreadManager.getInstance().execute(baselineMapGetThread);
        }
    }

    public boolean isMaster() {
        boolean isMaster = false;
        try {
            List<String> ip = NetUtil.getAllLocalIp();
            ip.retainAll(IPLIST);
            if (ip.size() > 0)
                isMaster = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isMaster;
    }

    @Override
    protected String getSide() {
        return "log";
    }

    @Override
    public String getSubSide() {
        return "event";
    }

}
