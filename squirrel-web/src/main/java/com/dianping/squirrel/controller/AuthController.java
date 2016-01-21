package com.dianping.squirrel.controller;

import com.dianping.cache.controller.vo.AuthParams;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.squirrel.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    CacheConfigurationService cacheConfigurationService;

    @RequestMapping(value = "/auth/authorize")
    @ResponseBody
    public boolean authorize(@RequestBody AuthParams authParams) throws Exception {
        authService.authorize(authParams.getApplication(),authParams.getResource());
        return true;
    }

    @RequestMapping(value = "/auth/unauthorize")
    @ResponseBody
    public boolean unauthorize(@RequestBody AuthParams authParams) throws Exception {
        authService.unauthorize(authParams.getApplication(),authParams.getResource());
        return true;
    }

    @RequestMapping(value = "/auth/password")
    @ResponseBody
    public boolean setPassword(@RequestBody AuthParams authParams){
        //quit
        return false;
    }


}
