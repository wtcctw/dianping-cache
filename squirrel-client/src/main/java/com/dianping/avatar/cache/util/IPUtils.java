/**
 * Project: com.dianping.avatar-core-1.0.0-SNAPSHOT
 * 
 * File Created at 2011-1-9
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.avatar.cache.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author danson.liu
 * 
 */
public abstract class IPUtils {

    private static Logger logger = LoggerFactory.getLogger(IPUtils.class);

    public static final String LOCAL_LOOP_ADDRESS = "127.0.0.1";

    public static final String LOCAL_ADDRESS_START = "192.168.";

    public static final String LOCAL = "本地";

    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Collection<String> getNoLoopbackIP4Addresses() {
        Collection<String> noLoopbackIP4Addresses = new ArrayList<String>();
        Collection<InetAddress> allInetAddresses = getAllHostAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()
                    && !Inet6Address.class.isInstance(address)) {
                noLoopbackIP4Addresses.add(address.getHostAddress());
            }
        }
        if (noLoopbackIP4Addresses.isEmpty()) {
            // 降低过滤标准，将site local address纳入结果
            for (InetAddress address : allInetAddresses) {
                if (!address.isLoopbackAddress() && !Inet6Address.class.isInstance(address)) {
                    noLoopbackIP4Addresses.add(address.getHostAddress());
                }
            }
        }
        return noLoopbackIP4Addresses;
    }

    /**
     * 获取第一个no loop address
     * 
     * @return first no loop address, or null if not exists
     */
    public static String getFirstNoLoopbackIP4Address() {
        Collection<String> allNoLoopbackIP4Addresses = getNoLoopbackIP4Addresses();
        if (allNoLoopbackIP4Addresses.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Cannot get ip address, seems you don't have a network card!");
            }
            return null;
        }
        return allNoLoopbackIP4Addresses.iterator().next();
    }

    /**
     * Retrive mac address for current machine
     */
    public static String getMacAddr() {
        String macAddr = "";
        String str = "";
        try {
            NetworkInterface nic = NetworkInterface.getByName("eth0");
            if (nic != null) {
                byte[] buf = nic.getHardwareAddress();
                if (buf != null) {
                    for (int i = 0; i < buf.length; i++) {
                        str = str + byteHEX(buf[i]);
                    }
                }
                macAddr = str.toUpperCase();
            } else {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                NetworkInterface networkInterface = null;
                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !Inet6Address.class.isInstance(inetAddress)) {
                            break;
                        }
                    }
                }
                if (networkInterface != null) {
                    byte[] buf = networkInterface.getHardwareAddress();
                    if (buf != null) {
                        for (int i = 0; i < buf.length; i++) {
                            str = str + byteHEX(buf[i]);
                        }
                    }
                }
                macAddr = str.toUpperCase();
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return macAddr;
    }
    
    /**
     * Retrive ip address for current machine
     */
    public static String getLocalIP() {
        String ip = "";
        try {
            Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                if (ni != null && !ni.getName().equals("eth0")) {
                    continue;
                } else {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address)
                            continue;
                        ip = ia.getHostAddress();
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return ip;
    }

    public static String byteHEX(byte ib) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] ob = new char[2];
        ob[0] = Digit[(ib >>> 4) & 0X0F];
        ob[1] = Digit[ib & 0X0F];
        String s = new String(ob);
        return s;
    }

}
