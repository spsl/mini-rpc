package com.github.spsl.minirpc.bytecode;

public interface Wrapper {
    Object invokeMethod(String methodName, Class<?>[] paramTypes, Object[] args);
}
