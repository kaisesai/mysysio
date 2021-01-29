package com.liukai.sysio.netty.rpc.server;

import com.liukai.sysio.netty.rpc.msg.MyMsgDecoder;
import com.liukai.sysio.netty.rpc.msg.MyMsgEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 基于 netty 的自定义 RPC 服务端程序
 */
public class MyRpcServer {
  
  public static void main(String[] args) {
    // 启动 netty 服务端
    NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    // NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    NioEventLoopGroup workerGroup = bossGroup;
    
    // 启动类
    ServerBootstrap server = new ServerBootstrap();
    try {
      ChannelFuture future = server.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<NioSocketChannel>() {
          @Override
          protected void initChannel(NioSocketChannel ch) throws Exception {
            // 添加处理器
            System.out.println(
              Thread.currentThread().getName() + " : server channel accept client channel...");
            // 消息编码器
            ch.pipeline().addLast(new MyMsgEncoder());
            // 消息解码器
            ch.pipeline().addLast(new MyMsgDecoder());
            // 消息读取器
            ch.pipeline().addLast(new ServerMsgHandler());
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
