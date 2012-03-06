package r.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.primitives.annotations.processor.WrapperGenerator;

import r.lang.Closure;
import r.lang.Environment;
import r.lang.SEXP;
import r.lang.Symbol;

public class PackageLoaderCompiler implements Opcodes {

  public static byte[] compile(String packageName, Environment packageEnvironment) {

    ClassWriter cw = new ClassWriter(0);
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, packageName, null,
        "java/lang/Object", null);

    writeInit(cw);
    writeLoadMethod(cw, packageName, packageEnvironment);
    
    return cw.toByteArray();
  }

  private static void writeInit(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();    
  }

  private static void writeLoadMethod(ClassWriter cw, String packageName, Environment rho) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "load",
        "(Lr/lang/Context;Lr/lang/Environment;)V", null, null);
    mv.visitCode();

    for(Symbol symbol : rho.getSymbolNames()) {
      SEXP value = rho.getVariable(symbol);
      if(value instanceof Closure) {
        storeClosure(mv, packageName, symbol);        
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private static void storeClosure(MethodVisitor mv, String packageName, Symbol symbol) {
    mv.visitVarInsn(ALOAD, 1);
    mv.visitLdcInsn(symbol.getPrintName());
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Symbol", "get", 
        "(Ljava/lang/String;)Lr/lang/Symbol;");
    mv.visitTypeInsn(NEW, "r/compiler/runtime/PromisedFunction");
    mv.visitInsn(DUP);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitLdcInsn(packageName + "/" + WrapperGenerator.toJavaName("", symbol.getPrintName()));
    mv.visitMethodInsn(INVOKESPECIAL, "r/compiler/runtime/PromisedFunction", "<init>",
        "(Lr/lang/Context;Lr/lang/Environment;Ljava/lang/String;)V");
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Environment", "setVariable", 
        "(Lr/lang/Symbol;Lr/lang/SEXP;)V");
  }
}
