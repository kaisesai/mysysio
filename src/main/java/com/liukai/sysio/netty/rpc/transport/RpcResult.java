package com.liukai.sysio.netty.rpc.transport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 的请求调用与结果映射
 */
public class RpcResult {
  
  private static final ConcurrentHashMap<String, CompletableFuture<Object>> MAP
    = new ConcurrentHashMap<>();
  
  public static void add(String requestId, CompletableFuture<Object> runnable) {
    MAP.putIfAbsent(requestId, runnable);
  }
  
  public static void complete(String requestId, Object value) {
    CompletableFuture<Object> runnable = MAP.get(requestId);
    if (runnable == null) {
      return;
    }
    // 完成任务
    runnable.complete(value);
    // 删除任务
    MAP.remove(requestId);
  }
  
}
