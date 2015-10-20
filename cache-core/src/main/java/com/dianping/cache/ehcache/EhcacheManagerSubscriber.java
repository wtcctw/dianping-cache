package com.dianping.cache.ehcache;

public abstract interface EhcacheManagerSubscriber
{
  public abstract void handle(EhcacheEvent paramEhcacheEvent);
}
