package com.liukai.sysio.react;

/**
 * 程序主线程，负责初始化和启动程序
 */
public class MainThread {
  
  public static void main(String[] args) {
    // 这里不做任何 IO 和业务的操作
    
    /*
      1. 创建一个 IO Thread（可以创建一个或者多个）
        混合模式：里边的线程及支持绑定端口，接收客户端连接，又有处理客户端连接的处理
      
      2. boss 和 worker 模式
        boss 需要持有 worker
        boss 负责绑定端口，它的线程负责建立 listen 连接，监听客户端连接，接收客户端连接，并且分配到 worker 组的线程上
        worker 负责处理客户端读写事件
     */
    SelectorThreadGroup boss = new SelectorThreadGroup(3);
    SelectorThreadGroup worker = new SelectorThreadGroup(3);
    
    // 设置工作者线程组
    boss.setWorkerGroup(worker);
    
    // 初始化线程组
    boss.init();
    
    // 绑定多个端口
    boss.bind(8091);
    boss.bind(8092);
    boss.bind(8093);
    boss.bind(8094);
    
  }
  
}
