package com.liukai.sysio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 非阻塞 IO 多路复用器服务端程序
 * <p>
 * 版本：v1 单线程
 */
public class NIOMultiplexingServerV1 {
  
  private ServerSocketChannel ssc;
  
  /**
   * 多路复用器
   * <p>
   * 在 Linux 中多路复用器的实现为：select、poll、epoll、kqueue
   */
  private Selector selector;
  
  public static void main(String[] args) throws IOException {
    NIOMultiplexingServerV1 server = new NIOMultiplexingServerV1();
    // 初始化服务
    server.initServer();
    // 开始工作
    server.process();
  }
  
  private void initServer() throws IOException {
    /*
      创建服务端 socket
      它约等于 listen 状态的文件描述符 fd4：java    13192 root    4u     IPv6 782666    0t0      TCP *:jamlink (LISTEN)
     */
    ssc = ServerSocketChannel.open();
    // 设置非阻塞选项
    ssc.configureBlocking(false);
    // 绑定端口
    ssc.bind(new InetSocketAddress(8091));
    
    /*
      创建多路复用器
      epoll 模型下，对应着 epoll_create，它会返回一个文件描述符 5 -> fd5：epoll_create(256) = 5
      
      Java 会默认使用 epoll 模型，也添加程序启动参数来选择指定 select、poll 模型，通过 -D 参数指定
      选择 epoll 模型：-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider
      启动程序，并且追踪系统调用：strace -ff -o out java -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.PollSelectorProvider -cp /root/netty-all-4.1.48.Final.jar:.  NettyIO
     */
    selector = Selector.open();
    
    /*
      注册服务端 socket 到多路复用器上，注册接收客户端连接事件
      select、poll 模型：在 jvm 里开辟一个数组，将 fd4 放进去
      epoll：执行 epoll_ctl(5, EPOLL_CTL_ADD, 4, EPOLLIN，意思是将 fd4 放入到 fd5（epoll 实例，它在内核实现中有一棵红黑树）中，并且注册它的 read 事件
      
      它是懒加载的，只有在真正调用 selector.select() 方法时才会触发。
     */
    ssc.register(selector, SelectionKey.OP_ACCEPT);
    System.out.println("Multiplexing Server up port 8091!");
  }
  
  /**
   * 执行工作
   */
  private void process() {
    
    // 使用多路复用器处理
    try {
      while (true) {
        Set<SelectionKey> keys = selector.keys();
        System.out.println("keys.size() = " + keys.size());
        
        /*
          selector.select() 调用多路复用器的方法。
          1. elect、poll 模型：对应的是内核的系统调用 select(fd4) 和 poll(fd4)
          2. epoll 模型：对应内核的系统调用 epoll_wait(5,
          
          这个方法可以带时间参数：时间为 0 表示永久阻塞；否则就是阻塞一定的时间。
          这个阻塞可以用 selector.wakeup() 方法唤醒
         */
        while (selector.select() > 0) {
          /*
            获取已经收到多路复用 key 事件
            这是返回有状态的文件描述符集合
           */
          Set<SelectionKey> selectionKeys = selector.selectedKeys();
          Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
          /*
            多路复用器只能返回有状态的文件描述符，程序还得一个一个的处理他们的 R/W，依旧是同步操作
            NIO 需要拿着一堆文件描述符调用系统调用，浪费资源，而这里的多路复用器只需要调用一次 select 方法，就知道具体的可以 R/W 的文件描述符，这是非常高效的！
           */
          while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            // 删除本次集合中 key（文件描述符），防止重复处理
            keyIterator.remove();
            if (key.isAcceptable()) {
              /*
                接收客户端连接事件，这里是重点，如果要去接受一个新的连接，那么语义上，accept 接受连接并且返回一个新的文件描述符 fd，这个 fd 在select、poll、epoll 模型下的处理方式是不同的。
                select、poll 模型：因为它们在内核中没有空间，所以就把新的 fd 保存到与前面那个服务端 listen fd4 所在的 jvm 的数组中。
                epoll 模型：我们会把这个 fd 通过 epoll_ctl 注册到内核空间上去（epoll 的红黑树中）
               */
              acceptHandler(key);
            } else if (key.isReadable()) {
              /*
                处理读事件
                这个方法做了读和写操作
                在当前线程中，该方法可能会阻塞，如果阻塞时间很长，那么其他的 IO 客户端可能已经断开了
                所以我们需要提出 IO Threads
                redis 也是使用了 epoll。它也有有个 IO Threads 的概念
                tomcat 8、9 也开始了多路复用器同步非阻塞和异步非阻塞模式
               */
              readHandler(key);
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // 关闭 ServerSocketChannel
      if (ssc != null) {
        try {
          ssc.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // 关闭 selector
      if (selector != null && selector.isOpen()) {
        try {
          selector.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * 处理读取数据事件
   *
   * @param key
   */
  private void readHandler(SelectionKey key) {
    System.out.println("处理 read 事件");
    // 获取客户端 SocketChannel
    SocketChannel sc = (SocketChannel) key.channel();
    // 获取 key 的附件
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    buffer.clear();
    try {
      // 读取客户端数据
      int num;
      // 因为 buffer 设置的容量有限，不能一次性都完客户端发送的数据，所以需要重复的读取
      while (true) {
        num = sc.read(buffer);
        SocketAddress remoteAddress = sc.getRemoteAddress();
        System.out.println("client: " + remoteAddress + ", dataSize: " + num);
        if (num > 0) {
          // 反转数据
          buffer.flip();
          // 将数据写回客户端
          if (buffer.hasRemaining()) {
            sc.write(buffer);
          }
          // 清空 buffer
          buffer.clear();
        } else if (num == 0) {
          System.out.println("client: " + remoteAddress + " no data");
          break;
        } else {
          // 客户端关闭了连接
          sc.close();
          System.out.println("client: " + remoteAddress + " closed");
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * 处理接收客户端连接事件
   *
   * @param key
   */
  private void acceptHandler(SelectionKey key) {
    System.out.println("处理 accept 事件");
    // 获取服务端 ServerSocketChannel
    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
    try {
      /*
        接收客户端的连接，文件描述符 f9
        accept(4, {sa_family=AF_INET6, sin6_port=htons(39750), inet_pton(AF_INET6, "::1", &sin6_addr), sin6_flowinfo=htonl(0), sin6_scope_id=0}, [28]) = 9
       */
      SocketChannel sc = ssc.accept();
      // 设置非阻塞选项
      sc.configureBlocking(false);
      
      // 分配一个 byteBuffer 缓存区
      ByteBuffer buffer = ByteBuffer.allocate(8192);
      
      // 将 sc、read 事件以及 buffer 注册到多路复用器上
      sc.register(selector, SelectionKey.OP_READ, buffer);
      System.out.println("与客户端建立连接：" + sc.getRemoteAddress());
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
}
