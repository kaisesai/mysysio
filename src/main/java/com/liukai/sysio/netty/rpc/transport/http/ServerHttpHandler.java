package com.liukai.sysio.netty.rpc.transport.http;

import com.liukai.sysio.netty.rpc.constant.Constants;
import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.service.ServiceInvoker;
import com.liukai.sysio.netty.rpc.util.SerDerUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 处理 http 请求的处理器
 */
public class ServerHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  
  public ServerHttpHandler(boolean autoRelease) {
    super(autoRelease);
  }
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
    //   FullHttpRequest msg1 = (FullHttpRequest) msg;
    // 如果 autoRelease = true，不能使用其他线程异步执行，会报出 io.netty.util.IllegalReferenceCountException: refCnt: 0 异常，http 解码器处理好
    ctx.executor().parent().next().execute(() -> {
      try {
        // doProcess(ctx, msg1);
        doProcess(ctx, msg);
      } catch (IOException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
  }
  
  private void doProcess(ChannelHandlerContext ctx, FullHttpRequest msg)
    throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
           InvocationTargetException {
    System.out.println("ServerHttpHandler received msg = " + msg);
    String requestID = msg.headers().get(Constants.HTTP_HEADER_MY_REQUEST_ID);
    System.out.println("requestID = " + requestID);
    
    // 读取 content
    byte[] data = new byte[msg.content().readableBytes()];
    msg.content().readBytes(data);
    
    // 序列化
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    MsgContent msgContent = (MsgContent) ois.readObject();
    Object result = ServiceInvoker.invokeTarget(msgContent);
    
    // 个客户端写回结果
    MsgContent content = new MsgContent();
    content.setResult(result);
    byte[] bytes = SerDerUtil.ser(content);
    
    // 设置 http 响应
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                   HttpResponseStatus.OK,
                                                                   Unpooled.copiedBuffer(bytes));
    // 将 requestID 写入请求头
    response.headers().add(Constants.HTTP_HEADER_MY_REQUEST_ID, requestID);
    
    // 添加响应头内容长度属性
    response.headers().add(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
    ctx.writeAndFlush(response);
  }
  
}
