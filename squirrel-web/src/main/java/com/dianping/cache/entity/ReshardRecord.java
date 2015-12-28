package com.dianping.cache.entity;

/**
 * Created by dp on 15/12/25.
 */
public class ReshardRecord {
    private int id;
    private String cluster;
    private String srcNode;
    private String desNode;
    private int slotsToMigrate;
    private int slotsDone = 0;
    private int slotMigrating = -1;
    private int order;
    private boolean migrateSwitch;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getSrcNode() {
        return srcNode;
    }

    public void setSrcNode(String srcNode) {
        this.srcNode = srcNode;
    }

    public String getDesNode() {
        return desNode;
    }

    public void setDesNode(String desNode) {
        this.desNode = desNode;
    }

    public int getSlotsToMigrate() {
        return slotsToMigrate;
    }

    public void setSlotsToMigrate(int slotsToMigrate) {
        this.slotsToMigrate = slotsToMigrate;
    }

    public int getSlotsDone() {
        return slotsDone;
    }

    public void setSlotsDone(int slotsDone) {
        this.slotsDone = slotsDone;
    }

    public int getSlotMigrating() {
        return slotMigrating;
    }

    public void setSlotMigrating(int slotMigrating) {
        this.slotMigrating = slotMigrating;
    }

    public boolean isMigrateSwitch() {
        return migrateSwitch;
    }

    public void setMigrateSwitch(boolean migrateSwitch) {
        this.migrateSwitch = migrateSwitch;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
