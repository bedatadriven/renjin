package org.renjin.gcc.translate;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.jimple.JimpleMethodRef;
import org.renjin.gcc.runtime.Builtins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

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
    addMethod("__isnan", Double.class, "isNaN");
    addMethod("__builtin_sin", Math.class, "sin");
    addMethod("__builtin_log", Math.class, "log");
    addMethod("__builtin_cos", Math.class, "cos");
    addMethod("__builtin_sqrt", Math.class, "sqrt");
    addMethod("__builtin_pow", Math.class, "pow");
    addMethod("__builtin_powi", Builtins.class, "powi");
    addMethod("_gfortran_pow_i4_i4", Builtins.class, "_gfortran_pow_i4_i4");
    


    addMethod("sqrt", Math.class);
    addMethod("floor", Math.class);
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

  public JimpleMethodRef resolve(String functionName) {
    MethodEntry entry = methods.get(functionName);
    if(entry != null) {
      Method method = findMethod(entry.clazz, entry.methodName);
      if(method != null) {
        return new JimpleMethodRef(method);
      }
    }
    for(Class clazz : referenceClasses) {
      Method method = findMethod(clazz, functionName);
      if(method != null) {
        return new JimpleMethodRef(method);
      }
    }

    throw new IllegalArgumentException("No matching method for " + functionName);
  }

  private Method findMethod(Class clazz, String methodName) {
    List<Method> methods = Lists.newArrayList();
    for(Method method : clazz.getMethods()) {
      if(method.getName().equals(methodName) &&
              Modifier.isStatic(method.getModifiers())) {
        methods.add(method);
      }
    }
    if(methods.size() > 1) {
      throw new IllegalArgumentException("Ambiguous method: " + methods.toString());
    } else if(methods.size() == 1) {
      return methods.get(0);
    } else {
      return null;
    }
  }

  public Field findField(GimpleExternal external) {
    for(Class clazz : referenceClasses) {
      for(Field field : clazz.getDeclaredFields()) {
        if(Modifier.isStatic(field.getModifiers()) &&
           Modifier.isPublic(field.getModifiers())) {

          return field;

        }
      }
    }
    throw new IllegalArgumentException("Could not find field: " + external.getName());
  }
}
