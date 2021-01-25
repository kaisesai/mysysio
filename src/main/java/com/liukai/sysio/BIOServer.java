package com.liukai.sysio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞 IO 服务端程序
 */
public class BIOServer {
  
  public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(100, 100, 60,
                                                                           TimeUnit.SECONDS,
                                                                           new LinkedBlockingQueue<>());
  
  public static void main(String[] args) throws IOException {
    // 创建 BIO 服务端 Socket，并且启用多个线程接收客户端连接
    try (ServerSocket serverSocket = new ServerSocket()) {
      // 配置服务端 socket
      configServerSocket(serverSocket);
      System.out.println("server up use 8091!");
      
      // 无限循环处理客户端连接
      while (true) {
        // 接收客户端连接
        Socket socket = serverSocket.accept();
        System.out.println("client = " + socket);
        
        // 配置客户端 socket
        configClientSocket(socket);
        
        // 处理客户端 socket
        processClientSocket(socket);
      }
    }
    
    // 最后关闭服务器
    
  }
  
  /**
   * 配置服务端 socket 参数
   *
   * @param serverSocket
   * @throws IOException
   */
  private static void configServerSocket(ServerSocket serverSocket) throws IOException {
    // 绑定端口
    serverSocket.bind(new InetSocketAddress(8091), 2);
    // 接收缓存区大小
    serverSocket.setReceiveBufferSize(10);
    // 是否重新使用地址，用于优化客户端关闭建立连接
    serverSocket.setReuseAddress(false);
    // 超时时间，0 为永久阻塞
    serverSocket.setSoTimeout(0);
  }
  
  /**
   * 配置客户端 socket 参数
   *
   * @param socket
   * @throws SocketException
   */
  private static void configClientSocket(Socket socket) throws SocketException {
    // 是否保存长连接
    socket.setKeepAlive(false);
    // 立即接收
    socket.setOOBInline(false);
    // 接收数据缓存区大小
    socket.setReceiveBufferSize(20);
    // 发送数据缓存区大小
    socket.setSendBufferSize(20);
    // 延迟时间
    socket.setSoLinger(true, 0);
    // 超时时间
    socket.setSoTimeout(0);
    // tcp 非延迟开关
    socket.setTcpNoDelay(false);
  }
  
  /**
   * 处理客户端 socket
   *
   * @param socket
   */
  private static void processClientSocket(Socket socket) {
    // 启动线程处理客户端
    EXECUTOR.submit(() -> {
      try {
        // 读取 socket 数据
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        char[] car = new char[1024];
        while (true) {
          int num = br.read(car);
          // 输出客户端 socket 的数据
          if (num > 0) {
            System.out
              .println("client read some data is : " + num + " val : " + new String(car, 0, num));
          } else if (num == 0) {
            System.out.println("client read nothing");
          } else {
            System.out.println("client read -1... ");
            // 阻塞等待用户输入
            // System.in.read();
            System.out.println("wait server input");
            // 关闭客户端
            socket.close();
            return;
          }
          
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (!socket.isClosed()) {
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        
      }
      
    });
  }
  
}
