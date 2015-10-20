package com.dianping.cache.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class JsonUtilsTest {

    @Test
    public void testToStr() throws Exception {
        // normal
        TestObject2 object2 = new TestObject2("hello", "world");
        String json = JsonUtils.toStr(object2);
        System.out.println(json);
        // null attribute also serialized, 
        object2 = new TestObject2("hello", null);
        json = JsonUtils.toStr(object2);
        System.out.println(json);
        // ignore attribute when serialize
        TestObject3 object3 = new TestObject3("a", "b", "c");
        json = JsonUtils.toStr(object3);
        System.out.println(json);
        json = "{\"attrA\":\"a\",\"attrB\":\"b\",\"attrC\":\"c\"}";
        // ignore unknown attribute achieve compatibility
        object2 = JsonUtils.fromStr(json, TestObject2.class);
        System.out.println(object2);
        // ignored attribute will not be deserialized
        object3 = JsonUtils.fromStr(json, TestObject3.class);
        System.out.println(object3);
    }

    public static class TestObject2 {
        private String attrA;
        private String attrB;

        public TestObject2() {}
        
        public TestObject2(String attrA, String attrB) {
            this.attrA = attrA;
            this.attrB = attrB;
        }
        
        public String getAttrA() {
            return attrA;
        }

        public void setAttrA(String attrA) {
            this.attrA = attrA;
        }

        public String getAttrB() {
            return attrB;
        }

        public void setAttrB(String attrB) {
            this.attrB = attrB;
        }
        
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }
    
    @JsonIgnoreProperties({"attrC"})
    public static class TestObject3 {
        private String attrA;
        private String attrB;
        private String attrC;

        public TestObject3() {}
        
        public TestObject3(String attrA, String attrB, String attrC) {
            this.attrA = attrA;
            this.attrB = attrB;
            this.attrC = attrC;
        }
        
        public String getAttrA() {
            return attrA;
        }

        public void setAttrA(String attrA) {
            this.attrA = attrA;
        }

        public String getAttrB() {
            return attrB;
        }

        public void setAttrB(String attrB) {
            this.attrB = attrB;
        }
        
        public String getAttrC() {
            return attrC;
        }

        public void setAttrC(String attrC) {
            this.attrC = attrC;
        }
        
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

}
