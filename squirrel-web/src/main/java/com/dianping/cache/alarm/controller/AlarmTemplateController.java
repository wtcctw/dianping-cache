package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmtemplate.AlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.AlarmTemplateDto;
import com.dianping.cache.alarm.controller.mapper.AlarmTemplateMapper;
import com.dianping.cache.alarm.entity.AlarmTemplate;
import com.dianping.cache.controller.AbstractSidebarController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Controller
public class AlarmTemplateController extends AbstractSidebarController {


    @Autowired
    private AlarmTemplateService alarmTemplateService;

    @RequestMapping(value = "/setting/alarmtemplate")
    public ModelAndView topicSetting() {
        return new ModelAndView("alarm/alarmtemplate", createViewMap());
    }


    @RequestMapping(value = "/setting/alarmtemplate/list")
    @ResponseBody
    public Object alarmtemplateList() {
        List<AlarmTemplate> alarmTemplates = alarmTemplateService.findAll();
        if(0 == alarmTemplates.size()){
            alarmTemplates = initAlarmTemplate();

            for(AlarmTemplate template:alarmTemplates){
                alarmTemplateService.insert(template);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", alarmTemplates.size());
        result.put("entities", alarmTemplates);
        return result;
    }

    List<AlarmTemplate> initAlarmTemplate(){
        AlarmTemplate memcacheDownAlarmTemplate = new AlarmTemplate(1,"Default","Memcache宕机",false,0,false,0,0,0,false,false,false,new Date(),new Date());
        AlarmTemplate memcacheMemAlarmTemplate = new AlarmTemplate(2,"Default","Memcache内存",false,95,false,10,50,60,false,false,false,new Date(),new Date());
        AlarmTemplate memcacheQPSAlarmTemplate = new AlarmTemplate(3,"Default","MemcacheQPS",false,80000,false,5000,50000,60,false,false,false,new Date(),new Date());
        AlarmTemplate memcacheConnAlarmTemplate = new AlarmTemplate(4,"Default","Memcache连接数",false,28000,false,3000,15000,60,false,false,false,new Date(),new Date());
        AlarmTemplate redisDownAlarmTemplate = new AlarmTemplate(5,"Default","Redis宕机",false,0,false,0,0,0,false,false,false,new Date(),new Date());
        AlarmTemplate redisMemAlarmTemplate = new AlarmTemplate(6,"Default","Redis内存",false,85,false,10,50,60,false,false,false,new Date(),new Date());
        AlarmTemplate redisQPSAlarmTemplate = new AlarmTemplate(7,"Default","RedisQPS",false,80000,false,5000,50000,60,false,false,false,new Date(),new Date());
        AlarmTemplate redisConsistencyAlarmTemplate = new AlarmTemplate(8,"Default","Redis主从一致",false,0,false,0,0,0,false,false,false,new Date(),new Date());

        List<AlarmTemplate> alarmTemplateList = new ArrayList<AlarmTemplate>();
        alarmTemplateList.add(memcacheDownAlarmTemplate);
        alarmTemplateList.add(memcacheMemAlarmTemplate);
        alarmTemplateList.add(memcacheQPSAlarmTemplate);
        alarmTemplateList.add(memcacheConnAlarmTemplate);
        alarmTemplateList.add(redisDownAlarmTemplate);
        alarmTemplateList.add(redisMemAlarmTemplate);
        alarmTemplateList.add(redisQPSAlarmTemplate);
        alarmTemplateList.add(redisConsistencyAlarmTemplate);
        return alarmTemplateList;
    }


    @RequestMapping(value = "/setting/alarmtemplate/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public boolean createAlarmTemplate(@RequestBody AlarmTemplateDto alarmTemplateDto) {
        boolean result = false;

        if (alarmTemplateDto.isUpdate()) {
            AlarmTemplate alarmTemplate = alarmTemplateService.findById(alarmTemplateDto.getId());

            result = alarmTemplateService.update(AlarmTemplateMapper.convertToAlarmTemplate(alarmTemplateDto));
        } else {
            alarmTemplateDto.setCreateTime(new Date());

            result = alarmTemplateService.insert(AlarmTemplateMapper.convertToAlarmTemplate(alarmTemplateDto));
        }
        return result;
    }


    @RequestMapping(value = "/setting/alarmtemplate/remove", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public int removeAlarmTemplate(int id) {
        int result = alarmTemplateService.deleteById(id);

        return result;
    }


    @Override
    protected String getSide() {
        return "alarm";
    }

    @Override
    public String getSubSide() {
        return "template";
    }

}
