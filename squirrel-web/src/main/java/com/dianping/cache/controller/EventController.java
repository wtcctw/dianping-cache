package com.dianping.cache.controller;

import com.dianping.cache.service.OperationLogService;
import com.dianping.cache.service.condition.OperationLogSearchCondition;
import com.dianping.core.type.PageModel;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dp on 16/1/11.
 */
@Controller
public class EventController extends AbstractCacheController {


    @Autowired
    private OperationLogService operationLogService;

    private String subside;

    @RequestMapping(value = "/event/audit")
    public ModelAndView audit() {
        subside = "audit";
        return new ModelAndView("event/audit", createViewMap());
    }

    @RequestMapping(value = "/audit/search")
    @ResponseBody
    public Object operatorSearch(@RequestParam("operator") String operator,
                                 @RequestParam("content") String content,
                                 @RequestParam("startTime") String startTime,
                                 @RequestParam("endTime") String endTime,
                                 @RequestParam("pageId") int pageId) {

        PageModel pageModel = new PageModel();
        pageModel.setPage(pageId);
        pageModel.setPageSize(20);
        // 设置操作日志的搜索条件
        OperationLogSearchCondition condition = new OperationLogSearchCondition();
        Date start = null;
        Date end = null;
        String _operator = null;
        String _content = null;
        if (StringUtils.isNotBlank(startTime)) {
            start = strToDate(startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            end = strToDate(endTime);
        }
        if (StringUtils.isNotBlank(operator)) {
            _operator = operator;
        }
        if (StringUtils.isNotBlank(content)) {
            _content = content;
        }

        condition.setContent(_content);
        condition.setOperator(_operator);
        condition.setOperateStart(start);
        condition.setOperateEnd(end);

        // 数据库检索相应的操作日志
        PageModel result = operationLogService.paginate(pageModel, condition);
        List<?> recodes = result.getRecords();
        Map<String, Object> paras = super.createViewMap();
        paras.put("entitys", recodes);
        paras.put("page", result.getPage());
        paras.put("totalpage", result.getPageCount());
        return paras;

    }


    private Date strToDate(String strTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date result = null;
        try {
            result = sdf.parse(strTime);
            return result;
        } catch (ParseException e) {
            logger.info("data tranform failed.", e);
            return new Date();
        }
    }

    @Override
    protected String getSide() {
        return "event";
    }

    @Override
    public String getSubSide() {
        return subside;
    }
}
