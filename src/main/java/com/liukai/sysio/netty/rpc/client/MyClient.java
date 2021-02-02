package com.liukai.sysio.netty.rpc.client;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;

/**
 * 客户端连接
 * <p>
 * 持有客户端连接和事件循环组
 */
public class MyClient implements Closeable {
  
  private final NioSocketChannel socketChannel;
  
  private final NioEventLoopGroup eventLoopGroup;
  
  public MyClient(NioSocketChannel socketChannel, NioEventLoopGroup eventLoopGroup) {
    this.socketChannel = socketChannel;
    this.eventLoopGroup = eventLoopGroup;
  }
  
  public void write(Object msg) {
    socketChannel.writeAndFlush(msg);
  }
  
  @Override
  public void close() {
    socketChannel.close();
    eventLoopGroup.shutdownGracefully();
  }
  
  public boolean isActive() {
    return socketChannel.isActive();
  }
  
}
