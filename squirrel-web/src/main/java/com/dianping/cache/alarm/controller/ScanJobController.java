package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.entity.ScanDetail;
import com.dianping.cache.alarm.report.scanService.ScanDetailService;
import com.dianping.cache.alarm.report.thread.ScanThread;
import com.dianping.cache.alarm.report.thread.ScanThreadFactory;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import com.dianping.cache.alarm.utils.DateUtil;
import com.dianping.cache.controller.AbstractSidebarController;
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


    @Autowired
    ScanDetailService scanDetailService;

    @Autowired
    ScanThreadFactory scanThreadFactory;


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

        List<ScanDetail>failDetails = new ArrayList<ScanDetail>();
        List<ScanDetail>delayDetails = new ArrayList<ScanDetail>();

        for(ScanDetail scanDetail:scanDetails){
            if(scanDetail.getAvgValue()>10){
               delayDetails.add(scanDetail);
            }else if(scanDetail.getFailPercent()>0.1){
                failDetails.add(scanDetail);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
//        result.put("size", scanDetails.size());
//        result.put("entities", scanDetails);

        result.put("size", scanDetails.size());
        result.put("failDetails", failDetails);
        result.put("delayDetails", delayDetails);
        return result;
    }

    @RequestMapping(value = "/report/search", method = RequestMethod.GET)
    @ResponseBody
    public Object getScanListByDay(String createTime) {

        List<ScanDetail> scanDetails = scanDetailService.findByCreateTime(createTime);

        List<ScanDetail>failDetails = new ArrayList<ScanDetail>();
        List<ScanDetail>delayDetails = new ArrayList<ScanDetail>();

        for(ScanDetail scanDetail:scanDetails){
            if(scanDetail.getAvgValue()>10){
                delayDetails.add(scanDetail);
            }else if(scanDetail.getFailPercent()>0.1){
                failDetails.add(scanDetail);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", scanDetails.size());
        result.put("entities", scanDetails);
//        result.put("failDetails", failDetails);
//        result.put("delayDetails", delayDetails);
        return result;
    }


    @RequestMapping(value = "/report/scanjob")
    @ResponseBody
    public void scanJob() {

        ScanThread scanThread = scanThreadFactory.createScanThread();

        ThreadManager.getInstance().execute(scanThread);

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
