package com.dianping.cache.monitor;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotifyManagerTest {

    @Test
    public void testNotifyString() {
        NotifyManager.getInstance().notifyEmail("hello world", "hello world!");
        NotifyManager.getInstance().notifySms("hello world!");
        NotifyManager.getInstance().notifyWeixin("hello world!");
    }

}
