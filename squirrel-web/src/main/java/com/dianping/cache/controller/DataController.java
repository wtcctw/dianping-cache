package com.dianping.cache.controller;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.task.ClearCategoryTask;
import com.dianping.squirrel.task.TaskManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DataController extends AbstractSidebarController {

    @RequestMapping(value = "/data/query")
    public ModelAndView viewCacheQuery() {
        return new ModelAndView("cache/query", createViewMap());
    }
    
    @Override
    protected String getSide() {
        return "data";
    }

    @Override
    public String getSubSide() {
        return "query";
    }

    @RequestMapping(value = "/data/delete", method = RequestMethod.GET)
    @ResponseBody
    public Object deleteCategory(@RequestParam("category")String category) {
        ClearCategoryTask task = new ClearCategoryTask(category);
        TaskManager.submit(task);
        return true;
    }

    public static void main(String[] args) {
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory("redis-del");
        for(int i = 0; i < 1000; i++) {
            StoreKey storeKey = new StoreKey("redis-del", i);
            storeClient.add(storeKey, i);
        }
    }

}
