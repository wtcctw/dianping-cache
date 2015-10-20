package com.dianping.cache.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.dianping.cache.util.FileUtils;

public class GzipCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) throws CompressException {
        if (bytes == null) {
            return null;
        }

        GZIPOutputStream gz = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            gz = new GZIPOutputStream(bos);
            gz.write(bytes);

            return bos.toByteArray();
        } catch (IOException e) {
            throw new CompressException("failed to compress data", e);
        } finally {
            FileUtils.close(gz);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws CompressException {
        if (bytes == null) {
            return null;
        }

        GZIPInputStream gis = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            gis = new GZIPInputStream(bis);

            byte[] buf = new byte[8192];
            int r = -1;
            while ((r = gis.read(buf)) > 0) {
                bos.write(buf, 0, r);
            }
            
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CompressException("failed to decompress data", e);
        } finally {
            FileUtils.close(gis);
        }
    }

}
