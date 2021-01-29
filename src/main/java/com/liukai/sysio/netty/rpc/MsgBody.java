package com.liukai.sysio.netty.rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息体
 */
@Data
public class MsgBody implements Serializable {
  
  /**
   * 接口信息
   */
  private String interfaceInfo;
  
  /**
   * 方法信息
   */
  private String method;
  
  /**
   * 方法参数信息
   */
  private Object[] methodArgs;
  
  /**
   * 方法参数类型信息
   */
  private Class<?>[] parameterTypes;
  
  /**
   * 执行结果
   */
  private Object result;
  
}
