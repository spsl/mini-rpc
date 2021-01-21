package com.github.spsl.minirpc.rpc;

import com.github.spsl.minirpc.exceptions.RpcException;


public interface Invoker<T> {

    Class<T> getInterface();

    Response invoke(Request request) throws RpcException;

}
