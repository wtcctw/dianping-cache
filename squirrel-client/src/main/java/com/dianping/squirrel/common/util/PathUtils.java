package com.dianping.squirrel.common.util;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PathUtils {

    public static final String CACHE_SERVICE_PATH = "/dp/cache/service";
    public static final String CACHE_MANAGER_PATH = "/dp/cache/monitor/manager";
    public static final String CACHE_CATEGORY_PATH = "/dp/cache/category";
    public static final String CACHE_RUNTIME_PATH = "/dp/cache/runtime";
    private static final String LOCAL_IP = getFirstLocalIp();
    
    private static final String CONFIG_KEY_ZOOKEEPER_ENABLED = "avatar-cache.zookeeper.enabled";
    
    private static final String[] SpecialCategories = {
        "DianPing.Common.StaticFile",
        "DianPing.Common.CityDAC",
        "DianPing.Common.ConfigurationDAC",
        "DianPing.API.RegionAPIService_CustomerCategory",
    };
    
    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    public static String getCategoryFromKey(String key) {
        if(key == null) {
            return null;
        }
        int idx = key.indexOf('.');
        if(idx == -1) {
            return null;
        }
        String category = key.substring(0, idx);
        if(category.equals("DianPing")) {
            for(int i=0; i<SpecialCategories.length; i++) {
                if(key.startsWith(SpecialCategories[i])) {
                    category = SpecialCategories[i];
                }
            }
        }
        return category;
    }
    
    public static String getCategoryPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').append(category);
        return buf.toString();
    }
    
    public static String getVersionPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').
            append(category).append("/version");
        return buf.toString();
    }
    
    public static String getExtensionPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').
            append(category).append("/extension");
        return buf.toString();
    }
    
    public static String getKeyPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').
        append(category).append("/key");
        return buf.toString();
    }
    
    public static String getBatchKeyParentPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').
        append(category).append("/keys");
        return buf.toString();
    }
    
    public static String getBatchKeyPath(String category) {
        int bucket = category.hashCode() % 50 + 50;
        StringBuilder buf = new StringBuilder(100);
        buf.append(CACHE_CATEGORY_PATH).append('/').append(bucket).append('/').
        append(category).append("/keys/").append(LOCAL_IP);
        return buf.toString();
    }    
    
    public static String getServicePath(String service) {
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_SERVICE_PATH).append('/').append(service);
        return buf.toString();
    }

    public static String getManagerPath(String service,String swimlane) {
        return getManagerPath(service) + "/" + swimlane;
    }

    public static String getManagerPath(String service) {
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_MANAGER_PATH).append('/').append(service);
        return buf.toString();
    }

    public static String getServicePath(String service,String swimlane) {
        return getServicePath(service) + "/" + swimlane;
    }

    public static String getRuntimeServicePath(String appName) {
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_RUNTIME_PATH).append('/').append(appName).append("/service");
        return buf.toString();
    }
    
    public static String getRuntimeCategoryPath(String appName) {
        StringBuilder buf = new StringBuilder(80);
        buf.append(CACHE_RUNTIME_PATH).append('/').append(appName).append("/category");
        return buf.toString();
    }
    
    public static boolean isZookeeperEnabled() {
        return configManager.getBooleanValue(CONFIG_KEY_ZOOKEEPER_ENABLED, true);
    }
    
    public static List<InetAddress> getAllLocalAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            List<InetAddress> addresses = new ArrayList<InetAddress>();

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

    public static List<String> getAllLocalIp() {
        List<String> noLoopbackAddresses = new ArrayList<String>();
        List<InetAddress> allInetAddresses = getAllLocalAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                noLoopbackAddresses.add(address.getHostAddress());
            }
        }

        return noLoopbackAddresses;
    }
    
    public static String getFirstLocalIp() {
        List<String> allNoLoopbackAddresses = getAllLocalIp();
        if (allNoLoopbackAddresses.isEmpty()) {
            throw new IllegalStateException("Sorry, seems you don't have a network card :( ");
        }
        return allNoLoopbackAddresses.get(allNoLoopbackAddresses.size() - 1);
    }
    
    public static void main(String[] args) throws Exception {
    	System.out.println(getBatchKeyPath("oUrlRegexList"));
    }

    public static String getCagegoryFromPath(String path) {
        if(path == null || !path.endsWith("/keys"))
            return null;
        int idx = path.lastIndexOf('/');
        if(idx == -1) {
            return null;
        }
        int idx2 = path.lastIndexOf('/', idx-1);
        if(idx2 != -1) {
            return path.substring(idx2+1, idx);
        }
        return null;
    }
    
}
