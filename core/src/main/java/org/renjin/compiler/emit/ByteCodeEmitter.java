package org.renjin.compiler.emit;


import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.io.PrintWriter;

public class ByteCodeEmitter implements Opcodes {

  private ClassWriter cw;
  private ClassVisitor cv;
  private ControlFlowGraph cfg;
  private String className;
  private VariableMap variableMap;

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
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    //cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null,
            "java/lang/Object", new String[] { "org/renjin/compiler/CompiledBody" });
  }


  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(8, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }


  private void writeImplementation() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "eval", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    writeBody(mv);
//    mv.visitInsn(ACONST_NULL);
//    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeBody(MethodVisitor mv) {

    EmitContext emitContext = new EmitContext(variableMap);

    for(BasicBlock bb : cfg.getBasicBlocks()) {
      
      for(IRLabel label : bb.getLabels()) {
        mv.visitLabel(emitContext.getAsmLabel(label));
      }

      for(Statement stmt : bb.getStatements()) {
        stmt.emit(emitContext, mv);
      }
    }
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }

  class MyClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

}
