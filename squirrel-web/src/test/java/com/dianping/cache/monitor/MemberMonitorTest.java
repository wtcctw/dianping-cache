package com.dianping.cache.monitor;

import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class MemberMonitorTest {

    @Test
    public void testGetInstance() throws Exception {
        MemberMonitor memberMonitor = new MemberMonitor();
        System.out.println(memberMonitor.getMemberId());
        System.out.println(memberMonitor.isMaster());
        System.out.println(memberMonitor.getMemberCount());
        System.out.println(StringUtils.join(memberMonitor.getAllMembers(), ','));
        memberMonitor.refreshAll();
        System.out.println(memberMonitor.getMemberId());
        System.out.println(memberMonitor.isMaster());
        System.out.println(memberMonitor.getMemberCount());
        System.out.println(StringUtils.join(memberMonitor.getAllMembers(), ','));
        System.in.read();
    }

    @Test
    public void testIsMaster() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAllMembers() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetMemberCount() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetMemberId() {
        fail("Not yet implemented");
    }

}
