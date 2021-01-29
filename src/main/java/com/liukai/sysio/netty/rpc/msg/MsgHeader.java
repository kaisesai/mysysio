package com.liukai.sysio.netty.rpc.msg;

import lombok.Data;

import java.io.Serializable;

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
  private long requestId;
  
  /**
   * 消息标识
   */
  private int flag;
  
  /**
   * 消息体长度
   */
  private int bodyLength;
  
}
