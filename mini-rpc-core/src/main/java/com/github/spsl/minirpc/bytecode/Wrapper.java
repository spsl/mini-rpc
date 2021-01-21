package com.github.spsl.minirpc.bytecode;

public interface Wrapper {

    String[] getPropertyNames();

    Class<?> getPropertyType(String pn);

    boolean hasProperty(String name);

    Object getPropertyValue(Object instance, String pn);

    void setPropertyValue(Object instance, String pn, Object pv);

    default Object[] getPropertyValues(Object instance, String[] pns) {
        Object[] ret = new Object[pns.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getPropertyValue(instance, pns[i]);
        }
        return ret;
    }

    default void setPropertyValues(Object instance, String[] pns, Object[] pvs) {
        if (pns.length != pvs.length) {
            throw new IllegalArgumentException("pns.length != pvs.length");
        }

        for (int i = 0; i < pns.length; i++) {
            setPropertyValue(instance, pns[i], pvs[i]);
        }
    }

    String[] getMethodNames();

    String[] getDeclaredMethodNames();

    default boolean hasMethod(String name) {
        for (String mn : getMethodNames()) {
            if (mn.equals(name)) {
                return true;
            }
        }
        return false;
    }

    Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args);
}
