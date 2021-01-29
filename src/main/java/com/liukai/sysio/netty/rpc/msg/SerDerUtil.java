package com.liukai.sysio.netty.rpc.msg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 对象序列化为字节数组的工具类
 */
public class SerDerUtil {
  
  private static final ByteArrayOutputStream BOS = new ByteArrayOutputStream();
  
  private static final byte[] EMPTY_BYTES = new byte[0];
  
  public synchronized static byte[] ser(Object object) {
    BOS.reset();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(BOS);
      oos.writeObject(object);
      return BOS.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return EMPTY_BYTES;
  }
  
}
