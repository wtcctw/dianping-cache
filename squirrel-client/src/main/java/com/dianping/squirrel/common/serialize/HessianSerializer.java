package com.dianping.squirrel.common.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

public class HessianSerializer extends AbstractSerializer {

    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);
    
    @Override
    public byte[] doToBytes(Object object) throws Exception {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            h2os.flush();
            return bos.toByteArray();
        } finally {
            close(h2os);
        }
    }
    
    @Override
    public String doToString(Object object) throws Exception {
        Hessian2Output h2os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            h2os = new Hessian2Output(bos);
            h2os.writeObject(object);
            h2os.flush();
            return new String(bos.toByteArray(), "ISO8859-1");
        } finally {
            close(h2os);
        }
    }

    @Override
    public Object doFromBytes(byte[] bytes) throws Exception {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            return rv;
        } finally {
            close(h2is);
        }
    }
    
    @Override
    public Object doFromString(String bytes) throws Exception {
        Hessian2Input h2is = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes.getBytes("ISO8859-1"));
            h2is = new Hessian2Input(bis);
            Object rv = h2is.readObject();
            return rv;
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
