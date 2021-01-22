package com.github.spsl.minirpc.extension;

import com.github.spsl.minirpc.annotations.SPI;

@SPI
public interface ExtensionFactory {

    <T> T getExtension(Class<T> type, String name);
}
