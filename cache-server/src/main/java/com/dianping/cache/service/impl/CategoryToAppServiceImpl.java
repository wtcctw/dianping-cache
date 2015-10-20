package com.dianping.cache.service.impl;

import java.util.List;

import com.dianping.cache.dao.CategoryToAppDao;
import com.dianping.cache.entity.CategoryToApp;
import com.dianping.cache.service.CategoryToAppService;

public class CategoryToAppServiceImpl implements CategoryToAppService{
	
	private CategoryToAppDao categoryToAppDao;

	@Override
	public void insert(String category,String node) {
		// TODO Auto-generated method stub
		CategoryToApp cta = new CategoryToApp();
		cta.setApplication(node);
		cta.setCategory(category);
		categoryToAppDao.insert(cta);
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		categoryToAppDao.deleteAll();
	}

	public CategoryToAppDao getCategoryToAppDao() {
		return categoryToAppDao;
	}

	public void setCategoryToAppDao(CategoryToAppDao categoryToAppDao) {
		this.categoryToAppDao = categoryToAppDao;
	}

	@Override
	public List<CategoryToApp> findByCategory(String category) {
		// TODO Auto-generated method stub
		return categoryToAppDao.findByCategory(category);
	}

}
