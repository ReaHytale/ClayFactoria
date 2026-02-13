package com.clayfactoria.utils;

import org.jetbrains.annotations.Nullable;

public class Utils {
  /**
   * Check if the given object is <code>null</code>, and throw a <code>NullPointerException</code>
   * with the given message if it is.
   *
   * @param obj The object to check.
   * @param msg The message to attach to the NullPointerException if <code>obj == null</code>
   * @throws NullPointerException If <code>obj == null</code>
   */
  public static void checkNull(Object obj, @Nullable String msg) throws NullPointerException {
    if (obj == null) {
      throw new NullPointerException(msg);
    }
  }

  /**
   * Check if the given object is <code>null</code>, and throw a <code>NullPointerException</code>
   * if it is.
   *
   * @param obj The object to check.
   * @throws NullPointerException If <code>obj == null</code>
   */
  public static void checkNull(Object obj) throws NullPointerException {
    checkNull(obj, null);
  }
}
