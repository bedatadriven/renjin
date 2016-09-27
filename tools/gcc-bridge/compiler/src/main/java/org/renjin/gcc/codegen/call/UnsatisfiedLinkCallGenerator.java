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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.runtime.UnsatisfiedLinkException;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

import static org.renjin.repackaged.asm.Type.getMethodDescriptor;

/**
 * Throws a runtime exception.
 */
public class UnsatisfiedLinkCallGenerator implements CallGenerator, MethodHandleGenerator {

  public static final Type HANDLE_TYPE = Type.getType(MethodHandle.class);
  private String functionName;

  public UnsatisfiedLinkCallGenerator(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    Type exceptionType = Type.getType(UnsatisfiedLinkException.class);
    mv.anew(exceptionType);
    mv.dup();
    mv.aconst(functionName);
    mv.invokeconstructor(exceptionType, Type.getType(String.class));
    mv.athrow();
  }

  @Override
  public JExpr getMethodHandle() {

    // Create a method handle that throws the UnsatisifiedLinkException.

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return HANDLE_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.aconst(functionName);
        mv.invokestatic(UnsatisfiedLinkException.class, "throwingHandle", 
            getMethodDescriptor(HANDLE_TYPE, Type.getType(String.class)));
      }
    };
  }
}
