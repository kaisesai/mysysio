package com.liukai.sysio.netty.rpc.service;

import com.liukai.sysio.netty.rpc.protocol.MsgContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务接口执行器
 */
public class ServiceInvoker {
  
  public static final Dispatcher DISPATCHER;
  
  static {
    DISPATCHER = Dispatcher.getInstance();
    DISPATCHER.registry(Car.class.getName(), new CarImpl());
  }
  
  /**
   * 执行目标方法
   *
   * @param content
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Object invokeTarget(MsgContent content)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Object server = DISPATCHER.getServer(content.getInterfaceInfo());
    Method method = server.getClass().getMethod(content.getMethod(), content.getParameterTypes());
    return method.invoke(server, content.getMethodArgs());
  }
  
}
