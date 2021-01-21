package com.github.spsl.minirpc.example.extension.impl;

import com.github.spsl.minirpc.example.extension.DemoService;
import com.github.spsl.minirpc.extension.Adaptive;

@Adaptive
public class AdaptiveDemoService implements DemoService {

    @Override
    public String sayHello() {
        return "hello adaptive";
    }
}
