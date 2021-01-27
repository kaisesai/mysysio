package com.liukai.sysio.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 手写 Netty 框架
 *
 * @author liukai 2021年01月26日
 */
public class MyNetty {
  
  public static void main(String[] args) throws InterruptedException {
    // 循环组执行任务
    // loopExecutor();
    
    // 启动客户端
    // clientMode();
    
    // 启动服务端
    // serverMode();
    
    // 启动 netty 客户端端
    nettyClient();
    
    // 启动 netty 服务端
    // nettyServer();
  }
  
  public static void nettyClient() throws InterruptedException {
    // 事件循环组
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
    // 客户端启动器
    Bootstrap client = new Bootstrap();
    // 配置启动器
    // 设置循环组
    ChannelFuture future = client.group(eventLoopGroup)
      // 配置 Channel
      .channel(NioSocketChannel.class)
      // 添加处理器
      .handler(new ChannelInit() {
        @Override
        public void init(ChannelHandlerContext ctx) {
          System.out.println("before MyNetty.init");
          ctx.pipeline().addLast(new MyHandler());
          System.out.println("after MyNetty.init");
        }
      })
      // 连接远端服务器
      .connect("www.kaige.com", 6001).sync();
    System.out.println("nett client started...");
    future.channel().closeFuture().sync();
    // 关闭事件循环组
    eventLoopGroup.shutdownGracefully();
    System.out.println("nett client closed...");
    
  }
  
  /**
   * netty 服务端程序
   */
  public static void nettyServer() throws InterruptedException {
    
    System.out.println("netty server starting...");
    
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
    ServerBootstrap server = new ServerBootstrap();
    ChannelFuture future = server.group(eventLoopGroup, eventLoopGroup)
      .channel(NioServerSocketChannel.class)
      // 自定义写法
      // .childHandler(new ChannelInit() {
      //   @Override
      //   public void init(ChannelHandlerContext ctx) {
      //     ctx.pipeline().addLast(new MyHandler());
      //   }
      // })
      // 官方写法
      .childHandler(new ChannelInitializer<NioSocketChannel>() {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
          System.out.println("before MyNetty.initChannel");
          ch.pipeline().addLast(new MyHandler());
          System.out.println("after MyNetty.initChannel");
        }
      }).bind(8091).sync();
    
    // 监听关闭事件
    future.channel().closeFuture().sync();
    
    System.out.println("netty server close...");
    
  }
  
  /**
   * 服务端模式
   */
  public static void serverMode() throws InterruptedException {
    // 事件循环组
    NioEventLoopGroup eventExecutors = new NioEventLoopGroup(1);
    
    // 创建服务端 socket
    NioServerSocketChannel channel = new NioServerSocketChannel();
    
    // 接收客户端，并且注册
    channel.pipeline().addLast(new MyAcceptHandler(eventExecutors, new ChannelInit() {
      @Override
      public void init(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(new MyHandler());
      }
    }));
    
    // 注册 selector
    eventExecutors.register(channel);
    
    // 绑定端口
    channel.bind(new InetSocketAddress(8091)).sync().channel().closeFuture().sync();
    System.out.println("server close...");
  }
  
  /**
   * 客户端
   *
   * @throws InterruptedException
   */
  public static void clientMode() throws InterruptedException {
    
    NioEventLoopGroup eventExecutors = new NioEventLoopGroup(1);
    
    // 客户端模式
    NioSocketChannel channel = new NioSocketChannel();
    // 将 channel 注册到 selector 上，对应执行 epoll_ctl 系统调用
    eventExecutors.register(channel);
    
    // 操作数据
    ChannelPipeline pipeline = channel.pipeline();
    pipeline.addLast(new MyHandler());
    
    // react 方式
    // 连接服务端，这是一个异步操作
    ChannelFuture channelFuture = channel.connect(new InetSocketAddress("www.kaige.com", 6001))
      .sync();
    
    // 给客户端发送数据
    // 使用非池化方式分配 ByteBuf
    ByteBuf byteBuf = Unpooled.copiedBuffer("hello server\n".getBytes(StandardCharsets.UTF_8));
    channel.writeAndFlush(byteBuf).sync();
    
    // 监听关闭
    channelFuture.channel().closeFuture().sync();
    
    eventExecutors.shutdownGracefully();
    System.out.println("client closed...");
    
  }
  
  /**
   * 循环事件执行器
   */
  public static void loopExecutor() {
    // 事件循环组，内部会根据参数来固定数量的创建线程组
    NioEventLoopGroup executors = new NioEventLoopGroup(2);
    
    executors.execute(() -> {
      for (; ; ) {
        try {
          System.out.println(Thread.currentThread().getName() + " hello world 001");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
    // 如果线程组数量为 1，下面的逻辑是不会执行的
    executors.execute(() -> {
      for (; ; ) {
        try {
          System.out.println(Thread.currentThread().getName() + " hello world 002");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
  }
  
}
