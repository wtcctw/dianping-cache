package com.dianping.squirrel.entity;


/**
 * Created by thunder on 16/1/5.
 */
public class Task {
    int id;
    int stat;
    int statMin;
    int statMax;
    String type;
    long commitTime;
    long startTime;
    long endTime;
    String commiter;
    String description;
    String typeDescription;

    public String getCommiter() {
        return commiter;
    }

    public void setCommiter(String commiter) {
        this.commiter = commiter;
    }

    public String getTypeDescription() {
        return type;
    }

    public void setTypeDescription(String typeDescription) {
        this.type = typeDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getStatMin() {
        return statMin;
    }

    public void setStatMin(int statMin) {
        this.statMin = statMin;
    }

    public int getStatMax() {
        return statMax;
    }

    public void setStatMax(int statMax) {
        this.statMax = statMax;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(long commitTime) {
        this.commitTime = commitTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
