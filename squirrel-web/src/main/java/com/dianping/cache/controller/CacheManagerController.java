package com.dianping.cache.controller;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cache.controller.dto.CategoryParams;
import com.dianping.cache.controller.dto.ConfigurationParams;
import com.dianping.cache.dao.TaskDao;
import jodd.util.StringUtil;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.avatar.exception.DuplicatedIdentityException;
import com.dianping.cache.entity.*;
import com.dianping.cache.monitor.storage.MemcacheStatsDataStorage;
import com.dianping.cache.monitor.storage.ServerStatsDataStorage;
import com.dianping.cache.service.*;
import com.dianping.cache.service.condition.CacheKeyConfigSearchCondition;
import com.dianping.cache.util.NetUtil;
import com.dianping.core.type.PageModel;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.lifecycle.Locatable;
import com.dianping.squirrel.task.TaskManager;
import com.dianping.squirrel.task.ClearCategoryTask;

@Controller
public class CacheManagerController extends AbstractCacheController {

    @Resource(name = "cacheConfigurationService")
    private CacheConfigurationService cacheConfigurationService;


    @Resource(name = "cacheKeyConfigurationService")
    private CacheKeyConfigurationService cacheKeyConfigurationService;

    @Resource(name = "storeClient")
    private StoreClient storeClient;

    @Resource(name = "serverService")
    private ServerService serverService;

    @Resource(name = "serverClusterService")
    private ServerClusterService serverClusterService;

    @Resource(name = "categoryToAppService")
    private CategoryToAppService categoryToAppService;

    @Resource(name = "taskDao")
    private TaskDao deamonTaskDao;

    @RequestMapping(value = "/cache/config")
    public ModelAndView viewCacheConfig() {
        subside = "config";
        return new ModelAndView("cache/config", createViewMap());
    }

    @RequestMapping(value = "/cache/config/edit")
    public ModelAndView viewCacheConfigEdit() {

        subside = "config";
        return new ModelAndView("cache/configedit", createViewMap());
    }

    @RequestMapping(value = "/cache/config/new")
    public ModelAndView viewCacheConfigNew() {

        subside = "config";
        return new ModelAndView("cache/confignew", createViewMap());
    }

    // 根据集群名获取集群配置
    @RequestMapping(value = "/cache/config/find")
    @ResponseBody
    public Object findByCacheKey(@RequestParam("cacheKey") String cacheKey,@RequestParam("swimlane") String swimlane ) {
        Map<String, Object> paras = super.createViewMap();

        CacheConfiguration config = cacheConfigurationService.findWithSwimLane(cacheKey,swimlane);
        paras.put("config", config);
        return paras;
    }

