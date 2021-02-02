package com.liukai.sysio.netty.rpc.server;

import com.liukai.sysio.netty.rpc.constant.Constants;
import com.liukai.sysio.netty.rpc.protocol.MsgContent;
import com.liukai.sysio.netty.rpc.service.ServiceInvoker;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * http servlet 处理器
 */
public class MyHttpServletRpcHandler extends HttpServlet {
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // 获取请求头 requestID
    String requestID = req.getHeader(Constants.HTTP_HEADER_MY_REQUEST_ID);
    
    // 读取请求数据
    ServletInputStream sis = req.getInputStream();
    ObjectInputStream ois = new ObjectInputStream(sis);
    try {
      MsgContent content = (MsgContent) ois.readObject();
      
      // 执行目标方法
      Object result = ServiceInvoker.invokeTarget(content);
      System.out.println("result = " + result);
      
      MsgContent msgContent = new MsgContent();
      msgContent.setResult(result);
      
      // 将 requestID 写回去
      resp.setHeader(Constants.HTTP_HEADER_MY_REQUEST_ID, requestID);
      
      // 写回客户端
      ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
      oos.writeObject(msgContent);
      
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
  
}
