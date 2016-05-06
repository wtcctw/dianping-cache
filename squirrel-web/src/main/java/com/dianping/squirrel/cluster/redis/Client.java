package com.dianping.squirrel.cluster.redis;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class Client {
    private int id;
    private String addr;
    private int fd;
    private String name;
    private int age;
    private int idle;
    private String flags;
    private int db;
    private int sub;
    private int psub;
    private int multi;
    private int qbuf;
    //private int qbuf_free;
    private int obl;
    private long oll;
    private long omem;
    private String events;
    private String cmd;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getFd() {
        return fd;
    }

    public void setFd(int fd) {
        this.fd = fd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getIdle() {
        return idle;
    }

    public void setIdle(int idle) {
        this.idle = idle;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public int getSub() {
        return sub;
    }

    public void setSub(int sub) {
        this.sub = sub;
    }

    public int getPsub() {
        return psub;
    }

    public void setPsub(int psub) {
        this.psub = psub;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }

    public int getQbuf() {
        return qbuf;
    }

    public void setQbuf(int qbuf) {
        this.qbuf = qbuf;
    }

    public int getObl() {
        return obl;
    }

    public void setObl(int obl) {
        this.obl = obl;
    }

    public long getOll() {
        return oll;
    }

    public void setOll(long oll) {
        this.oll = oll;
    }

    public long getOmem() {
        return omem;
    }

    public void setOmem(long omem) {
        this.omem = omem;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
