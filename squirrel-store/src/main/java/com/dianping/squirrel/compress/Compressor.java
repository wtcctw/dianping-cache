package com.dianping.squirrel.compress;

public interface Compressor {

    public enum CompressType {gzip, lzo, snappy, none}
    
    public byte[] compress(byte[] bytes) throws StoreCompressException;
    
    public byte[] decompress(byte[] bytes) throws StoreCompressException;
    
}
