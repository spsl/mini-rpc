package com.github.spsl.minirpc.bytecode;

import com.github.spsl.minirpc.annotations.SPI;
import com.github.spsl.minirpc.exceptions.RpcException;
import com.github.spsl.minirpc.rpc.Invoker;

import java.net.URL;
import java.util.List;

@SPI("javassist")
public interface ProxyFactory {

    <T> T getProxy(List<Class<?>> interfaceClasses, Invoker<T> invoker) throws RpcException;

    <T> T getProxy(Class<?> type, Invoker<T> invoker) throws RpcException;

    <T> Invoker<T> getInvoker(Class<T> type, T proxy) throws RpcException;
}
