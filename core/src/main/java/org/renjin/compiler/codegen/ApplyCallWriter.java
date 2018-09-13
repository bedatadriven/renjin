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

import org.renjin.compiler.CompiledLoopBody;
import org.renjin.compiler.JitClassLoader;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.util.TraceClassVisitor;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import java.io.PrintWriter;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Generates a MemoizedComputation implementation for
 * a deferred apply() call.
 */
public class ApplyCallWriter {

  private static final Type ATTRIBUTE_MAP_TYPE = Type.getType(AttributeMap.class);
  private static final Type VECTOR_TYPE = Type.getType(Vector.class);

  private ClassWriter cw;
  private ClassVisitor cv;

  private Type thisClass;
  private Type superClass;
  private InlinedFunction function;
  private Symbol elementFormalName;
  private ValueBounds elementBounds;
  private ValueBounds resultBounds;
  private final int uniqueId;

  public ApplyCallWriter(InlinedFunction function, Symbol elementFormalName, ValueBounds elementBounds, ValueBounds resultBounds) {
    this.function = function;
    this.elementFormalName = elementFormalName;
    this.elementBounds = elementBounds;
    this.resultBounds = resultBounds;

    uniqueId = System.identityHashCode(this);
    thisClass = Type.getType("org/renjin/DeferredApply" + uniqueId);
    superClass =  Type.getType(DoubleVector.class);
  }


  public Class<?> build() {

    startClass();
    writeVectorField();
    writeConstructor();

    writeApplyImpl();
    writeGetElementImpl();
    writeLengthImpl();
    writeIsConstantAccessTimeImpl();
    writeGetOperandsImpl();
    writeGetComputationNameImpl();

    writeClassEnd();

    return JitClassLoader.defineClass(CompiledLoopBody.class, thisClass.getInternalName().replace('/', '.'), cw.toByteArray());
  }


  private void startClass() {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, thisClass.getInternalName(), null,
        superClass.getInternalName(), new String[] { Type.getInternalName(MemoizedComputation.class) });
  }


  /**
   * Write a field to store the vector over which we are applying the function
   */
  private void writeVectorField() {
    cv.visitField(ACC_PRIVATE, "vector", Type.getDescriptor(Vector.class), null, null);
  }

  /**
   * Write a constructor which takes the input vector and an AttributeMap
   */
  private void writeConstructor() {

    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>",
        Type.getMethodDescriptor(Type.VOID_TYPE, VECTOR_TYPE, ATTRIBUTE_MAP_TYPE), null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0); // this
    mv.visitVarInsn(ALOAD, 2); // Attributes
    mv.visitMethodInsn(INVOKESPECIAL, superClass.getInternalName(), "<init>",
        Type.getMethodDescriptor(Type.VOID_TYPE, ATTRIBUTE_MAP_TYPE), false);

    // this.vector = vector
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1); // vector
    mv.visitFieldInsn(PUTFIELD, thisClass.getInternalName(), "vector", VECTOR_TYPE.getDescriptor());

    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 3);
    mv.visitEnd();
  }

  private void writeGetOperandsImpl() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getOperands", "()[Lorg/renjin/sexp/Vector;", null, null);
    mv.visitCode();
    mv.visitInsn(ICONST_1);
    mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(Vector.class));
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, thisClass.getInternalName(), "vector", VECTOR_TYPE.getDescriptor());
    mv.visitInsn(AASTORE);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(3, 1);
    mv.visitEnd();
  }


  private void writeLengthImpl() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "length", "()I", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, thisClass.getInternalName(), "vector", VECTOR_TYPE.getDescriptor());
    mv.visitMethodInsn(INVOKEINTERFACE, "org/renjin/sexp/SEXP", "length", "()I", true);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(3, 1);
    mv.visitEnd();
  }

  private void writeApplyImpl() {
    throw new UnsupportedOperationException("TODO");
//    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "apply", applySignature(), null, null);
//    mv.visitCode();
//
//    VariableSlots slots = new VariableSlots(elementBounds.storageType().getSize(), function.getTypes());
//    ApplyMethodContext emitContext = new ApplyMethodContext(function.getCfg(), elementFormalName,  elementBounds.storageType(), slots);
//
//    function.write(emitContext, new InstructionAdapter(mv));
//
//    mv.visitMaxs(3, 1);
//    mv.visitEnd();
  }

  private String applySignature() {
//    return Type.getMethodDescriptor(resultBounds.storageType(), elementBounds.storageType());
    throw new UnsupportedOperationException("TODO");
  }

  private void writeGetElementImpl() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getElementAsDouble", "(I)D", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);  // this
    mv.visitFieldInsn(GETFIELD, thisClass.getInternalName(), "vector", VECTOR_TYPE.getDescriptor());
    mv.visitVarInsn(ILOAD, 1); // index
    mv.visitMethodInsn(INVOKEINTERFACE, VECTOR_TYPE.getInternalName(), "getElementAsInt", "(I)I", true);
    mv.visitMethodInsn(INVOKESTATIC, thisClass.getInternalName(), "apply", applySignature(), false);
    mv.visitInsn(Opcodes.DRETURN);
    mv.visitMaxs(4, 2);
  }

  private void writeIsConstantAccessTimeImpl() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "isConstantAccessTime", "()Z", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);  // this
    mv.visitFieldInsn(GETFIELD, thisClass.getInternalName(), "vector", VECTOR_TYPE.getDescriptor());
    mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(DeferredComputation.class), "isConstantAccessTime", "()Z", true);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(3, 1);
    mv.visitEnd();
  }

  private void writeGetComputationNameImpl() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getComputationName", "()Ljava/lang/String;", null, null);
    mv.visitCode();
    mv.visitLdcInsn("apply" + uniqueId);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }


}
