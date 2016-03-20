package org.renjin.gcc.link;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Mapping from symbols to external JVM classes
 */
public class LinkSymbol {


  public enum SymbolType {
    FIELD,
    METHOD
  }

  private SymbolType symbolType;
  private Type declaringClass;
  private Type type;
  private String name;
  private List<Type> arguments;


  public LinkSymbol(Method method) {
    this.symbolType = SymbolType.METHOD;
    this.type = Type.getType(method.getReturnType());
    this.name = method.getName();
    this.arguments = Lists.newArrayList();
    for (Class<?> paramClass : method.getParameterTypes()) {
      arguments.add(Type.getType(paramClass));
    }
  }
  
  public LinkSymbol(Field field) {
    this.symbolType = SymbolType.FIELD;
    this.type = Type.getType(field.getType());
    this.declaringClass = Type.getType(field.getDeclaringClass());
    this.name = field.getName();
  }

  public SymbolType getSymbolType() {
    return symbolType;
  }

  public Type getDeclaringClass() {
    return declaringClass;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public List<Type> getArguments() {
    if(arguments == null) {
      throw new UnsupportedOperationException("fields do not have arguments");
    }
    return arguments;
  }
}

