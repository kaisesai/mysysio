package com.liukai.sysio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * C10K 客户端程序
 * <p>
 * C10K 的意思是由 100_000 个客户端连接，即表示服务端上有大量的客户端连接进来，导致服务端处理的性能变低的问题。
 *
 * @see <a href= "http://www.kegel.com/c10k.html">C10K</a>
 */
public class C10KClient {
  
  // 服务端地址
  public static final InetSocketAddress REMOTE = new InetSocketAddress("192.168.1.109", 8091);
  
  // 客户端数量
  public static final int CLIENT_NUM = 100_000;
  
  public static void main(String[] args) throws IOException {
    
    // 本机模拟对服务器端发起 10 万个连接
    LinkedList<SocketChannel> scs = new LinkedList<>();
    
    // 初始化客户端端口
    int initPort = 10_000;
    // 初始化客户端端口
    long start = System.currentTimeMillis();
    for (int i = 0; i < CLIENT_NUM; i++) {
      try {
        // 创建客户端 socket
        SocketChannel sc = SocketChannel.open();
        // 绑定端口
        sc.bind(new InetSocketAddress(initPort++));
        // 建立连接
        sc.connect(REMOTE);
        // 将连接保存起来
        scs.add(sc);
        System.out.println("client connect remote : " + sc.socket());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    System.out.println(
      "create clients size :" + scs.size() + ", cost " + (System.currentTimeMillis() - start)
        + "ms");
    
    // 阻塞
    // System.in.read();
  }
  
}
