package com.liukai.sysio.netty.rpc.client;

import com.liukai.sysio.netty.rpc.protocol.ProtocolType;
import com.liukai.sysio.netty.rpc.transport.http.ClientHttpHandler;
import com.liukai.sysio.netty.rpc.transport.rpc.ClientRpcHandler;
import com.liukai.sysio.netty.rpc.transport.rpc.MyRpcDecoder;
import com.liukai.sysio.netty.rpc.transport.rpc.MyRpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * 客户端连接池，与客户端的 Channel 通道
 */
@Data
public class ClientPool {
  
  private Random random = new Random();
  
  /**
   * 连接信息
   */
  private MyClient[] clients;
  
  private Object[] locks;
  
  /**
   * 协议类型
   */
  private ProtocolType protocolType;
  
  public ClientPool(int poolSize, ProtocolType protocolType) {
    this.clients = new MyClient[poolSize];
    locks = new Object[poolSize];
    this.protocolType = protocolType;
    
    // 初始化锁信息
    for (int i = 0; i < locks.length; i++) {
      locks[i] = new Object();
    }
  }
  
  /**
   * 获取客户端连接
   *
   * @param address 服务器端地址
   * @return
   */
  public MyClient getClient(InetSocketAddress address) {
    // 随机获取连接
    int index = random.nextInt(clients.length);
    
    // 连接存在并且有效
    if (clients[index] != null && clients[index].isActive()) {
      return clients[index];
    }
    
    // 创建 netty 客户端连接
    return createClient(address, index);
  }
  
  /**
   * 创建 NioSocketChannel
   *
   * @param address
   * @param index
   * @return
   */
  private MyClient createClient(InetSocketAddress address, int index) {
    synchronized (locks[index]) {
      NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
      Bootstrap bootstrap = new Bootstrap();
      try {
        ChannelFuture future = bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
              System.out.println("init client channel transport protocol = " + protocolType);
              if (protocolType == ProtocolType.HTTP) {
                // http 编解码处理器
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
                ch.pipeline().addLast(new ClientHttpHandler());
      
              } else if (protocolType == ProtocolType.RPC) {
                // 消息编码器
                ch.pipeline().addLast(new MyRpcEncoder());
                // 消息解码器
                ch.pipeline().addLast(new MyRpcDecoder());
                // 消息处理器
                ch.pipeline().addLast(new ClientRpcHandler());
              }
            }
          })
          // 同步连接服务器端
          .connect(address).sync();
        NioSocketChannel channel = (NioSocketChannel) future.channel();
        MyClient client = new MyClient(channel, eventLoopGroup);
        clients[index] = client;
        return client;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
  
}
