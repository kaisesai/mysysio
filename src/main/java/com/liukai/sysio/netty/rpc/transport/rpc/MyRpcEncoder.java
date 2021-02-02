package com.liukai.sysio.netty.rpc.transport.rpc;

import com.liukai.sysio.netty.rpc.protocol.MyMsg;
import com.liukai.sysio.netty.rpc.util.SerDerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器
 * <p>
 * 将 MyMsg 类型的消息转成 ByteBuf 类型
 */
public class MyRpcEncoder extends MessageToByteEncoder<MyMsg> {
  
  @Override
  protected void encode(ChannelHandlerContext ctx, MyMsg msg, ByteBuf out) throws Exception {
    System.out.println("msg = " + msg);
    
    // 序列化 body
    byte[] bodyBytes = SerDerUtil.ser(msg.getBody());
    System.out.println("bodyBytes = " + bodyBytes.length);
    
    // 设置 header 的 bodyLength 属性
    msg.getHeader().setBodyLength(bodyBytes.length);
    
    // 序列化 header
    byte[] headerBytes = SerDerUtil.ser(msg.getHeader());
    System.out.println("headerBytes = " + headerBytes.length);
    
    // 写到 ByteBuf
    out.writeBytes(headerBytes);
    out.writeBytes(bodyBytes);
  }
  
}
