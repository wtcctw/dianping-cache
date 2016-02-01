package com.dianping.cache.alarm.dataanalyse.task;

import com.dianping.cache.alarm.dataanalyse.service.MemcacheBaselineService;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;
import com.dianping.cache.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineCleanTask")
@Scope("prototype")
public class BaselineCleanTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RedisService redisService;

    @Autowired
    MemcacheBaselineService memcacheBaselineService;

    @Autowired
    RedisBaselineService redisBaselineService;

    public void run() {

        GregorianCalendar gc = new GregorianCalendar();
        Date now = new Date();
        gc.setTime(now);

        int remainDay = 0 - 8;//只保留最近一周计算出的历史记录
        gc.add(5, remainDay);

        Date endDate = gc.getTime();

        boolean memcacheResult = cleanMemcacheBaseline(endDate);

        boolean redisResult = cleanRedisBaseline(endDate);

        if (!memcacheResult) {
            logger.error("memcacheBaseline history clean error");
        }

        if (!redisResult) {
            logger.error("redisBaseline history clean error");
        }

    }

    private boolean cleanMemcacheBaseline(Date endDate) {

        String sqlForStart = "select * from memcache_baseline order by id asc limit 0,1";

        List<MemcacheBaseline> forStartId = memcacheBaselineService.search(sqlForStart);

        int startId = forStartId.get(0).getId();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String endString = df.format(endDate);

        String sqlForEnd = "select * from memcache_baseline where updateTime < '" + endString + "' order by id desc limit 0,1";

        List<MemcacheBaseline> forEndId = memcacheBaselineService.search(sqlForEnd);

        int endId = forEndId.get(0).getId();


        for (int i = startId; i <= endId; i += 200) {
            int end;
            if (i + 200 < endId) {
                end = i + 200;
            } else {
                end = endId;
            }
            String deleteSql = " delete from  memcache_baseline where id >= " + i + " and id <= " + end;

            memcacheBaselineService.search(deleteSql);

        }

        String checkSql = "select * from memcache_baseline where id >= " + startId + " and id <= " + endId;

        List<MemcacheBaseline> checkEnd = memcacheBaselineService.search(checkSql);

        if (checkEnd.isEmpty()) {
            logger.info("clean memcache history from "+ startId +" to "+ endId);

            return true;
        } else {
            return false;
        }
    }


    private boolean cleanRedisBaseline(Date endDate) {
        String sqlForStart = "select * from redis_baseline order by id asc limit 0,1";

        List<RedisBaseline> forStartId = redisBaselineService.search(sqlForStart);

        int startId = forStartId.get(0).getId();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String endString = df.format(endDate);

        String sqlForEnd = "select * from redis_baseline where updateTime < '" + endString + "' order by id desc limit 0,1";

        List<RedisBaseline> forEndId = redisBaselineService.search(sqlForEnd);

        int endId = forEndId.get(0).getId();


        for (int i = startId; i <= endId; i += 200) {
            int end;
            if (i + 200 < endId) {
                end = i + 200;
            } else {
                end = endId;
            }
            String deleteSql = " delete from  redis_baseline where id >= " + i + " and id <= " + end;

            memcacheBaselineService.search(deleteSql);

        }

        String checkSql = "select * from redis_baseline where id >= " + startId + " and id <= " + endId;

        List<RedisBaseline> checkEnd = redisBaselineService.search(checkSql);

        if (checkEnd.isEmpty()) {
            logger.info("clean redis history from "+ startId +" to "+ endId);
            return true;
        } else {
            return false;
        }
    }


    public static void main(String[] args) {
        BaselineCleanTask task = new BaselineCleanTask();
        task.run();
    }

}
