package com.liukai.sysio.netty.rpc.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 的请求调用与结果映射
 */
public class RpcResult {
  
  private static final ConcurrentHashMap<Long, CompletableFuture<Object>> MAP
    = new ConcurrentHashMap<>();
  //
  // private static final ConcurrentHashMap<Long, Runnable> MAP = new ConcurrentHashMap<>();
  
  public static void add(Long requestId, CompletableFuture<Object> runnable) {
    MAP.putIfAbsent(requestId, runnable);
  }
  
  // public static Object
  
  public static void complete(Long requestId, Object value) {
    CompletableFuture<Object> runnable = MAP.get(requestId);
    if (runnable == null) {
      return;
    }
    // 完成任务
    runnable.complete(value);
    // runnable.run();
    // 删除任务
    MAP.remove(requestId);
  }
  
}
