package com.luigivampa92.xlogger.domain;

public final class StringSerializationUtils {

   public static final String CONST_VALUE_NULL = "null";
   public static final String CONST_DELIMETER_ENTRIES = "*";
   public static final String CONST_DELIMETER_SINGLE_ENTRY = "^";
   public static final String CONST_DELIMETER_LOG_OBJECT_ELEMENT = "~";

   private StringSerializationUtils() {
      throw new IllegalAccessError("No instantiation!");
   }
}
