package com.dianping.cache.monitor;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TaskRunnerTest {

    @Test
    public void test() throws Exception {
        int loop = 1000;
        Random random = new Random();
        TaskRunner task = new TaskRunner("127.0.0.1:11211");
        for(int i=0; i<loop; i++) {
            task.run();
            ServerState ss = task.getServerState();
            System.out.println(ss);
            Thread.sleep(random.nextInt(5000));
        }
    }
    
    @Test
    public void testCheckNode() throws Exception {
        TaskRunner task = new TaskRunner("127.0.0.1:11211");
        boolean alive = task.checkNode();
        assertTrue(alive);
    }

}
