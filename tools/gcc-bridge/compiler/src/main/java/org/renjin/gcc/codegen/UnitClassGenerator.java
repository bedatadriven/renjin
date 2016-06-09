package org.renjin.gcc.codegen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.GlobalVarAllocator;
import org.renjin.gcc.codegen.var.ProvidedVarAllocator;
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
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a JVM class for a given Gimple compilation unit
 */
public class UnitClassGenerator {

  private final GimpleCompilationUnit unit;
  private final String className;

  private final UnitSymbolTable symbolTable;
  private final TypeOracle typeOracle;
  
  private final GlobalVarAllocator globalVarAllocator;
  private final List<GimpleVarDecl> varToGenerate = Lists.newArrayList();

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;


  public UnitClassGenerator(TypeOracle typeOracle,
                            GlobalSymbolTable functionTable,
                            Map<String, Field> providedVariables, GimpleCompilationUnit unit,
                            String className) {
    this.unit = unit;
    this.className = className;
    this.typeOracle = typeOracle;
    this.globalVarAllocator = new GlobalVarAllocator(className);
    this.symbolTable = new UnitSymbolTable(functionTable);
  
    for (GimpleVarDecl decl : unit.getGlobalVariables()) {
      TypeStrategy typeStrategy = typeOracle.forType(decl.getType());
      Expr varGenerator;
      
      if(isProvided(providedVariables, decl)) {
        Field providedField = providedVariables.get(decl.getName());
        varGenerator = typeStrategy.variable(decl, new ProvidedVarAllocator(providedField.getDeclaringClass()));
        
      } else {
        varGenerator = typeStrategy.variable(decl, globalVarAllocator);
        varToGenerate.add(decl);
      }

      symbolTable.addGlobalVariable(decl, varGenerator);
    }

    for (GimpleFunction function : unit.getFunctions()) {
      try {
        symbolTable.addFunction(function,
            new FunctionGenerator(className, function, typeOracle, symbolTable));
      } catch (Exception e) {
        throw new InternalCompilerException(String.format("Exception creating %s for %s in %s: %s",
            FunctionGenerator.class.getSimpleName(),
            function.getName(),
            unit.getSourceName(),
            e.getMessage()), e);
      }
    }
  }

  private boolean isProvided(Map<String, Field> providedVariables, GimpleVarDecl decl) {
    return decl.isExtern() && providedVariables.containsKey(decl.getName());
  }

  public String getClassName() {
    return className;
  }

  public void emit(TreeLogger parentLogger) {
    
    TreeLogger logger = parentLogger.branch("Generating code for " + unit.getSourceName());
    
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    //cw = new ClassWriter(0);

    if(GimpleCompiler.TRACE) {
      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    } else {
      cv = cw;
    }
    cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    cv.visitSource(unit.getSourceName(), null);
    emitDefaultConstructor();
    emitGlobalVariables();
    emitFunctions(logger, unit);
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
    
    // write actual field declarations
    globalVarAllocator.writeFields(cv);
    
    // and any static initialization that is required
    ExprFactory exprFactory = new ExprFactory(typeOracle, symbolTable);
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null));
    mv.visitCode();

    for (GimpleVarDecl decl : varToGenerate) {
      if(decl.getName().startsWith("_ZTI")) {
        // Skip rtti tables for now...
        continue;
      }
      try {
        LValue varGenerator = (LValue) symbolTable.getGlobalVariable(decl);
        if(decl.getValue() != null) {
          varGenerator.store(mv, exprFactory.findGenerator(decl.getValue()));
        }

      } catch (Exception e) {
        throw new InternalCompilerException(
            String.format("Exception writing static variable initializer %s %s = %s defined in %s", 
                decl.getType(),
                decl.getName(),
                decl.getValue(),
                unit.getSourceFile().getName()), e);
      }
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void emitFunctions(TreeLogger parentLogger, GimpleCompilationUnit unit) {

    // Check for duplicate names...
    Set<String> names = Sets.newHashSet();
    for (FunctionGenerator functionGenerator : symbolTable.getFunctions()) {
      if(names.contains(functionGenerator.getMangledName())) {
        throw new InternalCompilerException("Duplicate function names " + functionGenerator.getMangledName());
      }
      names.add(functionGenerator.getMangledName());
    }
    
    // Now actually emit the function bodies
    for (FunctionGenerator functionGenerator : symbolTable.getFunctions()) {
      try {
        functionGenerator.emit(parentLogger, cv);
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
