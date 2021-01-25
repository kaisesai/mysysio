package com.liukai.sysio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * 操作系统文件 IO
 *
 * @author liukai 2021-01-20 00:08:37
 */
public class OSFileIO {
  
  public static final String FILE_PATH = "out.txt";
  
  private static final byte[] DATA = "1234567\n".getBytes(StandardCharsets.UTF_8);
  
  public static void main(String[] args) throws IOException, InterruptedException {
    
    switch (args[0]) {
      case "0":
        // 普通文件输出流
        basicFileIO();
      case "1":
        // 字节缓存区的文件输出流（缓存区属于 JVM 内存级别）
        bufferedFileIO();
      case "2":
        // 随机文件访问
        randomAccessFileIO();
      default:
    }
    
  }
  
  private static void randomAccessFileIO() throws IOException, InterruptedException {
    // 创建随机访问文件
    RandomAccessFile file = new RandomAccessFile(FILE_PATH, "rw");
    
    // 写入数据
    file.write("hello kaige\n".getBytes(StandardCharsets.UTF_8));
    file.write("hello look\n".getBytes(StandardCharsets.UTF_8));
    System.out.println("write--------------");
    
    // 阻塞等待用户端输入
    System.in.read();
    
    // 跳转到随机访问文件的指定位置，这就是随机访问的关键方法
    file.seek(4);
    file.write("ooxx".getBytes(StandardCharsets.UTF_8));
    
    System.out.println("seek-----------");
    System.in.read();
    
    // 获取文件 channel
    FileChannel channel = file.getChannel();
    // 获取 mmap 内存映射
    // 直接获取 channel 的文件在内存中的映射
    MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
    
    map.put("@@".getBytes(StandardCharsets.UTF_8));// 不是系统调用，但是数据会到达内核的 pagecache
    // 曾今我们是需要 out.write() 这样的系统调用，才能让程序的数据进入内核的 pagecache，即用户态切换内核态
    // mmap 内存映射，依然是内核的 pagecache 体系锁约束的，换句话说就是它会丢数据
    // GitHub 上有 C 程序写的 JNI 扩展库，使用 linux 内核的 direct IO
    // 直接 IO 是忽略 linux 的 pagecache，它把 pagecache 交给了程序自己开辟的一个字节数组当做 pagecache，动用代码逻辑来维护一致性/dirty ... 等一些列复杂的问题
    System.out.println("map-put---------");
    System.in.read();
    
    // map.force();// flush
    
    file.seek(0);
    
    ByteBuffer buffer = ByteBuffer.allocate(8192);
    
    int read = channel.read(buffer);
    System.out.println("channel read data to buffer, size: " + read);
    System.out.println("buffer = " + buffer);
    
    // 反转 buffer
    buffer.flip();
    System.out.println("flip buffer = " + buffer);
    
    // 输出 buffer 中的内容到屏幕
    for (int i = 0; i < read; i++) {
      // Thread.sleep(200);
      System.out.print(((char) buffer.get(i)));
    }
    
  }
  
  /**
   * 基于 JVM 内存缓存的文件 IO，8kb 缓存区大小，syscall write(8kb)
   * 使用 JVM 级别的缓存区是为了减少系统调用（用户态与内核态的切换）
   *
   * @throws IOException
   */
  private static void bufferedFileIO() throws IOException {
    File file = new File(FILE_PATH);
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    while (true) {
      bos.write(DATA);
    }
  }
  
  /**
   * 基本的文件 IO，每一次的 write 都会调用 syscall 系统调用（用户态与内核态切换）
   *
   * @throws IOException
   */
  private static void basicFileIO() throws IOException {
    File file = new File(FILE_PATH);
    FileOutputStream fos = new FileOutputStream(file);
    while (true) {
      fos.write(DATA);
    }
  }
  
}
