package com.github.spsl.minirpc.bytecode.javassist;

import com.github.spsl.minirpc.bytecode.Wrapper;
import com.github.spsl.minirpc.exceptions.RpcException;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class JavassistWrapperGenerate<T> {

    private static final AtomicInteger count = new AtomicInteger();

    private final Class<?> cachedWrapperClass;

    private final Class<?> type;

    public JavassistWrapperGenerate(Class<T> type) {
        this.type = type;

        ClassPool pool = ClassPool.getDefault();

        String cName = "com.github.spsl.minirpc.bytecode.Wrapper$" + count.incrementAndGet();

        CtClass ctClass = pool.makeClass(cName);

        try {
            ctClass.addField(CtField.make(String.format("private %s handler;", type.getName()), ctClass));
            ctClass.addConstructor(CtNewConstructor.make(new CtClass[] {pool.getCtClass(type.getName())}, null, "{ handler = $1; }", ctClass));
            ctClass.addInterface(pool.getCtClass(Wrapper.class.getName()));

            StringBuilder methodContent = new StringBuilder("public Object invokeMethod(String methodNm, Class[] ts, Object[] args) { \n");

            for (Method method : type.getMethods()) {
                String name = method.getName();
                Class<?>[] paramTypes = method.getParameterTypes();
                int paramTypeLength = paramTypes.length;
                methodContent.append(String.format("if ($1.equals(\"%s\") \n", name));
                methodContent.append(String.format("      && $2.length == %s ", paramTypeLength));
                for (int i = 0; i < paramTypes.length; i++) {
                    Class<?> paramType = paramTypes[i];
                    methodContent.append(String.format("      \n&& \"%s\".equals($2[%s].getName()) %s", paramType.getName(), i));
                }
                methodContent.append(") { \n");
                if (Void.TYPE.equals(method.getReturnType())) {
                    methodContent.append(String.format("handler.%s(", name));
                    methodContent.append("\nreturn;");
                } else {
                    methodContent.append(String.format("return handler.%s(", name));
                }
                for (int i = 0; i < paramTypeLength; i++) {
                    methodContent.append(String.format("$3[%s]", i));
                    if (i < paramTypeLength - 1) {
                        methodContent.append(", ");
                    }
                }
                methodContent.append("); \n }");
            }

            methodContent.append(" \n throw new com.github.spsl.minirpc.exceptions.RpcException(\"no such method : \" + $1);");
            methodContent.append("\n }");
            ctClass.addMethod(CtNewMethod.make(methodContent.toString(), ctClass));
            cachedWrapperClass = ctClass.toClass();
        } catch (Exception e) {
            throw new RpcException("生成包装类型错误", e);
        }
    }

    public Wrapper generate(T implement) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (Wrapper) cachedWrapperClass.getConstructor(type).newInstance(implement);
    }


}
