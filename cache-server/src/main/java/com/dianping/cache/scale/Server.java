package com.dianping.cache.scale;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Server implements Node {
    
    private String address;
    private String ip;
    private int port;
    
    public Server(String address) {
        this.address = address;
        parseAddress(address);
    }
    
    private void parseAddress(String address) {
        if(address == null) {
            throw new NullPointerException("server address is null");
        }
        String[] parts = address.split(":");
        if(parts.length != 2) {
            throw new IllegalArgumentException("invalid server address: " + address);
        }
        this.ip = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }

    public String getAddress() {
        return address;
    }
    
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
    
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append(address).
                toString();
    }
    
    public boolean equals(Object object) {
        if(object instanceof Server) {
            Server server2 = (Server)object;
            return new EqualsBuilder().append(address, server2.address).isEquals();
        }
        return false;
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(address).toHashCode();
    }
    
}
