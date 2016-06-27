package org.renjin.compiler.emit;


import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;

import java.io.PrintWriter;

public class ByteCodeEmitter implements Opcodes {

  private ClassWriter cw;
  private ClassVisitor cv;
  private ControlFlowGraph cfg;
  private String className;

  private TypeSolver types;


  public ByteCodeEmitter(ControlFlowGraph cfg, TypeSolver types) {
    super();
    this.cfg = cfg;
    this.types = types;
    this.className = "Body" + System.identityHashCode(cfg);
  }

  public Class<CompiledBody> compile() {
    startClass();
    writeImplementation();
    writeConstructor();
    writeClassEnd();

    return new MyClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
  }


  private void startClass() {

    cw = new ClassWriter(0);
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
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "evaluate", 
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Context.class), Type.getType(Environment.class)), 
        null, null);
    mv.visitCode();
    writeBody(mv);
    mv.visitEnd();
  }

  private void writeBody(MethodVisitor mv) {

    int argumentSize = 3; // this + context + environment
    VariableSlots variableSlots = new VariableSlots(argumentSize, types);
    System.out.println(variableSlots);
    
    EmitContext emitContext = new EmitContext(cfg, variableSlots);

    InstructionAdapter instructionAdapter = new InstructionAdapter(mv);

    int maxStackSize = 0;
    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(emitContext.getAsmLabel(label));
        }

        for(Statement stmt : basicBlock.getStatements()) {
          try {
            int stackHeight = stmt.emit(emitContext, instructionAdapter);
            if (stackHeight > maxStackSize) {
              maxStackSize = stackHeight;
            }
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }

    mv.visitMaxs(maxStackSize, variableSlots.getNumLocals() + 2);
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
