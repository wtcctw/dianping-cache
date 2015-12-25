package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.MemcacheTemplateDto;
import com.dianping.cache.alarm.controller.mapper.MemcacheTemplateMapper;
import com.dianping.cache.alarm.entity.MemcacheTemplate;
import com.dianping.cache.controller.AbstractSidebarController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Controller
public class MemcacheAlarmTemplateController extends AbstractSidebarController {

    @Autowired
    private MemcacheAlarmTemplateService memcachetemplateService;

    @RequestMapping(value = "/setting/memcachetemplate")
    public ModelAndView topicSetting() {
        return new ModelAndView("alarm/memcachetemplate", createViewMap());
    }

    @RequestMapping(value = "/setting/memcachetemplate/list")
    @ResponseBody
    public Object memcachetemplateList() {
        List<MemcacheTemplate> memcacheTemplates = memcachetemplateService.findAll();
        if(0==memcacheTemplates.size()){
            MemcacheTemplate memcacheTemplate = new MemcacheTemplate();
            memcacheTemplate
                    .setId(0)
                    .setTemplateName("Default");
            memcacheTemplate
                    .setIsDown(true)
                    .setMemThreshold(95)
                    .setQpsThreshold(80000)
                    .setConnThreshold(28000)
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            memcachetemplateService.insert(memcacheTemplate);
            memcacheTemplates = memcachetemplateService.findAll();
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", memcacheTemplates.size());
        result.put("entities", memcacheTemplates);
        return result;
    }


    @RequestMapping(value = "/setting/memcachetemplate/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public boolean createMemcacheTemplate(@RequestBody MemcacheTemplateDto memcacheTemplateDto) {
        boolean result = false;

        if (memcacheTemplateDto.isUpdate()) {
            MemcacheTemplate memcacheTemplate = memcachetemplateService.findById(memcacheTemplateDto.getId());

            result = memcachetemplateService.update(memcacheTemplate);
        } else {
            memcacheTemplateDto.setCreateTime(new Date());

            result = memcachetemplateService.insert(MemcacheTemplateMapper.convertToMemcacheTemplate(memcacheTemplateDto));
        }
        return result;
    }


    @RequestMapping(value = "/setting/memcachetemplate/remove", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public int removeMemcacheTemplate(int id) {
        int result = memcachetemplateService.deleteById(id);

        return result;
    }


    @Override
    protected String getSide() {
        return "memcaches";
    }

    @Override
    public String getSubSide() {
        return "template";
    }

    @Override
    protected String getMenu() {
        return "tool";
    }
}
