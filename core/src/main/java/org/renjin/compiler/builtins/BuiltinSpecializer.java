package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.invoke.codegen.OverloadComparator;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;

import java.util.Collections;
import java.util.List;

/**
 * Generic builtin specializer that uses annotations to specialize method calls
 */
public class BuiltinSpecializer implements Specializer {

  private final Primitives.Entry primitive;
  private final List<JvmMethod> methods;

  public BuiltinSpecializer(Primitives.Entry primitive) {
    this.primitive = primitive;
    this.methods = JvmMethod.findOverloads(
        this.primitive.functionClass, 
        this.primitive.name, 
        this.primitive.methodName);

    Collections.sort( methods, new OverloadComparator());
  }
  
  @Override
  public Specialization trySpecialize(List<ValueBounds> argumentTypes) {
    JvmMethod method = selectOverload(argumentTypes);
    if(method == null) {
      return UnspecializedCall.INSTANCE;
    }
    
    if(method.isDataParallel()) {
      return new DataParallelCall(primitive, method, argumentTypes).specializeFurther();
    } else {
      return new StaticMethodCall(method).furtherSpecialize(argumentTypes);
    }
  }

  private JvmMethod selectOverload(List<ValueBounds> argumentTypes) {
    for (JvmMethod method : methods) {
      if(matches(method, argumentTypes)) {
        return method;
      }
    }
    return null;
  }

  private boolean matches(JvmMethod method, List<ValueBounds> argumentTypes) {
    if(!arityMatches(method, argumentTypes)) {
      return false;
    }
    for (int i = 0; i < method.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = method.getPositionalFormals().get(i);
      ValueBounds actualType = argumentTypes.get(i);

      if(!TypeSet.matches(formal.getClazz(), actualType.getTypeSet())) {
        return false;
      }
    }
    return true;
  }

  private boolean arityMatches(JvmMethod method, List<ValueBounds> argumentTypes) {
    int numPosArgs = method.getPositionalFormals().size();
    return (argumentTypes.size() == numPosArgs) ||
        (method.acceptsArgumentList() && (argumentTypes.size() >= numPosArgs));
  }

}
