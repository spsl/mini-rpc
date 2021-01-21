package com.github.spsl.minirpc.extension;

import com.alipay.remoting.util.ConcurrentHashSet;
import com.github.spsl.minirpc.bytecode.Compiler;
import com.github.spsl.minirpc.bytecode.JavassistCompiler;
import com.github.spsl.minirpc.exceptions.RpcException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final Set<ExtensionLoadingStrategy> strategySet = loadExtensionLoadingStrategy();


    private Class<T> type;

    private final AtomicReference<Object> cachedAdaptiveInstance = new AtomicReference<>();


    private ExtensionFactory extensionFactory;

    private Class<?> cachedAdaptiveExtensionClass;

    private Set<Class<?>> cachedWrapperClasses;

    private String cachedDefaultExtensionName;

    private Map<String, Class<?>> cachedClasses;

    private Map<String, AtomicReference<Object>> cachedExtensionInstances = new ConcurrentHashMap<>();


    private static Set<ExtensionLoadingStrategy> loadExtensionLoadingStrategy() {
        Set<ExtensionLoadingStrategy> result = new HashSet<>();
        ServiceLoader.load(ExtensionLoadingStrategy.class).forEach(item -> {
            result.add(item);
        });
        return result;
    }

    ExtensionLoader (Class<T> type) {
        this.type = type;
        this.extensionFactory = type == ExtensionFactory.class ? null : ExtensionLoaderFactory.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
        loadExtensionClass();
    }

    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            synchronized (cachedAdaptiveInstance) {
                instance = cachedAdaptiveInstance.get();
                if (instance == null) {
                    instance = createAdaptiveExtension();
                    cachedAdaptiveInstance.set(instance);
                }
            }
        }
        return (T) instance;
    }

    public List<String> getSupportedExtensionNameList() {
        return new ArrayList<>(cachedClasses.keySet());
    }

    public T getExtension(String name) {
        return getExtension(name, true);
    }

    public T getExtension(String name, boolean wrapper) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name cannot blank");
        }

        if ("default".equals(name)) {
            return getDefaultExtension();
        }

        return doGetExtensionByName(name, wrapper);
    }

    public T getDefaultExtension() {
        if (StringUtils.isBlank(cachedDefaultExtensionName)) {
            return null;
        }
        return doGetExtensionByName(cachedDefaultExtensionName, true);
    }

    private T doGetExtensionByName(String name, boolean wrapper) {
        final AtomicReference<Object> holder = getOrCreateReference(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name, wrapper);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    // 根据class 生成对象实例
    private T createExtension(String name, boolean wrap) {
        Class<?> clazz = cachedClasses.get(name);
        if (clazz == null) {
            return null;
        }
        try {
            Object instance = clazz.newInstance();
            return (T) instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AtomicReference<Object> getOrCreateReference(String name) {
        AtomicReference<Object> reference = cachedExtensionInstances.get(name);
        if (reference == null) {
            cachedExtensionInstances.putIfAbsent(name, new AtomicReference<>());
            reference = cachedExtensionInstances.get(name);
        }
        return reference;
    }

    private T createAdaptiveExtension() {
        try {
            return injectExtension((T)getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            logger.error("", e);
            throw new RpcException(e.getMessage());
        }
    }

    private T injectExtension(T instance) {
        // todo 执行注入逻辑
        return instance;
    }

    private Class<?> getAdaptiveExtensionClass() {
        if (cachedAdaptiveExtensionClass != null) {
            return cachedAdaptiveExtensionClass;
        }
        return cachedAdaptiveExtensionClass = createAdaptiveExtensionClass();
    }

    private void loadExtensionClass() {
        setDefaultExtensionName();
        cachedClasses = new ConcurrentHashMap<>();
        for (ExtensionLoadingStrategy strategy : strategySet) {
            loadDirectory(cachedClasses, strategy.getDirectory(), type.getName(), strategy.isExtensionLoaderClassLoaderFirst(), strategy.isOverridden(), strategy.excludePackages());
        }
    }

    private void setDefaultExtensionName() {
        SPI defaultValue = type.getAnnotation(SPI.class);
        if (defaultValue == null || StringUtils.isBlank(defaultValue.value())) {
            return;
        }
        cachedDefaultExtensionName = defaultValue.value();
    }

    // 通过动态字节码创建自适应扩展类class对象
    private Class<T> createAdaptiveExtensionClass() {
        String codeSource = new AdaptiveExtensionClassGenerator(type).generate();
        Compiler compiler = new JavassistCompiler();
        ClassLoader classLoader = findClassLoader();
        Class<T> tClass = compiler.compile(codeSource, findClassLoader());
        return tClass;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses,
                               String dir,
                               String type,
                               boolean extensionLoaderClassLoaderFirst,
                               boolean overridden,
                               String... excludedPackages) {

        String fileName = dir + type;
        try {
            Enumeration<URL> urls = null;
            ClassLoader classLoader = findClassLoader();

            // try to load from ExtensionLoader's ClassLoader first
            if (extensionLoaderClassLoaderFirst) {
                ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
                if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
                    urls = extensionLoaderClassLoader.getResources(fileName);
                }
            }

            if (urls == null || !urls.hasMoreElements()) {
                if (classLoader != null) {
                    urls = classLoader.getResources(fileName);
                } else {
                    urls = ClassLoader.getSystemResources(fileName);
                }
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL, overridden, excludedPackages);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,
                              java.net.URL resourceURL, boolean overridden, String... excludedPackages) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0 && !isExcluded(line, excludedPackages)) {
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name, overridden);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
//                            exceptions.put(line, e);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }


    private boolean isExcluded(String className, String... excludedPackages) {
        if (excludedPackages != null) {
            for (String excludePackage : excludedPackages) {
                if (className.startsWith(excludePackage + ".")) {
                    return true;
                }
            }
        }
        return false;
    }



    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name,
                           boolean overridden) throws NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + " is not subtype of interface.");
        }
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            cacheAdaptiveClass(clazz, overridden);
        } else if (isWrapperClass(clazz)) {
            cacheWrapperClass(clazz);
        } else {
            clazz.getConstructor();
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("name 不能为空");
            }
            saveInExtensionClass(extensionClasses, clazz, name, overridden);
        }
    }

    private void cacheAdaptiveClass(Class<?> clazz, boolean overridden) {
        if (cachedAdaptiveExtensionClass == null || overridden) {
            cachedAdaptiveExtensionClass = clazz;
        } else {
            // throw exception
        }
    }

    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {

        }
        return false;
    }

    private void cacheWrapperClass(Class<?> clazz) {
        if (cachedWrapperClasses == null) {
            cachedWrapperClasses = new ConcurrentHashSet<>();
        }
        cachedWrapperClasses.add(clazz);
    }

    private void saveInExtensionClass(Map<String, Class<?>> extensionClasses,
                                      Class<?> typeClass,
                                      String name,
                                      boolean overridden) {
        Class<?> old = extensionClasses.get(name);
        if (old == null || overridden) {
            extensionClasses.put(name, typeClass);
        } else if (old != typeClass) {
            String duplicateMsg = "Duplicate extension " + type.getName() + " name " + name + " on " + old.getName() + " and " + typeClass.getName();
            logger.error(duplicateMsg);
            throw new IllegalStateException(duplicateMsg);
        }
    }

    private ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

}
