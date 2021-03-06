package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.util.SpringLocator;
import com.dianping.squirrel.dao.AuthDao;
import com.dianping.squirrel.entity.Auth;
import com.dianping.squirrel.service.AuthService;
import com.dianping.squirrel.service.impl.AuthServiceImpl;
import com.dianping.squirrel.task.ClearCategoryTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

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
        ReshardPlan reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes, false,false);
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }


        System.out.println("Second----");
        reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes,false,false);
        reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }

        System.out.println("Third----");
        desNodes.add("127.0.0.1:7005");
        reshardPlan = new ReshardPlan("redis-wh", srcNodes, desNodes,false,false);
        reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }
    }

    @Test
    public void testReshard(){
        ReshardPlan reshardPlan = new ReshardPlan("redis-test", srcNodes, desNodes, false,false);List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }

        System.out.println("Start to Migrate");
       // RedisManager.reshard(reshardPlan);
    }

    @Test
    public void setStable(){
        Jedis srcNode = new Jedis("192.168.217.36",7003);
        Jedis destNode = new Jedis("192.168.211.63",7004);

        /** migrate every slot from src node to dest node */
        srcNode.clusterSetSlotStable(4098);
        destNode.clusterSetSlotStable(4098);
    }

    @Test
    public void testDao(){
        AuthDao authDao = SpringLocator.getBean("authDao");
        Auth auth = new Auth();
        auth.setApplications("a,b,c,d,e,r,f");
        auth.setStrict(false);
        auth.setResource("redis");
        auth.setPassword("1234566");
        authDao.insert(auth);

        Auth auth1 = authDao.findByResource("redis");
        System.out.println(auth1);

        auth1.setStrict(true);
        authDao.update(auth1);
        System.out.println(auth1);


        authDao.delete(auth1);
    }

    @Test
    public void testAuth() throws Exception {
        AuthService authService = SpringLocator.getBean(AuthServiceImpl.class);
        authService.authorize("application2","redis-wh");
    }



}