package com.liukai.sysio.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 入站数据的处理器
 */
// @ChannelHandler.Sharable
class MyHandler extends ChannelInboundHandlerAdapter {
  
  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    System.out.println("client registered...");
    super.channelRegistered(ctx);
  }
  
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("client active...");
    super.channelActive(ctx);
  }
  
  /**
   * 处理读事件
   *
   * @param ctx
   * @param msg
   * @throws Exception
   */
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    System.out.println("client read...");
    ByteBuf buf = (ByteBuf) msg;
    // 输出数据，这里使用 ByteBuf 的 getXXX 方法
    CharSequence charSequence = buf
      .getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8);
    System.out.println("charSequence = " + charSequence);
    // 将数据写回去
    ctx.writeAndFlush(buf);
  }
  
}
