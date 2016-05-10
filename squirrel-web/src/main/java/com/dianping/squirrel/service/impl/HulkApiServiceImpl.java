package com.dianping.squirrel.service.impl;

import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.dao.HulkClusterConfigDao;
import com.dianping.squirrel.entity.HulkClusterConfig;
import com.dianping.squirrel.service.CloudApiService;
import com.dianping.squirrel.vo.ScaleParams;
import com.dianping.squirrel.vo.hulk.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
@Service
public class HulkApiServiceImpl implements CloudApiService {

    private String HULK_URL = "http://hulk.sankuai.com";

    @Autowired
    private HulkClusterConfigDao hulkClusterConfigDao;


    @Override
    public int scaleOut(ScaleParams params) {
        HulkClusterConfig config = hulkClusterConfigDao.find(params.getClusterName());
        String authToken = config.getAuthToken();

        Map<String,String> httpHeaders = getHTTPHeaders(authToken);
        String paramsStr = null;
        try {
            paramsStr = JsonUtils.toStr(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String response = RequestUtil.sendPost(HULK_URL+"/api/scaleout",paramsStr,httpHeaders);
        ResponseCode code;
        try {
            code = JsonUtils.fromStr(response,ResponseCode.class);
        } catch (IOException e) {
            return 407;
        }
        return code.getRescode();
    }


    private Map<String,String> getHTTPHeaders(String authToken){
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Content-Type","application/json");
        headers.put("auth-token",authToken);
        return headers;
    }

    public static void main(String[] args) {
        HulkApiServiceImpl hulkApiService = new HulkApiServiceImpl();
        hulkApiService.scaleOut(null);
    }

}
