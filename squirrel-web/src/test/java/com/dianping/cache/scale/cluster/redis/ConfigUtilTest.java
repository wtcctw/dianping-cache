package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.util.ConfigUrlUtil;
import org.junit.Test;

import java.util.*;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/19.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class ConfigUtilTest {
    @Test
    public void testSplice(){
        List<String> serverslist = new ArrayList<String>(){{
            add("127.0.0.1");
            add("128.0.0.1");
            add("129");
        }};
        Map<String,String> properties = new HashMap<String, String>(){{
            put("maxReadTime","1000");
            put("password","1234567");
        }};
        String url = ConfigUrlUtil.spliceRedisUrl(serverslist,properties);
        System.out.print(url);

        Map<String,String> p = ConfigUrlUtil.properties(url);
        System.out.println(p);

        List<String> servers = ConfigUrlUtil.serverList(url);
        System.out.println(servers.toString());

        String password = ConfigUrlUtil.getProperty(url,"password7");

        System.out.println(password);

    }
}
