package com.dianping.cache.alarm.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.cache.alarm.entity.AlarmRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by lvshiyun on 15/11/22.
 */
@Transactional
public interface AlarmRecordDao extends GenericDao{

    /**
     * @param record
     * @return
     */
    boolean insert( AlarmRecord record);

    /**
     * retrieve all alarmRecords
     * @return
     */
    List<AlarmRecord> findAll();

    /**
     * find by type
     *
     * @param type
     * @return
     */
    List<AlarmRecord> findByType( int type);

    /**
     * @param offset,limit
     * @return
     */
    List<AlarmRecord>findByPage(@Param("offset")int offset, @Param("limit")int limit);


    public static class AlarmParam{

        private String receiver;

        private Date startTime;

        private Date endTime;

        private int offset;

        private int limit;

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

}
