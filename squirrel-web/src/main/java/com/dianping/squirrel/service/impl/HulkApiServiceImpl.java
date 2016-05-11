package com.dianping.squirrel.service.impl;

import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.dao.HulkClusterConfigDao;
import com.dianping.squirrel.entity.HulkClusterConfig;
import com.dianping.squirrel.service.HulkApiService;
import com.dianping.squirrel.vo.ScaleParams;
import com.dianping.squirrel.vo.hulk.ResponseCode;
import com.dianping.squirrel.vo.hulk.ScaleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HulkApiServiceImpl implements HulkApiService {

    private static Logger logger = LoggerFactory.getLogger(HulkApiServiceImpl.class);

    private String HULK_URL = "http://hulk.sankuai.com";

    @Autowired
    private HulkClusterConfigDao hulkClusterConfigDao;


    @Override
    public int scaleOut(ScaleParams params) {
        Map<String,String> httpHeaders = getHTTPHeaders(params.getClusterName());
        String paramsStr = null;
        try {
            paramsStr = JsonUtils.toStr(params);
        } catch (IOException e) {
            logger.error("scaleout error.",e);
        }
        RequestUtil.HTTPResponse response = RequestUtil.sendPost(HULK_URL+"/api/scaleout",paramsStr,httpHeaders);
        ResponseCode code;
        if(response.getCode() == 200){
            try {
                code = JsonUtils.fromStr(response.getContent(),ResponseCode.class);
            } catch (IOException e) {
                logger.error("scaleout error." + response,e);
                throw new RuntimeException(response.toString(),e);
            }
        } else {
            logger.error("scaleout error." + response);
            throw new RuntimeException(response.toString());
        }
        return code.getRescode();
    }

    @Override
    public ScaleResult scaleOutResult(ScaleParams params,int rescode) {
        ScaleResult result;
        HulkClusterConfig config = hulkClusterConfigDao.find(params.getClusterName());
        String authToken = config.getAuthToken();
        Map<String,String> httpHeaders = getHTTPHeaders(authToken);
        while (true){
            RequestUtil.HTTPResponse response = RequestUtil.sendGet(HULK_URL+"/api/scaleout/"+rescode,null,httpHeaders);
            if(response.getCode() == 200){
                try {
                    result = JsonUtils.fromStr(response.getContent(),ScaleResult.class);
                } catch (IOException e) {
                    logger.error("scaleOutResult error.rescode = " + rescode,e);
                    result = new ScaleResult();
                    result.setFailed(true);
                    return result;
                }
                if ("completed".equals(result.getStatus())){
                    return result;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private Map<String,String> getHTTPHeaders(String clusterName){
        HulkClusterConfig config = hulkClusterConfigDao.find(clusterName);
        String authToken = config.getAuthToken();
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Content-Type","application/json");
        headers.put("auth-token",authToken);
        return headers;
    }

    public static void main(String[] args) {
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Content-Type","application/json");
        headers.put("auth-token","");
        RequestUtil.HTTPResponse ret = RequestUtil.sendGet("http://hulk.sankuai.com/api/scaleout/"+6,null,headers);
        ScaleResult result;
        try {
            result = JsonUtils.fromStr(ret.getContent(),ScaleResult.class);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

}
