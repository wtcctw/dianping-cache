package com.dianping.cache.controller;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.StringUtil;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.internal.OperationFuture;

import org.codehaus.plexus.util.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mortbay.util.ajax.JSON;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.exception.DuplicatedIdentityException;
import com.dianping.cache.entity.*;
import com.dianping.cache.exception.CacheException;
import com.dianping.cache.monitor.storage.MemcacheStatsDataStorage;
import com.dianping.cache.monitor.storage.ServerStatsDataStorage;
import com.dianping.cache.service.*;
import com.dianping.cache.service.condition.CacheKeyConfigSearchCondition;
import com.dianping.cache.service.condition.OperationLogSearchCondition;
import com.dianping.cache.util.NetUtil;
import com.dianping.cache.util.RequestUtil;
import com.dianping.core.type.PageModel;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.core.Locatable;
import com.dianping.squirrel.client.impl.memcached.MemcachedStoreClient;
import com.dianping.squirrel.client.impl.memcached.MemcachedStoreClientImpl;

@Controller
public class CacheManagerController extends AbstractCacheController {

	private Map<String, String> operationStatus = new HashMap<String, String>();

	@Resource(name = "cacheConfigurationService")
	private CacheConfigurationService cacheConfigurationService;

	@Resource(name = "operationLogService")
	private OperationLogService operationLogService;

	@Resource(name = "cacheKeyConfigurationService")
	private CacheKeyConfigurationService cacheKeyConfigurationService;

	@Resource(name = "cacheService")
	private CacheService cacheService;

	@Resource(name = "serverService")
	private ServerService serverService;

	@Resource(name = "serverClusterService")
	private ServerClusterService serverClusterService;

	@RequestMapping(value = "/cache/config", method = RequestMethod.GET)
	public ModelAndView viewCacheConfig(HttpServletRequest request,
			HttpServletResponse response) {

		subside = "config";
		return new ModelAndView("cache/config", createViewMap());
	}

	@RequestMapping(value = "/cache/config/edit", method = RequestMethod.GET)
	public ModelAndView viewCacheConfigEdit() {

		subside = "config";
		return new ModelAndView("cache/configedit", createViewMap());
	}

	@RequestMapping(value = "/cache/config/new", method = RequestMethod.GET)
	public ModelAndView viewCacheConfigNew() {

		subside = "config";
		return new ModelAndView("cache/confignew", createViewMap());
	}

	// 根据集群名获取集群配置
	@RequestMapping(value = "/cache/config/find", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object findByCacheKey(@RequestParam("cacheKey") String cacheKey) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();

		CacheConfiguration config = cacheConfigurationService.find(cacheKey);
		paras.put("config", config);
		return paras;
	}

