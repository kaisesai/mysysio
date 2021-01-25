package com.liukai.sysio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 阻塞 IO 客户端程序
 */
public class BIOClient {
  
  // public static final String REMOTE = "192.168.1.109";
  public static final String REMOTE = "localhost";
  
  public static void main(String[] args) throws IOException {
    // 创建客户端 socket
    try (Socket socket = new Socket(REMOTE, 8091)) {
      // 配置客户端 socket 参数
      
      // 发送缓冲区
      socket.setSendBufferSize(20);
      // tcp 非延迟
      socket.setTcpNoDelay(false);
      // 立即发送
      socket.setOOBInline(false);
      
      OutputStream ops = socket.getOutputStream();
      
      // 从屏幕输入数据
      try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
        while (true) {
          String readLine = br.readLine();
          if (readLine != null) {
            // 向服务端写入数据
            byte[] bytes = readLine.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
              ops.write(b);
            }
          }
        }
      }
    }
    
  }
  
}
