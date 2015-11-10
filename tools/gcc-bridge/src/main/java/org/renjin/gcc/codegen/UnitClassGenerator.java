package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.FunctionTable;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.var.VariableTable;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a JVM class for a given Gimple compilation unit
 */
public class UnitClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private VariableTable globalVariables = new VariableTable();
  private FunctionTable functionTable;
  private GeneratorFactory generatorFactory;

  public UnitClassGenerator(GeneratorFactory generatorFactory, FunctionTable functionTable, String className) {
    this.functionTable = functionTable;
    this.className = className;
    this.generatorFactory = generatorFactory;
  }
  
  public void emit(GimpleCompilationUnit unit) {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    cv.visitSource(unit.getSourceName(), null);
    emitDefaultConstructor();
    emitGlobalVariables(unit);
    emitFunctions(unit);
    cv.visitEnd();
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

  private void emitGlobalVariables(GimpleCompilationUnit unit) {
    for (GimpleVarDecl gimpleVarDecl : unit.getGlobalVariables()) {
      try {
        FieldGenerator field = generatorFactory.forField(className, gimpleVarDecl.getName(), gimpleVarDecl.getType());
        field.emitStaticField(cv, gimpleVarDecl);
        globalVariables.add(gimpleVarDecl.getId(), field.staticExprGenerator());

      } catch (Exception e) {
        throw new InternalCompilerException("Exception writing static variable " + gimpleVarDecl.getName() + 
            " defined in " + unit.getSourceFile().getName(), e);
      }
    }
  }

  private void emitFunctions(GimpleCompilationUnit unit) {

    // First enumerate all functions as they may be reference from within each other
    List<FunctionGenerator> functions = new ArrayList<FunctionGenerator>();

    for (GimpleFunction function : unit.getFunctions()) {
      FunctionGenerator functionGenerator;
      try {
        functionGenerator = new FunctionGenerator(generatorFactory, function);
      } catch (Exception e) {
        throw new InternalCompilerException(function, e);
      }
      functions.add(functionGenerator);
      functionTable.add(className, functionGenerator);
    }

    // Now actually emit the function bodies
    for (FunctionGenerator functionGenerator : functions) {
      try {
        functionGenerator.emit(cv, globalVariables, functionTable);
      } catch (Exception e) {
        throw new InternalCompilerException(functionGenerator, e);
      }
    }
  }
  
  public byte[] toByteArray() {
    cv.visitEnd();
    return cw.toByteArray();
  }
}
