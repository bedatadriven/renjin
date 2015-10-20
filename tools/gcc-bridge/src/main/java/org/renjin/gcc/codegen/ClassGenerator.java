package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.codegen.call.FunctionTable;
import org.renjin.gcc.codegen.var.FieldGenerator;
import org.renjin.gcc.codegen.var.PtrFieldGenerator;
import org.renjin.gcc.codegen.var.ValueFieldGenerator;
import org.renjin.gcc.codegen.var.VariableTable;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


public class ClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private VariableTable globalVariables = new VariableTable();
  private FunctionTable functionTable;
  
  public ClassGenerator(String className) {
    this.className = className;
    
    functionTable = new FunctionTable();
    functionTable.addDefaults();
  }
  
  public void emit(List<GimpleCompilationUnit> units) {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    
    emitDefaultConstructor();
    emitGlobalVariables(units);
    emitFunctions(units);
  }


  private void emitDefaultConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void emitGlobalVariables(List<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl gimpleVarDecl : unit.getGlobalVariables()) {
        
        FieldGenerator field = findGlobalVarGenerator(gimpleVarDecl);
        field.emitField(cv);
        
        globalVariables.add(gimpleVarDecl.getId(), field);
      }
    }
  }

  private FieldGenerator findGlobalVarGenerator(GimpleVarDecl gimpleVarDecl) {
    if(gimpleVarDecl.getType() instanceof GimplePrimitiveType) {
      return new ValueFieldGenerator(gimpleVarDecl.getName(), className,
          gimpleVarDecl.getType(),
          ((GimplePrimitiveType) gimpleVarDecl.getType()).jvmType());
    
    } else if(gimpleVarDecl.getType() instanceof GimpleIndirectType) {
      return new PtrFieldGenerator(className, gimpleVarDecl);
    
    } else {
      throw new UnsupportedOperationException(gimpleVarDecl.toString());
    }
  }

  private void emitFunctions(List<GimpleCompilationUnit> units) {

    // First enumerate all functions as they may be reference from within each other
    
    List<FunctionGenerator> functions = new ArrayList<FunctionGenerator>();
    
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        FunctionGenerator functionGenerator = new FunctionGenerator(function);
        
        functions.add(functionGenerator);
        functionTable.add(className, function.getName(), functionGenerator);
      }
    }
    
    // Now actually emit the function bodies
    for (FunctionGenerator functionGenerator : functions) {
      functionGenerator.emit(cv, globalVariables, functionTable);
    }
  }
  
  
  public byte[] toByteArray() {
    cv.visitEnd();
    return cw.toByteArray();
  }
}
