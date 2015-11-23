package org.renjin.gcc.translate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.runtime.Builtins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Maintains a list of methods/functions that are visible to the Gimple
 * code being translated. These include builtin functions, JVM methods
 * provided by the compiler user, and functions currently being compiled.
 */
public class MethodTable {

  private class MethodEntry {
    private Class clazz;
    private String methodName;

    private MethodEntry(Class clazz, String methodName) {
      this.clazz = clazz;
      this.methodName = methodName;
    }
  }

  private final List<Class> referenceClasses = Lists.newArrayList();
  private final Map<String, MethodEntry> methods = Maps.newHashMap();
  

  public MethodTable() {
    addDefaults();
  }

  private void addDefaults() {

    // G77 builtins
    addMethod("__builtin_sin__", Math.class, "sin");
    addMethod("__builtin_log__", Math.class, "log");
    addMethod("__builtin_cos__", Math.class, "cos");
    addMethod("__builtin_sqrt__", Math.class, "sqrt");
    addMethod("__builtin_pow__", Math.class, "pow");
    addMethod("pow", Math.class, "pow");

    //addMethod("__builtin_copysign__", Math.class, "copySign");

    addMethod("sqrt", Math.class);
    addMethod("floor", Math.class);

    addReferenceClass(Builtins.class);
  }

  private void addMethod(String methodName, Class<Math> clazz) {
    addMethod(methodName, clazz, methodName);
  }

  public void addMethod(String alias, Class declaringClass, String methodName) {
    methods.put(alias, new MethodEntry(declaringClass, methodName));
  }

  public void addReferenceClass(Class clazz) {
    referenceClasses.add(clazz);
  }
  
  public void addMathLibrary() {
    methods.put("log", new MethodEntry(Math.class, "log"));
    methods.put("exp", new MethodEntry(Math.class, "exp"));
  }

  public Field findGlobal(String name) {
    for (Class clazz : referenceClasses) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &&
            field.getName().equals(name)) {

          return field;

        }
      }
    }
    return null;
  }

}
