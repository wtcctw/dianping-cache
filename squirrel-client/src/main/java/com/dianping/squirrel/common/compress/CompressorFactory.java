package com.dianping.squirrel.common.compress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.squirrel.common.compress.Compressor.CompressType;

public class CompressorFactory {

    private static ConcurrentMap<String, Compressor> compressorMap = new ConcurrentHashMap<String, Compressor>();
    
    public static Compressor getCompressor(String compress) {
        if(compress == null || CompressType.none.name().equalsIgnoreCase(compress)) {
            return null;
        }
        
        Compressor compressor = compressorMap.get(compress);
        if(compressor == null) {
            compressor = createCompressor(compress);
            compressorMap.putIfAbsent(compress, compressor);
        }
        
        return compressor;
    }

    private static Compressor createCompressor(String compress) {
        if(CompressType.gzip.name().equalsIgnoreCase(compress)) {
            return new GzipCompressor();
        }
        throw new RuntimeException("unsupported compress type: " + compress);
    }
    
}
