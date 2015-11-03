package com.dianping.squirrel.client.impl;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Bean implements Serializable {
    
    private int id;
    private String name;
    
    public Bean() {}
    
    public Bean(int id, String name) {
        this.setId(id);
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if(o instanceof Bean) {
            Bean b = (Bean)o;
            return new EqualsBuilder().append(id, b.id).
                            append(name, b.name).isEquals();
        }
        return false;
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).toHashCode();
    }
}

