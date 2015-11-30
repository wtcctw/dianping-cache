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

import org.apache.ibatis.annotations.Param;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.avatar.dao.annotation.DAOParamType;
import com.dianping.cache.entity.CacheKeyConfiguration;
import com.dianping.cache.service.condition.CacheKeyConfigSearchCondition;
import com.dianping.core.type.PageModel;

/**
 * CacheKeyConfiguration data access object
 * 
 * @author danson.liu
 * 
 */
public interface CacheKeyConfigurationDao extends GenericDao {

    /**
     * retrieve all configurations
     * 
     * @return
     */
    List<CacheKeyConfiguration> findAll();

    /**
     * inc version for specified category
     */
    void incVersion(String category);

    /**
     * retrive version for specified category
     */
    String loadVersion(String category);

	/**
	 * @param category
	 * @return
	 */
	CacheKeyConfiguration find(String category);

	/**
	 * @param config
	 */
	void create(CacheKeyConfiguration config);

	/**
	 * @param config
	 */
	void update(CacheKeyConfiguration config);

	/**
	 * @param paginater
	 * @param searchCondition
	 * @return
	 */
	PageModel paginate(PageModel paginater, CacheKeyConfigSearchCondition searchCondition);
	
	List<CacheKeyConfiguration> paginate(@Param("pageId") int pageId, @Param("cond") CacheKeyConfigSearchCondition searchCondition);
	int paginate_COUNT(CacheKeyConfigSearchCondition searchCondition);
	/**
	 * @param category
	 */
	void delete(String category);
}
