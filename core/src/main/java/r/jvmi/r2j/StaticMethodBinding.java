package r.jvmi.r2j;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import r.jvmi.binding.JvmMethod;
import r.lang.Symbol;

public class StaticMethodBinding {

  private final Symbol name;
  
  public StaticMethodBinding(Symbol name, Collection<Method> collection) {
    this.name = name;
  }

}