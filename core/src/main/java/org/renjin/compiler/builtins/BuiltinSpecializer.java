/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.codegen.OverloadComparator;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;

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
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    JvmMethod method = selectOverload(argumentTypes);
    if(method == null) {
      return UnspecializedCall.INSTANCE;
    }
    
    if(method.isGeneric()) {
      ValueBounds object = argumentTypes.get(0);
      Specialization genericMethod = maybeSpecializeToGenericCall(object);
      if(genericMethod != null) {
        return genericMethod;
      }
    }
    
    if(method.isDataParallel()) {
      return new DataParallelCall(primitive, method, argumentTypes).specializeFurther();
    } else {
      return new StaticMethodCall(method).furtherSpecialize(argumentTypes);
    }
  }

  private Specialization maybeSpecializeToGenericCall(ValueBounds object) {
    if(!object.isClassAttributeConstant()) {
      return GenericPrimitive.INSTANCE;
    }
    AtomicVector classVector = object.getConstantClassAttribute();
    if(classVector == Null.INSTANCE) {
      return null;
    }
    // TODO: see UseMethodCall
    return GenericPrimitive.INSTANCE;
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
