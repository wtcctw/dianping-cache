package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.MemcacheTemplateDto;
import com.dianping.cache.alarm.controller.mapper.MemcacheTemplateMapper;
import com.dianping.cache.alarm.entity.MemcacheTemplate;
import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
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
           ;

            result = memcachetemplateService.update(MemcacheTemplateMapper.convertToMemcacheTemplate(memcacheTemplateDto));
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


    @Autowired
    private CacheConfigurationService cacheConfigurationService;

    @RequestMapping(value = "/setting/memcachetemplate/query/memcacheclusters", method = RequestMethod.GET)
    @ResponseBody
    public Object findMemcacheClusters() {
        List<String> clusterNames = new ArrayList<String>();
        List<CacheConfiguration> configList = cacheConfigurationService.findAll();

        for (CacheConfiguration cacheConfiguration : configList) {
            if (cacheConfiguration.getCacheKey().contains("memcache")) {
                clusterNames.add(cacheConfiguration.getCacheKey());
            }
        }

        return clusterNames;
    }


    @Override
    protected String getSide() {
        return "alarm";
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
