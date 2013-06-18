package org.renjin.invoke;

import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.invoke.reflection.ClassDefinitionBinding;

/**
 * Provides R {@code Function}s which invoke JVM methods.
 */
public class ClassBindings {

  public static <T> ClassBinding getClassDefinitionBinding(Class instance) {
    return new ClassDefinitionBinding(ClassBindingImpl.get(instance));
  }

  public static ClassBinding getClassBinding(Class aClass) {
    return ClassBindingImpl.get(aClass);
  }
}
