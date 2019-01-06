/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.annotations.VarArgs;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.VariadicStrategy;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;

/**
 * Implements handling of variadic arguments in a way that mirrors the GCC's model for
 * variadic arguments.
 *
 * <p>In GCC world, all arguments to a function are simply pushed onto the stack. When control is transferred
 * to the callee, then GCC provides just a bit of support by helping the callee find a pointer to the position
 * within the static of the first extra argument.</p>
 *
 * <p>Of course, in JVM world you cannot just push extra arguments willy-nilly onto the stack. So instead, the
 * caller to a variadic function is responsible for allocating memory on the heap into which the extra arguments can
 * be copied. A pointer to this memory is passed as a single parameter to the callee, which can then provide this
 * pointer when __builtin_va_start() is called.</p>
 */
public class VPtrVariadicStrategy implements VariadicStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(Ptr.class));
  }

  @Override
  public List<JExpr> marshallVarArgs(MethodGenerator mv, ExprFactory exprFactory, List<GimpleExpr> extraArgs) {

    // Find the size, in bytes, of the remaining arguments
    int varArgsSize = 0;
    for (GimpleExpr argExpr : extraArgs) {
      varArgsSize += argExpr.getType().sizeOf();
    }

    // Allocate a block of memory for these arguments.
    VPtrExpr vptr = new VPtrExpr(mv.getLocalVarAllocator().reserve(Type.getType(Ptr.class)));
    VPtrStrategy vptrStrategy = new VPtrStrategy(new GimpleVoidType());
    vptr.store(mv, vptrStrategy.malloc(mv, Expressions.constantInt(varArgsSize)));

    // Now copy the arguments to the block
    int offset = 0;
    for (GimpleExpr argExpr : extraArgs) {
      GExpr gexpr = exprFactory.findGenerator(argExpr);
      vptr.valueOf(argExpr.getType(), constantInt(offset)).store(mv, gexpr);
      offset += argExpr.getType().sizeOf();
    }
    return Collections.singletonList(vptr.getBaseRef());
  }

  @Override
  public List<AnnotationNode> getParameterAnnotations() {
    AnnotationNode node = new AnnotationNode(Type.getDescriptor(VarArgs.class));
    return Collections.singletonList(node);
  }

  /**
   * True if the {@code i}th argument of the given {@code method} is annotated
   * with the @VarArgs annotation.
   */
  public static boolean isVarArgsPtr(Method method, int index) {
    if(!method.getParameterTypes()[index].equals(Ptr.class)) {
      return false;
    }
    Annotation[] annotations = method.getParameterAnnotations()[index];
    if(annotations != null) {
      for (Annotation annotation : annotations) {
        if(annotation.getClass().equals(VarArgs.class)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasVarArgsPtr(Method method) {
    return method.getParameterTypes().length != 0 &&
            isVarArgsPtr(method, method.getParameterTypes().length - 1);
  }
}