	@RequestMapping(value = "/cache/config/validate_port", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object manualAdd(@RequestParam("ip") String ip,
			@RequestParam("port") String port) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();
		boolean flag = true;
		MemcachedClient client = null;
		try {
			client = new MemcachedClient(AddrUtil.getAddresses(ip + ":" + port));
			logger.info("Try to test MemcachedClient with Address" + ip + ":"
					+ port);
			OperationFuture<Boolean> result = client.set("connect-test-key1",
					20, "connect-test-value1");
			result.get(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			flag = false;
			logger.info("Get memcached Connection with InterruptedException");
		} catch (TimeoutException e) {
			flag = false;
			logger.info("Get memcached Connection with TimeoutException");
		} catch (ExecutionException e) {
			flag = false;
			logger.info("Get memcached Connection with ExecutionException");
		} catch (Exception e) {
			flag = false;
			logger.info("IOException while test MemcachedClient Connection.");
		} finally {
			if (client != null) {
				client.shutdown();
				logger.info("MemcachedClient with Address" + ip + ":" + port
						+ "stoped");
			}
		}
		paras.put("flag", flag);
		return paras;
	}

	@RequestMapping(value = "/cache/config/findAll", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configSearch(HttpServletRequest request,
			HttpServletResponse response) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();

		List<CacheConfiguration> config = cacheConfigurationService.findAll();
		paras.put("entitys", config);
		return paras;
	}

	@RequestMapping(value = "/cache/config/update", method = RequestMethod.POST)
	public void configUpdate(@RequestParam("key") String cacheKey,
			@RequestParam("clientClazz") String clientClazz,
			@RequestParam("servers") String servers,
			@RequestParam("transcoderClazz") String transcoderClazz,
			HttpServletResponse response) {
		subside = "config";
		CacheConfiguration newConfig = new CacheConfiguration();

		newConfig.setCacheKey(cacheKey);
		newConfig.setClientClazz(clientClazz);
		newConfig.setServers(servers);
		newConfig.setTranscoderClazz(transcoderClazz);

		cacheConfigurationService.update(newConfig);

	}

	@RequestMapping(value = "/cache/config/updateServers", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configUpdateServers(@RequestParam("key") String cacheKey,
			@RequestParam("newservers") String newservers,
			@RequestParam("oldservers") String oldservers,
			HttpServletResponse response) {
		subside = "config";
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

	@RequestMapping(value = "/cache/config/addServer", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configAddServer(@RequestParam("key") String cacheKey,
			@RequestParam("server") String server,
			@RequestParam("oldservers") String oldservers,
			HttpServletResponse response) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();
		boolean flag = false;
		// 获取数据库中最新的 servers 信息 对比看是否有变化
		CacheConfiguration oldConfig = cacheConfigurationService.find(cacheKey);
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
				serverService.insert(server, null, null,0);
				serverClusterService.insert(server, cacheKey);
				ServerStatsDataStorage.REFRESH = true;
			} catch (DuplicateKeyException e) {
				// do nothing
			}
			flag = true;
		} else {
			// 数据库信息有变化 不执行更新

		}
		paras.put("flag", flag);
		return paras;
	}

	@RequestMapping(value = "/cache/config/deleteServer", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configReduceServer(@RequestParam("key") String cacheKey,
			@RequestParam("server") String server,
			@RequestParam("oldservers") String oldservers,
			HttpServletResponse response) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();
		boolean flag = false;
		// 获取数据库中最新的 servers 信息 对比看是否有变化
		CacheConfiguration oldConfig = cacheConfigurationService.find(cacheKey);
		if (oldservers != null && oldservers.equals(oldConfig.getServers())) {
			
			//判断是否需要关闭该sever 对应的 memcached 实例
			if(requireCacheClose(server,cacheKey)){
				
				Server stemp = serverService.findByAddress(server);
				
				//判断  是docker中实例  还是手动起的
				if (stemp.getInstanceId() != null) {
					// 对 docker 中的实例 删除
					flag = true;
					String str = RequestUtil.sendPost(
							"http://10.3.21.21:8080/api/v1/instances/app/"
									+ stemp.getAppId() + "/shutdown",
									"{\"instances\":[\"" + stemp.getInstanceId()
									+ "\"]}");
					Map json = (Map) JSON.parse(str);
					Long operationId = (Long) json.get("operationId");
					operationStatus.put(operationId.toString()+"server", server);
					operationStatus.put(operationId.toString(), cacheKey);
					operationStatus.put("status" + operationId, "100");
					operateShutDown(operationId.toString(), stemp.getAppId(),
							stemp.getInstanceId());
					paras.put("operationId", operationId);
					
				} else {
					// 老版本 手动起的实例    这里跟 不需要关闭服务端   直接从集群配置中删除该实例ip  步骤是一样的
					deleteServerFromCache(server, cacheKey);
				}
				
			}else{
				//不需要关闭服务端   直接从集群配置中删除该实例ip
				deleteServerFromCache(server, cacheKey);
			}

		} else {
			// 数据库信息有变化 不执行更新
		}
		paras.put("flag", flag);
		return paras;
	}

	@RequestMapping(value = "/cache/config/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configCreate(@RequestParam("key") String cacheKey,
			@RequestParam("clientClazz") String clientClazz,
			@RequestParam("servers") String servers,
			@RequestParam("transcoderClazz") String transcoderClazz,
			HttpServletResponse response) {
		subside = "config";
		Map<String, Object> paras = super.createViewMap();
		boolean flag = true;
		CacheConfiguration newConfig = new CacheConfiguration();

		// 非法输入 在ehcache 中   transcoderClazz servers 可以为空   
		if (StringUtil.isBlank(cacheKey) || StringUtil.isBlank(clientClazz)) {
			flag = false;
		} else {
			newConfig.setCacheKey(cacheKey);
			newConfig.setClientClazz(clientClazz);
			if("".equals(servers))
				servers = null;
			newConfig.setServers(servers);
			newConfig.setTranscoderClazz(transcoderClazz);
			try {
				cacheConfigurationService.create(newConfig);
			} catch (DuplicatedIdentityException e) {
				flag = false;
			}
			if(flag && cacheKey.contains("memcached") && servers!=null){
				for(String server : newConfig.getServerList()){
					try{
						serverService.insert(server, null, null,0);
						serverClusterService.insert(server, cacheKey);
					} catch (DuplicateKeyException e) {
					}
				}
			}
		}

		paras.put("flag", flag);
		return paras;
	}

	@RequestMapping(value = "/cache/config/delete", method = RequestMethod.POST)
	public void configDelete(@RequestParam("key") String cacheKey,
			HttpServletResponse response) {
		subside = "config";

		CacheConfiguration newConfig = new CacheConfiguration();
		newConfig.setCacheKey(cacheKey);

		cacheConfigurationService.delete(cacheKey);
		//TODO   delete some info on servers and server_cluster 
		;

	}

	@RequestMapping(value = "/cache/config/clear", method = RequestMethod.POST)
	public void configClear(@RequestParam("cacheKey") String cacheKey,
			@RequestParam("ipKey") String ipKey, HttpServletResponse response) {
		subside = "config";

		CacheConfiguration newConfig = new CacheConfiguration();
		newConfig.setCacheKey(cacheKey);

		cacheConfigurationService.clearByKey(cacheKey, ipKey);

	}

	@RequestMapping(value = "/cache/config/baseInfo", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object configInfo(HttpServletResponse response) {
		String[] impl = { "com.dianping.cache.memcached.MemcachedClientImpl",
				"com.dianping.cache.ehcache.EhcacheClientImpl" };
		String[] coder = { "com.dianping.cache.memcached.HessianTranscoder",
				"com.dianping.cache.memcached.KvdbTranscoder" };
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("impl", impl);
		p.put("coder", coder);

		ServiceLoader<StoreClient> loader = ServiceLoader
				.load(StoreClient.class);
		for (StoreClient implClass : loader) {
			System.out.println(implClass.getClass().toString());
		}
		// 转换成json格式
		return p;

	}

	@RequestMapping(value = "/cache/config/scale", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object scale(@RequestParam("app_id") String app_id,
			@RequestParam("cacheKey") String cacheKey) {
		String str = RequestUtil.sendPost("http://10.3.21.21:8080/api/v1/apps/"
				+ app_id + "/scale", "{\"instanceCount\" : 1}");
		Map json = (Map) JSON.parse(str);
		Long operationId = (Long) json.get("operationId");
		operationStatus.put(operationId.toString(), cacheKey);
		operationStatus.put("status" + operationId, "100");
		operate(app_id, operationId.toString());// 处理
		Map<String, Object> paras = super.createViewMap();
		paras.put("operationId", operationId.longValue());
		return paras;
	}


	private void operateShutDown(final String operationId, final String appId,
			final String instanceId) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (true) {

					String str = RequestUtil.sendGet(
							"http://10.3.21.21:8080/api/v1/operations/"
									+ operationId, null);
					Map json = (Map) JSON.parse(str);
					long paasOperationStatus = (Long) json.get("operationStatus");
					if (paasOperationStatus == 200) {
						operationStatus.put("status" + operationId, "200");
						String result = RequestUtil.sendPost(
								"http://10.3.21.21:8080/api/v1/instances/app/"
										+ appId + "/delete",
								"{\"instances\":[\"" + instanceId + "\"]}");
						
						
						Map jsonresult = (Map) JSON.parse(result);
						
						Long reduceOperationId = (Long) jsonresult.get("operationId");
						operationStatus.put("status" + reduceOperationId, "100");
						
						//传递 server  cacheKey 至 reduceOperationId
						String cacheKey = operationStatus.get(operationId);
						operationStatus.put(reduceOperationId.toString(), cacheKey);
						String server = operationStatus.get(operationId+"server");
						operationStatus.put(reduceOperationId.toString()+"server", server);
						
						operateReduce(reduceOperationId.toString());// 轮训处理
						break;
					} else if (paasOperationStatus == 100) {// doing
						operationStatus.put("status" + operationId, "100");
					} else {// failed
						operationStatus.put("status" + operationId, "110");
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}

		};
		Thread t = new Thread(runnable);
		t.start();
	}

	private void operateReduce(final String operationId) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (true) {

					String str = RequestUtil.sendGet(
							"http://10.3.21.21:8080/api/v1/operations/"
									+ operationId, null);
					Map json = (Map) JSON.parse(str);
					long paasOperationStatus = (Long) json
							.get("operationStatus");
					if (paasOperationStatus == 200) {
						operationStatus.put("status" + operationId, "200");
						
						String server = operationStatus.get(operationId+"server");
						String cacheKey = operationStatus.get(operationId);
						deleteServerFromCache(server,cacheKey);
						
						break;
					} else if (paasOperationStatus == 100) {// doing
						operationStatus.put("status" + operationId, "100");
					} else {// failed
						operationStatus.put("status" + operationId, "110");
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}

		};
		Thread t = new Thread(runnable);
		t.start();
	}

	private void operate(final String appId, final String operationId) {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {

				while (true) {

					String str = RequestUtil.sendGet(
							"http://10.3.21.21:8080/api/v1/operations/"
									+ operationId, null);
					Map json = (Map) JSON.parse(str);
					long paasOperationStatus = (Long) json
							.get("operationStatus");
					if (paasOperationStatus == 200) {

						String log = (String) json.get("log");
						try {
							Document document = DocumentHelper.parseText(log);
							Element root = document.getRootElement();
							Element detail = root.element("host-operation");
							Attribute ipattribute = detail
									.attribute("instance-ip");
							String ipvalue = ipattribute.getText();
							Attribute portattribute = detail.attribute("port");
							String portvalue = portattribute.getText();// 该值是 80
							Attribute idattribute = detail
									.attribute("instance-id");
							String instanceId = idattribute.getText();

							CacheConfiguration config = cacheConfigurationService
									.find(operationStatus.get(operationId));
							if (config != null) {
								List<String> serverList;
								if (config.getServers() == null) {
									serverList = new ArrayList<String>();
								} else {
									serverList = new ArrayList<String>(config.getServerList());
								}
								serverList.add(ipvalue + ":11211");
								config.setServerList(serverList);
								cacheConfigurationService.update(config);

								try {
									serverService.insert(ipvalue + ":11211",
											appId, instanceId,0);
								} catch (DuplicateKeyException e) {
									// do nothing
								}

								operationStatus.put("status" + operationId,
										"200");
								operationStatus
										.put("ip" + operationId, ipvalue);
								operationStatus.put("port" + operationId,
										"11211");
							} else {
								// TODO config == null
							}

							break;
						} catch (DocumentException e) {
							// 根据 operationId 销毁实例
							logger.error("DocumentException cann't parse the PAAS returned log !");
						}

					} else if (paasOperationStatus == 100) {// doing
						operationStatus.put("status" + operationId, "100");
					} else {// failed
						operationStatus.put("status" + operationId, "110");
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}

		};

		Thread t = new Thread(runnable);
		t.start();
	}

	@RequestMapping(value = "/cache/config/operateResult", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object operateResult(@RequestParam("operationId") String operationId) {
		Map<String, Object> paras = super.createViewMap();
		paras.put("status", operationStatus.get("status" + operationId));
		paras.put("ip", operationStatus.get("ip" + operationId));
		paras.put("port", operationStatus.get("port" + operationId));
		return paras;
	}



	@RequestMapping(value = "/cache/key", method = RequestMethod.GET)
	public ModelAndView viewCacheKey() {
		subside = "key";
		return new ModelAndView("cache/key", createViewMap());
	}

	@RequestMapping(value = "/cache/key/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object keySearch(@RequestParam("category") String category,
			@RequestParam("cacheType") String cacheType,
			@RequestParam("pageId") int pageId, HttpServletResponse response) {
		subside = "key";

		PageModel pageModel = new PageModel();
		pageModel.setPage(pageId);
		pageModel.setPageSize(20);
		// 设置搜索条件
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

		// 数据库检索
		PageModel result = cacheKeyConfigurationService.paginate(pageModel,
				condition);
		List<?> recodes = result.getRecords();
		Map<String, Object> paras = super.createViewMap();
		paras.put("entitys", recodes);
		paras.put("page", result.getPage());
		paras.put("totalpage", result.getPageCount());
		return paras;

	}

	@RequestMapping(value = "/cache/key/findAllCategory", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object findAllCategory() {

		List<CacheKeyConfiguration> result = cacheKeyConfigurationService
				.findAll();
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

	@RequestMapping(value = "/cache/key/findByCategory", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object findByCategory(@RequestParam("category") String category) {
		if (category == null) {
			return null;
		}
		CacheKeyConfiguration result = cacheKeyConfigurationService
				.find(category);
		return result;
	}

	@RequestMapping(value = "/cache/key/create", method = RequestMethod.POST)
	public void creatCacheKey(@RequestParam("category") String category,
			@RequestParam("cacheType") String cacheType,
			@RequestParam("duration") String duration,
			@RequestParam("indexTemplate") String indexTemplate,
			@RequestParam("indexDesc") String indexDesc,
			@RequestParam("version") String version,
			@RequestParam("hot") String hot,
			@RequestParam("sync2Dnet") String sync2Dnet,
			@RequestParam("extension") String extension,
			HttpServletResponse response) {

		subside = "key";

		if ("".equals(hot)) {
			hot = "false";
		}
		if ("".equals(sync2Dnet)) {
			sync2Dnet = "false";
		}
		if("".equals(extension)){
			extension = null;
		}
		CacheKeyConfiguration newCacheKey = new CacheKeyConfiguration();
		newCacheKey.setCategory(category);
		newCacheKey.setCacheType(cacheType);
		newCacheKey.setDuration(duration);
		newCacheKey.setHot(Boolean.parseBoolean(hot));
		newCacheKey.setIndexTemplate(indexTemplate);
		newCacheKey.setIndexDesc(indexDesc);
		newCacheKey.setSync2Dnet(Boolean.parseBoolean(sync2Dnet));
		newCacheKey.setVersion(0);
		newCacheKey.setExtension(extension);

		try {
			cacheKeyConfigurationService.create(newCacheKey);
		} catch (DuplicatedIdentityException e) {
		}

	}

	@RequestMapping(value = "/cache/key/update", method = RequestMethod.POST)
	public void updateCacheKey(@RequestParam("category") String category,
			@RequestParam("cacheType") String cacheType,
			@RequestParam("duration") String duration,
			@RequestParam("indexTemplate") String indexTemplate,
			@RequestParam("indexDesc") String indexDesc,
			@RequestParam("version") String version,
			@RequestParam("hot") String hot,
			@RequestParam("sync2Dnet") String sync2Dnet,
			@RequestParam("extension") String extension,
			HttpServletResponse response) {

		subside = "key";

		CacheKeyConfiguration newCacheKey = new CacheKeyConfiguration();
		newCacheKey.setCategory(category);
		newCacheKey.setCacheType(cacheType);
		newCacheKey.setDuration(duration);
		newCacheKey.setHot(Boolean.parseBoolean(hot));
		newCacheKey.setIndexTemplate(indexTemplate);
		newCacheKey.setIndexDesc(indexDesc);
		newCacheKey.setSync2Dnet(Boolean.parseBoolean(sync2Dnet));
		newCacheKey.setVersion(0);
		newCacheKey.setExtension(extension);
		// update
		cacheKeyConfigurationService.update(newCacheKey);

	}

	@RequestMapping(value = "/cache/key/delete", method = RequestMethod.POST)
	public void deleteCacheKeyByCategory(
			@RequestParam("category") String category,
			HttpServletResponse response) {

		subside = "key";
		cacheKeyConfigurationService.delete(category);

	}

	@RequestMapping(value = "/cache/key/clear", method = RequestMethod.POST)
	public void clearCacheKeyByCategory(
			@RequestParam("category") String category,
			HttpServletResponse response) {

		subside = "key";
		// 清除缓存并没有真正的清除该缓存的category，而是增加了version的值。
		cacheConfigurationService.clearByCategory(category);

	}

	@RequestMapping(value = "/cache/operator", method = RequestMethod.GET)
	public ModelAndView viewCacheOperator(HttpServletRequest request) {

		subside = "operator";
		return new ModelAndView("cache/operator", createViewMap());

	}

	@RequestMapping(value = "/cache/operator/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object operatorSearch(@RequestParam("operator") String operator,
			@RequestParam("content") String content,
			@RequestParam("startTime") String startTime,
			@RequestParam("endTime") String endTime,
			@RequestParam("pageId") int pageId) {
		subside = "operator";

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

	@RequestMapping(value = "/cache/query", method = RequestMethod.GET)
	public ModelAndView viewCacheQuery() {
		subside = "query";
		return new ModelAndView("cache/query", createViewMap());
	}

	@RequestMapping(value = "/cache/query/getKeyValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object getKeyValue(@RequestParam("finalKey") String finalKey) {

		CacheKeyConfiguration key = cacheKeyConfigurationService.find(finalKey.substring(0, finalKey.indexOf(".")));
		Map<String, Object> paras = super.createViewMap();
		Object o = cacheService.get(finalKey);
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

		CacheKey cacheKey = new CacheKey(category, params);
		boolean o = false;
		try {
			o = cacheService.set(cacheKey, value);
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
						serverService.insert(server, null, null,0);

					} catch (DuplicateKeyException e) {
						// do nothing
					}
					serverClusterService.insert(server, item.getCacheKey());
				}
			}
		}
		
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
	
	private void deleteServerFromCache(String server,String cacheKey){
		CacheConfiguration config = cacheConfigurationService.find(cacheKey);
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
	public static void main(String[] ars) {
		String str = RequestUtil.sendGet(
				"http://10.101.0.12:8080/api/v1/operations/" + "2362", null);
		Map json = (Map) JSON.parse(str);
		long operationStatus = (Long) json.get("operationStatus");
		if (operationStatus == 200) {

			String log = (String) json.get("log");
			try {
				Document document = DocumentHelper.parseText(log);
				Element root = document.getRootElement();
				Element detail = root.element("host-operation");
				Attribute attribute = detail.attribute("instance-ip");
				String value = attribute.getText();

			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	

}
