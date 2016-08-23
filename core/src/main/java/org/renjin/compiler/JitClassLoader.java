package org.renjin.compiler;

/**
 * Loads generated classes 
 */
public class JitClassLoader {

  public static <T> Class<T> defineClass(Class<T> classType, String className, byte[] bytes) {
    MyClassLoader myClassLoader = new MyClassLoader(JitClassLoader.class.getClassLoader());
    return (Class<T>)myClassLoader.defineClass(className, bytes);
  }

  private static class MyClassLoader extends ClassLoader {
    MyClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}
