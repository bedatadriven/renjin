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
package org.renjin.gnur;

import org.renjin.eval.Context;
import org.renjin.gcc.codegen.CodeGenerationContext;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.primitives.Native;
import org.renjin.repackaged.asm.ClassWriter;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class ContextClassWriter {

  public static final String THREAD_LOCAL_FIELD_NAME = "CURRENT";
  public static final String THREAD_LOCAL_DESCRIPTOR = Type.getDescriptor(ThreadLocal.class);
  public static final String ENTER_METHOD_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Object.class));

  private final Type contextClass;
  private ClassWriter cv;

  public ContextClassWriter(Type contextClass) {
    this.contextClass = contextClass;
    cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv.visit(V1_8, ACC_PUBLIC + ACC_SUPER, contextClass.getInternalName(), null,
        Type.getInternalName(Object.class), new String[0]);

    threadLocalHolder();
    staticInitializer();
    enterMethod();
  }

  /**
   * Declares a field of type {@code ThreadLocal<PackageContext>} that will hold the instance of the PackageContext
   * associated with the current thread. This is set by the {@code enter()} method called by the trampoline class.
   */
  private void threadLocalHolder() {
    cv.visitField(ACC_PUBLIC | ACC_STATIC, THREAD_LOCAL_FIELD_NAME, THREAD_LOCAL_DESCRIPTOR, null, null);
  }

  /**
   * Writes a static initializer that initialize our ThreadLocal static variable with a new instance.
   */
  private void staticInitializer() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ThreadLocal.class));
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ThreadLocal.class), "<init>", "()V", false);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, contextClass.getInternalName(), THREAD_LOCAL_FIELD_NAME, THREAD_LOCAL_DESCRIPTOR);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(1,1);
    mv.visitEnd();
  }

  /**
   * Writes a static enter() method that initializes the PackageState from this thread by calling
   * {@code Native.currentContext().getSingleton(org.renjin.cran.mypackage.Context.class)}
   */
  private void enterMethod() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "enter", ENTER_METHOD_DESCRIPTOR, null, null);
    mv.visitCode();

    mv.visitFieldInsn(Opcodes.GETSTATIC, contextClass.getInternalName(), THREAD_LOCAL_FIELD_NAME, THREAD_LOCAL_DESCRIPTOR);

    // Stack: ThreadLocal

    mv.visitInsn(Opcodes.DUP);

    // Stack: ThreadLocal, ThreadLocal

    // Retrieve the current value and store it a local variable

    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadLocal.class), "get",
        Type.getMethodDescriptor(Type.getType(Object.class)), false);

    mv.visitVarInsn(Opcodes.ASTORE, 0);

    // Stack: ThreadLocal

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Native.class), "currentContext",
        Type.getMethodDescriptor(Type.getType(Context.class)), false);

    // Stack: ThreadLocal, org.renjin.eval.Context

    mv.visitLdcInsn(contextClass);

    // Stack: ThreadLocal, org.renjin.eval.Context, org.renjin.cran.mypackage.Context

    // Call context.getSingleton(org.renjin.cran.mypackage.Context.class)

    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Context.class), "getSingleton",
        Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Class.class)), false);

    // Stack: ThreadLocal, org.renjin.cran.mypackage.Context

    // Update the ThreadLocal field (already on the stack)
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadLocal.class), "set",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);

    // Now return the previous value

    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(1,1);
    mv.visitEnd();
  }

  public void writeFields(List<ContextField> fields) {
    for (ContextField field : fields) {
      writeInstanceField(field);
    }
    for (ContextField field : fields) {
      writeGetter(field);
      writeSetter(field);
    }
  }

  private void writeInstanceField(ContextField sessionVar) {
    cv.visitField(ACC_PUBLIC, sessionVar.getVarName(), sessionVar.getType().getDescriptor(), null,
        sessionVar.getInitialValue().flatMap(this::initialFieldValue).orElse(null));

  }

  private void writeGetter(ContextField var) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, var.getGetterName(), var.getGetterDescriptor(), null, null);
    mv.visitCode();

    writeLoadCurrentContext(mv);


    // Retrieve the instance field
    mv.visitFieldInsn(Opcodes.GETFIELD, contextClass.getInternalName(), var.getVarName(), var.getType().getDescriptor());
    mv.visitInsn(var.getType().getOpcode(Opcodes.IRETURN));

    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }


  private void writeSetter(ContextField var) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, var.getSetterName(), var.getSetterDescriptor(), null, null);
    mv.visitCode();

    // Retrieve the current ThreadLocal instance
    writeLoadCurrentContext(mv);

    // Load the new value parameter
    mv.visitVarInsn(var.getType().getOpcode(Opcodes.ILOAD), 0);

    // Set the field with the new value
    mv.visitFieldInsn(Opcodes.PUTFIELD, contextClass.getInternalName(), var.getVarName(), var.getType().getDescriptor());

    mv.visitInsn(Opcodes.RETURN);

    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeLoadCurrentContext(MethodVisitor mv) {
    // Retrieve the current ThreadLocal instance
    mv.visitFieldInsn(Opcodes.GETSTATIC, contextClass.getInternalName(), THREAD_LOCAL_FIELD_NAME, Type.getDescriptor(ThreadLocal.class));
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadLocal.class), "get", Type.getMethodDescriptor(Type.getType(Object.class)), false);

    // Cast to our context class
    mv.visitTypeInsn(Opcodes.CHECKCAST, contextClass.getInternalName());
  }


  private Optional<Object> initialFieldValue(JExpr initialValue) {
    if(initialValue instanceof ConstantValue) {
      return Optional.of(((ConstantValue) initialValue).getValue());
    } else {
      return Optional.empty();
    }
  }

  public void writeTo(CodeGenerationContext output) throws IOException {
    output.writeClassFile(contextClass, cv.toByteArray());
  }

  /**
   * Writes an instance constructor that initializes the global variables.
   */
  public void writeConstructor(CodeGenerationContext generationContext, List<ContextField> contextFields, List<GimpleVarDecl> globalVars) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

    MethodGenerator methodGenerator = new MethodGenerator(contextClass, mv);

    // Reserve "this" variable
    methodGenerator.getLocalVarAllocator().reserve(contextClass);

    for (ContextField contextField : contextFields) {
      contextField.writeFieldInit(methodGenerator);
    }

    for (GimpleVarDecl globalVar : globalVars) {
      writeGlobalVarInit(generationContext, methodGenerator, globalVar);
    }


    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeGlobalVarInit(CodeGenerationContext generationContext, MethodGenerator mv, GimpleVarDecl globalVar) {

    GimpleExpr initialValue = globalVar.getValue();
    if(initialValue == null) {
      if(globalVar.getType() instanceof GimpleIndirectType) {
        initialValue = ((GimpleIndirectType) globalVar.getType()).nullValue();
      }
    }

    if(initialValue != null) {
      SymbolTable symbolTable = generationContext.getSymbolTable(globalVar.getUnit());
      ExprFactory exprFactory = new ExprFactory(generationContext.getTypeOracle(), symbolTable, mv);

      GExpr initialValueExpr = exprFactory.findGenerator(initialValue);
      GExpr varExpr = symbolTable.getVariable(globalVar.newRef());

      varExpr.store(mv, initialValueExpr);
    }
  }
}
