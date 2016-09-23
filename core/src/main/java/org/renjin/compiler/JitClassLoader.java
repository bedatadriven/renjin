/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
