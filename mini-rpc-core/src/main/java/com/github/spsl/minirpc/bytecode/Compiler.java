package com.github.spsl.minirpc.bytecode;

import com.github.spsl.minirpc.annotations.SPI;

@SPI
public interface Compiler {

    <T> Class<T> compile(String code, ClassLoader classLoader);
}
