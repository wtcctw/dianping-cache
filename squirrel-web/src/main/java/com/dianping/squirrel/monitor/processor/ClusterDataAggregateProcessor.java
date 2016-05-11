package com.dianping.squirrel.monitor.processor;

import com.dianping.squirrel.monitor.data.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class ClusterDataAggregateProcessor extends AbstractProcessor{


    @Override
    public void process(Data data) {

    }

    @Override
    public List<String> getType() {
        return new ArrayList<String>(){{
            add(Data.DataType.RedisStats.toString());
        }};
    }

}
