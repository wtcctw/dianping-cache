/**
 * Project: com.dianping.avatar-core-1.0.0-SNAPSHOT
 * 
 * File Created at 2011-1-9
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.squirrel.client.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author danson.liu
 * 
 */
public abstract class ClassUtils {

    /**
     * construct instance violently
     * 
     * @param <T>
     * @param clazz
     * @param parameters
     * @return
     */
    public static <T> T newInstance(Class<T> clazz, Object... parameters) {
        try {
            if (parameters == null) {
                parameters = new Object[0];
            }
            int paramLen = parameters.length;
            Class<?>[] parameterTypes = new Class<?>[paramLen];
            for (int i = 0; i < paramLen; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }
            Constructor<T> constructor = getMatchingDeclaredConstructor(clazz, parameterTypes);
            boolean accessible = constructor.isAccessible();
            if (accessible) {
                return constructor.newInstance(parameters);
            } else {
                synchronized (constructor) {
                    try {
                        constructor.setAccessible(true);
                        return constructor.newInstance(parameters);
                    } finally {
                        constructor.setAccessible(accessible);
                    }
                }
            }
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
        throw new IllegalStateException("Should never get here");
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getMatchingDeclaredConstructor(Class<T> clazz, Class<?>[] parameterTypes) {
        try {
            Constructor<T> ctor = clazz.getConstructor(parameterTypes);
            try {
                ctor.setAccessible(true);
            } catch (SecurityException se) {
                // do nothing
            }
            return ctor;

        } catch (NoSuchMethodException e) {
        }

        int paramSize = parameterTypes.length;
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        for (int i = 0, size = ctors.length; i < size; i++) {
            Class<?>[] ctorParams = ctors[i].getParameterTypes();
            int ctorParamSize = ctorParams.length;
            if (ctorParamSize == paramSize) {
                boolean match = true;
                for (int n = 0; n < ctorParamSize; n++) {
                    if (!MethodUtils.isAssignmentCompatible(ctorParams[n], parameterTypes[n])) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    Constructor<?> ctor = getDeclaredConstructor(ctors[i]);
                    if (ctor != null) {
                        return (Constructor<T>) ctor;
                    }
                }
            }
        }
        return null;
    }

    public static <T> Constructor<T> getDeclaredConstructor(Constructor<T> ctor) {
        if (ctor == null) {
            return (null);
        }
        Class<T> clazz = ctor.getDeclaringClass();
        if (Modifier.isPublic(clazz.getModifiers())) {
            return (ctor);
        }
        return null;

    }

    /**
     * getInstance from className by using reflection
     * @param <T>
     * @param className
     * @param interfaceClazz
     * @return instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Class<?> interfaceClazz) {
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException("\"className\" parameter must not be empty!");
        }
        if (interfaceClazz == null) {
            throw new IllegalArgumentException("\"interfaceClazz\" parameter must not be null!");
        }
        T result = null;
        Class<?> cz = null;
        try {
            cz = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
        }

        if (cz == null) {
            try {
                cz = ClassUtils.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("The implementation [" + className + "] class is not found.");
            }
        }

        if (!interfaceClazz.isAssignableFrom(cz)) {
            throw new IllegalArgumentException("The implementation[" + className + "] is not drived from "
                    + interfaceClazz.getName());
        }

        try {
            result = (T) cz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cann't instantiate " + interfaceClazz.getName() + " implementation["
                    + className + "]", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }

        return result;
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        Assert.notNull(clazz);
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> currentClazz = clazz; currentClazz != Object.class; currentClazz = currentClazz
                .getSuperclass()) {
            fields.addAll(Arrays.asList(currentClazz.getDeclaredFields()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

}
