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
package org.renjin.compiler.codegen;


import org.renjin.compiler.*;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.LiveSet;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Textifier;
import org.renjin.repackaged.asm.util.TraceMethodVisitor;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

import static org.renjin.repackaged.asm.Type.getMethodDescriptor;
import static org.renjin.repackaged.asm.Type.getType;

public class ByteCodeEmitter implements Opcodes {

  private static final AtomicLong CLASS_COUNTER = new AtomicLong(1);
  
  private ClassWriter cw;
  private ClassVisitor cv;
  private ControlFlowGraph cfg;
  private String className;

  private final LiveSet liveSet;
  private TypeSolver types;


  public ByteCodeEmitter(ControlFlowGraph cfg, LiveSet liveSet, TypeSolver types) {
    super();
    this.cfg = cfg;
    this.liveSet = liveSet;
    this.types = types;
    this.className = "Body" + CLASS_COUNTER.getAndIncrement();
  }

  public Class<CompiledBody> compile() {
    startClass(CompiledBody.class);
    writeImplementation();
    writeConstructor();
    writeClassEnd();

    return JitClassLoader.defineClass(CompiledBody.class, className.replace('/', '.'), cw.toByteArray());
  }

  public Class<CompiledLoopBody> compileLoopBody() {
    startClass(CompiledLoopBody.class);
    writeLoopImplementation();
    writeConstructor();
    writeClassEnd();

    return JitClassLoader.defineClass(CompiledLoopBody.class, className.replace('/', '.'), cw.toByteArray());
  }
  
  private void startClass(Class<?> interfaceClass) {

    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = cw;
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
            Type.getInternalName(Object.class), new String[] { Type.getInternalName(interfaceClass) });
  }


  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 1);
    mv.visitEnd();
  }


  private void writeImplementation() {
    int argumentSize = 3; // this + context + environment

    LocalVarAllocator allocator = new LocalVarAllocator(argumentSize);
    EmitContext emitContext = new EmitContext(cfg, liveSet, types, allocator, SexpTypes.SEXP_TYPE);
    
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "evaluate", 
        getMethodDescriptor(Type.getType(SEXP.class), getType(Context.class), getType(Environment.class)), 
        null, null);

    Textifier p = new Textifier();
    mv = new TraceMethodVisitor(mv, p);
    
    mv.visitCode();
    writeBody(emitContext, mv, cfg);
    mv.visitEnd();
    
    try (PrintWriter pw = new PrintWriter(System.out)) {
      p.print(pw);
    }
  }

  private void writeLoopImplementation() {
    int argumentSize = 5; // this + context + environment + sequence + iteration
    LocalVarAllocator localVarAllocator = new LocalVarAllocator(argumentSize);
    EmitContext emitContext = new EmitContext(cfg, liveSet, types, localVarAllocator, SexpTypes.SEXP_TYPE);
    emitContext.setLoopVectorIndex(3);
    emitContext.setLoopIterationIndex(4);

    MethodNode methodNode = new MethodNode(ACC_PUBLIC, "run",
        getMethodDescriptor(Type.getType(SEXP.class), getType(Context.class), getType(Environment.class),
        getType(SEXP.class), Type.INT_TYPE),
        null, null);

    MethodVisitor mv = methodNode;

//    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "run",
//        getMethodDescriptor(Type.getType(SEXP.class), getType(Context.class), getType(Environment.class),
//        getType(SEXP.class), Type.INT_TYPE),
//        null, null);


    Textifier p = new Textifier();
    mv = new TraceMethodVisitor(mv, p);

    mv.visitCode();
    writeBody(emitContext, mv, cfg);
    mv.visitEnd();

    PrintWriter pw = new PrintWriter(System.out);
    p.print(pw);
    pw.flush();

    methodNode.accept(cv);
  }

  public static void writeBody(EmitContext emitContext, MethodVisitor mv, ControlFlowGraph cfg) {
    InstructionAdapter instructionAdapter = new InstructionAdapter(mv);

    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock.isLive() && basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }

        for(Statement stmt : basicBlock.getStatements()) {
          try {
            stmt.emit(emitContext, instructionAdapter);
          } catch (NotCompilableException e) {
            throw e;
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }
    emitContext.writeDone(instructionAdapter);

    mv.visitMaxs(0, emitContext.getLocalVariableCount());
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }

}
