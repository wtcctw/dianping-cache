package com.dianping.cache.monitor;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.util.IPUtils;

public class MemberMonitor implements CuratorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberMonitor.class);
    
    private final String LOCAL_IP = IPUtils.getFirstNoLoopbackIP4Address();
    
    private final int STATE_SUCC = 1;
    
    private final int STATE_FAIL = -1;
    
    private CuratorFramework curatorClient = CuratorManager.getInstance().getCuratorClient();
    
    private String memberId;
    
    private NavigableMap<String, String> memberMap;
    
    private boolean isMaster;
    
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    
    private Lock rlock = rwlock.readLock();
    
    private Lock wlock = rwlock.writeLock();
    
    private int state = 0;
    
    public MemberMonitor() {
        CuratorManager.getInstance().ensurePath(Constants.MEMBER_PATH);
        CuratorManager.getInstance().addHandler(this);
        refreshAll();
    }

    void refreshAll() {
        wlock.lock();
        try {
            memberId = registerMembership();
            logger.info("registered membership, member id: " + memberId);
            memberMap = loadMembers();
            logger.info("loaded members: " + StringUtils.join(memberMap.values(), ','));
            isMaster = checkMaster();
            state = STATE_SUCC;
        } catch (Exception e) {
            state = STATE_FAIL;
            logger.error("failed to refresh membership", e);
        } finally {
            wlock.unlock();
        }
    }
    
    private boolean checkMaster() {
        boolean isMaster = false;
        Entry<String, String> firstMember = (memberMap == null ? null : memberMap.firstEntry());
        if(firstMember != null) {
            logger.info("master is " + firstMember.getKey() + ", ip is " + firstMember.getValue());
            isMaster = firstMember.getKey().equals(memberId);
        }
        return isMaster;
    }
    
    void refreshMembers() {
        wlock.lock();
        try {
            memberMap = loadMembers();
            logger.info("loaded members: " + StringUtils.join(memberMap.values(), ','));
            isMaster = checkMaster();
            state = STATE_SUCC;
        } catch (Exception e) {
            state = STATE_FAIL;
            logger.error("failed to refresh members", e);
        } finally {
            wlock.unlock();
        }
    }
    
    public String registerMembership() throws Exception {
        unregisterLegacyMembership();
        return registerNewMembership();
    }

    private void unregisterLegacyMembership() throws Exception {
        if(memberId != null) {
            String path = Constants.MEMBER_PATH + "/" + memberId;
            CuratorManager.getInstance().deletePath(path);
            logger.info("unregistered membership, member id: " + memberId);
            memberId = null;
        }
    }
    
    private String registerNewMembership() throws Exception {
        String path = Constants.MEMBER_PATH + "/member-";
        String actualPath = curatorClient.create().
                withMode(CreateMode.EPHEMERAL_SEQUENTIAL).
                forPath(path, LOCAL_IP.getBytes("UTF-8"));
        return actualPath.substring(Constants.MEMBER_PATH.length() + 1);
    }

    private NavigableMap<String, String> loadMembers() throws Exception {
        List<String> children = curatorClient.getChildren().watched().forPath(Constants.MEMBER_PATH);
        NavigableMap<String, String> memberMap = new TreeMap<String, String>();
        if(children != null && children.size() > 0) {
            for(String child : children) {
                byte[] data = curatorClient.getData().forPath(Constants.MEMBER_PATH + "/" + child);
                memberMap.put(child, new String(data, "UTF-8"));
            }
        }
        return memberMap;
    }
    
    public boolean isMaster() {
        rlock.lock();
        try {
            return (state == STATE_SUCC) ? isMaster : false;
        } finally {
            rlock.unlock();
        }
    }
    
    public Collection<String> getAllMembers() {
        rlock.lock();
        try {
            return state == STATE_SUCC ? memberMap.values() : null;
        } finally {
            rlock.unlock();
        }
    }
    
    public int getMemberCount() {
        rlock.lock();
        try {
            return state == STATE_SUCC ? memberMap.size() : 0;
        } finally {
            rlock.unlock();
        }
    }
    
    public String getMemberId() {
        rlock.lock();
        try {
            return state == STATE_SUCC ? memberId : null;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void reconnected() {
        refreshAll();
    }

    @Override
    public void eventReceived(WatchedEvent we) {
        String path = we.getPath();
        if(Constants.MEMBER_PATH.equals(path) && EventType.NodeChildrenChanged == we.getType()) {
            // member change
            refreshMembers();
        }
    }

}
