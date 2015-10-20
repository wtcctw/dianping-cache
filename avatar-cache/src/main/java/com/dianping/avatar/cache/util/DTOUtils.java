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
package com.dianping.avatar.cache.util;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Util to support dto transform
 * @author danson.liu
 *
 */
public class DTOUtils {

    public static void copyProperties(Object dest, Object orig) {
        try {
            BeanUtils.copyProperties(dest, orig);
        } catch (Exception e) {
            throw new RuntimeException("Copy properties to dto failed.", e);
        }
    }
    
}
