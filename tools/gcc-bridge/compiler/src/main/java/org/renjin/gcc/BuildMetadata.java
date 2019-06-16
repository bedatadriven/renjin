package org.renjin.gcc;

import org.renjin.gcc.annotations.GlobalVar;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.link.LinkSymbol.SymbolType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Scans the provided list of classes for static methods and fields should be eligible for linking
 * to compiled Gimple code.
 *
 * <ul>
 *   <li>Each static method annotated with {@link GlobalVar} is </li>
 * </ul>
 */
public class BuildMetadata {

  public static void main(String[] args) throws ClassNotFoundException, IOException {

    File outputDir = new File(".");
    if(!outputDir.exists()) {
      outputDir.mkdirs();
    }

    for (String arg : args) {
      Class<?> clazz = Class.forName(arg);
      for (Method method : clazz.getMethods()) {
        if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {

          // skip methods that have been @Deprecated
          if(method.getAnnotation(Deprecated.class) != null) {
            continue;
          }

          // Skip methods that are to be treated as global variables
          if(method.isAnnotationPresent(GlobalVar.class)) {
            LinkSymbol.forMethod(method, SymbolType.GETTER).write(outputDir);
          } else {
            LinkSymbol.forMethod(method, SymbolType.METHOD).write(outputDir);
          }
        }
      }

      for (Field field : clazz.getFields()) {
        if(Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &&
            field.getAnnotation(Deprecated.class) == null) {

          LinkSymbol symbol = LinkSymbol.forField(field);
          symbol.write(outputDir);
        }
      }

    }
  }
}
