package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
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
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a main JVM class in which all Gimple functions are declared as public static methods.
 */
public class MainClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private Map<GimpleCompilationUnit, VariableTable> globalVariables = Maps.newHashMap();
  private FunctionTable functionTable;
  private GeneratorFactory generatorFactory;

  public MainClassGenerator(GeneratorFactory generatorFactory, FunctionTable functionTable, String className) {
    this.functionTable = functionTable;
    this.className = className;
    this.generatorFactory = generatorFactory;
  }
  
  public void emit(List<GimpleCompilationUnit> units) {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    
    emitDefaultConstructor();
    emitGlobalVariables(units);
    emitFunctions(units);
    
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

  private void emitGlobalVariables(List<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      VariableTable variableTable = new VariableTable();
      for (GimpleVarDecl gimpleVarDecl : unit.getGlobalVariables()) {
        try {
          FieldGenerator field = generatorFactory.forField(className, gimpleVarDecl.getName(), gimpleVarDecl.getType());
          field.emitStaticField(cv, gimpleVarDecl);
          variableTable.add(gimpleVarDecl.getId(), field.staticExprGenerator());

        } catch (Exception e) {
          throw new InternalCompilerException("Exception writing static variable " + gimpleVarDecl.getName() + 
              " defined in " + unit.getSourceFile().getName(), e);
        }
      }
      globalVariables.put(unit, variableTable);
    }
  }

  private void emitFunctions(List<GimpleCompilationUnit> units) {

    // First enumerate all functions as they may be reference from within each other
    List<FunctionGenerator> functions = new ArrayList<FunctionGenerator>();

    for (GimpleCompilationUnit unit : units) {
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
    }

    // Now actually emit the function bodies
    for (FunctionGenerator functionGenerator : functions) {
      try {
        functionGenerator.emit(cv, globalVariables.get(functionGenerator.getCompilationUnit()), functionTable);
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
