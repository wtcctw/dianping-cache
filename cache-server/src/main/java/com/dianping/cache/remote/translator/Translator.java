/**
 * Project: avatar-common-remote
 * 
 * File Created at 2010-10-15
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.cache.remote.translator;

/**
 * Translator interface to translate source object to destination object
 * @author danson.liu
 *
 */
public interface Translator<S, T> {

    /**
     * translate source to destination
     * @param source
     * @return
     */
    T translate(S source);
    
}
