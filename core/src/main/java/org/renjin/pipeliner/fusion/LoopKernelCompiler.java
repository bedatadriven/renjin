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
package org.renjin.pipeliner.fusion;

import org.renjin.compiler.JitClassLoader;
import org.renjin.pipeliner.ComputeMethod;
import org.renjin.pipeliner.VectorPipeliner;
import org.renjin.pipeliner.fusion.kernel.CompiledKernel;
import org.renjin.pipeliner.fusion.kernel.LoopKernel;
import org.renjin.pipeliner.fusion.node.LoopNode;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.ClassWriter;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.asm.util.Textifier;
import org.renjin.repackaged.asm.util.TraceMethodVisitor;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Compiles a graph of deferred calculations into a single method.
 *
 * <p>This class is used to efficiently compute complex vector operations like
 *
 * <pre>
 * mean(acos(x*x)))
 * </pre>
 *
 * Instead of computing each of the operations in sequence we emit a new
 * class that looks something like this:
 *
 * <pre>
 *   double[] x_arr = x.toDoubleArrayUnsafe();
 *   double sum;
 *   for(int i=0;i!=x.length;++i) {
 *    x_i = x[i];
 *    sum += Math.acos(x_i*x_i);
 *   }
 *   return sum / x.length()
 * </pre>
 *
 * <p>This is identical to the mean function defined in {@link org.renjin.primitives.Summary#mean(org.renjin.sexp.Vector)}
 * but we replace the virtual invocations to DoubleArrayVector.getElementAsDouble() or
 * R$primitive$acos$deferred_d.getElementAsDouble() with direct array references or static calls that the
 * JVM can be expected to quickly inline. (I would also think
 * the jvm should be capable of inlining virtual invocations in loops of 25m + iterations, but it doesn't seem
 * to happen in practice.
 *
 * <p>Because we totally inline getElementAsDouble,
 * we need a new Jitted class for each combination of operators and vector classes.</p>
 */
public class LoopKernelCompiler implements Callable<CompiledKernel> {
  
  public static final boolean DEBUG = System.getProperty("renjin.vp.jit.debug") != null;

  private static final String KERNEL_INTERFACE = Type.getInternalName(CompiledKernel.class);

  private final LoopKernel kernel;
  private final LoopNode[] operands;

  private String className;
  private ClassVisitor cv;

  public LoopKernelCompiler(LoopKernel kernel, LoopNode[] operands) {
    this.kernel = kernel;
    this.operands = operands;
    this.className = "Jit" + System.identityHashCode(this);
  }

  public CompiledKernel call()  {
    long startTime = System.nanoTime();
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = cw;
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[]{ KERNEL_INTERFACE });

    writeConstructor();
    if(DEBUG) {
      writeComputeDebug(kernel, operands);
    } else {
      writeCompute(kernel, operands);
    }
    cv.visitEnd();

    byte[] classBytes = cw.toByteArray();
    long compileTime = System.nanoTime() - startTime;

    Class<CompiledKernel> jitClass = JitClassLoader.defineClass(CompiledKernel.class, className, classBytes);

    long loadTime = System.nanoTime() - startTime - compileTime;

    if(VectorPipeliner.DEBUG) {
      System.out.println(className + ": compile: " + (compileTime/1e6) + "ms");
      System.out.println(className + ": load: " + (loadTime / 1e6) + "ms");
      if(DEBUG) {
        try {
          File classFile = File.createTempFile("Specialization", ".class");
          Files.write(classBytes, classFile);
          System.out.println("Wrote class file to " + classFile);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    try {
      return jitClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not invoke jitted computation", e);
    }
  }

  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeCompute(LoopKernel kernel, LoopNode[] operands) {
    String typeDescriptor = "([Lorg/renjin/sexp/Vector;)[D";

    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "compute", typeDescriptor, null, null);
//
//    mv = new CheckMethodAdapter(ACC_PUBLIC, "compute", typeDescriptor, mv, new HashMap());
//    mv.visitCode();

    ComputeMethod methodContext = new ComputeMethod(mv);

    kernel.compute(methodContext, operands);

    mv.visitMaxs(1, methodContext.getMaxLocals());
    mv.visitEnd();
  }

  private void writeComputeDebug(LoopKernel kernel, LoopNode[] operands) {

    MethodNode mv = new MethodNode(ACC_PUBLIC, "compute", "([Lorg/renjin/sexp/Vector;)[D", null, null);
    mv.visitCode();

    ComputeMethod methodContext = new ComputeMethod(mv);

    kernel.compute(methodContext, operands);

    mv.visitMaxs(1, methodContext.getMaxLocals());
    mv.visitEnd();

    try {
      mv.accept(cv);

      if(DEBUG) {
        System.out.println(toString(mv));
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Toxic bytecode generated: " + toString(mv), e);
    }
  }


  private String toString(MethodNode methodNode) {
    try {
      Textifier p = new Textifier();
      methodNode.accept(new TraceMethodVisitor(p));
      StringWriter sw = new StringWriter();
      try (PrintWriter pw = new PrintWriter(sw)) {
        p.print(pw);
      }
      return sw.toString();
    } catch (Exception e) {
      return "<Exception generating bytecode: " + e.getClass().getName() + ": " + e.getMessage() + ">";
    }
  }

}
