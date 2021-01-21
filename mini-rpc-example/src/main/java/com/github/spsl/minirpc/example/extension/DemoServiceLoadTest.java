package com.github.spsl.minirpc.example.extension;

import com.github.spsl.minirpc.extension.ExtensionLoaderFactory;

public class DemoServiceLoadTest {
    public static void main(String[] args) {
        DemoService demoService = ExtensionLoaderFactory.getExtensionLoader(DemoService.class).getAdaptiveExtension();
        System.out.println(demoService.sayHello());

        demoService = ExtensionLoaderFactory.getExtensionLoader(DemoService.class).getDefaultExtension();
        System.out.println(demoService.sayHello());
    }
}
