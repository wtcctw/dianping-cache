package com.dianping.cache.compress;

public interface Compressor {

    public enum CompressType {gzip, lzo, snappy, none}
    
    public byte[] compress(byte[] bytes) throws CompressException;
    
    public byte[] decompress(byte[] bytes) throws CompressException;
    
}
