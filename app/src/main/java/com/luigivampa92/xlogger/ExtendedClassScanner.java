package com.luigivampa92.xlogger;

import android.content.Context;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// todo remove ?
//    private Set<Class<? extends HostApduService>> performHostApduServicesSearchByReflection(Context context) {
//        List<String> excludeList = Arrays.asList("androidx");
//        ExtendedClassScanner<HostApduService> extendedClassScanner = new ExtendedClassScanner<>(context, HostApduService.class, null, excludeList);
//        extendedClassScanner.scan();
//        return extendedClassScanner.getResult();
//    }



public class ExtendedClassScanner<T> extends ClassScanner {

    private final Class parentClass;
    private final HashSet<Class<? extends T>> result;
    private final List<String> packageNamesToInclude;
    private final List<String> packageNamesToExclude;

    public ExtendedClassScanner(Context context, Class<T> parentClass, List<String> packageNamesToInclude, List<String> packageNamesToExclude) {
        super(context);
        this.parentClass = parentClass;
        this.result = new HashSet<>();
        this.packageNamesToInclude = packageNamesToInclude;
        this.packageNamesToExclude = packageNamesToExclude;
    }

    @Override
    protected boolean isTargetClassName(String className) {
        boolean classIsNotInnerClass = !className.contains("$");
        if (!classIsNotInnerClass) {
            return false;
        }

        if (packageNamesToExclude != null && !packageNamesToExclude.isEmpty()) {
            for (String packageName : packageNamesToExclude) {
                if (className.startsWith(packageName)) {
                    return false;
                }
            }
        }

        if (packageNamesToInclude != null && !packageNamesToInclude.isEmpty()) {
            boolean classIsInScannedPackages = false;
            for (String packageName : packageNamesToInclude) {
                if (className.startsWith(packageName)) {
                    classIsInScannedPackages = true;
                    break;
                }
            }
            return classIsInScannedPackages;
        }

        return true;
    }

    @Override
    protected boolean isTargetClass(Class clazz) {
        boolean heirOfSeekedClass = parentClass.isAssignableFrom(clazz);
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        return heirOfSeekedClass && notAbstract;
    }

    @Override
    protected void onScanResult(Class clazz) {
        result.add(clazz);
    }

    public Set<Class<? extends T>> getResult() {
        return result;
    }
}
