/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

/**
 * Generic builtin specializer that uses annotations to specialize method calls
 */
public class AnnotationBasedSpecializer implements BuiltinSpecializer {

  private final Primitives.Entry primitive;
  private final String genericGroup;
  private final List<JvmMethod> methods;

  public AnnotationBasedSpecializer(Primitives.Entry primitive) {
    this.primitive = primitive;
    this.methods = JvmMethod.findOverloads(
        this.primitive.functionClass, 
        this.primitive.name, 
        this.primitive.methodName);

    this.genericGroup = findGenericGroup(methods);

    methods.sort(new OverloadComparator());
  }

  public List<JvmMethod> getMethods() {
    return methods;
  }

  @Override
  public String getName() {
    return primitive.name;
  }

  @Override
  public String getGroup() {
    return genericGroup;
  }

  private static String findGenericGroup(List<JvmMethod> methods) {
    for (JvmMethod method : methods) {
      if(method.isGroupGeneric()) {
        return method.getGenericGroup();
      }
    }
    return null;
  }

  public boolean isGeneric() {
    for (JvmMethod method : methods) {
      if (method.isGeneric()) {
        return true;
      }
    }
    return false;
  }

  public static JvmMethod findMethod(String primitiveName) {
    Primitives.Entry entry = Primitives.getBuiltinEntry(primitiveName);
    return Iterables.getOnlyElement(JvmMethod.findOverloads(
        entry.functionClass,
        entry.name,
        entry.methodName));
  }


  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> namedArguments) {
    JvmMethod method = selectOverload(namedArguments);
    if(method == null) {
      return new WrapperApplyCall(primitive, namedArguments);
    }
    
    if(method.isDataParallel()) {
      return new DataParallelCall(primitive, method, namedArguments).specialize();
    } else {
      return new StaticMethodCall(method, namedArguments).furtherSpecialize();
    }
  }

  public JvmMethod selectOverload(List<ArgumentBounds> argumentTypes) {
    for (JvmMethod method : methods) {
      if(matches(method, argumentTypes)) {
        return method;
      }
    }
    return null;
  }

  private boolean matches(JvmMethod method, List<ArgumentBounds> argumentTypes) {
    if(!arityMatches(method, argumentTypes)) {
      return false;
    }
    for (int i = 0; i < method.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = method.getPositionalFormals().get(i);
      ValueBounds actualType = argumentTypes.get(i).getBounds();

      if(!TypeSet.matches(formal.getClazz(), actualType.getTypeSet())) {
        return false;
      }
    }
    return true;
  }

  private boolean arityMatches(JvmMethod method, List<ArgumentBounds> argumentTypes) {
    int numPosArgs = method.getPositionalFormals().size();
    return (argumentTypes.size() == numPosArgs) ||
        (method.acceptsArgumentList() && (argumentTypes.size() >= numPosArgs));
  }

}
