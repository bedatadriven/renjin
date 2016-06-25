package org.renjin.compiler.emit;


import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.RegisterAllocation;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.io.PrintWriter;

public class ByteCodeEmitter implements Opcodes {

  private ClassWriter cw;
  private ClassVisitor cv;
  private ControlFlowGraph cfg;
  private String className;

  private static final int DO_NOT_COMPUTE_FRAMES = 0;


  public ByteCodeEmitter(ControlFlowGraph cfg) {
    super();
    this.className = "Body" + System.identityHashCode(cfg);
    this.cfg = cfg;

  }

  public Class<CompiledBody> compile() {
    startClass();
    writeImplementation();
    writeConstructor();
    writeClassEnd();

    return new MyClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
  }


  private void startClass() {

    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out, true));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
            Type.getInternalName(Object.class), new String[] { Type.getInternalName(CompiledBody.class) });
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
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "evaluate", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    writeBody(mv);
    mv.visitEnd();
  }

  private void writeBody(MethodVisitor mv) {

    int nextSlot = 3; // this + context + environment
    RegisterAllocation registerAllocation = new RegisterAllocation(cfg, nextSlot);

    int numLocals = 3 + registerAllocation.getSize();

    System.out.println(registerAllocation);

    EmitContext emitContext = new EmitContext(cfg, registerAllocation);

    int maxStackSize = 0;
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      if(bb != cfg.getEntry() && bb != cfg.getExit()) {
        for(IRLabel label : bb.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }

        for(Statement stmt : bb.getStatements()) {
          int stackHeight = stmt.emit(emitContext, mv);
          if(stackHeight > maxStackSize) {
            maxStackSize = stackHeight;
          }
        }
      }
    }

    mv.visitMaxs(maxStackSize, numLocals);
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }

  private class MyClassLoader extends ClassLoader {
    Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}
