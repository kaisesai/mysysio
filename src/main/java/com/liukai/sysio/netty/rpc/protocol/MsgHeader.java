package com.liukai.sysio.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * 消息头
 * <p>
 * 这个类序列化之后得到的字节长度为 109
 */
@Data
public class MsgHeader implements Serializable {
  
  /**
   * 消息 ID
   */
  private String requestId;
  
  /**
   * 消息标识
   */
  private int flag;
  
  /**
   * 消息体长度
   */
  private int bodyLength;
  
  public static MsgHeader getMsgHeader() {
    MsgHeader header = new MsgHeader();
    int flag = 0x14141414;
    header.setFlag(flag);
    header.setRequestId(buildRequestID());
    return header;
  }
  
  public static String buildRequestID() {
    return String.valueOf(UUID.randomUUID().getLeastSignificantBits());
  }
  
}
