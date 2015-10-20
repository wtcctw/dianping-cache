package com.dianping.cache.test;

import org.apache.commons.lang.StringUtils;

public class Type {
    private int id;
    private String value;

    public Type() {
    }

    public Type(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + id;
        hash = hash * 31 + (value == null ? 0 : value.hashCode()); 
        return hash;
    }
    
    public boolean equals(Object object) {
        if(object instanceof Type) {
            Type t = (Type)object;
            return t.getId() == this.id && StringUtils.equals(t.getValue(), this.value);
        }
        return false;
    }
    
}