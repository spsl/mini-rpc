package com.github.spsl.minirpc.bytecode.javassist;

import com.github.spsl.minirpc.bytecode.Proxy;
import com.github.spsl.minirpc.exceptions.RpcException;
import javassist.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JavassistProxy implements Proxy {

    private Class<?> cachedInstanceClass;

    private static final AtomicInteger proxyCount = new AtomicInteger();

    public JavassistProxy(List<Class<?>> classes) {
        final String className = "com.github.spsl.minirpc.bytecode.javassist.Proxy$"
                + proxyCount.incrementAndGet();

        try {
            // 创建类
            ClassPool pool = ClassPool.getDefault();

            CtClass ctClass = pool.makeClass(className);
            CtClass[] interfaces = new CtClass[classes.size()];
            for (int i = 0; i < classes.size(); i++) {
                interfaces[i] = pool.getCtClass(classes.get(i).getName());
            }
            ctClass.setInterfaces(interfaces);

            // 添加私有成员handler
            ctClass.addField(CtField.make(String.format("private final %s handler;", InvocationHandler.class.getName()), ctClass));

            // 添加静态方法引用
            ctClass.addField(CtField.make(String.format("public static %s[] methods;", Method.class.getName()), ctClass));

            // 添加构造方法
            ctClass.addConstructor(CtNewConstructor.make(new CtClass[]{pool.get(InvocationHandler.class.getName())}, null, "{handler = $1;}", ctClass));

            // 添加方法
            Set<String> readySet = new HashSet<>();
            List<Method> methods = new ArrayList<>();

            for (Class<?> item : classes) {
                for (Method method : item.getMethods()) {
                    String methodSign = getMethodSign(method);
                    if (readySet.contains(methodSign)) {
                        continue;
                    }
                    if (Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }
                    readySet.add(methodSign);

                    int index = methods.size();
                    Class<?> returnType = method.getReturnType();
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Class<?>[] exceptions = method.getExceptionTypes();
                    CtClass[] params = new CtClass[paramTypes.length];
                    CtClass[] exceptionTypes = new CtClass[exceptions.length];

                    for (int i = 0; i < exceptions.length; i++) {
                        exceptionTypes[i] = pool.getCtClass(exceptions[i].getName());
                    }
                    StringBuilder codeDesc = new StringBuilder("{");

                    codeDesc.append(String.format("java.lang.Object[] args = new java.lang.Object[%s];\n", paramTypes.length));
                    for (int i = 0; i < paramTypes.length; i++) {
                        codeDesc.append(String.format("args[%s] = ($w)$%s;\n", i, i + 1));
                        params[i] = pool.getCtClass(paramTypes[i].getName());
                    }
                    codeDesc.append(String.format("java.lang.Object result = handler.invoke(this, methods[%s], args);\n", index));
                    if (!Void.TYPE.equals(returnType)) {
                        codeDesc.append(String.format("return %s;\n", asArgument(returnType, "result")));
                    }
                    codeDesc.append("}");
                    methods.add(method);
                    CtMethod ctMethod = CtNewMethod.make(pool.getCtClass(returnType.getName()), method.getName(), params, exceptionTypes, codeDesc.toString(), ctClass);
                    ctClass.addMethod(ctMethod);
                }
            }

            cachedInstanceClass = ctClass.toClass();
            Method[] methodArray = new Method[methods.size()];
            for (int i = 0; i < methods.size(); i++) {
                methodArray[i] = methods.get(i);
            }
            cachedInstanceClass.getField("methods").set(null, methodArray);
        } catch (Exception e) {
            throw new RpcException("生成代理对象异常", e);
        }
    }

    @Override
    public Object newInstance(InvocationHandler handler) {
        try {
            return cachedInstanceClass.getConstructor(InvocationHandler.class).newInstance(handler);
        } catch (Exception e) {
            throw new RpcException("实例化代理对象异常", e);
        }
    }


    private String getMethodSign(Method m) {
        StringBuilder ret = new StringBuilder(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            ret.append(parameterTypes[i].getName());
        }
        ret.append(')').append(m.getReturnType().getName());
        return ret.toString();
    }

    private static String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + cl.getName() + ")" + name;
    }

}
