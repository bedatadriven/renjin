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
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import static org.renjin.repackaged.asm.Opcodes.*;

public class ClassBuffer {

  private final String className;
  private final Set<String> methodNames = new HashSet<>();
  private final ClassWriter writer;
  private final ClassVisitor visitor;

  private boolean open = true;
  private Class loadedClass = null;

  public ClassBuffer(String className, String sourceFile) {
    this.className = className;
    writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_7, ACC_PUBLIC, className, null, Type.getInternalName(Object.class), null);
    writer.visitSource(sourceFile, null);

    visitor = new TraceClassVisitor(writer, new PrintWriter(System.out));

    writeConstructor();
  }

  ClassVisitor getClassVisitor() {
    return visitor;
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
    if(loadedClass == null) {
      loadedClass = JitClassLoader.defineClass(Object.class, className.replace('/', '.'), writer.toByteArray());
    }
    return loadedClass;
  }
}
