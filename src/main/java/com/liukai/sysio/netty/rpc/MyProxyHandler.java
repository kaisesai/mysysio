package com.liukai.sysio.netty.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理处理器
 */
public class MyProxyHandler implements InvocationHandler {
  
  private final Class<?> interfaceInfo;
  
  public MyProxyHandler(Class<?> interfaceInfo) {
    this.interfaceInfo = interfaceInfo;
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
    // 消息体
    MsgBody body = getMsgBody(method, args);
    // 消息头
    MsgHeader header = getMsgHeader();
    // 创建消息类
    MyMsg myMsg = new MyMsg(header, body);
    
    // netty 通道 连接池
    MyClient myClient = ClientFactory.getClientFactory()
      .getMyClient(new InetSocketAddress("127.0.0.1", 9090));
    // 创建一个任务等待完成
    CompletableFuture<Object> future = new CompletableFuture<>();
    RpcResult.add(header.getRequestId(), future);
    
    myClient.write(myMsg);
  
    // 阻塞等待 IO 线程执行结果
    return future.get();
  }
  
  private MsgHeader getMsgHeader() {
    MsgHeader header = new MsgHeader();
    // int flag = 0x141414;
    int flag = 0x14141414;
    long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
    header.setFlag(flag);
    header.setRequestId(requestID);
    return header;
  }
  
  private MsgBody getMsgBody(Method method, Object[] args) {
    MsgBody body = new MsgBody();
    body.setInterfaceInfo(interfaceInfo.getName());
    body.setMethod(method.getName());
    body.setMethodArgs(args);
    body.setParameterTypes(method.getParameterTypes());
    return body;
  }
  
}
