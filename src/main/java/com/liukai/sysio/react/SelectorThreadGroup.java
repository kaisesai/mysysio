package com.liukai.sysio.react;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IO 线程组
 * <p>
 * 负责提供绑定服务，以及分配 IO 线程选择器的工作。
 * <p>
 * 它维护 IO 线程、工作线程组
 */
public class SelectorThreadGroup {
  
  /**
   * IO 线程组
   */
  private final SelectorThread[] threads;
  
  private final AtomicInteger xid = new AtomicInteger(0);
  
  /**
   * worker IO 线程组，用来具体干活的线程
   */
  private SelectorThreadGroup workerGroup;
  
  public SelectorThreadGroup(int threadNum) {
    // 创建 IO 线程
    threads = new SelectorThread[threadNum];
    for (int i = 0; i < threadNum; i++) {
      threads[i] = new SelectorThread();
    }
  }
  
  public void init() {
    // 启动各个 IO 线程
    for (SelectorThread thread : threads) {
      thread.start();
    }
    if (workerGroup != null) {
      workerGroup.init();
    }
  }
  
  public void setWorkerGroup(SelectorThreadGroup workerGroup) {
    this.workerGroup = workerGroup;
    // 为每一个 boss 线程设置它的工作组
    for (SelectorThread thread : threads) {
      thread.setWorkerGroup(workerGroup);
    }
  }
  
  /**
   * 绑定端口
   * <p>
   * 需要轮询的分配需要绑定的线程
   *
   * @param port
   */
  public void bind(int port) {
    try {
      // 创建 ServerSocketChannel
      ServerSocketChannel serverSocketChannel = getServerSocketChannel(port);
      // 需要注册到哪个 selector 上去呢？此时就需要轮询的选择了
      // 注册到多路复用器上
      nextSelector(serverSocketChannel);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * 将 channel 分配到下一个 IO 线程多路复用器上
   *
   * @param channel
   */
  public void nextSelector(Channel channel) {
    if (channel instanceof ServerSocketChannel) {
      // 通过一定的算法选择一个 IO 线程
      // SelectorThread bossSelectorThread = nextBossSelectorThread();
      SelectorThread bossSelectorThread = nextSelectorThread();
      // 将 channel 放入 IO 线程对应的队列中
      bossSelectorThread.putTask(channel);
      // 修改 bossSelectorThread 的线程组，因为 boss 线程的 accept 方法需要接受客户端连接，
      // 并且将连接通过工作组分配给 worker 线程，而不是分配给自己所属组的线程
      // bossSelectorThread.setWorkerGroup(this.workerGroup);
      // 唤醒 IO 线程里 selector 的阻塞状态，让对应的线程去自己的逻辑中完成注册 selector
      bossSelectorThread.wakeupSelector();
    } else {
      // 通过一定的算法选择一个 IO 线程
      // SelectorThread workerSelectorThread = nextWorkerSelectorThread();
      SelectorThread workerSelectorThread = nextSelectorThread();
      // 将 channel 放入 IO 线程对应的队列中
      workerSelectorThread.putTask(channel);
      // 唤醒 IO 线程里 selector 的阻塞状态，让对应的线程去自己的逻辑中完成注册 selector
      workerSelectorThread.wakeupSelector();
      
    }
  }
  
  /**
   * 轮询算法
   * <p>
   * 缺点：会发生倾斜。比如一些客户端连接来了，分配到 3 个 IO 线程 t0、t1、t2 上，如果 t0 和 t1 上对应的客户端关闭了连接，
   * 这样 t2 上的有效连接就比较多，下次来的一些连接还是会轮询注册到 t2 线上去。
   * <p>
   * 其他的算法：hash一致性、根据有效连接数排序分配等
   *
   * @return
   */
  private SelectorThread nextSelectorThread() {
    // 轮询的分配算法
    int increment = xid.getAndIncrement();
    int index = increment % this.threads.length;
    System.out.println(Thread.currentThread().getName() + " " + super.hashCode()
                         + " nextSelectorThread 选择 IO 线程的 index = " + index + ", increment = "
                         + increment);
    return this.threads[index];
  }
  
  private ServerSocketChannel getServerSocketChannel(int port) throws IOException {
    ServerSocketChannel server = ServerSocketChannel.open();
    // 设置非阻塞参数
    server.configureBlocking(false);
    // 绑定端口
    server.bind(new InetSocketAddress(port));
    return server;
  }
  
}
