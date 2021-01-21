package com.github.spsl.minirpc.example.extension.impl;

import com.github.spsl.minirpc.example.extension.DemoService;

public class DefaultDemoService implements DemoService {
    @Override
    public String sayHello() {
        return "hello default";
    }
}
