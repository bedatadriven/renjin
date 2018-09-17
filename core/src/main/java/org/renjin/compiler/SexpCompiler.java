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
package org.renjin.compiler;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DeadCodeElimination;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.codegen.ClassGenerator;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.InlineEmitContext;
import org.renjin.compiler.codegen.LoopBodyEmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.VariableMap;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.HomogeneousList;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Compiles SEXPs to JVM bytecode using runtime type information.
 */
public class SexpCompiler {

  private static final boolean DEBUG = false;

  private final RuntimeState runtimeState;
  private final IRBody body;

  private final ControlFlowGraph cfg;
  private final UseDefMap useDefMap;

  @VisibleForTesting
  final TypeSolver types;

  private final SsaTransformer ssaTransformer;

  public SexpCompiler(RuntimeState runtimeState, IRBody body, boolean environmentVisible) {
    this.runtimeState = runtimeState;
    this.body = body;

    cfg = new ControlFlowGraph(body);

    ssaTransformer = new SsaTransformer(cfg);
    if(environmentVisible) {
      ssaTransformer.insertEnvironmentUpdates();
    }
    ssaTransformer.transform();

    useDefMap = new UseDefMap(cfg);

    types = new TypeSolver(cfg, useDefMap);
  }


  public static CachedApplyCall compileApplyCall(Context context,
                                                 Environment rho,
                                                 Vector vector,
                                                 Function function,
                                                 boolean simplify) throws InstantiationException, IllegalAccessException {

    RuntimeState runtimeState = new RuntimeState(context, rho);
    ValueBounds vectorBounds;
    if(vector instanceof AtomicVector) {
      vectorBounds = ValueBounds.builder()
          .setTypeSet(TypeSet.of(vector))
          .addFlags(sequenceFlags(vector))
          .build();
    } else if(vector instanceof ListVector) {
      vectorBounds = listBounds((ListVector) vector);
    } else {
      throw new IllegalArgumentException();
    }

    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.buildApplyCall(function, vectorBounds, simplify);

    SexpCompiler compiler = new SexpCompiler(runtimeState, body, false);

    return new CachedApplyCall(vectorBounds, runtimeState.getAssumptions(), compiler.compileApplyCall());
  }

  private static ValueBounds listBounds(ListVector vector) {
    ValueBounds.Builder builder = ValueBounds.builder();
    builder.setTypeSet(0);
    for (SEXP sexp : vector) {
      builder.addTypes(sexp.getTypeSet());
      AttributeMap attributes = sexp.getAttributes();
      if(!attributes.isEmpty()) {
        builder.setAttributes(attributes);
      }
    }

    return ValueBounds.builder()
        .setShape(new HomogeneousList(builder.build()))
        .addFlags(ValueBounds.MAYBE_ATTRIBUTES)
        .setTypeSet(TypeSet.LIST)
        .build();
  }

  public static CachedLoopBody compileForLoop(Context context, Environment rho, FunctionCall call, SEXP sequence) throws InstantiationException, IllegalAccessException {

    RuntimeState runtimeState = new RuntimeState(context, rho);
    ValueBounds sequenceBounds = ValueBounds.builder()
        .setTypeSet(TypeSet.of(sequence))
        .addFlags(sequenceFlags(sequence))
        .build();

    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.buildLoopBody(call, sequenceBounds);

    SexpCompiler compiler = new SexpCompiler(runtimeState, body, true);
    CompiledLoopBody compiledLoopBody = compiler.compileForLoopBody();

    return new CachedLoopBody(compiledLoopBody, sequenceBounds, runtimeState.getAssumptions());
  }

  private static int sequenceFlags(SEXP sequence) {
    int flags = 0;
    if(sequence instanceof IntSequence) {
      IntSequence intSequence = (IntSequence) sequence;
      flags |= ValueBounds.FLAG_NO_NA;
      if(intSequence.isPositive()) {
        flags |= ValueBounds.FLAG_POSITIVE;
      }
    }
    return flags;
  }

  public static CachedBody compileSexp(Context context, Environment rho, SEXP expression) throws InstantiationException, IllegalAccessException {
    RuntimeState runtimeState = new RuntimeState(context, rho);
    IRBody body = new IRBodyBuilder(runtimeState).build(expression);

    SexpCompiler compiler = new SexpCompiler(runtimeState, body, true);
    CompiledBody compiledBody = compiler.compileBody();

    return new CachedBody(compiledBody, runtimeState.getAssumptions());
  }

