package com.dianping.cache.monitor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class TaskManagerTest {

    @Test
    public void test() throws Exception {
        TaskManager taskManager = new TaskManager();
        taskManager.start();
        new CountDownLatch(1).await();
    }

}
