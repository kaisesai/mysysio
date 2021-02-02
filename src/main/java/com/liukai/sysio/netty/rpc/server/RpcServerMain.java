package com.liukai.sysio.netty.rpc.server;

import com.liukai.sysio.netty.rpc.protocol.ProtocolType;
import com.liukai.sysio.netty.rpc.transport.http.ServerHttpHandler;
import com.liukai.sysio.netty.rpc.transport.rpc.MyRpcDecoder;
import com.liukai.sysio.netty.rpc.transport.rpc.MyRpcEncoder;
import com.liukai.sysio.netty.rpc.transport.rpc.ServerRpcHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * 基于 netty 的自定义 RPC 服务端程序
 */
public class RpcServerMain {
  
  public static void main(String[] args) {
    // 用 netty 的方式启动
    // startFromNetty();
    
    // 用 jetty 的方式启动
    startFromJetty();
  }
  
  private static void startFromJetty() {
    try {
      // 利用 jetty 启动 http web 服务器
      Server server = new Server(9090);
      ServletContextHandler sch = new ServletContextHandler(server, "/");
      sch.addServlet(MyHttpServletRpcHandler.class, "/*");
      server.setHandler(sch);
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  private static void startFromNetty() {
    // 启动 netty 服务端
    NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    
    ProtocolType protocolType = ProtocolType.HTTP;
    // 启动类
    ServerBootstrap server = new ServerBootstrap();
    try {
      ChannelFuture future = server.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
          @Override
          protected void initChannel(NioSocketChannel ch) throws Exception {
            // 添加处理器
            System.out.println(Thread.currentThread().getName()
                                 + " : server channel accept client channel... transport protocol = "
                                 + protocolType);
            /*
              1. 自定义的 rpc 需要关注的问题：粘包拆包、header + body
              2. 小火车，传输协议用的就是 http
             */
            if (protocolType == ProtocolType.RPC) {
              // 消息编码器
              ch.pipeline().addLast(new MyRpcEncoder());
              // 消息解码器
              ch.pipeline().addLast(new MyRpcDecoder());
              // 消息读取器
              ch.pipeline().addLast(new ServerRpcHandler());
            } else if (protocolType == ProtocolType.HTTP) {
              // http 相关的编解码处理器
              ch.pipeline().addLast(new HttpServerCodec());
              ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
              ch.pipeline().addLast(new ServerHttpHandler(false));
            }
            
          }
        }).bind(9090).sync();
      
      System.out.println("my rpc server up...");
      // 监听关闭事件
      future.channel().closeFuture().sync();
      
      System.out.println("my rpc server down...");
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
  
}
