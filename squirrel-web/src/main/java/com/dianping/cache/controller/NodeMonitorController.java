package com.dianping.cache.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;
import com.dianping.cache.util.ParseServersUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerClusterService;
import com.dianping.cache.service.ServerService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class NodeMonitorController  extends AbstractSidebarController{

	@Autowired
	private CacheConfigurationService cacheConfigurationService ;
	
	@Autowired
	private ServerClusterService serverClusterService;
	
	@Autowired
	private MemcacheStatsService memcacheStatsService;
	
	@Autowired
	private ServerService serverService;
	
	@RequestMapping(value = "/node", method = RequestMethod.GET)
	public ModelAndView viewCacheConfig(HttpServletRequest request, HttpServletResponse response){
		
		return new ModelAndView("monitor/nodedashboard",createViewMap());
	}

	@RequestMapping(value = "/node/delete", method = RequestMethod.GET)
	public void deleteNode(String address){
		Server server = serverService.findByAddress(address);
		deleteServer(address);// delete server from cluster config and server_cluster table
		String instanceId = server.getInstanceId();
		if(null != instanceId  &&  !"".equals(instanceId)){
			// container instance
			String appId = server.getAppId();
			//AutoScale autoScale = new DockerScale();
			//autoScale.scaleDown(AppId.valueOf(appId),address);
		}else{
			// none container
			serverService.delete(address);
		}
	}

	private void deleteServer(String server){
		List<ServerCluster> relate = serverClusterService.findByServer(server);
		for(ServerCluster sc : relate){
			String cluster = sc.getCluster();
			deleteServerFromConfig(cluster,server);
		}
		serverClusterService.deleteServer(server);//delete all servercluster which contains server
	}

	private void deleteServerFromConfig(String cluster, String server) {
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String servers = config.getServers();
		if(cluster.contains("redis")){
			config.setAddTime(System.currentTimeMillis());
			List<String> serverlist = ParseServersUtil.parseRedisServers(servers);
			String urlHead = "redis-cluster://";
			String urlTail = servers.substring(servers.indexOf("?"));
			serverlist.remove(server);
			String newServers = "";
			for(String str : serverlist){
				newServers = newServers + str + ",";
			}
			newServers = newServers.substring(0,newServers.length()-1);//delete last ";"
			newServers = urlHead + newServers + urlTail;
			config.setServers(newServers);
			cacheConfigurationService.update(config);
		}else if(cluster.contains("memcached")){
			config.setAddTime(System.currentTimeMillis());
			List<String> serverList = new ArrayList<String>(config.getServerList());
			server = server.trim();
			serverList.remove(server);
			config.setServerList(serverList);
			config.setAddTime(System.currentTimeMillis());
			cacheConfigurationService.update(config);
		}
	}




	@Override
	protected String getSide() {
		// TODO Auto-generated method stub
		return "node";
	}

	@Override
	public String getSubSide() {
		// TODO Auto-generated method stub
		return "node";
	}

	@Override
	protected String getMenu() {
		// TODO Auto-generated method stub
		return "monitor";
	}

}
