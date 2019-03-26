/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.aot;

import org.renjin.compiler.JitClassLoader;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.asm.util.TraceClassVisitor;
import org.renjin.sexp.SEXP;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;

import static org.renjin.repackaged.asm.Opcodes.*;

public class ClassBuffer {

  private final String className;
  private final Set<String> methodNames = new HashSet<>();
  private final ClassWriter writer;
  private final ClassVisitor visitor;

  private boolean open = true;
  private Class loadedClass = null;

  private List<SEXP> astBuffer = new ArrayList<>();
  private HashMap<SEXP, Integer> astMap = new HashMap<>();

  public ClassBuffer(String className, String sourceFile) {
    this.className = className;
    writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_7, ACC_PUBLIC, className, null, Type.getInternalName(Object.class), null);
    writer.visitSource(sourceFile, null);

    visitor = new TraceClassVisitor(writer, new PrintWriter(System.out));

    writeSexpPoolField();
    writeConstructor();
    writeSexpPoolAccessor();
  }

  ClassVisitor getClassVisitor() {
    return visitor;
  }


  public String getClassName() {
    return className;
  }

  private void writeSexpPoolField() {
    visitor.visitField(ACC_STATIC | ACC_PUBLIC, "SEXP_POOL", Type.getDescriptor(SEXP[].class), null, null);

  }

  private void writeSexpPoolAccessor() {
    MethodVisitor mv = visitor.visitMethod(ACC_STATIC | ACC_PRIVATE, "$sexp",
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.INT_TYPE),null, null);
    mv.visitCode();
    mv.visitFieldInsn(GETSTATIC, className, "SEXP_POOL", Type.getDescriptor(SEXP[].class));
    mv.visitVarInsn(ILOAD, 0);
    mv.visitInsn(AALOAD);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(3, 1);
    mv.visitEnd();
  }

  private void writeConstructor() {
    MethodVisitor mv = visitor.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 1);
    mv.visitEnd();
  }

  public CompiledSexp sexp(SEXP sexp) {
    int index = astMap.computeIfAbsent(sexp, s -> {
      astBuffer.add(s);
      return astBuffer.size() - 1;
    });

    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        mv.iconst(index);
        mv.invokestatic(className, "$sexp",
              Type.getMethodDescriptor(Type.getType(SEXP.class), Type.INT_TYPE), false);
      }
    };
  }

  public String newUniqueMethodName(String function) {

    if(!open) {
      throw new IllegalStateException("ClassBuffer is closed");
    }

    if(methodNames.add(function)) {
      return function;
    }
    int number = 2;
    while(true) {
      String alias = function + "$" + number;
      if(methodNames.add(alias)) {
        return alias;
      }
      number ++;
    }
  }

  public void flush() {
    if(open) {
      open = false;
      writer.visitEnd();
    }
  }

  public Class flushAndLoad() {
    flush();

    try {
      File file = File.createTempFile("renjin",".class");
      org.renjin.repackaged.guava.io.Files.write(writer.toByteArray(), file);
      System.err.println("Wrote to " + file);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if(loadedClass == null) {
      loadedClass = JitClassLoader.defineClass(Object.class, className.replace('/', '.'), writer.toByteArray());

      try {
        Field field = loadedClass.getField("SEXP_POOL");
        field.set(null, astBuffer.toArray(new SEXP[0]));

      } catch (IllegalAccessException | NoSuchFieldException e) {
        throw new IllegalStateException(e);
      }

    }
    return loadedClass;
  }

}
