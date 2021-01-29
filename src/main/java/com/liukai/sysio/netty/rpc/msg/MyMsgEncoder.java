package com.liukai.sysio.netty.rpc.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器
 * <p>
 * 将 MyMsg 类型的消息转成 ByteBuf 类型
 */
public class MyMsgEncoder extends MessageToByteEncoder<MyMsg> {
  
  @Override
  protected void encode(ChannelHandlerContext ctx, MyMsg msg, ByteBuf out) throws Exception {
    // 序列化 header
    byte[] headerBytes = SerDerUtil.ser(msg.getHeader());
    System.out.println("headerBytes = " + headerBytes.length);
    
    // 序列化 body
    byte[] bodyBytes = SerDerUtil.ser(msg.getBody());
    System.out.println("bodyBytes = " + bodyBytes.length);
    
    // 写到 ByteBuf
    out.writeBytes(headerBytes);
    out.writeBytes(bodyBytes);
    
    // ctx.flush();
  }
  
}
