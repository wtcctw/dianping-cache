package com.dianping.cache.dao;

import java.util.List;

import com.dianping.cache.entity.CategoryToApp;

public interface CategoryToAppDao {
	int insert(CategoryToApp cta);
	void deleteAll();
	List<CategoryToApp> findByCategory(String category);
}
