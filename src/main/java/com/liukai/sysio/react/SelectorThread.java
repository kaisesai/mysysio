package com.liukai.sysio.react;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多路复用器线程
 * <p>
 * 它只要负责执行 IO 操作，比如处理客户端连接、客户端读写事件
 * <p>
 * 有自己的 selector 多路复用器
 */
public class SelectorThread extends Thread {
  
  private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
  
  private final ThreadLocal<LinkedBlockingQueue<Channel>> threadLocal = ThreadLocal
    .withInitial(LinkedBlockingQueue::new);
  
  /**
   * 任务队列，用于不同线程之间的通信
   */
  private final LinkedBlockingQueue<Channel> taskQueue = threadLocal.get();
  
  /**
   * 每个线程都有自己的多路复用器
   */
  private Selector selector;
  
  /**
   * 每个IO 线程有自己的所属组，用来分配 IO 线程
   */
  private SelectorThreadGroup workerGroup;
  
  public SelectorThread() {
    try {
      // 初始化属性
      selector = Selector.open();
      // 设置线程名称
      String name = "SelectorThread-" + THREAD_COUNTER.incrementAndGet();
      this.setName(name);
      // 保存组的引用
      // this.selectorThreadGroup = selectorThreadGroup;
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  public void setWorkerGroup(SelectorThreadGroup workerGroup) {
    this.workerGroup = workerGroup;
  }
  
  @Override
  public void run() {
    // 无限循环的执行任务
    while (true) {
      // 执行选择
      try {
        // 阻塞的等待事件的到来，如果想要唤醒它可以调用 selector.wakeup() 方法
        int nums = selector.select();
        if (nums > 0) {
          // 获取有状态的事件
          Set<SelectionKey> keys = selector.selectedKeys();
          Iterator<SelectionKey> iterator = keys.iterator();
          // 此时是线性的处理过程
          while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (key.isAcceptable()) {
              // 处理请求接收
              acceptHandler(key);
            } else if (key.isReadable()) {
              // 处理读接收
              readHandler(key);
            }
          }
        }
        
        // 接着执行队列中的任务
        if (!taskQueue.isEmpty()) {
          // 队列中的数据
          // 处理 channel
          Channel channel = taskQueue.take();
          if (channel instanceof ServerSocketChannel) {
            // 服务端连接，它是来自于 SelectorThreadGroup 的 bind 方法
            ServerSocketChannel server = (ServerSocketChannel) channel;
            // 注册 accept 事件
            server.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println(Thread.currentThread().getName() + " register listen");
          } else if (channel instanceof SocketChannel) {
            // 客户端的连接，来资源其他的 SelectorThread 线程的 acceptHandler 方法
            SocketChannel client = (SocketChannel) channel;
            // 分配附件
            ByteBuffer buffer = ByteBuffer.allocate(8096);
            // 注册 read 事件
            client.register(this.selector, SelectionKey.OP_READ, buffer);
            System.out.println(
              Thread.currentThread().getName() + " register client : " + client.getRemoteAddress());
          }
        }
        
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
    
  }
  
  /**
   * 处理读事件
   *
   * @param key
   */
  private void readHandler(SelectionKey key) {
    System.out.println(Thread.currentThread().getName() + " readHandler...");
    SocketChannel channel = (SocketChannel) key.channel();
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    buffer.clear();
    
    while (true) {
      try {
        int num = channel.read(buffer);
        if (num > 0) {
          // 将数据写回客户端
          System.out.println(Thread.currentThread().getName() + "");
          // 反转
          buffer.flip();
          while (buffer.hasRemaining()) {
            // 判断缓冲区是否还能读数据
            // 将缓冲区的数据输出并写回客户端
            System.out.println(
              Thread.currentThread().getName() + " client send data : " + new String(buffer.array(),
                                                                                     buffer
                                                                                       .position(),
                                                                                     buffer
                                                                                       .limit()));
            channel.write(buffer);
          }
          
        } else if (num == 0) {
          break;
        } else {
          System.out.println(
            Thread.currentThread().getName() + "client closed : " + channel.getRemoteAddress());
          // num 为 -1 说明客户端已经关闭连接
          // 取消注册
          key.cancel();
          channel.close();
          break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
  }
  
  /**
   * 处理 accept 事件
   *
   * @param key
   */
  private void acceptHandler(SelectionKey key) {
    System.out.println(Thread.currentThread().getName() + " acceptHandler...");
    ServerSocketChannel server = (ServerSocketChannel) key.channel();
    try {
      // 接收客户端
      SocketChannel client = server.accept();
      // 设置非阻塞选项
      client.configureBlocking(false);
  
      /*
        通过一定的算法，将新接收的 client 分配到一个 IO 线程的 selector 上
        此时需要引入关联的 IO 线程组，通过它来分配注册
      */
      workerGroup.nextSelector(client);
      // this.putTask(client);
      
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  public void putTask(Channel channel) {
    try {
      this.taskQueue.put(channel);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * 唤醒 selector，即中断它的阻塞状态
   */
  public void wakeupSelector() {
    this.selector.wakeup();
  }
  
}
