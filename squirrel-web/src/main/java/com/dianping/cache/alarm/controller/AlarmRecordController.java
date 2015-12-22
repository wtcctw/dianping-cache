package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmrecord.AlarmRecordService;
import com.dianping.cache.alarm.controller.dto.AlarmMetaBatchDto;
import com.dianping.cache.alarm.controller.dto.AlarmMetaDto;
import com.dianping.cache.alarm.entity.AlarmRecord;
import com.dianping.cache.controller.AbstractSidebarController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
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

    @RequestMapping(value = "/setting/alarmrecord")
    public ModelAndView topicSetting(HttpServletRequest request, HttpServletResponse response){
        return new ModelAndView("alarm/alarmrecord", createViewMap());
    }

    @RequestMapping(value = "/setting/alarmrecord/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object alarmMetaList(int offset, int limit, HttpServletRequest request, HttpServletResponse response){
        List<AlarmRecord> alarmRecords = alarmRecordService.findByPage(offset, limit);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", alarmRecords.size());
        result.put("entities",alarmRecords);
        return result;
    }

    @Override
    protected String getSide() {
        return "alarm";
    }

    @Override
    public String getSubSide() {
        return "alarmmeta";
    }

    @Override
    protected String getMenu() {
        return "tool";
    }
}
