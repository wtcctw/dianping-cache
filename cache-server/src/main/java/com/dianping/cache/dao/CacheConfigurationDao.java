/**
 * Project: cache-server
 * 
 * File Created at 2010-10-15
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.cache.dao;

import java.util.List;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.avatar.dao.annotation.DAOParamType;
import com.dianping.cache.entity.CacheConfiguration;

/**
 * CacheKeyConfiguration data access object
 * @author danson.liu
 *
 */
public interface CacheConfigurationDao extends GenericDao {

	/**
	 * retrieve all configurations
	 * @return
	 */
	List<CacheConfiguration> findAll();

	/**
	 * @param key
	 * @return
	 */
	CacheConfiguration find( String key);

	/**
	 * @param config
	 * @return
	 */
	void create( CacheConfiguration config);

	/**
	 * @param config
	 */
	void update(CacheConfiguration config);

	/**
	 * @param key
	 */
	void delete(String key);
	
}
