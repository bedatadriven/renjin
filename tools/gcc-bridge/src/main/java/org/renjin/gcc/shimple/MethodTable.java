package org.renjin.gcc.shimple;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleCall;
import org.renjin.gcc.gimple.expr.GimpleExternal;

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
    addMethod("sqrt", Math.class);
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

  public Method resolve(GimpleCall call) {
    MethodEntry entry = methods.get(call.getFunction());
    if(entry != null) {
      Method method = findMethod(call, entry.clazz, entry.methodName);
      if(method != null) {
        return method;
      }
    }
    for(Class clazz : referenceClasses) {
      Method method = findMethod(call, clazz, call.getFunction());
      if(method != null) {
        return method;
      }
    }

    throw new IllegalArgumentException("No matching method for " + call);
  }

  private Method findMethod(GimpleCall call, Class clazz, String methodName) {
    List<Method> methods = Lists.newArrayList();
    for(Method method : clazz.getMethods()) {
      if(method.getName().equals(methodName) && method.getParameterTypes().length == call.getArgumentCount() ) {
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
