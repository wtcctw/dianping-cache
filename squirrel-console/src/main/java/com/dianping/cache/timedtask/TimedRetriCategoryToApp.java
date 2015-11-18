package com.dianping.cache.timedtask;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import com.dianping.cache.monitor.CuratorManager;
import com.dianping.cache.service.CategoryToAppService;
import com.dianping.combiz.spring.context.SpringLocator;


public class TimedRetriCategoryToApp {
	
	private final String APPLICATION_PATH = "/dp/cache/runtime";
	
	private CategoryToAppService categoryToAppService;
	
	private CuratorFramework curatorClient;
	
	private ScheduledExecutorService scheduled  = Executors.newSingleThreadScheduledExecutor();
	
	public TimedRetriCategoryToApp(){
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				runRetrieve();
			}
			
		},0 , 24 * 60 * 60, TimeUnit.SECONDS);
	}
	
	private void init(){
		categoryToAppService = SpringLocator.getBean("categoryToAppService");
		curatorClient = CuratorManager.getInstance().getCuratorClient();
		
	}
	
	private void runRetrieve(){
		try {
			categoryToAppService.deleteAll();
			List<String> appnodes = curatorClient.getChildren().forPath(APPLICATION_PATH);
			for(String node : appnodes){
				  byte[] catenodes = curatorClient.getData().forPath(APPLICATION_PATH + "/"+node+"/category");
				  String result = new String(catenodes, "GB2312");
				  String[] categorys = result.split(",");
				  for(String category : categorys){
					  category = category.trim();
					  if(category.length() >= 50){
						  int end = 0;
						  while(!Character.isDigit(category.charAt(end))){
							  end++;
						  }
						  category = category.substring(0,end);
					  }
					  categoryToAppService.insert(category.trim(),node);
				  }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] ags) throws Exception{
	  CuratorFramework curatorClient = CuratorFrameworkFactory.newClient("192.168.213.144:2181", 60 * 1000, 30 * 1000, 
                new RetryNTimes(3, 1000));
	  curatorClient.start();
	  List<String> appnodes = curatorClient.getChildren().forPath("/dp/cache/runtime");
	  for(String node : appnodes){
		  byte[] catenodes = curatorClient.getData().forPath("/dp/cache/runtime/"+node+"/category");
		  System.out.println(node + "\n++++++++++++++" + new String(catenodes, "GB2312"));
	  }
	  curatorClient.close();
	}
	
}
