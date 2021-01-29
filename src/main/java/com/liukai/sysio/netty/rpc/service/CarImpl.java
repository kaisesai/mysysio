package com.liukai.sysio.netty.rpc.service;

/**
 * 服务端 Car 的实现
 */
public class CarImpl implements Car {
  
  @Override
  public String drive(String name) {
    System.out.println(Thread.currentThread().getName() + " server get client args: " + name);
    return "server res" + name;
  }
  
}
