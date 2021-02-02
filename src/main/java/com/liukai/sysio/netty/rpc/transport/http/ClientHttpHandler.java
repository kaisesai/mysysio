package com.liukai.sysio.netty.rpc.transport.http;

import com.liukai.sysio.netty.rpc.constant.Constants;
import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.transport.RpcResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * 客户端 http 消息处理器
 */
public class ClientHttpHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
    System.out.println("ClientHttpHandler received msg from server: " + msg);
    // 获取消息头
    String requestID = msg.headers().get(Constants.HTTP_HEADER_MY_REQUEST_ID);
    System.out.println("requestID = " + requestID);
    
    // 读取 content
    byte[] data = new byte[msg.content().readableBytes()];
    msg.content().readBytes(data);
    
    // 反序列化数据
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    MsgContent msgContent = (MsgContent) ois.readObject();
    
    // 完成任务 ID 对应的线程任务
    RpcResult.complete(requestID, msgContent.getResult());
  }
  
}
