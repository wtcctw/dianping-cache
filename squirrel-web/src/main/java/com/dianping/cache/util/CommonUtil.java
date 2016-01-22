package com.dianping.cache.util;

import java.text.DecimalFormat;

/**
 * Created by thunder on 16/1/20.
 */
public class CommonUtil {
    public static String ConvertBytesName(long amount) {
        String[] suffixs = {"b", "Kb", "Mb", "Gb", "Tb", "Pb"};
        int i = 0;
        long last = 0;
        while(amount / 1024 != 0) {
            last = amount % 1024;
            amount /= 1024;
            i++;
        }
        double last2f = ((int)(( (amount * 1024 + last) / 1024.0) * 100 + 0.5)) / 100.0;
        return last2f + suffixs[i];
    }
}
