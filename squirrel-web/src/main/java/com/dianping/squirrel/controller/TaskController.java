package com.dianping.squirrel.controller;

import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.squirrel.dao.TaskDao;
import com.dianping.squirrel.entity.Task;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thunder on 16/1/13.
 */
@Controller
public class TaskController extends AbstractSidebarController {

    @Resource(name = "taskDao")
    TaskDao taskDao;

    @RequestMapping(value = "/task")
    public ModelAndView showTasksView() {
        return new ModelAndView("cache/tasks", createViewMap());
    }

    @RequestMapping(value = "/task/list")
    @ResponseBody
    public Object getTaskList() {
        List<Task> tasks = taskDao.selectAll();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", tasks);
        return result;
    }

    @Override
    protected String getSide() {
        return null;
    }

    @Override
    public String getSubSide() {
        return "task";
    }
}
