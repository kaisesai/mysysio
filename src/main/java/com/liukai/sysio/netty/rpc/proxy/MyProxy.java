package com.liukai.sysio.netty.rpc.proxy;

import java.lang.reflect.Proxy;

/**
 * 代理工具类
 */
public class MyProxy {
  
  /**
   * 获取代理对象
   * <p>
   * 代理对象主要负责建立所代理的服务信息
   *
   * @param carClass
   * @param <T>
   * @return
   */
  @SuppressWarnings(value = "unchecked")
  public static <T> T getProxy(Class<T> interfaceClass) {
    // 使用 JDK 的动态代理，还可以使用 CGLIB 代理
    // 类加载器
    ClassLoader classLoader = interfaceClass.getClassLoader();
    // 接口信息
    Class<?>[] interfaces = {interfaceClass};
    // 执行处理器
    return (T) Proxy.newProxyInstance(classLoader, interfaces, new MyProxyHandler(interfaceClass));
  }
  
}
