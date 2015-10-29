package com.dianping.squirrel.client.impl.memcached;

public enum CASResponse {
	  /**
	   * Status indicating that the CAS was successful and the new value is stored
	   * in the cache.
	   */
	  OK,
	  /**
	   * Status indicating the value was not found in the cache (an add operation
	   * may be issued to store the value).
	   */
	  NOT_FOUND,
	  /**
	   * Status indicating the value was found in the cache, but exists with a
	   * different CAS value than expected. In this case, the value must be
	   * refetched and the CAS operation tried again.
	   */
	  EXISTS,
	  /**
	   * Status indicating there was an error in specifying the arguments for
	   * the Observe.
	   */
	  OBSERVE_ERROR_IN_ARGS,
	  /**
	   * Status indicating the CAS operation succeeded but the value was
	   * subsequently modified during Observe.
	   */
	  OBSERVE_MODIFIED,
	  /**
	   * Status indicating there was a Timeout in the Observe operation.
	   */
	  OBSERVE_TIMEOUT;
	}
