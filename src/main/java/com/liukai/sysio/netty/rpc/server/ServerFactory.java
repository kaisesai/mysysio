package com.liukai.sysio.netty.rpc.server;

import com.liukai.sysio.netty.rpc.service.Car;
import com.liukai.sysio.netty.rpc.service.CarImpl;

import java.util.concurrent.ConcurrentHashMap;

public class ServerFactory {
  
  private static final ConcurrentHashMap<String, Object> SERVER_MAP = new ConcurrentHashMap<>();
  
  static {
    SERVER_MAP.put(Car.class.getName(), new CarImpl());
  }
  
  public static Object getServer(String serverName) {
    return SERVER_MAP.get(serverName);
  }
  
}
