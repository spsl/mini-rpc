package com.github.spsl.minirpc.extension;

@SPI
public interface ExtensionFactory {

    <T> T getExtension(Class<T> type, String name);
}
