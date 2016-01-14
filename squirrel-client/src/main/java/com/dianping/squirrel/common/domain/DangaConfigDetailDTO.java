package com.dianping.squirrel.common.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp on 15/12/7.
 */
public class DangaConfigDetailDTO  extends CacheConfigDetailDTO {
    /**
     *
     */
    private static final long serialVersionUID = 5119818771064933L;

    /**
     *
     */
    private static final String DEFAULT_TRANSCODER_CLASS = "com.dianping.cache.danga.DangaTranscoder";

    /**
     * e.g. 10.10.1.1:8081
     */
    private List<String> serverList = new ArrayList<String>();

    private String transcoderClazz = DEFAULT_TRANSCODER_CLASS;

    /**
     *
     */
    public DangaConfigDetailDTO() {
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
