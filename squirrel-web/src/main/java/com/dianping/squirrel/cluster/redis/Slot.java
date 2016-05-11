package com.dianping.squirrel.cluster.redis;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class Slot {
    public static final int SLOTSNUM = 16384;
    private List<Integer> slots;
    private boolean migrating;
    private boolean importing;
    private boolean exporting;
    private List<Integer> importingSlots;
    private List<Integer> exportingSlots;

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public boolean getMigrating() {
        return migrating;
    }

    public void setMigrating(boolean migrating) {
        this.migrating = migrating;
    }

    public boolean getImporting() {
        return importing;
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    public boolean getExporting() {
        return exporting;
    }

    public void setExporting(boolean exporting) {
        this.exporting = exporting;
    }

    public List<Integer> getImportingSlots() {
        return importingSlots;
    }

    public void setImportingSlots(List<Integer> importingSlots) {
        this.importingSlots = importingSlots;
    }

    public List<Integer> getExportingSlots() {
        return exportingSlots;
    }

    public void setExportingSlots(List<Integer> exportingSlots) {
        this.exportingSlots = exportingSlots;
    }
}
