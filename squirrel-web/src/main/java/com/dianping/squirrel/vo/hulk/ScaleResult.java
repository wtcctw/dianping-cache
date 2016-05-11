package com.dianping.squirrel.vo.hulk;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/11.
 */
public class ScaleResult {
    private boolean failed;
    private String status;
    private HulkInstance[] instances;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HulkInstance[] getInstances() {
        return instances;
    }

    public void setInstances(HulkInstance[] instances) {
        this.instances = instances;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
