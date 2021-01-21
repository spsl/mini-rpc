package com.github.spsl.minirpc.test.extension;

import com.github.spsl.minirpc.extension.ExtensionFactory;
import com.github.spsl.minirpc.extension.ExtensionLoaderFactory;
import org.junit.Test;

public class ExtensionLoaderTest {

    @Test
    public void test() {

        ExtensionFactory extensionFactory = ExtensionLoaderFactory.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
        System.out.println("hello");
    }
}
