package org.renjin.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.primitives.annotations.processor.WrapperGenerator2;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class PackageLoaderCompiler implements Opcodes {

  
  private static final int THIS = 0;
  private static final int CONTEXT = 1;
  private static final int ENVIRONMENT = 2;


  public static byte[] compile(String packageName, Environment packageEnvironment) {

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, CompiledNames.loaderClassName(packageName), null,
        "java/lang/Object", new String[] { "org/renjin/compiler/runtime/PackageLoader" });

    writeInit(cw);
    writeLoadMethod(cw, packageName, packageEnvironment);
    
    return cw.toByteArray();
  }

  private static void writeInit(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, THIS);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();    
  }

  private static void writeLoadMethod(ClassWriter cw, String packageName, Environment rho) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "load",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)V", null, null);
    mv.visitCode();

    for(Symbol symbol : rho.getSymbolNames()) {
      SEXP value = rho.getVariable(symbol);
      try {
        if(value instanceof Closure) {
          storeClosure(mv, packageName, symbol);        
        } else {
          storeConstant(mv, symbol, rho.getVariable(symbol));
        }
      } catch(Exception e) {
        throw new RuntimeException("Error generating code for '" + symbol + "'", e);
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }


  private static void storeClosure(MethodVisitor mv, String packageName, Symbol symbol) {
    mv.visitVarInsn(ALOAD, ENVIRONMENT);
    mv.visitLdcInsn(symbol.getPrintName());
    mv.visitTypeInsn(NEW, "org/renjin/compiler/runtime/PromisedFunction");
    mv.visitInsn(DUP);
    mv.visitVarInsn(ALOAD, CONTEXT);
    mv.visitVarInsn(ALOAD, ENVIRONMENT);
    mv.visitLdcInsn(functionClassName(packageName, symbol));
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/compiler/runtime/PromisedFunction", "<init>",
        "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Ljava/lang/String;)V");
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/Environment", "setVariable", 
        "(Ljava/lang/String;Lorg/renjin/sexp/SEXP;)V");
  }

  private static void storeConstant(MethodVisitor mv, Symbol symbol, SEXP value) {
    mv.visitVarInsn(ALOAD, ENVIRONMENT);
    mv.visitLdcInsn(symbol.getPrintName());
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    value.accept(cgv);
  }
  
  private static String functionClassName(String packageName, Symbol symbol) {
    return packageName.replace('/', '.') + "." + WrapperGenerator2.toJavaName("", symbol.getPrintName());
  }
}
