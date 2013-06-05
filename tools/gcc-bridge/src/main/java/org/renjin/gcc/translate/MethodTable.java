package org.renjin.gcc.translate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.translate.call.JvmMethodRef;
import org.renjin.gcc.translate.call.MethodRef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

  public MethodRef resolve(String functionName) {
    MethodEntry entry = methods.get(functionName);
    if (entry != null) {
      Method method = findMethod(entry.clazz, entry.methodName);
      if (method != null) {
        return new JvmMethodRef(method);
      }
    }
    for (Class clazz : referenceClasses) {
      Method method = findMethod(clazz, functionName);
      if (method != null) {
        return new JvmMethodRef(method);
      }
    }

    throw new IllegalArgumentException("No matching method for " + functionName);
  }

  private Method findMethod(Class clazz, String methodName) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName) && Modifier.isStatic(method.getModifiers())) {
        methods.add(method);
      }
    }
    if (methods.size() > 1) {
      throw new IllegalArgumentException("Ambiguous method: " + methods.toString());
    } else if (methods.size() == 1) {
      return methods.get(0);
    } else {
      return null;
    }
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
