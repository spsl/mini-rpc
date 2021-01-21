package com.github.spsl.minirpc.bytecode;

import com.github.spsl.minirpc.exceptions.RpcException;
import com.github.spsl.minirpc.rpc.Invoker;

import java.net.URL;

public interface ProxyFactory {

    <T> T getProxy(Invoker<T> invoker) throws RpcException;

    <T> T getProxy(Invoker<T> invoker, boolean generic) throws RpcException;

    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;
}
