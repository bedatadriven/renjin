package org.renjin.gcc.codegen;

import com.google.common.collect.Lists;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.symbols.GlobalSymbolTable;
import org.renjin.gcc.symbols.UnitSymbolTable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a JVM class for a given Gimple compilation unit
 */
public class UnitClassGenerator {

  private GimpleCompilationUnit unit;
  private String className;

  private UnitSymbolTable symbolTable;
  private final GeneratorFactory generatorFactory;
  
  private List<GimpleVarDecl> varToGenerate = Lists.newArrayList();

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;


  public UnitClassGenerator(GeneratorFactory generatorFactory,
                            GlobalSymbolTable functionTable,
                            Map<String, Field> providedVariables, GimpleCompilationUnit unit,
                            String className) {
    this.unit = unit;
    this.className = className;
    this.generatorFactory = generatorFactory;
    this.symbolTable = new UnitSymbolTable(functionTable, className);

    for (GimpleVarDecl decl : unit.getGlobalVariables()) {
      if(isProvided(providedVariables, decl)) {
        // TODO: this requires the jvm field to use the same representation as we would
        // use when compiling. Should check and perhaps provide an adaptation
        Field providedField = providedVariables.get(decl.getName());
        symbolTable.addGlobalVariable(decl, generatorFactory.forField(
            Type.getInternalName(providedField.getDeclaringClass()), 
            providedField.getName(), decl.getType()));
      } else {
        symbolTable.addGlobalVariable(decl, generatorFactory.forField(className, decl.getName(), decl.getType()));
        varToGenerate.add(decl);
      }
    }

    for (GimpleFunction function : unit.getFunctions()) {
      symbolTable.addFunction(className, function,
          new FunctionGenerator(className, function, generatorFactory, symbolTable));
    }
  }

  private boolean isProvided(Map<String, Field> providedVariables, GimpleVarDecl decl) {
    return decl.isExtern() && providedVariables.containsKey(decl.getName());
  }

  public String getClassName() {
    return className;
  }

  public void emit() {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    if(GimpleCompiler.TRACE) {
      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    } else {
      cv = cw;
    }
    cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    cv.visitSource(unit.getSourceName(), null);
    emitDefaultConstructor();
    emitGlobalVariables();
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

  private void emitGlobalVariables() {
    
    // write the field declarations
    for (GimpleVarDecl decl : varToGenerate) {
      try {
        FieldGenerator generator = symbolTable.getVariable(decl);
        generator.emitStaticField(cv, decl);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception writing static variable " + decl.getName() +
            " defined in " + unit.getSourceFile().getName(), e);
      }
    }
    
    // and any static initialization that is required
    ExprFactory exprFactory = new ExprFactory(generatorFactory, symbolTable, unit.getCallingConvention());
    MethodVisitor mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();

    for (GimpleVarDecl decl : varToGenerate) {
      if(decl.getValue() != null) {
        try {
          ExprGenerator globalVariable = symbolTable.getVariable(decl).staticExprGenerator();
          ExprGenerator value = exprFactory.findGenerator(decl.getValue());
          globalVariable.emitStore(mv, value);

        } catch (Exception e) {
          throw new InternalCompilerException("Exception writing static variable initializer " + decl.getName() +
              " defined in " + unit.getSourceFile().getName(), e);
        }
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
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
