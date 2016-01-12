package com.dianping.cache.timedtask;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;

import com.dianping.cache.monitor.CuratorManager;
import com.dianping.cache.service.CategoryToAppService;
import com.dianping.cache.util.SpringLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimedRetriCategoryToApp {
	
	private final String APPLICATION_PATH = "/dp/cache/runtime";
	
	private CategoryToAppService categoryToAppService;
	
	private CuratorFramework curatorClient;
	
    private Logger logger = LoggerFactory.getLogger(TimedRetriCategoryToApp.class);

    private ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();

    public TimedRetriCategoryToApp() {
        init();
        scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                runRetrieve();
            }

        }, 37, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    private void init() {
        categoryToAppService = SpringLocator.getBean("categoryToAppService");
        curatorClient = CuratorManager.getInstance().getCuratorClient();

    }

    private void runRetrieve() {
        try {
            logger.info("start to map category to appName");
            categoryToAppService.deleteAll();
            List<String> appnodes = curatorClient.getChildren().forPath(APPLICATION_PATH);
            for (String node : appnodes) {
                try {
                    byte[] catenodes = curatorClient.getData().forPath(APPLICATION_PATH + "/" + node + "/category");
                    String result = new String(catenodes, "GB2312");
                    String[] categorys = result.split(",");
                    for (String category : categorys) {
                        category = category.trim();
                        if (category.length() >= 50) {
                            int end = 0;
                            while (!Character.isDigit(category.charAt(end))) {
                                end++;
                            }
                            category = category.substring(0, end);
                        }
                        int counts = 0,retry=0;
                        while(counts == 0 && retry++ < 3){
                            counts = categoryToAppService.insert(category.trim(), node);
                        }
                    }
                } catch (Throwable e) {
                    logger.warn("map category exception : " + e);
                    curatorClient = CuratorManager.getInstance().getCuratorClient();
                }
            }
        } catch (Throwable e) {
            logger.warn("map category exception : " + e);
        }
    }

}
