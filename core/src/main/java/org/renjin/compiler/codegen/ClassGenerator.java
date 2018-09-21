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


import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.JitClassLoader;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Textifier;
import org.renjin.repackaged.asm.util.TraceMethodVisitor;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.renjin.repackaged.asm.Type.getMethodDescriptor;
import static org.renjin.repackaged.asm.Type.getType;

public class ClassGenerator<T> implements Opcodes {

  private static final boolean DEBUG = false;

  private static final AtomicLong CLASS_COUNTER = new AtomicLong(1);

  private final Class<T> interfaceClass;

  private final ClassWriter cw;
  private final ClassVisitor cv;
  private String className;

  public ClassGenerator(Class<T> interfaceClass) {
    super();
    this.interfaceClass = interfaceClass;
    this.className = "Body" + CLASS_COUNTER.getAndIncrement();
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cv = cw;
    cv.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null,
        Type.getInternalName(Object.class), new String[] { Type.getInternalName(interfaceClass) });

    writeConstructor();
  }

  public Class<CompiledBody> compile() {
    writeConstructor();
    writeClassEnd();

    return JitClassLoader.defineClass(CompiledBody.class, className.replace('/', '.'), cw.toByteArray());
  }

  public Class<T> finishAndLoad() {
    writeClassEnd();

    if(true) {
      try {
        File file = new File("/tmp/" + className + ".class");
        org.renjin.repackaged.guava.io.Files.write(cw.toByteArray(), file);

        System.out.println("Class file: " + file.getAbsolutePath());

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return JitClassLoader.defineClass(interfaceClass, className.replace('/', '.'), cw.toByteArray());
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

  public void addLoopBodyMethod(Consumer<InstructionAdapter> writer) {

    addMethod("run", getMethodDescriptor(Type.getType(SEXP.class),
        getType(Context.class),
        getType(Environment.class),
        getType(SEXP.class),
        Type.INT_TYPE), writer);
  }


  public void addBodyMethod(Consumer<InstructionAdapter> writer) {
    addMethod("evaluate", getMethodDescriptor(Type.getType(SEXP.class),
        getType(Context.class),
        getType(Environment.class)), writer);
  }


  public void addMethod(String methodName, String descriptor, Consumer<InstructionAdapter> writer) {
    if(DEBUG) {
      MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, descriptor,null, null);

      MethodVisitor mv = methodNode;

      Textifier p = new Textifier();
      mv = new TraceMethodVisitor(mv, p);

      mv.visitCode();

      writer.accept(new InstructionAdapter(mv));

      mv.visitEnd();

      PrintWriter pw = new PrintWriter(System.err);
      p.print(pw);
      pw.flush();

      methodNode.accept(cv);

    } else {

      MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, methodName, descriptor, null, null);

      mv.visitCode();
      writer.accept(new InstructionAdapter(mv));
      mv.visitEnd();
    }
  }


  private void writeClassEnd() {
    cv.visitEnd();
  }

}
