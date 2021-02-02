package com.liukai.sysio.netty.rpc.service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 派发器
 */
public class Dispatcher {
  
  private final ConcurrentHashMap<String, Object> SERVER_MAP = new ConcurrentHashMap<>();
  
  private Dispatcher() {
  }
  
  public static Dispatcher getInstance() {
    return new Dispatcher();
  }
  
  public void registry(String name, Object server) {
    SERVER_MAP.put(name, server);
  }
  
  public Object getServer(String serverName) {
    return SERVER_MAP.get(serverName);
  }
  
}
