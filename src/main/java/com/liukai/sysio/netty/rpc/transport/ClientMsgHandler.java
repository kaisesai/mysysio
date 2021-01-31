package com.liukai.sysio.netty.rpc.transport;

import com.liukai.sysio.netty.rpc.protocol.MsgBody;
import com.liukai.sysio.netty.rpc.protocol.MsgHeader;
import com.liukai.sysio.netty.rpc.protocol.MyMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端消息接收处理器
 * <p>
 * 用来接收服务器端的消息，服务器端的消息也是 MyMsg 格式
 */
public class ClientMsgHandler extends SimpleChannelInboundHandler<MyMsg> {
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MyMsg msg) {
    System.out.println("received msg from server: " + msg);
    MyMsg myMsg = msg;
    // 读取消息并且返回个用户线程
    // 获取消息头
    MsgHeader header = myMsg.getHeader();
    MsgBody body = myMsg.getBody();
    // 完成任务 ID 对应的线程任务
    RpcResult.complete(header.getRequestId(), body.getResult());
  }
  
}