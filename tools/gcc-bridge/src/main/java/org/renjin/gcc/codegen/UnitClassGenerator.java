package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.symbols.GlobalSymbolTable;
import org.renjin.gcc.symbols.UnitSymbolTable;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a JVM class for a given Gimple compilation unit
 */
public class UnitClassGenerator {

  private GimpleCompilationUnit unit;
  private String className;

  private UnitSymbolTable symbolTable;
  private final Object generatorFactory;

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;


  public UnitClassGenerator(GeneratorFactory generatorFactory, GlobalSymbolTable functionTable, GimpleCompilationUnit unit, String className) {
    this.unit = unit;
    this.className = className;
    this.generatorFactory = generatorFactory;
    this.symbolTable = new UnitSymbolTable(functionTable, className);

    for (GimpleVarDecl decl : unit.getGlobalVariables()) {
      symbolTable.addGlobalVariable(decl, generatorFactory.forField(className, decl.getName(), decl.getType()));
    }

    for (GimpleFunction function : unit.getFunctions()) {
      symbolTable.addFunction(className, function,
          new FunctionGenerator(className, function, generatorFactory, symbolTable));
    }
  }

  public String getClassName() {
    return className;
  }

  public void emit() {
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
    for (GimpleVarDecl decl : unit.getGlobalVariables()) {
      try {

        FieldGenerator generator = symbolTable.getVariable(decl);
        generator.emitStaticField(cv, decl);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception writing static variable " + decl.getName() +
            " defined in " + unit.getSourceFile().getName(), e);
      }
    }
  }

  private void emitFunctions(GimpleCompilationUnit unit) {


    // Now actually emit the function bodies
    for (FunctionGenerator functionGenerator : symbolTable.getFunctions()) {
      try {
        functionGenerator.emit(cv);
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
