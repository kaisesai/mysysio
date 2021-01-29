package com.liukai.sysio.netty.rpc.server;

import com.liukai.sysio.netty.rpc.client.MsgUtil;
import com.liukai.sysio.netty.rpc.msg.MyMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * 服务端消息接收处理器
 * <p>
 * 处理客户端的消息
 */
public class ServerMsgHandler extends SimpleChannelInboundHandler<MyMsg> {
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MyMsg msg) throws Exception {
    System.out.println("received msg from client: " + msg);
    // 读取消息并且返回个用户线程
    // 获取消息头
    // MsgHeader header = msg.getHeader();
    
    // 需要关注 rpc 消息通信
    
    // IO 线程的名称
    String ioThreadName = Thread.currentThread().getName();
    
    /*
      业务的处理可以有三种方式
      1. 在当前 IO 线程上处理
      2. 在自定义的线程池上处理
      3. 将业务逻辑处理作为一个任务放入在 IO 线程的任务队列中取处理
      4. 将业务逻辑处理作为一个任务放入其他的 IO 线程（充分现有利用资源）的任务队列中取处理
     */
    
    // 利用当前执行器处理，先处理 IO，在处理业务
    /*ctx.executor().execute(() -> {
      // 通过反射执行目标接口的方法
      execBusiness(msg, ioThreadName);
    });*/
    
    // 注意此方法不能分配，因为当前 eventLoop 只有一个线程，next 调用之后还是自己
    // ctx.executor().next().execute(()-> execBusiness(msg, ioThreadName, ctx));
    
    // 利用其它线程组的线程执行
    ctx.executor().parent().next().execute(() -> execBusiness(msg, ioThreadName, ctx));
    
  }
  
  /**
   * 执行业务
   *
   * @param msg
   * @param ioThreadName
   * @param ctx
   */
  private void execBusiness(MyMsg msg, String ioThreadName, ChannelHandlerContext ctx) {
    try {
      Object server = ServerFactory.getServer(msg.getBody().getInterfaceInfo());
      Method method = server.getClass()
        .getMethod(msg.getBody().getMethod(), msg.getBody().getParameterTypes());
      Object result = method.invoke(server, msg.getBody().getMethodArgs());
      System.out.println("result = " + result);
      msg.getBody().setResult(result);
      
      // 输出成字节数组
      int bodyLength = MsgUtil.getBodyLength(msg.getBody());
      // 重新写入 body 长度
      msg.getHeader().setBodyLength(bodyLength);
      
      // 写数据
      ctx.writeAndFlush(msg);
      
      // 业务线程的名称
      // String busThreadName = Thread.currentThread().getName();
      // System.out.println("io thread: " + ioThreadName + " bus thread: " + busThreadName + " from args: " + msg
      //   .getBody().getMethodArgs()[0]);
      
      // 序列化 body
      // byte[] bodyBytes = SerDerUtil.ser(msg.getBody());
      
/*      // 新的消息体
      MsgBody msgBody = new MsgBody();
      msgBody.setResult(result);
      byte[] bodyBytes = SerDerUtil.ser(msgBody);
      System.out
        .println(Thread.currentThread().getName() + " server bodyBytes = " + bodyBytes.length);
      
      // 新的消息头
      MsgHeader msgHeader = new MsgHeader();
      msgHeader.setRequestId(msgHeader.getRequestId());
      msgHeader.setFlag(msg.getHeader().getFlag());
      msgHeader.setBodyLength(bodyBytes.length);
      byte[] headerBytes = SerDerUtil.ser(msgHeader);
      System.out
        .println(Thread.currentThread().getName() + " server headerBytes = " + headerBytes.length);
      
      // 分配 Bytebuf
      ByteBuf out = PooledByteBufAllocator.DEFAULT
        .directBuffer(headerBytes.length + bodyBytes.length);

      // 写到 ByteBuf
      out.writeBytes(headerBytes);
      out.writeBytes(bodyBytes);
      // 将执行结果写回客户端
      ctx.writeAndFlush(out);
      */
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
