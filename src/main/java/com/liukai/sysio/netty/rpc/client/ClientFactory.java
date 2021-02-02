package com.liukai.sysio.netty.rpc.client;

import com.liukai.sysio.netty.rpc.constant.Constants;
import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.protocol.MsgHeader;
import com.liukai.sysio.netty.rpc.protocol.MyMsg;
import com.liukai.sysio.netty.rpc.protocol.ProtocolType;
import com.liukai.sysio.netty.rpc.transport.RpcResult;
import com.liukai.sysio.netty.rpc.util.SerDerUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端工厂
 * <p>
 * 负责创建客户端 Channel 连接池，每个服务有自己的连接池
 */
public class ClientFactory {
  
  public static final int POOL_SIZE = 10;
  
  public static final String HOSTNAME = "127.0.0.1";
  
  private static final ClientFactory CLIENT_FACTORY = new ClientFactory();
  
  private final ConcurrentHashMap<InetSocketAddress, ClientPool> clientPoolMap
    = new ConcurrentHashMap<>();
  
  private ClientFactory() {
  
  }
  
  public static ClientFactory getClientFactory() {
    return CLIENT_FACTORY;
  }
  
  /**
   * 传输协议
   *
   * @param content
   * @param protocolType
   * @return
   * @throws InterruptedException
   */
  public static CompletableFuture<Object> transport(MsgContent content, ProtocolType protocolType)
    throws InterruptedException {
    
    /*
      content 是一个货物，现在可以用定义的 rpc 传输协议（有状态），也可以用 http 协议作为载体传输
      我们先手工用 http 协议作为载体，那这样就代表我们可以让 provider 是一个 tomcat、jetty 基于 http 协议的容器
      有无状态来自于你使用的什么协议，那么 http 协议肯定是无状态的，没请求对应一个连接
      dubbo 是一个 rpc 框架，net统一是一个 io 框架
      dubbo 中传输协议上，可以是自定义的 rpc 传输协议、http 协议
     */
    
    CompletableFuture<Object> future = new CompletableFuture<>();
    
    // 判断协议类型
    if (protocolType == ProtocolType.RPC) {
      // 执行 RPC 协议
      nettyTs(content, future, protocolType);
    } else if (protocolType == ProtocolType.HTTP) {
      // 走 HTTP 协议
      // urlTs(content, future);
      
      // 使用 netty 执行客户端 http 协议连接
      nettyHttpTs(content, future, protocolType);
    }
    
    return future;
  }
  
  /**
   * 使用 url 方式的 http 传输协议
   *
   * @param content
   * @param future
   */
  private static void urlTs(MsgContent content, CompletableFuture<Object> future) {
    // 这种方式是每个请求占用一个连接的方式，因为使用的是 http 协议
    Object result = null;
    try {
      URL url = new URL("http://localhost:9090");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      
      // post 请求
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      
      OutputStream os = connection.getOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(content);
      
      if (connection.getResponseCode() == 200) {
        InputStream in = connection.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(in);
        MsgContent msgContent = (MsgContent) ois.readObject();
        result = msgContent.getResult();
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    future.complete(result);
  }
  
  /**
   * 基于 Netty 的 Http 协议处理
   *
   * @param content
   * @param future
   */
  private static void nettyHttpTs(MsgContent content, CompletableFuture<Object> future,
                                  ProtocolType protocolType) throws InterruptedException {
    
    byte[] data = SerDerUtil.ser(content);
    
    // 创建一个 http 请求
    String requestID = MsgHeader.buildRequestID();
    
    DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                                HttpMethod.POST, "/",
                                                                Unpooled.copiedBuffer(data));
    // 将请求 ID 写入请求头
    request.headers().add(Constants.HTTP_HEADER_MY_REQUEST_ID, requestID);
    // 写入内容长度
    request.headers().add(HttpHeaderNames.CONTENT_LENGTH, data.length);
    
    // http 1.1 必须的请求头
    request.headers().add(HttpHeaderNames.HOST, HOSTNAME);
    
    // 创建消息类
    // netty 通道 连接池
    MyClient myClient = ClientFactory.getClientFactory()
      .getMyClient(new InetSocketAddress(HOSTNAME, 9090), protocolType);
    // 创建一个任务等待完成
    
    RpcResult.add(requestID, future);
    
    myClient.write(request);
  }
  
  /**
   * 基于 Netty 的 RPC 协议处理
   *
   * @param content
   * @param future
   */
  private static void nettyTs(MsgContent content, CompletableFuture<Object> future,
                              ProtocolType protocolType) throws InterruptedException {
    // 消息头
    MsgHeader header = MsgHeader.getMsgHeader();
    // 创建消息类
    MyMsg myMsg = new MyMsg(header, content);
    // netty 通道 连接池
    MyClient myClient = ClientFactory.getClientFactory()
      .getMyClient(new InetSocketAddress("127.0.0.1", 9090), protocolType);
    // 创建一个任务等待完成
    
    RpcResult.add(header.getRequestId(), future);
    
    myClient.write(myMsg);
  }
  
  /**
   * 根据服务名称获取连接
   *
   * @param address 服务名称
   * @return
   */
  public synchronized MyClient getMyClient(InetSocketAddress address, ProtocolType protocolType) {
    ClientPool clientPool = clientPoolMap.get(address);
    if (clientPool != null) {
      return clientPool.getClient(address);
    }
    clientPool = new ClientPool(POOL_SIZE, protocolType);
    clientPoolMap.putIfAbsent(address, clientPool);
    return clientPool.getClient(address);
  }
  
}
