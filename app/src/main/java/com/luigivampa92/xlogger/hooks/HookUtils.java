package com.luigivampa92.xlogger.hooks;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public final class HookUtils {

    private HookUtils() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static Map<Class<?>, Set<Class<?>>> findChildClassesInClassLoader(Context context, Class<?>[] parentClasses) throws IOException, ClassNotFoundException {
        HashMap<Class<?>, Set<Class<?>>> result = new HashMap<>();
        if (context == null || parentClasses == null || parentClasses.length == 0) {
            return result;
        }

        String packageName = String.valueOf(context.getPackageName());
        XLog.v("Package %s - classloader scan - start", packageName);
        long timeStart = System.currentTimeMillis();;

        PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
        DexFile dexFile = new DexFile(context.getPackageCodePath());
        Enumeration<String> classNames = dexFile.entries();
        while (classNames.hasMoreElements()) {
            String className = classNames.nextElement();
            Class<?> targetClass;
            try {
                targetClass = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                continue;
            }
            boolean targetClassIsNotAbstract = !Modifier.isAbstract(targetClass.getModifiers());
            boolean targetClassIsNotANonStaticInnerClass = !targetClass.getName().contains("$");
            if (targetClassIsNotAbstract && targetClassIsNotANonStaticInnerClass) {
                for (Class<?> parentClass : parentClasses) {
                    boolean targetClassIsAssignableFromParentClass = parentClass.isAssignableFrom(targetClass);
                    if (targetClassIsAssignableFromParentClass) {
                        boolean resultAlreadyContainsSet = result.containsKey(parentClass);
                        if (!resultAlreadyContainsSet) {
                            result.put(parentClass, new HashSet<>());
                        }
                        Set<Class<?>> childClassesSet = result.get(parentClass);
                        childClassesSet.add(targetClass);
                        XLog.v("Package %s - classloader scan - child class of [ %s ] found - [ %s ]", packageName, parentClass.getCanonicalName(), targetClass.getName());
                    }
                }
            }
        }

        long timeStop = System.currentTimeMillis();
        long timeSpent = timeStop - timeStart;
        XLog.v("Package %s - classloader scan - complete - took " + timeSpent + " ms", packageName);

        return result;
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
