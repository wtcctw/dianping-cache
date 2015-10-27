package com.dianping.squirrel.client.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;

@Component
public class SpringTest {

    @Autowired
    private StoreClient storeClient;
    
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        StoreClient sc = (StoreClient) context.getBean("storeClient");
        Object value = sc.get(new StoreKey("myredis", "string"));
        System.out.println(value);
        SpringTest springTest = (SpringTest) context.getBean("springTest");
        value = springTest.getStoreClient().get(new StoreKey("myredis", "string"));
        System.out.println(value);
    }
    
    public StoreClient getStoreClient() {
        return storeClient;
    }
}
