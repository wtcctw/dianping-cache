package com.dianping.squirrel.common.serialize;

import java.io.Serializable;

import de.ruedigermoeller.serialization.FSTConfiguration;

public class FstSerializer extends AbstractSerializer {
    
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Override
    protected byte[] doToBytes(Object object) throws Exception {
        byte[] bytes = conf.asByteArray((Serializable) object);
        return bytes;
    }
    
    @Override
    protected Object doFromBytes(byte[] bytes) throws Exception {
        return conf.asObject(bytes);
    }

}
