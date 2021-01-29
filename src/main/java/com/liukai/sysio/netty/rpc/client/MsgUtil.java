package com.liukai.sysio.netty.rpc.client;

import com.liukai.sysio.netty.rpc.msg.MsgBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MsgUtil {
  
  /**
   * 获取 body 的长度
   *
   * @param body
   * @return
   * @throws IOException
   */
  public static int getBodyLength(MsgBody body) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(body);
    int bodyLength = bos.size();
    return bodyLength;
  }
  
}
