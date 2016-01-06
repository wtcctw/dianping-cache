package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.AlarmConfigDto;
import com.dianping.cache.alarm.controller.mapper.AlarmConfigMapper;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.MemcacheTemplate;
import com.dianping.cache.alarm.entity.RedisTemplate;
import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.cache.controller.RedisDataUtil;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.service.CacheConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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
public class AlarmConfigController extends AbstractSidebarController {

    @Autowired
    private AlarmConfigService alarmConfigService;

    @Autowired
    private MemcacheAlarmTemplateService memcacheAlarmTemplateService;

    @Autowired
    private RedisAlarmTemplateService redisAlarmTemplateService;

    @RequestMapping(value = "/setting/alarmrule")
    public ModelAndView topicSetting(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("alarm/alarmrule", createViewMap());
    }

    @RequestMapping(value = "/setting/alarmrule/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object alarmMetaList(int offset, int limit, HttpServletRequest request, HttpServletResponse response) {
        List<AlarmConfig> alarmConfigs = alarmConfigService.findByPage(offset, limit);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", alarmConfigs.size());
        result.put("entities", alarmConfigs);
        return result;
    }


    @RequestMapping(value = "/setting/alarmrule/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public boolean createAlarmConfig(@RequestBody AlarmConfigDto alarmConfigDto) {
        boolean result = false;

        if (alarmConfigDto.isUpdate()) {
            AlarmConfig alarmConfig = alarmConfigService.findById(alarmConfigDto.getId());
            alarmConfigDto.setCreateTime(alarmConfig.getCreateTime());
            alarmConfigDto.setUpdateTime(new Date());

            result = alarmConfigService.update(AlarmConfigMapper.convertToAlarmConfig(alarmConfigDto));
        } else {
            alarmConfigDto.setCreateTime(new Date());
            alarmConfigDto.setUpdateTime(new Date());
            result = alarmConfigService.insert(AlarmConfigMapper.convertToAlarmConfig(alarmConfigDto));
        }
        return result;
    }


    @RequestMapping(value = "/setting/alarmrule/remove", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public int removeAlarmConfig(int id) {
        int result = alarmConfigService.deleteById(id);

        return result;
    }

    @Autowired
    private CacheConfigurationService cacheConfigurationService;

    @RequestMapping(value = "/setting/alarmrule/query/memcacheclusters", method = RequestMethod.GET)
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

    @RequestMapping(value = "/setting/alarmrule/query/redisclusters", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findRedisClusters() {
        List<String> clusterNames = new ArrayList<String>();
        List<RedisClusterData> redisClusterDatas = RedisDataUtil.getClusterData();

        for (RedisClusterData redisClusterData : redisClusterDatas) {
            clusterNames.add(redisClusterData.getClusterName());
        }

        return clusterNames;
    }

    @RequestMapping(value = "/setting/alarmrule/query/memcachetemplates", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findMemcacheTemplates() {
        List<String> templateNames = new ArrayList<String>();
        List<MemcacheTemplate> memcacheTemplates = memcacheAlarmTemplateService.findAll();

        for (MemcacheTemplate memcacheTemplate : memcacheTemplates) {
            templateNames.add(memcacheTemplate.getTemplateName());
        }

        return templateNames;
    }


    @RequestMapping(value = "/setting/alarmrule/query/redistemplates", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findRedisTemplates() {
        List<String> templateNames = new ArrayList<String>();
        List<RedisTemplate> redisTemplates = redisAlarmTemplateService.findAll();

        for (RedisTemplate redisTemplate : redisTemplates) {
            templateNames.add(redisTemplate.getTemplateName());
        }

        return templateNames;
    }

    @Override
    protected String getSide() {
        return "alarm";
    }

    @Override
    public String getSubSide() {
        return "memcache";
    }

    @Override
    protected String getMenu() {
        return "tool";
    }
}
