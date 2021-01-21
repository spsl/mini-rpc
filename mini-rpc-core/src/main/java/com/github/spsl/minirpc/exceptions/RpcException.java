package com.github.spsl.minirpc.exceptions;

public class RpcException extends RuntimeException {

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable throwable) {
        super(msg, throwable);
    }


}
