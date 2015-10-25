package com.dianping.squirrel.client.impl.ehcache;

public abstract interface EhcacheManagerSubscriber
{
  public abstract void handle(EhcacheEvent paramEhcacheEvent);
}
