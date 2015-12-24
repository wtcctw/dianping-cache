package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.RedisTemplateDto;
import com.dianping.cache.alarm.controller.mapper.RedisTemplateMapper;
import com.dianping.cache.alarm.entity.RedisTemplate;
import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.cache.controller.RedisDashBoardUtil;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
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
public class RedisAlarmTemplateController extends AbstractSidebarController {


    @Autowired
    private RedisAlarmTemplateService redistemplateService;

    @RequestMapping(value = "/setting/redistemplate")
    public ModelAndView topicSetting() {
        return new ModelAndView("alarm/redistemplate", createViewMap());
    }


    @RequestMapping(value = "/setting/redistemplate/list")
    @ResponseBody
    public Object redistemplateList() {
        List<RedisTemplate> redisTemplates = redistemplateService.findAll();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", redisTemplates.size());
        result.put("entities", redisTemplates);
        return result;
    }


    @RequestMapping(value = "/setting/redistemplate/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public boolean createRedisTemplate(@RequestBody RedisTemplateDto redisTemplateDto) {
        boolean result = false;

        if (redisTemplateDto.isUpdate()) {
            RedisTemplate redisTemplate = redistemplateService.findById(redisTemplateDto.getId());
            ;

            result = redistemplateService.update(RedisTemplateMapper.convertToRedisTemplate(redisTemplateDto));
        } else {
            redisTemplateDto.setCreateTime(new Date());

            result = redistemplateService.insert(RedisTemplateMapper.convertToRedisTemplate(redisTemplateDto));
        }
        return result;
    }


    @RequestMapping(value = "/setting/redistemplate/remove", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public int removeRedisTemplate(int id) {
        int result = redistemplateService.deleteById(id);

        return result;
    }

    @RequestMapping(value = "/setting/redistemplate/query/redisclusters", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findRedisClusters() {
        List<String> clusterNames = new ArrayList<String>();
        List<RedisClusterData> redisClusterDatas = RedisDashBoardUtil.getClusterData();

        for (RedisClusterData redisClusterData : redisClusterDatas) {
            clusterNames.add(redisClusterData.getClusterName());
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
