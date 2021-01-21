package com.github.spsl.minirpc.extension;

public class DefaultExtensionLoadingStrategy implements ExtensionLoadingStrategy {
    @Override
    public String getDirectory() {
        return "META-INF/mini-rpc/";
    }

    @Override
    public boolean isOverridden() {
        return false;
    }

    @Override
    public String[] excludePackages() {
        return new String[0];
    }
}
