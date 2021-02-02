package com.liukai.sysio.netty.rpc.protocol;

import lombok.Data;

/**
 * 消息体
 * <p>
 * 包括消息头和消息体
 */
@Data
public class MyMsg {
  
  /**
   * 消息头
   */
  private MsgHeader header;
  
  /**
   * 消息体
   */
  private MsgContent body;
  
  public MyMsg(MsgHeader header, MsgContent body) {
    this.header = header;
    this.body = body;
  }
  
}
