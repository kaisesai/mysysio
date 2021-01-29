package com.liukai.sysio.netty.rpc;

import java.lang.reflect.Proxy;

/**
 * 实现基于 netty 网络通信的 rpc 框架客户端
 *
 * @author liukai 2021年01月27日
 */
public class RpcClientMain {
  
  // private static ConcurrentHashMap<String, Object> serverMap = new ConcurrentHashMap<>();
  
  public static void main(String[] args) {
    
    /*
      RPC：远程方法调用，有客户端和服务端，客户端调用服务端的方法可以获取数据
      1. 需要双端输出数据，需要有连接信息，数据包的拆包
      2. 客户端调用服务端接口，需要动态代理，利用网络通信框架连接服务端，执行目标代码，需要数据的序列化、协议封装
      3. 客户端需要建立连接池，保存与服务器端的连接通道，避免每次连接重新建立
     */
    // 服务端代码、测试、多线程、 动态代理
    for (int i = 0; i < 10; i++) {
      int finalI = i;
      new Thread(() -> {
        // 获取代理对象
        Car car = getProxy(Car.class);
        // 执行代理对象的方法
        String result = car.drive("雅阁" + finalI);
        System.out.println("client over msg : " + result);
      }, "t" + i).start();
    }
    
  }
  
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
  private static <T> T getProxy(Class<T> interfaceClass) {
    // 使用 JDK 的动态代理，还可以使用 CGLIB 代理
    // 类加载器
    ClassLoader classLoader = interfaceClass.getClassLoader();
    // 接口信息
    Class<?>[] interfaces = {interfaceClass};
    // 执行处理器
    return (T) Proxy.newProxyInstance(classLoader, interfaces, new MyProxyHandler(interfaceClass));
  }
  
}
