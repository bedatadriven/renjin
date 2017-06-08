/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen;

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.GlobalVarAllocator;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.symbols.GlobalSymbolTable;
import org.renjin.gcc.symbols.UnitSymbolTable;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.ClassWriter;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.util.TraceClassVisitor;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

import static org.renjin.repackaged.asm.Opcodes.*;


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

    // Setup global variables that have global scoping
    for (GimpleVarDecl decl : unit.getGlobalVariables()) {
      if(!isIgnored(decl)) {
        TypeStrategy typeStrategy = typeOracle.forType(decl.getType());
        GExpr varGenerator;

        if (isProvided(providedVariables, decl)) {
          Field providedField = providedVariables.get(decl.getName());
          varGenerator = typeStrategy.providedGlobalVariable(decl, providedField);

        } else {
          try {
            varGenerator = typeStrategy.variable(decl, globalVarAllocator);
          } catch (Exception e) {
            throw new InternalCompilerException("Global variable " + decl.getName() + " in " + unit.getSourceName(), e);
          }
          varToGenerate.add(decl);
        }
        symbolTable.addGlobalVariable(decl, varGenerator);
      }
    }

    for (GimpleFunction function : unit.getFunctions()) {
      try {
        symbolTable.addFunction(function,
            new FunctionGenerator(className, function, typeOracle, globalVarAllocator, symbolTable));
      } catch (Exception e) {
        throw new InternalCompilerException(String.format("Exception creating %s for %s in %s: %s",
            FunctionGenerator.class.getSimpleName(),
            function.getName(),
            unit.getSourceName(),
            e.getMessage()), e);
      }
    }

    for (GimpleAlias alias : unit.getAliases()) {
      symbolTable.addAlias(alias);
    }

  }

  private boolean isIgnored(GimpleVarDecl decl) {
    if(decl.getMangledName() == null) {
      return false;
    }
    return decl.getMangledName().equals("_ZTVN10__cxxabiv117__class_type_infoE") || 
        decl.getMangledName().equals("_ZTVN10__cxxabiv120__si_class_type_infoE");
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
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
      @Override
      protected String getCommonSuperClass(String type1, String type2) {
        try {
          return super.getCommonSuperClass(type1, type2);
        } catch (Exception e) {
          return Type.getInternalName(Object.class);
        }
      }
    };
    //cw = new ClassWriter(0);

    if(GimpleCompiler.TRACE) {
      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    } else {
      cv = cw;
    }
    cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    cv.visitSource(unit.getSourceName(), null);
    emitDefaultConstructor();
    emitFunctions(logger, unit);
    emitGlobalVariables();
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
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null));
    mv.visitCode();

    ExprFactory exprFactory = new ExprFactory(typeOracle, symbolTable, mv);
    
    globalVarAllocator.writeFieldInitialization(mv);
  
    for (GimpleVarDecl decl : varToGenerate) {
      if(decl.getName().startsWith("_ZTI")) {
        // Skip rtti tables for now...
        continue;
      }
      try {
        GExpr varGenerator = symbolTable.getGlobalVariable(decl);
        if(decl.getValue() != null) {
          try {
            varGenerator.store(mv, exprFactory.findGenerator(decl.getValue()));
          } catch (Exception e) {
            System.err.println("Warning: could not generate code for global variable " + decl.getMangledName() +
                ": " + e.getMessage());
          }
        }

      } catch (Exception e) {
        throw new InternalCompilerException(
            String.format("Exception writing static variable initializer %s %s = %s defined in %s", 
                decl.getType(),
                decl.getName(),
                decl.getValue(),
                unit.getSourceName()), e);
      }
    }

    for (FunctionGenerator function : symbolTable.getFunctions()) {
      function.emitLocalStaticVarInitialization(mv);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void emitFunctions(TreeLogger parentLogger, GimpleCompilationUnit unit) {

    // Check for duplicate names...
    Set<String> names = Sets.newHashSet();
    for (FunctionGenerator functionGenerator : symbolTable.getFunctions()) {
      if(names.contains(functionGenerator.getSafeMangledName())) {
        throw new InternalCompilerException("Duplicate function names " + functionGenerator.getSafeMangledName());
      }
      names.add(functionGenerator.getSafeMangledName());
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

  /**
   * Writes out a java source file for this compilation unit with only the method declarations so that
   * we can produce javadoc.
   *
   * @param outputDir the root directory for the sources
   */
  public void emitJavaDoc(File outputDir) throws IOException {
    File sourceFile = new File(outputDir.getAbsolutePath() + File.separator + className + ".java");
    if(!sourceFile.getParentFile().exists()) {
      boolean created = sourceFile.getParentFile().mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory for java source file: " + sourceFile.getParentFile());
      }
    }

    String[] javaNames = getJavaNames();

    PrintWriter s = new PrintWriter(sourceFile);
    s.println("package " + javaNames[0] + ";");
    s.println();
    s.println("public class " + javaNames[1] + " {");
    s.println();

    for (FunctionGenerator functionGenerator : symbolTable.getFunctions()) {
      s.print("  public static ");
      s.print(javaName(functionGenerator.getReturnStrategy().getType()));
      s.print(" ");
      s.print(functionGenerator.getMangledName());
      s.print("(");

      boolean needsComma = false;

      List<GimpleParameter> params = functionGenerator.getFunction().getParameters();
      for (int i = 0; i < params.size(); i++) {
        GimpleParameter param = params.get(i);
        ParamStrategy paramStrategy = functionGenerator.getParamStrategies().get(i);

        List<Type> types = paramStrategy.getParameterTypes();
        List<String> names = paramStrategy.getParameterNames(param.getName());
        if(types.size() != names.size()) {
          throw new IllegalStateException("strategy: " + paramStrategy);
        }

        for (int j = 0; j < types.size(); j++) {
          if(needsComma) {
            s.print(", ");
          }
          s.print(javaName(types.get(j)));
          s.print(" ");
          s.print(names.get(j));
          needsComma = true;
        }
      }
      s.println(") { throw new UnsupportedOperationException(); }");
    }

    s.println("}");
    s.close();
  }


  private String[] getJavaNames() {
    int packageEnd = className.lastIndexOf('/');
    if(packageEnd == -1) {
      throw new IllegalStateException("className: " + className);
    }

    return new String[] {
        className.substring(0, packageEnd).replace('/', '.'),
        className.substring(packageEnd+1)
    };
  }


  private String javaName(Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return "void";
      case Type.BOOLEAN:
        return "boolean";
      case Type.BYTE:
        return "byte";
      case Type.SHORT:
        return "short";
      case Type.CHAR:
        return "char";
      case Type.INT:
        return "int";
      case Type.LONG:
        return "long";
      case Type.FLOAT:
        return "float";
      case Type.DOUBLE:
        return "double";

      case Type.OBJECT:
        return type.getInternalName().replace('/', '.');

      case Type.ARRAY:
        return javaName(type.getElementType()) + "[]";

      default:
        throw new IllegalArgumentException("type: " + type);
    }
  }

}
