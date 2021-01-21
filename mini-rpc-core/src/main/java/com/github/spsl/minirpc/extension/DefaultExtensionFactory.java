package com.github.spsl.minirpc.extension;

public class DefaultExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoaderFactory.getExtensionLoader(type);
            return loader.getAdaptiveExtension();
        }
        return null;
    }
}
