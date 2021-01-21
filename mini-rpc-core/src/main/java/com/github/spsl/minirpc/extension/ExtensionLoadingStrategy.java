package com.github.spsl.minirpc.extension;

public interface ExtensionLoadingStrategy {

    String getDirectory();

    default boolean isOverridden() { return false; }

    default boolean isExtensionLoaderClassLoaderFirst() { return true;}

    default String[] excludePackages() { return new String[0];}
}
