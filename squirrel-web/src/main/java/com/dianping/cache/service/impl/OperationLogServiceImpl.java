/**
 * Project: cache-server
 * 
 * File Created at 2011-9-18
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.cache.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.dao.OperationLogDao;
import com.dianping.cache.entity.OperationLog;
import com.dianping.cache.service.OperationLogService;
import com.dianping.cache.service.condition.OperationLogSearchCondition;
import com.dianping.cache.util.RequestUtil;
import com.dianping.core.type.PageModel;
import com.dianping.pigeon.util.ContextUtils;

/**
 * TODO Comment of OperationLogServiceImpl
 * @author danson.liu
 *
 */
public class OperationLogServiceImpl implements OperationLogService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private OperationLogDao operationLogDao;

	@Override
	public void create(boolean succeed, String content, boolean critical) {
		try {
			if (isLogRequired()) {
				createLog(succeed, content, critical);
			} else {
			    String clientIp = (String) ContextUtils.getLocalContext("CLIENT_IP");
			    logger.info("cache oplog {}: {} => {}", new Object[] {succeed ? "success" : "failure", clientIp, content});
			}
		} catch (Throwable throwable) {
			logger.error("Create operation log failed.", throwable);
		}
	}

	@Override
	public void create(boolean succeed, String operation, Map<String, String> detail, boolean critical) {
		try {
			if (isLogRequired()) {
				StringBuilder content = new StringBuilder(operation.endsWith(".") ? operation.substring(0, operation.length() - 1) : operation);
				if (detail != null && !detail.isEmpty()) {
					content.append("(");
					int index = 0;
					for (Entry<String, String> entry : detail.entrySet()) {
						content.append(index++ > 0 ? ", " : "").append(entry.getKey()).append("=").append(entry.getValue());
					}
					content.append(")");
				}
				// content.append(".");
				createLog(succeed, content.toString(), critical);
			} else {
	            String clientIp = (String) ContextUtils.getLocalContext("CLIENT_IP");
	            StringBuilder content = new StringBuilder(operation.endsWith(".") ? operation.substring(0, operation.length() - 1) : operation);
                if (detail != null && !detail.isEmpty()) {
                    content.append("(");
                    int index = 0;
                    for (Entry<String, String> entry : detail.entrySet()) {
                        content.append(index++ > 0 ? ", " : "").append(entry.getKey()).append("=").append(entry.getValue());
                    }
                    content.append(")");
                }
                logger.info("cache oplog {}: {} => {}", new Object[] {succeed ? "success" : "failure", clientIp, content});
			}
		} catch (Throwable throwable) {
			logger.error("Create operation log failed.", throwable);
		}
	}

	private void createLog(boolean succeed, String content, boolean critical) {
	    logger.info("cache oplog {}: {} => {}", new Object[] {succeed ? "success" : "failure", RequestUtil.getUsername(), content});
		OperationLog log = new OperationLog();
		log.setOperator(RequestUtil.getUsername());
		log.setSucceed(succeed);
		log.setContent(content);
		log.setCritical(critical);
		operationLogDao.create(log);
	}

	@Override
	public PageModel paginate(PageModel paginater, OperationLogSearchCondition searchCondition) {
		PageModel p = new PageModel();
		p.setPage(paginater.getPage());
		p.setRecords(operationLogDao.paginate((paginater.getPage()-1)*20, searchCondition));
		p.setRecordCount(operationLogDao.paginate_COUNT(searchCondition));
		p.setPageSize(20);
		return p;
//		return operationLogDao.paginate(paginater, searchCondition);
	}

	@Override
	public void delete(Date before) {
		operationLogDao.delete(before);
	}

	public void setOperationLogDao(OperationLogDao operationLogDao) {
		this.operationLogDao = operationLogDao;
	}
	
	private boolean isLogRequired() {
		//从cache-server-ui登陆用户操作的才记录日志
		
		return RequestUtil.getUsername() != null;
	}

}
