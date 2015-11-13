package com.dianping.squirrel.client.impl.memcached;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetFuture;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.dianping.cat.Cat;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class NodeMonitor {

	private static Logger logger = LoggerFactory.getLogger(NodeMonitor.class);

	private static final String EVENT_TYPE_NODES = "Squirrel.memcached.nodes";

	private static final String EVENT_TYPE_SERVER = "Squirrel.memcached.server";

	private static final int NODE_MONITOR_PERIOD = ConfigManagerLoader.getConfigManager().getIntValue(
			"squirrel-client.monitor.memcached.nodes.period", 60);

	private static final boolean enableMonitorNodes = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"squirrel-client.monitor.memcached.nodes.enable", true);

	private static boolean enableMonitorServer = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"squirrel-client.monitor.memcached.server.enable", true);

	private static boolean enableMonitorServerForMget = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"squirrel-client.monitor.memcached.server.mget.enable", true);

	private static int serverMonitorPercent = ConfigManagerLoader.getConfigManager().getIntValue(
			"squirrel-client.monitor.memcached.server.percent", 100);

	private Timer timer;

	private Map<SocketAddress, CatServerNodeItem> ipCatServerNodeDataMap;

	private Map<SocketAddress, Collection<MemcachedNode>> ipMemcachedNodeMap;

	private Map<String, Collection<SocketAddress>> clusterNodes = new HashMap<String, Collection<SocketAddress>>();

	private static Random random = new Random();
	
    private static Field rvField = null;
    private static Field opField = null;
    private static Field opsField = null;
    
	static {
		try {
            rvField = GetFuture.class.getDeclaredField("rv");
            rvField.setAccessible(true);
        } catch (Throwable t) {
            logger.error("failed to get 'GetFuture.rv' field", t);
        }
        try {
            opField = OperationFuture.class.getDeclaredField("op");
            opField.setAccessible(true);
        } catch (Throwable t) {
            logger.error("failed to get 'OperationFuture.op' field", t);
        }
        try {
            opsField = BulkGetFuture.class.getDeclaredField("ops");
            opsField.setAccessible(true);
        } catch (Throwable t) {
            logger.error("failed to get 'BulkGetFuture.ops' field", t);
        }
        try {
            ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new ConfigChangeHandler());
        } catch (Exception e) {
            logger.error("faield to register config change listener", e);
        }
	}

	private static class ConfigChangeHandler implements ConfigChangeListener {

		@Override
		public void onChange(String key, String value) {
			if (key.endsWith("squirrel-client.monitor.memcached.server.enable")) {
				try {
					enableMonitorServer = Boolean.valueOf(value);
				} catch (RuntimeException e) {
					logger.warn("error while loading config:" + key + "=" + value + ", caused by " + e.toString());
				}
			} else if (key.endsWith("squirrel-client.monitor.memcached.server.mget.enable")) {
				try {
					enableMonitorServerForMget = Boolean.valueOf(value);
				} catch (RuntimeException e) {
					logger.warn("error while loading config:" + key + "=" + value + ", caused by " + e.toString());
				}
			} else if (key.endsWith("squirrel-client.monitor.memcached.server.percent")) {
				try {
					serverMonitorPercent = Integer.valueOf(value);
				} catch (RuntimeException e) {
					logger.warn("error while loading config:" + key + "=" + value + ", caused by " + e.toString());
				}
			}
		}

	}

	private class NodeStatusTimerTask extends TimerTask {
		public void run() {
			try {
				monitorServerNode();
				catServerNode();
			} catch (Throwable e) {
				logger.error("error with spymemcached node monitor", e);
			}
		}
	}

	private NodeMonitor() {
		if (enableMonitorNodes) {
			init();
		}
	}

	private static class CatServerNodeHolder {
		private static final NodeMonitor INSTANCE = new NodeMonitor();
	}

	public static NodeMonitor getInstance() {
		return CatServerNodeHolder.INSTANCE;
	}

	private void init() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new NodeStatusTimerTask(), NODE_MONITOR_PERIOD * 1000, NODE_MONITOR_PERIOD * 1000);
		ipCatServerNodeDataMap = new HashMap<SocketAddress, CatServerNodeItem>();
		ipMemcachedNodeMap = new HashMap<SocketAddress, Collection<MemcachedNode>>();
	}

	public synchronized void clear(String key) {
		Collection<SocketAddress> nodes = clusterNodes.get(key);
		if (!CollectionUtils.isEmpty(nodes)) {
			for (SocketAddress sa : nodes) {
				ipMemcachedNodeMap.remove(sa);
				ipCatServerNodeDataMap.remove(sa);
			}
			nodes.clear();
		}
	}

	private String getNodeAddress(MemcachedNode node) {
		if (node != null) {
			InetSocketAddress addr = (InetSocketAddress) node.getSocketAddress();
			return addr.getAddress().getHostAddress() + ":" + addr.getPort();
		}
		return "";
	}

	public void logNode(MemcachedClient client, Collection<String> keys, String status, String desc) {
		boolean enableMonitor = enableMonitorServerForMget;
		if (enableMonitor) {
			boolean isHit = random.nextInt(serverMonitorPercent) < 1;
			if (!isHit) {
				enableMonitor = false;
			}
		}
		if (enableMonitor) {
			try {
				for (String key : keys) {
					MemcachedNode node = client.getNodeLocator().getPrimary(key);
					Cat.logEvent(EVENT_TYPE_SERVER, getNodeAddress(node), status, desc);
				}
			} catch (Throwable t) {
				logger.warn("error while logging node:" + t.getMessage());
			}
		}
	}

	public void logNode(MemcachedClient client, String key, String status, String desc) {
		boolean enableMonitor = enableMonitorServer;
		if (enableMonitor) {
			boolean isHit = random.nextInt(serverMonitorPercent) < 1;
			if (!isHit) {
				enableMonitor = false;
			}
		}
		if (enableMonitor) {
			try {
				MemcachedNode node = client.getNodeLocator().getPrimary(key);
				Cat.logEvent(EVENT_TYPE_SERVER, getNodeAddress(node), status, desc);
			} catch (Throwable t) {
				logger.warn("error while logging node:" + t.getMessage());
			}
		}
	}

	public synchronized void addNodes(String key, Collection<MemcachedClient> memcachedClients) {
		if (enableMonitorNodes) {
			genIpMemcachedNodeMap(key, memcachedClients);
			genIpCatServerNodeDataMap();
		}
	}

	private void genIpMemcachedNodeMap(String key, Collection<MemcachedClient> memcachedClients) {
		Collection<SocketAddress> nodes = clusterNodes.get(key);
		if (nodes == null) {
			nodes = new HashSet<SocketAddress>();
			clusterNodes.put(key, nodes);
		}
		for (MemcachedClient memcachedClient : memcachedClients) {
			if (memcachedClient != null && memcachedClient.getNodeLocator() != null) {
				Collection<MemcachedNode> memcachedNodes = memcachedClient.getNodeLocator().getAll();
				for (MemcachedNode memcachedNode : memcachedNodes) {
					SocketAddress serverAddress = memcachedNode.getSocketAddress();
					Collection<MemcachedNode> tmpMemcachedNodes = ipMemcachedNodeMap.get(serverAddress);
					nodes.add(serverAddress);
					if (tmpMemcachedNodes == null) {
						tmpMemcachedNodes = new HashSet<MemcachedNode>();
						tmpMemcachedNodes.add(memcachedNode);
						ipMemcachedNodeMap.put(serverAddress, tmpMemcachedNodes);
					} else {
						tmpMemcachedNodes.add(memcachedNode);
					}
				}
			}
		}
	}

	private void genIpCatServerNodeDataMap() {
		for (SocketAddress socketAddress : ipMemcachedNodeMap.keySet()) {
			ipCatServerNodeDataMap.put(socketAddress, new CatServerNodeItem());
		}
	}

	private void catServerNode() {
		Cat.getProducer().logEvent(EVENT_TYPE_NODES, ipCatServerNodeDataMap.toString());
	}

	private void monitorServerNode() {
		for (Map.Entry<SocketAddress, Collection<MemcachedNode>> entry : ipMemcachedNodeMap.entrySet()) {
			SocketAddress socketAddress = entry.getKey();
			CatServerNodeItem catServerNodeData = ipCatServerNodeDataMap.get(socketAddress);
			catServerNodeData.clear();

			for (MemcachedNode memcachedNode : entry.getValue()) {
				CatServerNodeItem tmpCatServerNodeData = getServerNodeData(memcachedNode);
				catServerNodeData.add(tmpCatServerNodeData);
			}

			ipCatServerNodeDataMap.put(socketAddress, catServerNodeData);
		}
	}

	private CatServerNodeItem getServerNodeData(MemcachedNode memcachedNode) {
		String rawQueueStr = memcachedNode.toString();

		Integer readQueueSize = parseServerNodeData(rawQueueStr, "#Rops=", ",");
		Integer writeQueueSize = parseServerNodeData(rawQueueStr, "#Wops=", ",");
		Integer inputQueueSize = parseServerNodeData(rawQueueStr, "#iq=", ",");

		CatServerNodeItem catServerNodeItem = new CatServerNodeItem();
		catServerNodeItem.setReadQueueSize(readQueueSize);
		catServerNodeItem.setWriteQueueSize(writeQueueSize);
		catServerNodeItem.setInputQueueSize(inputQueueSize);

		return catServerNodeItem;
	}

	private int parseServerNodeData(String rawQueueStr, String startStr, String endStr) {
		int startIndex, endIndex;
		startIndex = rawQueueStr.indexOf(startStr);

		if (startIndex == -1) {
			throw new IllegalArgumentException();
		} else {
			startIndex += startStr.length();
			endIndex = rawQueueStr.indexOf(endStr, startIndex);

			if (endIndex == -1) {
				throw new IllegalArgumentException();
			} else {
				return Integer.parseInt(rawQueueStr.substring(startIndex, endIndex));
			}
		}
	}

	static class CatServerNodeItem {

		private Integer readQueueSize;

		private Integer writeQueueSize;

		private Integer inputQueueSize;

		public CatServerNodeItem() {
			clear();
		}

		public Integer getReadQueueSize() {
			return readQueueSize;
		}

		public void setReadQueueSize(Integer readQueueSize) {
			this.readQueueSize = readQueueSize;
		}

		public Integer getWriteQueueSize() {
			return writeQueueSize;
		}

		public void setWriteQueueSize(Integer writeQueueSize) {
			this.writeQueueSize = writeQueueSize;
		}

		public Integer getInputQueueSize() {
			return inputQueueSize;
		}

		public void setInputQueueSize(Integer inputQueueSize) {
			this.inputQueueSize = inputQueueSize;
		}

		public void clear() {
			this.readQueueSize = 0;
			this.writeQueueSize = 0;
			this.inputQueueSize = 0;
		}

		public void add(CatServerNodeItem catServerNodeItem) {
			this.readQueueSize += catServerNodeItem.getReadQueueSize();
			this.writeQueueSize += catServerNodeItem.getWriteQueueSize();
			this.inputQueueSize += catServerNodeItem.getInputQueueSize();
		}

		@Override
		public String toString() {
			return "(" + readQueueSize.toString() + "," + writeQueueSize.toString() + "," + inputQueueSize.toString()
					+ ")";
		}
	}

    public void logNode(OperationFuture future) {
        boolean enableMonitor = enableMonitorServer;
        if (enableMonitor) {
            boolean isHit = random.nextInt(serverMonitorPercent) < 1;
            if (!isHit) {
                enableMonitor = false;
            }
        }
        if (enableMonitor) {
            try {
                MemcachedNode node = getNode(future);
                if(node != null) {
                    Cat.logEvent(EVENT_TYPE_SERVER, getNodeAddress(node));
                }
            } catch (Throwable t) {
                logger.warn("error while logging node: " + t.getMessage());
            }
        }
    }
    
    private MemcachedNode getNode(OperationFuture future) throws Exception {
        if(opField != null && future != null) {
            Operation op = (Operation) opField.get(future);
            if(op != null) {
                return op.getHandlingNode();
            }
        }
        return null;
    }

    public void logNode(GetFuture future) {
        boolean enableMonitor = enableMonitorServer;
        if (enableMonitor) {
            boolean isHit = random.nextInt(serverMonitorPercent) < 1;
            if (!isHit) {
                enableMonitor = false;
            }
        }
        if (enableMonitor) {
            try {
                MemcachedNode node = getNode(future);
                if(node != null) {
                    Cat.logEvent(EVENT_TYPE_SERVER, getNodeAddress(node));
                }
            } catch (Throwable t) {
                logger.warn("error while logging node: " + t.getMessage());
            }
        }
    }

    private MemcachedNode getNode(GetFuture future) throws Exception {
        if(rvField != null && future != null) {
            OperationFuture rv = (OperationFuture) rvField.get(future);
            if(opField != null && rv != null) {
                Operation op = (Operation) opField.get(rv);
                if(op != null) {
                    return op.getHandlingNode();
                }
            }
        }
        return null;
    }

    public void logNode(BulkFuture future) {
        boolean enableMonitor = enableMonitorServerForMget;
        if (enableMonitor) {
            boolean isHit = random.nextInt(serverMonitorPercent) < 1;
            if (!isHit) {
                enableMonitor = false;
            }
        }
        if (enableMonitor) {
            try {
                List<MemcachedNode> nodes = getNodes(future);
                if(nodes != null) {
                    for (MemcachedNode node : nodes) {
                        Cat.logEvent(EVENT_TYPE_SERVER, getNodeAddress(node));
                    }
                }
            } catch (Throwable t) {
                logger.warn("error while logging node:" + t.getMessage());
            }
        }
    }

    private List<MemcachedNode> getNodes(BulkFuture future) throws Exception {
        if(opsField != null && future != null) {
            Collection<Operation> ops = (Collection<Operation>) opsField.get(future);
            if(ops != null) {
                List<MemcachedNode> nodes = new ArrayList<MemcachedNode>(ops.size());
                for(Operation op : ops) {
                    nodes.add(op.getHandlingNode());
                }
                return nodes;
            }
        }
        return null;
    }
    
}
