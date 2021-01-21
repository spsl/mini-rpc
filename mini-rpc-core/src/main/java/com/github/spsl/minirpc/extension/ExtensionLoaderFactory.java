package com.github.spsl.minirpc.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoaderFactory {

    private static final Map<Class<?>, ExtensionLoader<?>> CACHED_EXTENSION_LOADER = new ConcurrentHashMap<>();

    // 对于每个type都有一个extensionLoader
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        ExtensionLoader<T> loader = (ExtensionLoader<T>) CACHED_EXTENSION_LOADER.get(type);
        if (loader == null) {
            CACHED_EXTENSION_LOADER.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) CACHED_EXTENSION_LOADER.get(type);
        }
        return loader;
    }
}
