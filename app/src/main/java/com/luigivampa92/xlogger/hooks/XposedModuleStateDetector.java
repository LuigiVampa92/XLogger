package com.luigivampa92.xlogger.hooks;

public final class XposedModuleStateDetector {

   public static final String KEY_XLOGGER_XPOSED_MODULE_ACTIVATED = "com.luigivampa92.xlogger.xposed.module.activated";

   private XposedModuleStateDetector() {
      throw new IllegalAccessError("No instantiation!");
   }

   public static boolean isXposedModuleActive() {
      return "true".equals(System.getProperty(KEY_XLOGGER_XPOSED_MODULE_ACTIVATED, "false"));
   }
}
