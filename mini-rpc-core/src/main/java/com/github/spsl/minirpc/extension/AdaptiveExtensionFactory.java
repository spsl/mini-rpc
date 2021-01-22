package com.github.spsl.minirpc.extension;

import com.github.spsl.minirpc.annotations.Adaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

    private final List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory() {
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoaderFactory.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
        loader.getSupportedExtensionNameList().forEach(name -> {
            list.add(loader.getExtension(name));
        });
        factories = Collections.unmodifiableList(list);
    }

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }
}
