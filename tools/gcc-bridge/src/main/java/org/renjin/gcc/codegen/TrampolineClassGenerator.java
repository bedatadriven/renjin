package org.renjin.gcc.codegen;


import com.google.common.collect.Maps;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Writes a single "trampoline" class that provides a method for all the functions
 * with external linkage.
 */
public class TrampolineClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private final Map<String, FieldGenerator> fields = Maps.newHashMap();
  private GeneratorFactory factory;

  public TrampolineClassGenerator(String className) {
    this.className = className;
    sw = new StringWriter();
    pw = new PrintWriter(sw);
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
    for (ParamGenerator generator : functionGenerator.getParamGenerators()) {
      for (Type type : generator.getParameterTypes()) {
        mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), varIndex);
        varIndex += type.getSize();
      }
    }
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, functionGenerator.getClassName(), 
        functionGenerator.getMangledName(),
        functionGenerator.getFunctionDescriptor(),
        false);
    
    Type returnType = functionGenerator.getReturnGenerator().getType();

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
