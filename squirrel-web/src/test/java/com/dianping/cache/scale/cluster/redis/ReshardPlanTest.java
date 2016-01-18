package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.ReshardRecord;
import com.dianping.squirrel.task.ClearCategoryTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by dp on 15/12/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class ReshardPlanTest {

    private List<String> srcNodes = new ArrayList<String>(){{
        add("192.168.217.36:7003");
    }};
    private List<String> desNodes = new ArrayList<String>(){{
        add("192.168.211.63:7004");
    }};

    @Test
    public void testDelete() {
        ClearCategoryTask task = new ClearCategoryTask("redis-del");
        task.run();
    }

    @Test
    public void testMakePlan() throws Exception {
        ReshardPlan reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes, false);
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }


        System.out.println("Second----");
        reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes, true);
        reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }

        System.out.println("Third----");
        desNodes.add("127.0.0.1:7005");
        reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes, false);
        reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }
    }

    @Test
    public void testReshard(){
        ReshardPlan reshardPlan = new ReshardPlan("redis-test", srcNodes, desNodes, false);List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }

        System.out.println("Start to Migrate");
        RedisManager.reshard(reshardPlan);
    }

    @Test
    public void setStable(){
        Jedis srcNode = new Jedis("192.168.217.36",7003);
        Jedis destNode = new Jedis("192.168.211.63",7004);

        /** migrate every slot from src node to dest node */
        srcNode.clusterSetSlotStable(4098);
        destNode.clusterSetSlotStable(4098);
    }
}