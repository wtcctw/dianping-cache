package com.dianping.cache.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

public class HessianSerializer implements Serializer {

    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);
    
    @Override
    public <T> byte[] toBytes(T object) throws SerializeException {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializeException("failed to serialize object", e);
        } finally {
            close(h2os);
        }
    }
    
    @Override
    public <T> String toString(T object) throws SerializeException {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            return bos.toString("UTF-8");
        } catch (IOException e) {
            throw new SerializeException("failed to serialize object", e);
        } finally {
            close(h2os);
        }
    }

    @Override
    public <T> T fromBytes(byte[] bytes, Class<T> clazz) throws SerializeException {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            return (T)rv;
        } catch(IOException e) {
            throw new SerializeException("failed to deserialize data", e);
        } finally {
            close(h2is);
        }
    }
    
    @Override
    public <T> T fromString(String bytes, Class<T> clazz) throws SerializeException {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes.getBytes("UTF-8"));
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            return (T)rv;
        } catch(IOException e) {
            throw new SerializeException("failed to deserialize data", e);
        } finally {
            close(h2is);
        }
    }
    
    private void close(Hessian2Output h2os) {
        if(h2os != null) {
            try {
                h2os.close();
            } catch(Exception e) {
                logger.info("failed to close hessian output stream", e);
            }
        }
    }
    
    private void close(Hessian2Input h2is) {
        if(h2is != null) {
            try {
                h2is.close();
            } catch(Exception e) {
                logger.info("failed to close hessian input stream", e);
            }
        }
    }

}
