package com.dianping.squirrel.client.impl.danga;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.Bean;

public class DangaStoreClientImplTest {

    private static final String CATEGORY = "danga";
    
    private static final String STORE_TYPE = "danga";
    
    private static final String VALUE = "dp@123456";
    
    private static final Bean BEAN = new Bean(12345678, "BEAN@12345678");
    
	@Test
	public void testSet() throws Exception{
		DangaStoreClient client = (DangaStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE); 
		StoreKey key = new StoreKey(CATEGORY, "t");
		Object result = client.set(key, VALUE);
		assertEquals(Boolean.TRUE, result);
		
		
		Thread.sleep(100);
		result = client.get(key);
        assertEquals(VALUE, result);
        

		Thread.sleep(500);
		result = client.get(key);
        assertEquals(VALUE, result);
        
        
        
        result = client.set(key, BEAN);
        assertEquals(Boolean.TRUE, result);
        result = client.get(key);
        assertEquals(BEAN, result);
	}
	
	
	@Test
    public void testCas() {
        DangaStoreClient client = (DangaStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        client.set(key, VALUE);
        CASValue<String> casValue = client.gets(key);
        assertEquals(VALUE, casValue.getValue());
        System.out.println(casValue);
        boolean result = client.cas(key, casValue.getCas()-1, "cas-value");
        assertEquals(false, result);
        result = client.cas(key, casValue.getCas(), "cas-value2");
        assertEquals(true, result);
        String value = client.get(key);
        assertEquals("cas-value2", value);
    }

    @Test
    public void testMultiServer(){

    }

	
	
}
