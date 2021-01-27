package com.liukai.sysio.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * channel 初始化器，用于把接收到的客户端 channel 注册到 selector，并且为它添加处理器
 * <p>
 * 它是用作
 */
@ChannelHandler.Sharable
abstract class ChannelInit extends ChannelInboundHandlerAdapter {
  
  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    System.out.println("ChannelInit.channelRegistered");
    // 接收客户端
    // 执行初始化方法
    try {
      System.out.println("before init");
      init(ctx);
      System.out.println("after init");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      System.out.println("删除该 channel，因为改类只需要在客户端建立时用一次，之后就不需要再使用了");
      // 3. client::pipeline[ChannelInit,MyInHandler]
      ctx.pipeline().remove(this);
    }
    
  }
  
  /**
   * 抽象的方法，用于初始化客户端处理器，一般是用来给客户端添加一些自定的处理器
   *
   * @param ctx
   */
  public abstract void init(ChannelHandlerContext ctx);
  
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("ChannelInit.channelActive");
    super.channelActive(ctx);
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("ChannelInit.channelRead");
    super.channelRead(ctx, msg);
  }
  
}
