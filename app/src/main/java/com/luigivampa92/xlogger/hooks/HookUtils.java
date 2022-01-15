package com.luigivampa92.xlogger.hooks;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

public final class HookUtils {

    private HookUtils() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static boolean hasNonAbstractMethodImplementation(Class<?> cls, String methodName) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    public static void logClassList(Collection<Class<?>> classes, String packageName) {
        try {
            if (classes != null && !classes.isEmpty()) {
                for (final Class<?> serviceClass : classes) {
                    XLog.d("Package %s - class found - %s", packageName, serviceClass.getCanonicalName());
                }
            }
        }
        catch (Throwable e) {
        }
    }
}
