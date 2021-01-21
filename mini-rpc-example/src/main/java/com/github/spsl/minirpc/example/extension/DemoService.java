package com.github.spsl.minirpc.example.extension;

import com.github.spsl.minirpc.extension.SPI;

@SPI("default")
public interface DemoService {
    String sayHello();
}
