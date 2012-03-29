package org.renjin.compiler.ir.tac;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_DOUBLE;
import static org.objectweb.asm.Opcodes.V1_6;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.ir.tac.IRBody;

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
