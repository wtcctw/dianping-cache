package com.dianping.cache.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DataController extends AbstractCacheController {

    @RequestMapping(value = "/data/query", method = RequestMethod.GET)
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

}
