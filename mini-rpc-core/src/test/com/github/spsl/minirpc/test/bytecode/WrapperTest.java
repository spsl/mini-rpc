package com.github.spsl.minirpc.test.bytecode;

import com.github.spsl.minirpc.bytecode.Wrapper;
import com.github.spsl.minirpc.bytecode.javassist.JavassistWrapperGenerate;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class WrapperTest {

    @Test
    public void wrapperTest() {

        try {
            Wrapper wrapper = new JavassistWrapperGenerate<SayHello>(SayHello.class).generate(new SayHelloImpl());
            System.out.println(wrapper.invokeMethod("hello2", new Class[]{}, new Object[]{}));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
