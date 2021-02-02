package com.liukai.sysio.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 消息体
 */
@Data
public class MsgContent implements Serializable {
  
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
  
  public static MsgContent getMsgContent(Method method, Object[] args, Class<?> interfaceInfo) {
    MsgContent body = new MsgContent();
    body.setInterfaceInfo(interfaceInfo.getName());
    body.setMethod(method.getName());
    body.setMethodArgs(args);
    body.setParameterTypes(method.getParameterTypes());
    return body;
  }
  
}
