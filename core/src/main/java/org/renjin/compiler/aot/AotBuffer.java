/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.aot;

import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Textifier;
import org.renjin.repackaged.asm.util.TraceMethodVisitor;
import org.renjin.sexp.ListVector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.renjin.repackaged.asm.Opcodes.ACC_PUBLIC;
import static org.renjin.repackaged.asm.Opcodes.ACC_STATIC;

public class AotBuffer {

  private static final boolean DEBUG = true;
  private final String packageName;
  private final Map<String, ClassBuffer> sourceMap = new HashMap<>();


  public AotBuffer(String packageName) {
    this.packageName = packageName;
  }

  public AotHandle newFunction(String sourceFile, String functionName, String descriptor, Function<InstructionAdapter, ListVector> writer) {
    ClassBuffer classBuffer = classBuffer(sourceFile);

    String methodName = classBuffer.newUniqueMethodName(functionName);
    ListVector frameVars;
    if(DEBUG) {
      MethodNode methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, methodName, descriptor,null, null);

      MethodVisitor mv = methodNode;

      Textifier p = new Textifier();
      mv = new TraceMethodVisitor(mv, p);

      mv.visitCode();

      frameVars = writer.apply(new InstructionAdapter(mv));

      mv.visitEnd();

      PrintWriter pw = new PrintWriter(System.err);
      p.print(pw);
      pw.flush();

      methodNode.accept(classBuffer.getClassVisitor());

    } else {

      MethodVisitor mv = classBuffer.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_STATIC, methodName, descriptor, null, null);
      mv.visitCode();
      frameVars = writer.apply(new InstructionAdapter(mv));
      mv.visitEnd();
    }

    return new AotHandle(classBuffer.getClassName(), methodName, frameVars, () -> {
      return classBuffer.flushAndLoad();
    });
  }

  public ClassBuffer classBuffer(String sourceFile) {
    String sourceName;

    if (sourceFile == null) {
      sourceName = "Jit";

    } else if (sourceFile.toUpperCase().endsWith(".R") ||
               sourceFile.toUpperCase().endsWith(".S")) {

      sourceName = sourceFile.substring(0, sourceFile.length() - 2);

    } else {
      sourceName = sourceFile;
    }

    String className = packageName.replace('.', '/') + "/" + WrapperGenerator2.toJavaMethod(sourceName);

    return sourceMap.computeIfAbsent(sourceFile,
        s -> new ClassBuffer(className, sourceFile));
  }


  public void flushTo(File outputDir) throws IOException {
    for (ClassBuffer classBuffer : sourceMap.values()) {
      classBuffer.flushTo(outputDir);
    }
  }
}
