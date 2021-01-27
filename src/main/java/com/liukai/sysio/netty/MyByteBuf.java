package com.liukai.sysio.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 演示 netty 中 ByteBuf 的用法
 *
 * @author liukai 2021年01月26日
 */
public class MyByteBuf {
  
  public static void main(String[] args) {
    
    // 使用池化技术分配 ByteBuf
    // ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(8, 20);
    ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer(8, 20);
    printBuffer(buffer);
    
    // write 数据
    buffer.writeBytes(new byte[] {1, 2, 3, 4});
    printBuffer(buffer);
    
    buffer.writeBytes(new byte[] {1, 2, 3, 4});
    printBuffer(buffer);
    
    buffer.writeBytes(new byte[] {1, 2, 3, 4});
    printBuffer(buffer);
    
    buffer.writeBytes(new byte[] {1, 2, 3, 4});
    printBuffer(buffer);
    
    buffer.writeBytes(new byte[] {1, 2, 3, 4});
    printBuffer(buffer);
    
    // read 数据
    buffer.readBytes(new byte[4]);
    printBuffer(buffer);
    
    // get 数据
    byte aByte = buffer.getByte(0);
    System.out.println("aByte = " + aByte);
    printBuffer(buffer);
    
    // set 数据
    buffer.setByte(0, 9);
    printBuffer(buffer);
    
  }
  
  private static void printBuffer(ByteBuf buffer) {
    // 是否可读
    System.out.println("buffer.isReadable() = " + buffer.isReadable());
    // 读的索引
    System.out.println("buffer.readerIndex() = " + buffer.readerIndex());
    // 可读取的字节数
    System.out.println("buffer.readableBytes() = " + buffer.readableBytes());
    // 是否可写
    System.out.println("buffer.isWritable() = " + buffer.isWritable());
    // 写的索引
    System.out.println("buffer.writerIndex() = " + buffer.writerIndex());
    // 可写的字节数量
    System.out.println("buffer.writableBytes() = " + buffer.writableBytes());
    // buffer 的容量
    System.out.println("buffer.capacity() = " + buffer.capacity());
    // buffer 最大的容量
    System.out.println("buffer.maxCapacity() = " + buffer.maxCapacity());
    // buffer 是否是直接（堆外）内存
    System.out.println("buffer.isDirect() = " + buffer.isDirect());
    System.out.println("-------------");
  }
  
}
