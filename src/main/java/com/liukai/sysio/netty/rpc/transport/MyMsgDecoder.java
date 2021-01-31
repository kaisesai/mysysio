package com.liukai.sysio.netty.rpc.transport;

import com.liukai.sysio.netty.rpc.protocol.MsgBody;
import com.liukai.sysio.netty.rpc.protocol.MsgHeader;
import com.liukai.sysio.netty.rpc.protocol.MyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * 消息解码器
 * <p>
 * 将接收到的 ByteBuf 转成 MyMsg 类型
 * <p>
 * ByteToMessageDecoder 类执行了 channelRead，并且它使用了 Cumulator 累加器累加 ByteBuf 中的数据。
 */
public class MyMsgDecoder extends ByteToMessageDecoder {
  
  public static final int HEADER_SIZE = 105;
  
  /**
   * @param ctx
   * @param in  其实是父类的累加器，它是一个可以保存上次读取到的数据，以及拼接现在获取的数据
   * @param out 可以传递给下一个读处理器的数据结果集
   * @throws Exception
   */
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    System.out.println("MyMsgDecoder.decode: " + in.toString());
    // 无限循环的读取，因为还有事件循环器线程不断的监听 selector 将收到的数据写入到这个 ByteBuf 上去
    while (in.readableBytes() >= HEADER_SIZE) {
      // 读取前 110 个字节解析为消息头，将字节数组反序列化成 Java 对象
      byte[] bytes = new byte[HEADER_SIZE];
      // 使用 get 方式读取 in 数据，不修改 index 的位置
      in.getBytes(in.readerIndex(), bytes);
      // 反序列化消息头
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bis);
      MsgHeader header = (MsgHeader) ois.readObject();
  
      // 剩余的读取消息体，当前 ByteBuf 中剩余的数据是否够一个 body，如果不够则退出方法，保留头部数据和剩余数据，留着下次有数据到达再合并起来处理。
      if (in.readableBytes() >= header.getBodyLength() + HEADER_SIZE) {
        // 读取消息体处理指针
        in.readBytes(HEADER_SIZE);
        // 读取消息体
        System.out.println(
          "header.getBodyLength() = " + header.getBodyLength() + " in: " + in.toString()
            + " in.readerIndex()=" + in.readerIndex() + " in.readableBytes()=" + in
            .readableBytes());
        byte[] data = new byte[(int) header.getBodyLength()];
        in.readBytes(data);
        // 反序列化消息体
        ByteArrayInputStream bis2 = new ByteArrayInputStream(data);
        ObjectInputStream ois2 = new ObjectInputStream(bis2);
    
        MsgBody body = (MsgBody) ois2.readObject();
    
        // 这里还需要注意下，处理需要解析 header 中的 flag，需要根据它来判断不同业务协议，从而处理不同的类型
        // 构建 MyMsg
        MyMsg myMsg = new MyMsg(header, body);
        // 写给结果集
        out.add(myMsg);
      } else {
        // 保留头部数据和剩余数据，留着下次有数据到达再合并起来处理。
        break;
      }
    }
  }
  
}
