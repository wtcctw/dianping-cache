package com.dianping.squirrel.common.serialize;

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
    public byte[] toBytes(Object object) throws StoreSerializeException {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            h2os.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new StoreSerializeException("failed to serialize object", e);
        }
    }
    
    @Override
    public String toString(Object object) throws StoreSerializeException {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            h2os.close();
            return bos.toString("UTF-8");
        } catch (IOException e) {
            throw new StoreSerializeException("failed to serialize object", e);
        }
    }

    @Override
    public Object fromBytes(byte[] bytes, Class clazz) throws StoreSerializeException {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            h2is.close();
            return rv;
        } catch(IOException e) {
            throw new StoreSerializeException("failed to deserialize data", e);
        }
    }
    
    @Override
    public Object fromString(String bytes, Class clazz) throws StoreSerializeException {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes.getBytes("UTF-8"));
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            h2is.close();
            return rv;
        } catch(IOException e) {
            throw new StoreSerializeException("failed to deserialize data", e);
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
