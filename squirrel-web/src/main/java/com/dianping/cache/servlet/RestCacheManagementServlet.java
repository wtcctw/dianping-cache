/**
 * Project: cache-server
 * 
 * File Created at 2010-10-18 $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.util.IPUtils;
import com.dianping.cache.remote.CacheManageWebService;
import com.dianping.cache.util.SpringLocator;
import com.dianping.pigeon.util.ContextUtils;

public class RestCacheManagementServlet extends HttpServlet {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 7602875515792719125L;

	private CacheManageWebService cacheManageService = SpringLocator.getBean("cacheManageWebService");

	private static final String REQ_CLEAR_BY_CATEGORY = "clearCacheByCategory";
	private static final String REQ_CLEAR_BY_KEY = "clearCacheByKey";
	private static final String REQ_INC_CATEGORY_VERSION = "incCacheCategoryVersion";
	private static final String REQ_PUSH_CATEGORY_CONFIG = "pushCacheCategoryConfig";

	private static final String PARAM_CATEGORY = "category";
	private static final String PARAM_IPS = "ips";
	private static final String PARAM_KEY = "key";
	private static final String PARAM_CACHETYPE = "type";

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Get building message parameters, and then send this message to JMS
	 * server.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		try {
			String uri = req.getRequestURI();
			if (StringUtils.isBlank(uri)) {
				out.println("Uri can not be null or empty.");
			}
			String action = uri.substring(uri.lastIndexOf("/") + 1);
			if (action.indexOf("?") != -1) {
				action = action.substring(0, action.indexOf("?"));
			}
			logger.info("uri:" + uri + ", action:" + action);
			if (!REQ_CLEAR_BY_CATEGORY.equalsIgnoreCase(action) && !REQ_CLEAR_BY_KEY.equalsIgnoreCase(action)
					&& !REQ_INC_CATEGORY_VERSION.equalsIgnoreCase(action)
					&& !REQ_PUSH_CATEGORY_CONFIG.equalsIgnoreCase(action)) {
				out.println("Invalid uri:"
						+ uri
						+ "(Use clearCacheByCategory, clearCacheByKey, incCacheCategoryVersion or pushCacheCategoryConfig)");
			}

			ContextUtils.putLocalContext("CLIENT_IP", IPUtils.getUserIP(req));
			if (REQ_CLEAR_BY_CATEGORY.equalsIgnoreCase(action)) {
				cacheManageService.clearByCategory(req.getParameter(PARAM_CATEGORY), req.getParameter(PARAM_IPS));
			} else if (REQ_CLEAR_BY_KEY.equalsIgnoreCase(action)) {
				cacheManageService.clearByKey(req.getParameter(PARAM_CACHETYPE), req.getParameter(PARAM_KEY));
			} else if (REQ_INC_CATEGORY_VERSION.equalsIgnoreCase(action)) {
				cacheManageService.incVersion(req.getParameter(PARAM_CATEGORY));
			} else {
				cacheManageService.pushCategoryConfig(req.getParameter(PARAM_CATEGORY), req.getParameter(PARAM_IPS));
			}

			out.println("OK");
		} catch(RuntimeException e) {
		    out.println("ERROR: " + e.getMessage());
		} finally {
			out.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
