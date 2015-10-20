package com.dianping.cache.ehcache;

import net.sf.ehcache.CacheManager;

public class EhcacheEvent
{
  private CacheManager cacheManager;

  public EhcacheEvent(CacheManager cacheManager)
  {
    this.cacheManager = cacheManager;
  }

  public CacheManager getCacheManager() {
    return this.cacheManager;
  }
}
