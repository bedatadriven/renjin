package org.renjin.compiler.ir.tac;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassFileWriter {

  ClassWriter cw = new ClassWriter(0);
  private IRBody block;

  
  public ClassFileWriter(IRBody block, String className) {
    this.block = block;
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, 
        "java/lang/Object", new String[] { "org/renjin/compiler/CompiledBlock" });

  }

  
  

  public byte[] toByteCode (String className) throws Exception {

    FieldVisitor fv;
    MethodVisitor mv;
    AnnotationVisitor av0;

    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }

    {
      mv = cw.visitMethod(ACC_PUBLIC, "eval", "(Lorg/renjin/eval/Context;Lorg/renjin/eval/Environment;)Lorg/renjin/eval/SEXP;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ALOAD, 2);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitTypeInsn(NEW, "org/renjin/eval/DoubleVector");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_1);
      mv.visitIntInsn(NEWARRAY, T_DOUBLE);
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitLdcInsn(new Double("42.0"));
      mv.visitInsn(DASTORE);
      mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/eval/DoubleVector", "<init>", "([D)V");
      mv.visitTypeInsn(NEW, "org/renjin/eval/DoubleVector");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_1);
      mv.visitIntInsn(NEWARRAY, T_DOUBLE);
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitInsn(DCONST_1);
      mv.visitInsn(DASTORE);
      mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/eval/DoubleVector", "<init>", "([D)V");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitMethodInsn(INVOKESTATIC, "r/base/primitives/R$primitive$_$43$_", "doApply", "(Lorg/renjin/eval/Context;Lorg/renjin/eval/Environment;Lorg/renjin/eval/SEXP;Lorg/renjin/eval/SEXP;)Lorg/renjin/eval/SEXP;");
      mv.visitInsn(ARETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitMaxs(10, 3);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
}
