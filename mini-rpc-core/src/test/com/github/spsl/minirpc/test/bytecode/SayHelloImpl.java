package com.github.spsl.minirpc.test.bytecode;

public class SayHelloImpl implements SayHello {
    @Override
    public String hello() {
        return "hello";
    }
}
