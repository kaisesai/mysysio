package com.liukai.sysio.netty.rpc.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端工厂
 * <p>
 * 负责创建客户端 Channel 连接池，每个服务有自己的连接池
 */
public class ClientFactory {
  
  public static final int POOL_SIZE = 10;
  
  private static final ClientFactory CLIENT_FACTORY = new ClientFactory();
  
  private final ConcurrentHashMap<InetSocketAddress, ClientPool> clientPoolMap
    = new ConcurrentHashMap<>();
  
  private ClientFactory() {
  }
  
  public static ClientFactory getClientFactory() {
    return CLIENT_FACTORY;
  }
  
  /**
   * 根据服务名称获取连接
   *
   * @param serverName 服务名称
   * @return
   */
  public synchronized MyClient getMyClient(InetSocketAddress address) {
    ClientPool clientPool = clientPoolMap.get(address);
    if (clientPool != null) {
      return clientPool.getClient(address);
    }
    clientPool = new ClientPool(POOL_SIZE);
    clientPoolMap.putIfAbsent(address, clientPool);
    return clientPool.getClient(address);
  }
  
}
