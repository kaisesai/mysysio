package com.liukai.sysio.netty.rpc.proxy;

import com.liukai.sysio.netty.rpc.client.ClientFactory;
import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.protocol.ProtocolType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理处理器
 */
public class MyProxyHandler implements InvocationHandler {
  
  private final Class<?> interfaceInfo;
  
  private final ProtocolType protocolType;
  
  public MyProxyHandler(Class<?> interfaceInfo, ProtocolType protocolType) {
    this.interfaceInfo = interfaceInfo;
    this.protocolType = protocolType;
  }
  
  /**
   * 真正的处理逻辑，代理类在执行具体某些方法会调用到这里，我们需要判断这些方法，并且对其做增强处理，即调用远程服务
   *
   * @param proxy  JDK 生成的代理对象
   * @param method 执行的目标方法
   * @param args   参数
   * @return
   * @throws Throwable
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Object server = ServiceInvoker.DISPATCHER.getServer(interfaceInfo.getName());
    Object server = null;
    if (server != null) {
      System.out.println("local FC...");
      Method serverMethod = server.getClass()
        .getMethod(method.getName(), method.getParameterTypes());
      return serverMethod.invoke(server, args);
    } else {
      System.out.println("remote RPC...");
      // 消息体
      MsgContent content = MsgContent.getMsgContent(method, args, interfaceInfo);
      // 传输
      CompletableFuture<Object> future = ClientFactory.transport(content, protocolType);
      // 阻塞等待 IO 线程执行结果
      return future.get();
    }
  }
  
}
