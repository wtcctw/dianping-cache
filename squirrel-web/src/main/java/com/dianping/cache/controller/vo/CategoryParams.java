package com.dianping.cache.controller.vo;

/**
 * Created by dp on 15/12/16.
 */
public class CategoryParams {

    private String category;

    private String duration;

    private String indexTemplate;

    private String indexDesc;

    private String cacheType;

    private String extension;

    private int version;

    private boolean sync2Dnet = false;

    private boolean hot = false;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getIndexTemplate() {
        return indexTemplate;
    }

    public void setIndexTemplate(String indexTemplate) {
        this.indexTemplate = indexTemplate;
    }

    public String getIndexDesc() {
        return indexDesc;
    }

    public void setIndexDesc(String indexDesc) {
        this.indexDesc = indexDesc;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isSync2Dnet() {
        return sync2Dnet;
    }

    public void setSync2Dnet(boolean sync2Dnet) {
        this.sync2Dnet = sync2Dnet;
    }

    public boolean isHot() {
        return hot;
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }
}
