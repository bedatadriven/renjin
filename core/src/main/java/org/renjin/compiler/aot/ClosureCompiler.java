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

import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.*;
import org.renjin.compiler.codegen.VariableMapping;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

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
    ClosureTranslationContext translationContext = new ClosureTranslationContext(closure.getBody());
    IRBody body = builder.build(translationContext, closure.getBody(), false);

    ControlFlowGraph cfg = new ControlFlowGraph(body);

    SsaTransformer ssaTransformer = new SsaTransformer(cfg);
    ssaTransformer.transform();


    UseDefMap useDefMap = new UseDefMap(cfg);
    TypeSolver typeSolver = new TypeSolver(cfg, useDefMap);

    for (Symbol symbol : translationContext.getSymbolsUsedAsFunctions()) {
      Function function = closure.getEnclosingEnvironment().findFunction(context, symbol);
      typeSolver.setInitialBounds(symbol, ValueBounds.constantValue(function));
    }

    typeSolver.execute();

    typeSolver.dumpBounds();

    ConstantFolder folder = new ConstantFolder(cfg);
    folder.fold();

    DeadCodeElimination dce = new DeadCodeElimination(cfg, useDefMap, typeSolver);
    dce.run();

    ssaTransformer.removePhiFunctions(typeSolver);

    VariableMapping.lowerEnvironmentVariables(cfg);

    cfg.dumpBody(false);

    ClassBuffer classBuffer = buffer.classBuffer(body.getSourceFile());
    ClosureEmitContext emitContext = new ClosureEmitContext(classBuffer, closure.getFormals());

    String methodDescriptor = Type.getMethodDescriptor(Type.getType(SEXP.class),
        Type.getType(Context.class),
        Type.getType(FunctionEnvironment.class));

    handle = buffer.newFunction(body.getSourceFile(), name.getPrintName(), methodDescriptor, mv -> {

      for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
        if(basicBlock.isLive()) {
          Label label = null;
          for (IRLabel irLabel : basicBlock.getLabels()) {
            label = emitContext.getBytecodeLabel(irLabel);
            mv.visitLabel(label);
          }

          for (Statement statement : basicBlock.getStatements()) {
            statement.emit(emitContext, mv);
          }
        }
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
