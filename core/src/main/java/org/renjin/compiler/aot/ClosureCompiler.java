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
package org.renjin.compiler.aot;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Closure;
import org.renjin.sexp.FunctionEnvironment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

/**
 * Compiles a closure to a java method
 */
public class ClosureCompiler {

  private final AotHandle handle;

  public ClosureCompiler(Context context, Closure closure) {
    this(new AotBuffer("org.renjin"), context, Symbol.get("fn"), closure);
  }

  public ClosureCompiler(AotBuffer buffer, Context context, Symbol name, Closure closure) {
    RuntimeState runtimeState = new RuntimeState(context, closure.getEnclosingEnvironment(), rho -> false);
    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.build(new ClosureTranslationContext(), closure.getBody(), false);
    System.out.println(body);

    for (Statement statement : body.getStatements()) {
      statement.getRHS().updateTypeBounds(e -> ValueBounds.UNBOUNDED);
    }

    ClassBuffer classBuffer = buffer.classBuffer(body.getSourceFile());
    ClosureEmitContext emitContext = new ClosureEmitContext(classBuffer, closure.getFormals());

    String methodDescriptor = Type.getMethodDescriptor(Type.getType(SEXP.class),
        Type.getType(Context.class),
        Type.getType(FunctionEnvironment.class));


    handle = buffer.newFunction(body.getSourceFile(), name.getPrintName(), methodDescriptor, mv -> {

      for (int i = 0; i < body.getStatements().size(); i++) {

        Label label = null;
        for (IRLabel irLabel : body.getInstructionLabels(i)) {
          label = emitContext.getBytecodeLabel(irLabel);
          mv.visitLabel(label);
        }
        int lineNumber = body.getLineNumber(i);
        if(lineNumber != -1) {
          if(label == null) {
            label = new Label();
          }
          mv.visitLineNumber(lineNumber, label);
        }

        body.getStatements().get(i).emit(emitContext, mv);

      }
      mv.visitMaxs(0, emitContext.getLocalVarAllocator().getCount());
      mv.visitEnd();

      return emitContext.getFrameVariableNames();
    });
  }


  public AotHandle getHandle() {
    return handle;
  }
}
