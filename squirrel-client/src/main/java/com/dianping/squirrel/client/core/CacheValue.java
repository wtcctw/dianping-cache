package com.dianping.squirrel.client.core;

import java.io.Serializable;

public class CacheValue implements Serializable {

	private static final long serialVersionUID = 1L;
	private Object v = null;
	private long exp;

	public CacheValue(Object v, long exp) {
		this.v = v;
		this.exp = exp;
	}

	public Object getV() {
		return v;
	}

	public void setV(Object v) {
		this.v = v;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

}
