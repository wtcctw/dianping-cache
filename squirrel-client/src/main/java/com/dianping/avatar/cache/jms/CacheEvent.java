package com.dianping.avatar.cache.jms;

public class CacheEvent {

    enum CacheEventType {
        ServiceChange, CategoryChange, VersionChange, KeyRemove, BatchKeyRemove
    }
    
    private CacheEventType type;
    private Object content;
    

    public CacheEvent() {}
    
    public CacheEvent(CacheEventType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public CacheEventType getType() {
        return type;
    }

    public void setType(CacheEventType type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
    
}
