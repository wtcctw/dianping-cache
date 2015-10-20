package com.dianping.cache.memcached;

import java.io.Serializable;

import de.ruedigermoeller.serialization.FSTConfiguration;
import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;

public class FstTranscoder extends SpyObject implements Transcoder<Object> {

    private static final int MAX_SIZE = 1 * 1024 * 1024;
    
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    
    @Override
    public boolean asyncDecode(CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(Object o) {
        byte data[] = conf.asByteArray((Serializable) o);
        return new CachedData(0, data, getMaxSize());
    }

    @Override
    public Object decode(CachedData d) {
        return conf.asObject(d.getData());
    }

    @Override
    public int getMaxSize() {
        return MAX_SIZE;
    }

}
