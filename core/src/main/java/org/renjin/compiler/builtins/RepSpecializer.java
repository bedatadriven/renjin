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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.primitives.sequence.RepFunction;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Specializes calls to rep()
 */
public class RepSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    if(arguments.isEmpty()) {
      return new ConstantCall(Null.INSTANCE);
    }

    final MatchedArgumentPositions match = RepFunction.ARGUMENT_MATCHER.match(arguments);
    int x = match.getActualIndex(RepFunction.FORMAL_X);
    int lengthOut = match.getActualIndex(RepFunction.FORMAL_LENGTH_OUT);
    int each = match.getActualIndex(RepFunction.FORMAL_EACH);
    int times = match.getActualIndex(RepFunction.FORMAL_TIMES);

    if(x == -1) {
      throw new InvalidSyntaxException("Missing argument 'x' to rep()");
    }

    ValueBounds input = arguments.get(x).getBounds();
    ValueBounds result = ValueBounds.builder()
        .setTypeSet(input.getTypeSet())
        .addFlagsFrom(input, ValueBounds.FLAG_POSITIVE | ValueBounds.MAYBE_NAMES | ValueBounds.FLAG_NO_NA)
        .build();


    return new Specialization() {
      @Override
      public ValueBounds getResultBounds() {
        return result;
      }

      @Override
      public boolean isPure() {
        return true;
      }

      @Override
      public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {

        return new SexpExpr() {
          @Override
          public void loadSexp(EmitContext context, InstructionAdapter mv) {

            // Invoke:
            // Vector rep(Vector x, Vector times, int lengthOut, int each)

            arguments.get(x).getExpression().getCompiledExpr(context).loadSexp(context, mv);
            mv.checkcast(Type.getType(Vector.class));

            if(times == -1) {
              ConstantBytecode.pushConstant(mv, RepFunction.DEFAULT_TIMES_ARGUMENT);
            } else {
              arguments.get(times).getExpression().getCompiledExpr(context).loadSexp(context, mv);
              mv.checkcast(Type.getType(Vector.class));
            }

            if(lengthOut == -1) {
              mv.visitLdcInsn(RepFunction.DEFAULT_LENGTH_OUT);
            } else {
              arguments.get(lengthOut).getExpression().getCompiledExpr(context).loadScalar(context, mv, VectorType.INT);
            }

            if(each == -1) {
              mv.visitLdcInsn(RepFunction.DEFAULT_EACH);
            } else {
              arguments.get(each).getExpression().getCompiledExpr(context).loadScalar(context, mv, VectorType.INT);
            }

            mv.invokestatic(Type.getInternalName(RepFunction.class), "rep",
                Type.getMethodDescriptor(Type.getType(Vector.class),
                    Type.getType(Vector.class), Type.getType(Vector.class), Type.INT_TYPE, Type.INT_TYPE), false);

          }
        };
      }
    };
  }
}
