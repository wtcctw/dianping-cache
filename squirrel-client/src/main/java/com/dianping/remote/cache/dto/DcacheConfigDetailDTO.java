package com.dianping.remote.cache.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp on 15/12/7.
 */
public class DcacheConfigDetailDTO extends CacheConfigDetailDTO{
    /**
     *
     */
    private static final long serialVersionUID = 5119818234771064933L;

    /**
     *
     */
    private static final String DEFAULT_TRANSCODER_CLASS = "com.dianping.cache.dcache.HessianTranscoder";

    /**
     * e.g. 10.10.1.1:8081
     */
    private List<String> serverList = new ArrayList<String>();

    private String transcoderClazz = DEFAULT_TRANSCODER_CLASS;

    /**
     *
     */
    public DcacheConfigDetailDTO() {
        this.clientClazz = "com.dianping.cache.danga.DangaClientImpl";
        this.className = this.getClass().getName();
    }

    public List<String> getServerList() {
        return serverList;
    }

    public void setServerList(List<String> serverList) {
        this.serverList = serverList;
    }

    public String getTranscoderClazz() {
        return transcoderClazz;
    }

    public void setTranscoderClazz(String transcoderClazz) {
        this.transcoderClazz = transcoderClazz;
    }
}
