package com.liukai.sysio.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 接收客户端连接
 */
class MyAcceptHandler extends ChannelInboundHandlerAdapter {
  
  private final NioEventLoopGroup eventLoopGroup;
  
  private final ChannelInit channelInit;
  
  public MyAcceptHandler(NioEventLoopGroup eventLoopGroup, ChannelInit myHandler) {
    this.eventLoopGroup = eventLoopGroup;
    this.channelInit = myHandler;
  }
  
  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    System.out.println("MyAcceptHandler registered...");
    super.channelRegistered(ctx);
  }
  
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("MyAcceptHandler active...");
    super.channelRegistered(ctx);
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 接收到的是客户端连接
    NioSocketChannel client = (NioSocketChannel) msg;
    System.out.println("client = " + client);
    // 为客户端连接添加处理器
    // client.pipeline().addLast(new MyHandler());
    
    // 为客户端添加到处理器初始化器，此时处理器 client#pipeline[ChannelInit]
    client.pipeline().addLast(channelInit);
    // 将客户端注册到 selector
    eventLoopGroup.register(client);
  }
  
}
