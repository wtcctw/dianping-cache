package com.dianping.cache.scale.cluster.redis;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dp on 15/12/29.
 */
public class Slot {
    public static List<Integer> slotStringToList(String slotString){
        if (slotString == null)
            return new ArrayList<Integer>();
        String[] segments = slotString.split(",");
        List<Integer> slotList = new ArrayList<Integer>();
        for (String segment : segments) {
            segment = segment.trim();
            if (StringUtils.isEmpty(segment))
                continue;
            int idx = segment.indexOf('-');
            if (idx == -1) {
                slotList.add(Integer.parseInt(segment));
            } else if (segment.startsWith("[")) { // 正在传输
            } else {
                int end = Integer.parseInt(segment.substring(idx + 1).trim());
                int start = Integer.parseInt(segment.substring(0, idx).trim());
                if (end < start) {
                    start = start ^ end;
                    end = start ^ end;
                    start = start ^ end;
                }
                for (int i = start; i <= end; i++) {
                    slotList.add(i);
                }

            }
        }
        // deduplicate
        Collections.sort(slotList);
        List<Integer> newList = new ArrayList<Integer>();
        if (slotList.size() > 0) {
            newList.add(slotList.get(0));
        }
        for (int i = 1; i < slotList.size(); i++) {
            int n = slotList.get(i);
            if (n != slotList.get(i - 1)) {
                newList.add(n);
            }
        }
        return newList;
    }

    public static String slotListToString(List<Integer> slotList){
        Collections.sort(slotList);
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < slotList.size()) {
            int j = i;
            for (; j < slotList.size() - 1
                    && (slotList.get(j + 1) == slotList.get(j) + 1 || slotList
                    .get(j + 1).equals(slotList.get(j))); j++) {
            }
            if (j == i || slotList.get(i).equals(slotList.get(j))) {
                ss.append(slotList.get(i)).append(',');
            } else {
                ss.append(slotList.get(i)).append('-').append(slotList.get(j))
                        .append(',');
            }
            i = j + 1;
        }
        return ss.length() > 0 ? ss.substring(0, ss.length() - 1) : "";
    }
}
