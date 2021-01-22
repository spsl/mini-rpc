package com.github.spsl.minirpc.example.extension;

import com.github.spsl.minirpc.annotations.SPI;

@SPI("default")
public interface DemoService {
    String sayHello();
}
