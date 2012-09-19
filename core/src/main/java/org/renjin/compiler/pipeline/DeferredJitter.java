package org.renjin.compiler.pipeline;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

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
public class DeferredJitter {

  private String className;
  private ClassVisitor cv;

  public DeferredJitter() {
    className = "Jit" + System.identityHashCode(this);
  }

  public JittedComputation compile(DeferredNode node)  {
    long startTime = System.nanoTime();
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = cw;
//    if(DeferredGraph.DEBUG) {
//      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
//    }
    //cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
            new String[]{"org/renjin/compiler/pipeline/JittedComputation"});

    writeConstructor();
    writeCompute(node);

    cv.visitEnd();

    byte[] classBytes = cw.toByteArray();
    long compileTime = System.nanoTime() - startTime;

    Class jitClass = new MyClassLoader().defineClass(className, classBytes);

    long loadTime = System.nanoTime() - startTime - compileTime;

    if(DeferredGraph.DEBUG) {
      System.out.println("compile: " + (compileTime/1e6) + "ms");
      System.out.println("load: " + (loadTime/1e6) + "ms");
    }

    try {
      return (JittedComputation) jitClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not invoke jitted computation", e);
    }
  }

  private void writeConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeCompute(DeferredNode node) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "compute", "([Lorg/renjin/sexp/Vector;)[D", null, null);
    mv.visitCode();

    ComputeMethod methodContext = new ComputeMethod(mv);

    FunctionJitter function = getFunction(node);
    function.compute(methodContext, node);

    mv.visitMaxs(1, methodContext.getMaxLocals());
    mv.visitEnd();
  }

  private FunctionJitter getFunction(DeferredNode node) {
    if(node.getComputation().getComputationName().equals("mean")) {
      return new MeanJitter();
    } else if(node.getComputation().getComputationName().equals("rowMeans")) {
      return new RowMeanJitter();
    } else {
      throw new UnsupportedOperationException(node.toString());
    }
  }

  class MyClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}
