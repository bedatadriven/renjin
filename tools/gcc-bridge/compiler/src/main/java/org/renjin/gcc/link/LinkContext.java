package org.renjin.gcc.link;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Defines linkage between symbols and external JVM methods and fields
 */
public class LinkContext {
  
  
  private final Map<String, LinkSymbol> symbolMap = Maps.newHashMap();

  /**
   * Adds all public static methods and fields from {@code clazz} that are not annotated with 
   * {@code @Deprecated}
   */
  public void addClass(Class<?> clazz) {
    for (Field field : clazz.getFields()) {
      if(Modifier.isStatic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
        addField(field);
      }
    }
    for (Method method : clazz.getMethods()) {
      if (Modifier.isStatic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        addMethod(method);
      }
    }
  }

  private void addField(Field field) {
    symbolMap.put(field.getName(), new LinkSymbol(field));
  }
  
  public void addMethod(Method method) {
    symbolMap.put(method.getName(), new LinkSymbol(method));
  }

  
  public LinkSymbol get(String name) {
    return symbolMap.get(name);
  }
  
}
