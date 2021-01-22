package com.github.spsl.minirpc.test.bytecode;

import com.github.spsl.minirpc.bytecode.ProxyFactory;
import com.github.spsl.minirpc.bytecode.javassist.JavassistProxy;
import com.github.spsl.minirpc.exceptions.RpcException;
import com.github.spsl.minirpc.extension.ExtensionLoaderFactory;
import com.github.spsl.minirpc.rpc.Invoker;
import com.github.spsl.minirpc.rpc.Request;
import com.github.spsl.minirpc.rpc.Response;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;

public class ProxyTest {

    @Test
    public void testProxy() {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return "hello";
            }
        };

        SayHello sayHello = (SayHello) new JavassistProxy(Collections.singletonList(SayHello.class)).newInstance(invocationHandler);

        System.out.println(sayHello.hello());

        sayHello = ExtensionLoaderFactory.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension().getProxy(SayHello.class, new Invoker<SayHello>() {
            @Override
            public Class<SayHello> getInterface() {
                return SayHello.class;
            }

            @Override
            public Response invoke(Request request) throws RpcException {
                return null;
            }
        });

    }
}
