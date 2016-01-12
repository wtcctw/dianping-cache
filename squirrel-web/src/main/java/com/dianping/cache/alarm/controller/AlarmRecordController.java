package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmrecord.AlarmRecordService;
import com.dianping.cache.alarm.controller.dto.RecordSearchDto;
import com.dianping.cache.alarm.entity.AlarmRecord;
import com.dianping.cache.controller.AbstractSidebarController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Controller
public class AlarmRecordController extends AbstractSidebarController {

    @Autowired
    private AlarmRecordService alarmRecordService;

    @RequestMapping(value = "/event")
    public ModelAndView topicSetting(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("alarm/alarmrecord", createViewMap());
    }

    @RequestMapping(value = "/event/list", method = RequestMethod.GET)
    @ResponseBody
    public Object alarmRecordList(int offset, int limit) {
        List<AlarmRecord> alarmRecords = alarmRecordService.findByPage(offset, limit);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", alarmRecords.size());
        result.put("entities", alarmRecords);
        return result;
    }


    @RequestMapping(value = "/event/search")
    @ResponseBody
    public Object alarmRecordSearchedList(@RequestBody RecordSearchDto recordSearchDto) {

        //转换时间格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sql = "SELECT id, alarmType, alarmTitle ,clusterName,ip ,val ,createTime " +
                "FROM alarm_records" +
                " WHERE ";

        boolean flag = false;
        if (null != recordSearchDto.getTitle()) {
            flag = true;
            sql += "alarmTitle = '" + recordSearchDto.getTitle() + "'";
        }

        if (null != recordSearchDto.getClusterName()) {
            if (true == flag) {
                sql += " AND ";
            } else {
                flag = true;
            }

            sql += "clusterName = '" + recordSearchDto.getClusterName() + "'";
        }

        if (null != recordSearchDto.getIp()) {
            if (true == flag) {
                sql += " AND ";
            } else {
                flag = true;
            }
            sql += "ip = '" + recordSearchDto.getIp() + "'";
        }

        if (null != recordSearchDto.getStartDate()) {
            if (true == flag) {
                sql += " AND ";
            } else {
                flag = true;
            }
            sql += "createTime > '" + df.format(recordSearchDto.getStartDate()) + "'";
        }

        if (null != recordSearchDto.getEndDate()) {
            if (true == flag) {
                sql += " AND ";
            } else {
                flag = true;
            }
            sql += "createTime < '" + df.format(recordSearchDto.getEndDate())+"'";
        }

        sql += ";";
        if (false == flag) {
            return alarmRecordList(0, 30);
        } else {
            List<AlarmRecord> alarmRecords = alarmRecordService.search(sql);
//        List<AlarmRecord> alarmRecords = null;
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("size", alarmRecords.size());
            result.put("entities", alarmRecords);
            return result;
        }
    }


    @Override
    protected String getSide() {
        return "log";
    }

    @Override
    public String getSubSide() {
        return "event";
    }

    @Override
    protected String getMenu() {
        return "tool";
    }
}
