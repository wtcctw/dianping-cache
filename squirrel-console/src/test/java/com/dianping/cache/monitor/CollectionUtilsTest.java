package com.dianping.cache.monitor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.dianping.cache.util.CollectionUtils;

import edu.emory.mathcs.backport.java.util.Arrays;

public class CollectionUtilsTest {

    @Test
    public void testSubtract() {
        List<String> lista = Arrays.asList(new String[] {"a", "b", "c"});
        List<String> listb = Arrays.asList(new String[] {"b", "c", "d"});
        Collection<String> diff = CollectionUtils.subtract(lista, listb);
        assertEquals(diff.size(), 1);
        System.out.println(CollectionUtils.toString(diff));
        diff = CollectionUtils.subtract(listb, lista);
        assertEquals(diff.size(), 1);
        System.out.println(CollectionUtils.toString(diff));
    }

    @Test
    public void testJoinCollectionOfT() {
        String str = CollectionUtils.toString(null);
        assertEquals(str, "");
        str = CollectionUtils.toString(Arrays.asList(new String[0]));
        assertEquals(str, "");
        str = CollectionUtils.toString(Arrays.asList(new String[] {"a", "b", "c"}));
        assertEquals(str, "a,b,c");
    }

    @Test
    public void testJoinCollectionOfTChar() {
        String str = CollectionUtils.toString(null, '|');
        assertEquals(str, "");
        str = CollectionUtils.toString(Arrays.asList(new String[0]), '|');
        assertEquals(str, "");
        str = CollectionUtils.toString(Arrays.asList(new String[] {"a", "b", "c"}), '|');
        assertEquals(str, "a|b|c");
    }

    @Test
    public void testSize() {
        int size = CollectionUtils.size(null);
        assertEquals(size, 0);
        size = CollectionUtils.size(new ArrayList());
        assertEquals(size, 0);
        size = CollectionUtils.size(Arrays.asList(new String[] {"a", "b", "c"}));
        assertEquals(size, 3);
    }

}
