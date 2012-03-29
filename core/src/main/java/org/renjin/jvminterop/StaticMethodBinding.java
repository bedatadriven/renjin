package org.renjin.jvminterop;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;


public class StaticMethodBinding {

  private final Symbol name;
  
  public StaticMethodBinding(Symbol name, Collection<Method> collection) {
    this.name = name;
  }

}