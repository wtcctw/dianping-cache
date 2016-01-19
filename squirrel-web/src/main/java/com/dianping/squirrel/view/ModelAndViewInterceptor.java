package com.dianping.squirrel.view;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.dianping.cache.entity.User;
import com.dianping.cache.service.UserService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;


public class ModelAndViewInterceptor extends HandlerInterceptorAdapter{
	
	private static final String USERNAME = "username";
	private static final String ISADMIN = "isadmin";
	private static final String ISVISITOR = "isvisitor";
	private static final String LOGOUTURL = "logouturl";
	
	@Resource(name = "userService")
	private UserService userService;
	
	private static final Logger logger = Logger
			.getLogger(ModelAndViewInterceptor.class);
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		String logoutUrl = null;
		try {
			ConfigCache configCache = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
			String prelogoutUrl = configCache.getProperty("cas-server-webapp.logoutUrl").trim();
			logoutUrl = configCache.getProperty("squirrel-web.sso.url").trim();//localhost%3A8081%2Fcache-server
			logoutUrl = prelogoutUrl + "?service=" + logoutUrl.replaceAll(":", "%3A").replaceAll("/", "%2F");
		} catch (LionException e) {
			logger.error("Use lion to get url error.", e);
		}
		String username = RequestUtil.getUsername();
		User user = userService.findUser(username);
		boolean isadmin = false;
		if(user != null){
			isadmin = true;
			username = user.getRealName();
		}
		
		modelAndView.addObject(ISADMIN, isadmin);
		modelAndView.addObject(ISVISITOR, !isadmin);
		
		modelAndView.addObject(USERNAME, username);
		modelAndView.addObject(LOGOUTURL, logoutUrl);

	}

	
}
