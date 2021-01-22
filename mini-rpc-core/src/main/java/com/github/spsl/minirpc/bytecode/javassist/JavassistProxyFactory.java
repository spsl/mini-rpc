package com.github.spsl.minirpc.bytecode.javassist;

import com.github.spsl.minirpc.annotations.Adaptive;
import com.github.spsl.minirpc.bytecode.Proxy;
import com.github.spsl.minirpc.bytecode.ProxyFactory;
import com.github.spsl.minirpc.exceptions.RpcException;
import com.github.spsl.minirpc.rpc.Invoker;
import com.github.spsl.minirpc.rpc.Request;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Adaptive
public class JavassistProxyFactory implements ProxyFactory {

    private Map<String, Proxy> cachedProxy = new ConcurrentHashMap<>();

    @Override
    public <T> T getProxy(List<Class<?>> interfaceClasses, Invoker<T> invoker) throws RpcException {
        try {
            if (CollectionUtils.isEmpty(interfaceClasses)) {
                throw new IllegalArgumentException("interfaceClasses 不能为空");
            }

            String key = genericKey(interfaceClasses);
            Proxy proxy = cachedProxy.get(key);

            if (proxy == null) {
                synchronized (cachedProxy) {
                    proxy = cachedProxy.get(key);
                    if (proxy == null) {
                        proxy = createProxy(interfaceClasses);
                        cachedProxy.put(key, proxy);
                    }
                }
            }
            return (T) proxy.newInstance(new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Request request = new Request();
                    return invoker.invoke(request);
                }
            });
        } catch (IllegalArgumentException e) {
            throw new RpcException("生成代理对象异常", e);
        }
    }

    @Override
    public <T> T getProxy(Class<?> type, Invoker<T> invoker) throws RpcException {
        return getProxy(Collections.singletonList(type), invoker);
    }

    private JavassistProxy createProxy(List<Class<?>> interfaceClasses) {
        return new JavassistProxy(interfaceClasses);
    }

    private String genericKey(List<Class<?>> interfaceClasses) {
        return interfaceClasses.stream().map(Class::getName).collect(Collectors.joining(","));
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        return null;
    }
}