  private void compileForBody() {

    types.execute();
    if(DEBUG) {
      cfg.dumpGraph();
      types.dumpBounds();
    }
    types.verifyFunctionAssumptions(runtimeState);
    lowerSSA();
  }

  private void lowerSSA() {

    DeadCodeElimination dce = new DeadCodeElimination(cfg, useDefMap);
    dce.run();

    ssaTransformer.removePhiFunctions(types);
  }

  private CompiledLoopBody compileForLoopBody() throws IllegalAccessException, InstantiationException {

    if(DEBUG) {
      System.out.println(cfg);
    }

    compileForBody();


    LocalVarAllocator localVars = new LocalVarAllocator(CompiledLoopBody.PARAM_SIZE);
    VariableMap variableMap = new VariableMap(cfg, localVars, types, useDefMap);
    LoopBodyEmitContext emitContext = new LoopBodyEmitContext(localVars, variableMap);

    ClassGenerator<CompiledLoopBody> classGenerator = new ClassGenerator<>(CompiledLoopBody.class);
    classGenerator.addLoopBodyMethod(mv -> {
      mv.visitCode();

      emitBody(emitContext, mv);

      mv.visitMaxs(0, localVars.getCount());
      mv.visitEnd();
    });

    return classGenerator.finishAndLoad().newInstance();
  }

  private CompiledBody compileBody() throws IllegalAccessException, InstantiationException {
    compileForBody();

    LocalVarAllocator localVars = new LocalVarAllocator(CompiledBody.PARAM_SIZE);
    VariableMap variableMap = new VariableMap(cfg, localVars, types, useDefMap);
    LoopBodyEmitContext emitContext = new LoopBodyEmitContext(localVars, variableMap);

    ClassGenerator<CompiledBody> classGenerator = new ClassGenerator<>(CompiledBody.class);
    classGenerator.addBodyMethod(mv -> {
      mv.visitCode();
      emitBody(emitContext, mv);
      mv.visitMaxs(0, localVars.getCount());
      mv.visitEnd();
    });

    return classGenerator.finishAndLoad().newInstance();
  }

  private CompiledApplyCall compileApplyCall() throws IllegalAccessException, InstantiationException {

    compileForBody();

    LocalVarAllocator localVars = new LocalVarAllocator(CompiledApplyCall.PARAM_SIZE);
    VariableMap variableMap = new VariableMap(cfg, localVars, types, useDefMap);
    LoopBodyEmitContext emitContext = new LoopBodyEmitContext(localVars, variableMap);

    ClassGenerator<CompiledApplyCall> classGenerator = new ClassGenerator<>(CompiledApplyCall.class);
    classGenerator.addMethod("apply",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/Vector;)Lorg/renjin/sexp/Vector;", mv -> {
          mv.visitCode();
          emitBody(emitContext, mv);
          mv.visitMaxs(0, localVars.getCount());
          mv.visitEnd();
        });

    return classGenerator.finishAndLoad().newInstance();
  }

  public void compileInline(EmitContext emitContext,
                            InstructionAdapter mv,
                            List<CompiledSexp> parameters,
                            VariableStrategy returnVariable) {

    // Last check
    types.verifyFunctionAssumptions(runtimeState);
//    types.dumpBounds();

    lowerSSA();

    // Now map our variables to storage strategies
    VariableMap variableMap = new VariableMap(cfg, emitContext.getLocalVarAllocator(), types, useDefMap);

    InlineEmitContext inlineContext = new InlineEmitContext(emitContext, parameters, variableMap, returnVariable);

    emitBody(inlineContext, mv);

  }

  private void emitBody(EmitContext emitContext, InstructionAdapter mv) {

    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock.isLive() && basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getBytecodeLabel(label));
        }

        for(Statement stmt : basicBlock.getStatements()) {
          try {
            stmt.emit(emitContext, mv);
          } catch (NotCompilableException e) {
            throw e;
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }
    emitContext.writeDone(mv);
  }


  public ControlFlowGraph getControlFlowGraph() {
    return cfg;
  }

  public void updateTypes() {
    types.execute();
  }

  public boolean isPure() {
    return types.isPure();
  }

}
