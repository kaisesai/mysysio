package com.liukai.sysio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * 非阻塞 IO 服务端程序
 * <p>
 * 服务端的 listen socket 设置非阻塞选项，那么它在执行 accept 方法时，不论客户端有没有连接，都会立刻返回结果，
 * <p>
 * 优点：
 * 1. 接收客户端的连接和处理客户端 IO 不会阻塞
 * 2. 只需要一个线程或者几个线程就可以完成接收客户端连接与处理已连接的客户端
 * <p>
 * 缺点：
 * 1. C10K 问题：当客户端连接数量 N 很大时，那么处理客户端数据时就需要遍历 N 次，即调用 N 次系统调用（用户态与内核态切换）
 * 2. 并且这些客户款并不全是有数据要处理，造成很多无用的系统调用，浪费资源
 */
public class NIOServer {
  
  public static void main(String[] args) throws IOException, InterruptedException {
    
    LinkedList<SocketChannel> scs = new LinkedList<>();
    
    // 创建服务端非阻塞 socket
    try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
      // 绑定端口
      ssc.bind(new InetSocketAddress(8091));
      // 设置非阻塞
      ssc.configureBlocking(false);
      
      // 连接客户端
      while (true) {
        
        /*
          接收客户端 socket 连接
          这个 accept 方法会调用内核的 accept 系统调用，因为设置了非阻塞选项，所以它会立刻返回结果。
          如有没有客户端连接，则返回-1，否则就返回对应 socket 的文件描述符
        */
        SocketChannel socket = ssc.accept();
        
        if (socket == null) {
          // Thread.sleep(200);
          // System.out.println("null...");
        } else {
          /*
            设置非阻塞
            服务端的 listen socket（三次握手之后，往我这里仍，我去通过 accept 得到连接的 socket，连接 socket（用于连接后的数据读写使用的））
           */
          socket.configureBlocking(false);
          System.out.println("client connected :" + socket.socket());
          
          scs.add(socket);
        }
        
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        
        // 遍历处理客户端 socket
        for (SocketChannel sc : scs) {
          // 读取数据到 buffer
          int read = sc.read(buffer);
          if (read > 0) {
            // 反转 buffer
            buffer.flip();
            // 将 buffer 中读到的数据读到 byte[]
            byte[] car = new byte[buffer.limit()];
            buffer.get(car);
            System.out.println("client " + sc.socket() + " data : " + new String(car));
            // 清理 buffer
            buffer.clear();
          }
          
        }
        
      }
      
    }
  }
  
}
