package com.dianping.squirrel.client.impl.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 用户
 * 
 * @author fei.chang
 *
 */
public class User implements Serializable{
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5931604754584382052L;
    
    private String userId;
    
    private String city;
    
    private List<String> dpIdList;
    
    private List<String> bankCardList;
    
    public User() {};
    
    public User(String userId, String city, String dpId, String bankCard) {
        this.userId = userId;
        this.city = city;
        addDpId(dpId);
        addBankCard(bankCard);
    }
    
    public void addDpId(String dpId){
        if(null == dpIdList){
            dpIdList = new ArrayList<String>();
        }
        dpIdList.add(dpId);
    }
    
    public void addBankCard(String cardToken){
        if(null == bankCardList){
            bankCardList = new ArrayList<String>();
        }
        bankCardList.add(cardToken);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getDpIdList() {
        return dpIdList;
    }

    public void setDpIdList(List<String> dpIdList) {
        this.dpIdList = dpIdList;
    }

    public List<String> getBankCardList() {
        return bankCardList;
    }

    public void setBankCardList(List<String> bankCardList) {
        this.bankCardList = bankCardList;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
    
    public boolean equals(Object object) {
        if(object instanceof User) {
            User user = (User)object;
            return new EqualsBuilder().append(this.userId, user.userId).append(this.city, user.city).
                            append(this.dpIdList, user.dpIdList).append(this.bankCardList, user.bankCardList).isEquals();
        }
        return false;
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(userId).append(city).append(dpIdList).append(bankCardList).toHashCode();
    }
}