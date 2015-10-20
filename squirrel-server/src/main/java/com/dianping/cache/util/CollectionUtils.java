package com.dianping.cache.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {

    public static <T> Collection<T> subtract(Collection<T> source, Collection<T> dest) {
        if(source == null) {
            throw new NullPointerException("source collection is null");
        }
        List<T> collection = new ArrayList<T>(source);
        if(dest != null) {
            collection.removeAll(dest);
        }
        return collection;
    }
    
    public static <T> String toString(Collection<T> list) {
        return toString(list, ',');
    }
    
    public static <T> String toString(Collection<T> list, char sp) {
        if(list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for(T e : list) {
            buf.append(e).append(sp);
        }
        buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }
    
    public static int size(Collection<?> c) {
        return c == null ? 0 : c.size();
    }
    
    public static List<String> toList(String string, String sp) {
        String[] elements = string.split(sp);
        List<String> list = new ArrayList<String>(elements.length);
        for(String element : elements) {
            element = element.trim();
            if(element.length() > 0) {
                list.add(element);
            }
        }
        return list;
    }

    public static boolean isEmpty(List<?> devices) {
        return devices == null ? true : devices.size() == 0;
    }
}
