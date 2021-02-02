package com.liukai.sysio.netty.rpc.transport.rpc;

import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.protocol.MsgHeader;
import com.liukai.sysio.netty.rpc.protocol.MyMsg;
import com.liukai.sysio.netty.rpc.service.ServiceInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 服务端消息接收处理器
 * <p>
 * 处理客户端的消息
 */
public class ServerRpcHandler extends SimpleChannelInboundHandler<MyMsg> {
  
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MyMsg msg) {
    System.out.println("received msg from client: " + msg);
    MyMsg myMsg = msg;
    
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
    execBusiness(msg, ioThreadName, ctx);
    });*/
    
    // 注意此方法不能分配，因为当前 eventLoop 只有一个线程，next 调用之后还是自己
    // ctx.executor().next().execute(()-> execBusiness(msg, ioThreadName, ctx));
    
    // 利用其它线程组的线程执行
    ctx.executor().parent().next().execute(() -> execBusiness(myMsg, ioThreadName, ctx));
    
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
      // 执行目标接口
      Object result = ServiceInvoker.invokeTarget(msg.getBody());
  
      // 业务线程的名称
      String busThreadName = Thread.currentThread().getName();
      System.out.println(
        "io thread: " + ioThreadName + " bus thread: " + busThreadName + " from args: " + msg
          .getBody().getMethodArgs()[0]);
  
      // 写回数据
      MsgHeader header = getMsgHeader(msg.getHeader().getRequestId());
      MsgContent body = getMsgBody(result);
      MyMsg myMsg = new MyMsg(header, body);
      ctx.writeAndFlush(myMsg);
  
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private MsgHeader getMsgHeader(String requestID) {
    MsgHeader header = new MsgHeader();
    int flag = 0x14141414;
    header.setFlag(flag);
    header.setRequestId(requestID);
    return header;
  }
  
  private MsgContent getMsgBody(Object result) {
    MsgContent body = new MsgContent();
    body.setResult(result);
    return body;
  }
  
}
