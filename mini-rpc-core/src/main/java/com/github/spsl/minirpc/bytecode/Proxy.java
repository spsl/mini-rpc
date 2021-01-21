package com.github.spsl.minirpc.bytecode;

import java.lang.reflect.InvocationHandler;

public interface Proxy {

    Object newInstance(InvocationHandler handler);
}