    @RequestMapping(value = "/cache/config/validate_port")
    @ResponseBody
    public Map<String, Object> manualAdd(@RequestParam("ip") String ip,
                                         @RequestParam("port") String port) {
        Map<String, Object> paras = super.createViewMap();
        boolean flag = true;
        MemcachedClient client = null;
        try {
            client = new MemcachedClient(AddrUtil.getAddresses(ip + ":" + port));
            OperationFuture<Boolean> result = client.set("connect-test-key1", 20, "connect-test-value1");
            result.get(1000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            flag = false;
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
        paras.put("flag", flag);
        return paras;
    }

    @RequestMapping(value = "/cache/config/findAll")
    @ResponseBody
    public Map<String, Object> configSearch() {
        Map<String, Object> paras = super.createViewMap();

        List<CacheConfiguration> config = cacheConfigurationService.findAll();
        paras.put("entitys", config);
        return paras;
    }

    @RequestMapping(value = "/cache/config/update", method = RequestMethod.POST)
    public void configUpdate(@RequestParam("key") String cacheKey,
                             @RequestParam("clientClazz") String clientClazz,
                             @RequestParam("servers") String servers,
                             @RequestParam("swimlane") String swimlane,
                             @RequestParam("transcoderClazz") String transcoderClazz) {
        CacheConfiguration newConfig = new CacheConfiguration();

        newConfig.setCacheKey(cacheKey);
        newConfig.setClientClazz(clientClazz);
        newConfig.setServers(servers);
        newConfig.setTranscoderClazz(transcoderClazz);
        newConfig.setSwimlane(swimlane);
        cacheConfigurationService.update(newConfig);

    }

    @RequestMapping(value = "/cache/config/updateServers")
    @ResponseBody
    public Object configUpdateServers(@RequestParam("key") String cacheKey,
                                      @RequestParam("newservers") String newservers,
                                      @RequestParam("oldservers") String oldservers,
                                      HttpServletResponse response) {
        Map<String, Object> paras = super.createViewMap();
        boolean flag = false;
        CacheConfiguration newConfig = new CacheConfiguration();
        // 获取数据库中最新的 servers 信息 对比看是否有变化
        CacheConfiguration oldConfig = cacheConfigurationService.find(cacheKey);
        if (oldservers != null && oldservers.equals(oldConfig.getServers())) {

            newConfig.setCacheKey(cacheKey);
            newConfig.setClientClazz(oldConfig.getClientClazz());
            if("".equals(newservers))
                newservers = null;
            newConfig.setServers(newservers);
            newConfig.setTranscoderClazz(oldConfig.getTranscoderClazz());

            cacheConfigurationService.update(newConfig);
            ServerStatsDataStorage.REFRESH = true;
            flag = true;
        } else {
            // 数据库信息有变化 不执行更新
        }
        paras.put("flag", flag);
        return paras;
    }

    @RequestMapping(value = "/cache/config/addServer")
    @ResponseBody
    public Object configAddServer(@RequestParam("key") String cacheKey,
                                  @RequestParam("server") String server,
                                  @RequestParam("swimlane") String swimlane,
                                  @RequestParam("oldservers") String oldservers) {
        Map<String, Object> paras = super.createViewMap();
        boolean flag = false;
        // 获取数据库中最新的 servers 信息 对比看是否有变化
        CacheConfiguration oldConfig = cacheConfigurationService.findWithSwimLane(cacheKey, swimlane);
        if ((oldservers != null && oldservers.equals(oldConfig.getServers()))
                || ("".equals(oldservers) && oldConfig.getServers() == null)) {
            List<String> serverList;
            if (oldConfig.getServers() == null) {
                serverList = new ArrayList<String>();
            } else {
                serverList = new ArrayList<String>(oldConfig.getServerList());
            }
            serverList.add(server);
            oldConfig.setServerList(serverList);
            oldConfig.setAddTime(System.currentTimeMillis());
            cacheConfigurationService.update(oldConfig);
            try {
                serverService.insert(server, null, null, 0, null);
            } catch (DuplicateKeyException e) {
                // do nothing
            } finally {
                serverClusterService.insert(server, cacheKey);
                ServerStatsDataStorage.REFRESH = true;
            }
            flag = true;
        } else {
            // 数据库信息有变化 不执行更新
        }
        paras.put("flag", flag);
        return paras;
    }

    @RequestMapping(value = "/cache/config/deleteServer")
    @ResponseBody
    public Map<String, Object> configReduceServer(@RequestParam("key") String cacheKey,
                                                  @RequestParam("server") String server,
                                                  @RequestParam("swimlane") String swimlane,
                                                  @RequestParam("oldservers") String oldservers) {
        Map<String, Object> paras = super.createViewMap();
        boolean flag = false;
        // 获取数据库中最新的 servers 信息 对比看是否有变化
        CacheConfiguration oldConfig = cacheConfigurationService.findWithSwimLane(cacheKey,swimlane);
        if (oldservers != null && oldservers.equals(oldConfig.getServers())) {

            //判断是否需要关闭该sever 对应的 memcached 实例
            if (requireCacheClose(server, cacheKey)) {

                Server stemp = serverService.findByAddress(server);
                if (stemp.getInstanceId() != null) {
                    // TODO
                    flag = true;
                } else {
                    deleteServerFromCache(server, cacheKey,swimlane);
                }
            } else {
                deleteServerFromCache(server, cacheKey,swimlane);
            }
        }
        paras.put("flag", flag);
        return paras;
    }

    @RequestMapping(value = "/cache/config/create")
    @ResponseBody
    public Map<String, Object> configCreate(@RequestBody ConfigurationParams configurationParams) {
        Map<String, Object> paras = super.createViewMap();
        boolean flag = true;
        CacheConfiguration newConfig = new CacheConfiguration();
        if (StringUtil.isBlank(configurationParams.getCacheKey()) || StringUtil.isBlank(configurationParams.getClientClazz())) {
            flag = false;
        } else {
            newConfig.setCacheKey(configurationParams.getCacheKey());
            newConfig.setSwimlane(configurationParams.getSwimlane());
            newConfig.setClientClazz(configurationParams.getClientClazz());
            newConfig.setServers(configurationParams.getServers());
            newConfig.setTranscoderClazz(configurationParams.getTranscoderClazz());
            try {
                cacheConfigurationService.create(newConfig);
            } catch (DuplicatedIdentityException e) {
                flag = false;
            }
            if (flag && configurationParams.getCacheKey().contains("memcached")
                    && configurationParams.getServers() != null
                    && StringUtil.isBlank(configurationParams.getSwimlane())) {
                for (String server : newConfig.getServerList()) {
                    try {
                        serverService.insert(server, null, null, 0, null);
                        serverClusterService.insert(server, configurationParams.getCacheKey());
                    } catch (DuplicateKeyException e) {
                    }
                }
            }
        }

        paras.put("flag", flag);
        return paras;
    }

    @RequestMapping(value = "/cache/config/delete")
    @ResponseBody
    public Boolean configDelete(@RequestBody ConfigurationParams configurationParams) {
        cacheConfigurationService.deleteWithSwimLane(configurationParams.getCacheKey(), configurationParams.getSwimlane());
        return Boolean.TRUE;
    }

    @RequestMapping(value = "/cache/config/clear")
    @ResponseBody
    public void configClear(@RequestParam("cacheKey") String cacheKey,
                            @RequestParam("ipKey") String ipKey) {
        CacheConfiguration newConfig = new CacheConfiguration();
        newConfig.setCacheKey(cacheKey);
        cacheConfigurationService.clearByKey(cacheKey, ipKey);
    }

    @RequestMapping(value = "/cache/key")
    public ModelAndView viewCacheKey() {
        subside = "key";
        return new ModelAndView("cache/key", createViewMap());
    }

    @RequestMapping(value = "/cache/key/search")
    @ResponseBody
    public Map<String, Object> keySearch(@RequestParam("category") String category,
                                         @RequestParam("cacheType") String cacheType,
                                         @RequestParam("pageId") int pageId) {

        PageModel pageModel = new PageModel();
        pageModel.setPage(pageId);
        pageModel.setPageSize(20);
        CacheKeyConfigSearchCondition condition = new CacheKeyConfigSearchCondition();
        String _category = null;
        String _cacheType = null;
        if (StringUtils.isNotBlank(category)) {
            _category = category;
        }
        if (StringUtils.isNotBlank(cacheType)) {
            _cacheType = cacheType;
        }
        condition.setCacheType(_cacheType);
        condition.setCategory(_category);
        PageModel result = cacheKeyConfigurationService.paginate(pageModel,condition);
        List<?> recodes = result.getRecords();
        Map<String, Object> paras = super.createViewMap();
        paras.put("entitys", recodes);
        paras.put("page", result.getPage());
        paras.put("totalpage", result.getPageCount());
        return paras;
    }

    @RequestMapping(value = "/cache/key/findAllCategory")
    @ResponseBody
    public Map<String, Object> findAllCategory() {

        List<CacheKeyConfiguration> result = cacheKeyConfigurationService.findAll();
        Set<String> categorySet = new HashSet<String>();
        Set<String> cacheTypeSet = new HashSet<String>();
        for (CacheKeyConfiguration item : result) {
            categorySet.add(item.getCategory());
            cacheTypeSet.add(item.getCacheType());
        }
        Map<String, Object> paras = super.createViewMap();
        paras.put("cacheTypeSet", cacheTypeSet);
        paras.put("categorySet", categorySet);
        return paras;
    }

    @RequestMapping(value = "/cache/key/findByCategory")
    @ResponseBody
    public Object findByCategory(@RequestParam("category") String category) {
        if (category == null) {
            return null;
        }
        CacheKeyConfiguration result = cacheKeyConfigurationService
                .find(category);
        return result;
    }

    @RequestMapping(value = "/cache/key/create")
    @ResponseBody
    public void creatCacheKey(@RequestBody CategoryParams categoryParams) {

        CacheKeyConfiguration newCacheKey = new CacheKeyConfiguration();
        newCacheKey.setCategory(categoryParams.getCategory());
        newCacheKey.setCacheType(categoryParams.getCacheType());
        newCacheKey.setDuration(categoryParams.getDuration());
        newCacheKey.setHot(categoryParams.isHot());
        newCacheKey.setIndexTemplate(categoryParams.getIndexTemplate());
        newCacheKey.setIndexDesc(categoryParams.getIndexDesc());
        newCacheKey.setSync2Dnet(categoryParams.isSync2Dnet());
        newCacheKey.setVersion(0);
        newCacheKey.setExtension(categoryParams.getExtension());
        try {
            cacheKeyConfigurationService.create(newCacheKey);
        } catch (DuplicatedIdentityException e) {
        }

    }

    @RequestMapping(value = "/cache/key/update")
    @ResponseBody
    public void updateCacheKey(@RequestBody CategoryParams categoryParams) {

        CacheKeyConfiguration newCacheKey = new CacheKeyConfiguration();
        newCacheKey.setCategory(categoryParams.getCategory());
        newCacheKey.setCacheType(categoryParams.getCacheType());
        newCacheKey.setDuration(categoryParams.getDuration());
        newCacheKey.setHot(categoryParams.isHot());
        newCacheKey.setIndexTemplate(categoryParams.getIndexTemplate());
        newCacheKey.setIndexDesc(categoryParams.getIndexDesc());
        newCacheKey.setSync2Dnet(categoryParams.isSync2Dnet());
        newCacheKey.setVersion(categoryParams.getVersion());
        newCacheKey.setExtension(categoryParams.getExtension());
        cacheKeyConfigurationService.update(newCacheKey);
    }

    @RequestMapping(value = "/cache/key/delete")
    @ResponseBody
    public void deleteCacheKeyByCategory(@RequestBody CategoryParams categoryParams) {
        cacheKeyConfigurationService.delete(categoryParams.getCategory());
    }

    @RequestMapping(value = "/cache/key/clear")
    @ResponseBody
    public void clearCacheKeyByCategory(@RequestBody CategoryParams categoryParams) {
        cacheConfigurationService.clearByCategory(categoryParams.getCategory());
    }

    @RequestMapping(value = "/cache/key/applist")
    @ResponseBody
    public List<CategoryToApp> getAppList(@RequestBody CategoryParams categoryParams) {
        return categoryToAppService.findByCategory(categoryParams.getCategory());
    }





    @RequestMapping(value = "/data/query", method = RequestMethod.GET)
    public ModelAndView viewCacheQuery() {
        subside = "query";
        return new ModelAndView("cache/query", createViewMap());
    }

    @RequestMapping(value = "/cache/query/deleteCategory", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object deleteCategory(@RequestParam("category")String category) {
        ClearCategoryTask task = new ClearCategoryTask(category);
        TaskManager.submit(task);
        return true;
    }


    @RequestMapping(value = "/cache/query/getKeyValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getKeyValue(@RequestParam("finalKey") String finalKey) {

//        DeamonTask task = new DeamonTask();
//        task.setCommitTime(System.currentTimeMillis());
//        task.setDescription("123");
//        task.setEndTime(System.currentTimeMillis());
//        task.setStartTime(System.currentTimeMillis());
//        task.setStat(1);
//        task.setStatMax(1);
//        task.setStatMin(1);
//        task.setType(1);
//        deamonTaskDao.insert(task);

        CacheKeyConfiguration key = cacheKeyConfigurationService.find(finalKey.substring(0, finalKey.indexOf(".")));
        Map<String, Object> paras = super.createViewMap();
        Object o = storeClient.get(finalKey);
        paras.put("result", o);
        StoreClient cc = StoreClientFactory.getStoreClient(key.getCacheType());
        if(cc instanceof Locatable){
            String location = ((Locatable)cc).locate(finalKey);
            paras.put("address", location);
        }
        return paras;
    }

    @RequestMapping(value = "/cache/query/setKeyValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object setKeyValue(@RequestParam("category") String category,
                              @RequestParam("value") String value,
                              @RequestParam("params") Object... params) {

        StoreKey storeKey = new StoreKey(category, params);
        boolean o = false;
        o = storeClient.set(storeKey, value);
        return o;
    }

    @RequestMapping(value = "/initServerCluster", method = RequestMethod.GET)
    public void init() {
        List<CacheConfiguration> configList = cacheConfigurationService.findAll();
        for (CacheConfiguration item : configList) {
            if (item.getCacheKey().contains("memcached")
                    && !"memcached-leo".equals(item.getCacheKey())) {
                List<String> serverList = item.getServerList();
                for (String server : serverList) {
                    try {
                        serverService.insert(server, null, null,0,null);

                    } catch (DuplicateKeyException e) {
                        // do nothing
                    }
                    serverClusterService.insert(server, item.getCacheKey());
                }
            }
        }
    }

    @RequestMapping(value = "/insertServerCluster", method = RequestMethod.GET)
    public void serverCluster_(String server,String cluster) {
        serverClusterService.insert(server, cluster);
    }

    @RequestMapping(value = "/deleteServer", method = RequestMethod.GET)
    public void deleteServer_(String server) {
        serverService.delete(server);
    }

    @RequestMapping(value = "/deleteServerCluster", method = RequestMethod.GET)
    public void deleteServerCluster_(String server,String cluster) {
        serverClusterService.delete(server, cluster);
    }

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public void startStorage(){
        MemcacheStatsDataStorage.START_MS = true;
        ServerStatsDataStorage.START_SS = true;
    }

    @RequestMapping(value = "/close", method = RequestMethod.GET)
    public void closeStorage(){
        MemcacheStatsDataStorage.START_MS = false;
        ServerStatsDataStorage.START_SS = false;
    }


    @RequestMapping(value = "/cache/query/getip", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object ip() {


        InetAddress ia=null;
        String localip = "";
        try {
            ia=InetAddress.getLocalHost();
            localip=ia.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localip;
    }
    @RequestMapping(value = "/cache/query/getipNet", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object ipNet() {

        return NetUtil.getAllLocalIp();
    }

    @Override
    protected String getSide() {
        return "cachemanager";
    }

    private String subside = "config";

    @Override
    public String getSubSide() {
        return subside;
    }


    private boolean requireCacheClose(String server, String cacheKey) {
        boolean hasServer = false;
        List<CacheConfiguration> configList = cacheConfigurationService.findAll();
        for (CacheConfiguration item : configList) {
            String servers = item.getServers();
            if (!item.getCacheKey().equals(cacheKey) && servers != null && servers.contains(server)) {
                hasServer = true;
                break;
            }
        }

        return !hasServer;
    }

    private void deleteServerFromCache(String server,String cacheKey,String swimlane){
        CacheConfiguration config = cacheConfigurationService.findWithSwimLane(cacheKey,swimlane);
        List<String> serverList = new ArrayList<String>(config.getServerList());
        server = server.trim();
        serverList.remove(server);
        config.setServerList(serverList);
        config.setAddTime(System.currentTimeMillis());
        cacheConfigurationService.update(config);
        // 删除 server cluster 关系表中对应项
        serverClusterService.delete(server, cacheKey);
        // 如果 对server  已经没有集群使用  删除servers 表中对应数据
        if(requireCacheClose(server,cacheKey)){
            serverService.delete(server);
            ServerStatsDataStorage.REFRESH = true;
        }
    }


}
