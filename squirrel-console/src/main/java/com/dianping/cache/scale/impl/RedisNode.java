package com.dianping.cache.scale.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.cache.scale.Node;

public class RedisNode implements Node {
    
    private RedisServer master;
    
    private RedisServer slave;
    
    public RedisNode() {}
    
    public RedisNode(RedisServer master, RedisServer slave) {
        this.master = master;
        this.slave = slave;
    }

    public RedisServer getMaster() {
        return master;
    }

    public void setMaster(RedisServer master) {
        this.master = master;
    }
    
    public RedisServer getSlave() {
        return slave;
    }
    
    public void setSlave(RedisServer slave) {
        this.slave = slave;
    }
    
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append(master).append(slave).append(master.getSlotString()).
                toString();
    }
    
	public int hashCode() {
	    return new HashCodeBuilder(17, 37).
	            append(master).
	            append(slave).
	            toHashCode();
	}

	public boolean equals(Object obj) {
	    if (obj == null) { return false; }
	    if (obj == this) { return true; }
	    if (obj.getClass() != getClass()) {
	        return false;
	    }
	    RedisNode node = (RedisNode) obj;
	    return new EqualsBuilder().
	            append(master, node.getMaster()).
	            append(slave, node.getSlave()).
	            isEquals();
	} 
    
}
