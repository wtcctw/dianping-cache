package com.dianping.cache.service;

import com.dianping.cache.entity.ReshardRecord;

import java.util.List;

/**
 * Created by dp on 15/12/25.
 */
public interface ReshardRecordService {
    List<ReshardRecord> getAvaliableRecords(String cluster);
    void insert(ReshardRecord reshardRecord);
}
