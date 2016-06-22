package org.renjin.gcc.codegen;


import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.type.ParamStrategy;

import java.io.IOException;
import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates a single "trampoline" class that provides a method for all the functions
 * with external linkage.
 */
public class TrampolineClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;

  public TrampolineClassGenerator(String className) {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    if(GimpleCompiler.TRACE) {
      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    } else {
      cv = cw;
    }
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
  
    emitDefaultConstructor();
  }

  private void emitDefaultConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  public void emitTrampolineMethod(FunctionGenerator functionGenerator) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC,
        functionGenerator.getMangledName(),
        functionGenerator.getFunctionDescriptor(),
        null, null);
    
    mv.visitCode();
    
    int varIndex = 0;
    for (ParamStrategy generator : functionGenerator.getParamStrategies()) {
      for (Type type : generator.getParameterTypes()) {
        mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), varIndex);
        varIndex += type.getSize();
      }
    }
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, functionGenerator.getClassName(), 
        functionGenerator.getMangledName(),
        functionGenerator.getFunctionDescriptor(),
        false);
    
    Type returnType = functionGenerator.getReturnStrategy().getType();

    if (returnType.equals(Type.VOID_TYPE)) {
      mv.visitInsn(Opcodes.RETURN);
    } else {
      mv.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
    }
    
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  public byte[] generateClassFile() throws IOException {
    cv.visitEnd();
    return cw.toByteArray();
  }
}
