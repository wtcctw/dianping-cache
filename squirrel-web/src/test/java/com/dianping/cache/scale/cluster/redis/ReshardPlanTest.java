package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.ReshardRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dp on 15/12/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class ReshardPlanTest {

    private List<String> srcNodes = new ArrayList<String>(){{
        add("192.168.211.63:7004");
    }};
    private List<String> desNodes = new ArrayList<String>(){{
        add("192.168.211.63:7003");
    }};


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
        ReshardPlan reshardPlan = new ReshardPlan("redis-test", srcNodes, desNodes, true);
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }

        System.out.println("Start to Migrate");
        RedisManager.reshard(reshardPlan);
    }
}