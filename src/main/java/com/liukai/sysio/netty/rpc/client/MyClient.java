package com.liukai.sysio.netty.rpc.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;
import java.io.IOException;

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
  
  public void write(Object msg) throws InterruptedException {
    ChannelFuture channelFuture = socketChannel.writeAndFlush(msg);
    // 要同步发送？
    // TODO: 2021/1/29
    channelFuture.sync();
  }
  
  @Override
  public void close() throws IOException {
    socketChannel.close();
    eventLoopGroup.shutdownGracefully();
  }
  
  public boolean isActive() {
    return socketChannel.isActive();
  }
  
}
