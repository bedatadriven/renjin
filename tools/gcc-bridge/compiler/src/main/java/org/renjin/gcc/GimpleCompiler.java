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
package org.renjin.gcc;

import org.renjin.gcc.analysis.*;
import org.renjin.gcc.annotations.GlobalVar;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.TrampolineClassGenerator;
import org.renjin.gcc.codegen.UnitClassGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.symbols.GlobalSymbolTable;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a set of {@link GimpleCompilationUnit}s to bytecode
 *
 * <p>The {@code GimpleCompiler} compiles the Gimple ASTs emitted by the 
 * GCC Bridge Plugin to a set of JVM class files.
 *
 * <p>Each {@code GimpleCompilationUnit} is compiled to a seperate JVM class file with the same
 * name as the compilation unit. If the {@code className} is set, an additional "trampoline" class is 
 * generated that contains a wrapper methods to all 'extern' functions.</p>
 *
 */
public class GimpleCompiler  {

  public static boolean TRACE = false;

  private File outputDirectory;

  private File javadocOutputDirectory;

  private String packageName;

  private boolean verbose;

  private GlobalSymbolTable globalSymbolTable;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();

  private final TypeOracle typeOracle = new TypeOracle();

  private final Map<String, Class> providedRecordTypes = Maps.newHashMap();
  private final Map<String, ProvidedGlobalVar> providedVariables = Maps.newHashMap();

  private String trampolineClassName;
  private String recordClassPrefix = "record";

  private TreeLogger rootLogger = new NullTreeLogger();

  private boolean pruneUnusedSymbols = true;
  
  private Predicate<GimpleFunction> entryPointPredicate = new DefaultEntryPointPredicate();

  public GimpleCompiler() {
    functionBodyTransformers.add(AddressableSimplifier.INSTANCE);
    functionBodyTransformers.add(FunctionCallPruner.INSTANCE);
    functionBodyTransformers.add(LocalVariablePruner.INSTANCE);
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionBodyTransformers.add(LocalVariableInitializer.INSTANCE);
    globalSymbolTable = new GlobalSymbolTable(typeOracle);
    globalSymbolTable.addDefaults();
  }

  public TreeLogger getLogger() {
    return rootLogger;
  }

  public void setLogger(TreeLogger rootLogger) {
    this.rootLogger = rootLogger;
  }

  /**
   * Sets the package name to use for the compiled JVM classes.
   *
   * @param name the package name, separated by dots. For example "com.acme"
   */
  public void setPackageName(String name) {
    this.packageName = name;
  }

  /**
   * Sets the output directory to place compiled class files.
   *
   */
  public void setOutputDirectory(File directory) {
    this.outputDirectory = directory;
  }


  /**
   * Sets the output directory for writing java source stubs for use by the javadoc tool.
   *
   * @param javadocOutputDirectory the root directory, or {@code null} if no stub sources should
   *                               be written.
   */
  public void setJavadocOutputDirectory(File javadocOutputDirectory) {
    this.javadocOutputDirectory = javadocOutputDirectory;
  }

  public void setEntryPointPredicate(Predicate<GimpleFunction> entryPointPredicate) {
    this.entryPointPredicate = entryPointPredicate;
  }

  public void setPruneUnusedSymbols(boolean pruneUnusedSymbols) {
    this.pruneUnusedSymbols = pruneUnusedSymbols;
  }

  /**
   * Sets the name of the trampoline class that contains a static wrapper method for all 'extern' functions.
   */
  public void setClassName(String className) {
    this.trampolineClassName = className;
  }

  public void addReferenceClass(Class<?> clazz) {
    globalSymbolTable.addMethods(clazz);

    for (Method method : clazz.getMethods()) {
      if(method.getAnnotation(GlobalVar.class) != null) {
        if(method.getParameterTypes().length != 0) {
          throw new IllegalStateException("Method " + method + " cannot be used as a " +
              "@" + GlobalVar.class.getSimpleName() + ", it must have zero arguments");
        }
        providedVariables.put(method.getName(), new ProvidedGlobalVarGetter(method));
      }
    }

    for (Field field : clazz.getFields()) {
      if(Modifier.isStatic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) &&
          field.getAnnotation(Deprecated.class) == null) {
        addVariable(field.getName(), field);
      }
    }
  }

  public void addMathLibrary() {
    globalSymbolTable.addMethod("log", Math.class);
    globalSymbolTable.addMethod("exp", Math.class);
  }

  public void addLibrary(SymbolLibrary lib) {
    globalSymbolTable.addLibrary(lib);
  }

  public void addTransformer(FunctionBodyTransformer transformer) {
    functionBodyTransformers.add(transformer);
  }

  public void addMethod(String functionName, Class declaringClass, String methodName) {
    globalSymbolTable.addMethod(functionName, declaringClass, methodName);
  }

  public void addRecordClass(String typeName, Class recordClass) {
    providedRecordTypes.put(typeName, recordClass);
  }


  public void compileSources(List<File> sourceFiles) throws Exception {
    GimpleParser parser = new GimpleParser();

    List<GimpleCompilationUnit> units = new ArrayList<>();
    for (File sourceFile : sourceFiles) {
      try {
        units.add(parser.parse(sourceFile));
      } catch (Exception e) {
        throw new IOException("Exception parsing gimple file " + sourceFile.getName(), e);
      }
    }
    compile(units);
  }

  
  /**
   * Compiles the given {@link GimpleCompilationUnit}s to JVM class files.
   */
  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    try {

      PmfRewriter.rewrite(units);
      GlobalVarMerger.merge(units);

      // Prune unused functions
      if(pruneUnusedSymbols) {
        SymbolPruner.prune(rootLogger, units, entryPointPredicate);
      }

      typeOracle.initRecords(units, providedRecordTypes);

      // First apply any transformations needed by the code generation process
      transform(units);

      // Identify variables and fields that must be addressable
      AddressableFinder addressableFinder = new AddressableFinder(units);
      addressableFinder.mark();


      // Next, do a round of compilation units to make sure all externally visible functions and 
      // symbols are added to the global symbol table.
      // This allows us to effectively do linking at the same time as code generation
      List<UnitClassGenerator> unitClassGenerators = Lists.newArrayList();
      for (GimpleCompilationUnit unit : units) {
        String className = classNameForUnit(unit.getName());
        UnitClassGenerator generator = new UnitClassGenerator(
            typeOracle,
            globalSymbolTable,
            providedVariables,
            unit, className);
        unitClassGenerators.add(generator);
      }

      // Finally, run code generation
      TreeLogger codegenLogger = rootLogger.branch("Generating bytecode");
      for (UnitClassGenerator generator : unitClassGenerators) {
        generator.emit(codegenLogger);
        writeClass(generator.getClassName(), generator.toByteArray());

        if(trampolineClassName == null && javadocOutputDirectory != null) {
          generator.emitJavaDoc(javadocOutputDirectory);
        }
      }

      // Write link metadata to META-INF/org.renjin.gcc.symbols
      writeLinkMetadata(unitClassGenerators);

      // If requested, generate a single class that wraps all exported functions
      if (trampolineClassName != null) {
        writeTrampolineClass();
      }

    } finally {
      try {
        rootLogger.finish();
      } catch (Exception e) {
        System.out.println("Failed to write logs");
        e.printStackTrace();
      }
    }
  }

  private void writeLinkMetadata(List<UnitClassGenerator> unitClassGenerators) throws IOException {

    for (Map.Entry<String, CallGenerator> entry : globalSymbolTable.getFunctions()) {
      if (entry.getValue() instanceof FunctionCallGenerator) {
        FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) entry.getValue();
        if (functionCallGenerator.getStrategy() instanceof FunctionGenerator) {
          FunctionGenerator functionGenerator = (FunctionGenerator) functionCallGenerator.getStrategy();
          for (String mangledName : functionGenerator.getMangledNames()) {
            LinkSymbol symbol = LinkSymbol.forFunction(mangledName, functionGenerator.getMethodHandle());
            try {
              symbol.write(outputDirectory);
            } catch (FileNotFoundException e) {
              System.err.println("Exception writing link metadata for " + symbol.getName() + ": " + e.getMessage());
            }
          }
        }
      }
    }

    for (UnitClassGenerator unit : unitClassGenerators) {
      for (LinkSymbol symbol : unit.getGlobalVariableSymbols()) {
        symbol.write(outputDirectory);
      }
    }

  }


  private String classNameForUnit(String className) {
    // if we are using a trampoline class, then decorate the 
    // classes with the actual implementation to avoid conflict
    if(trampolineClassName != null) {
      return getInternalClassName(className) + "__";
    } else {
      return getInternalClassName(className);
    }
  }

  private String getInternalClassName(String className) {
    // sanitize class name: file names may not be legal class names, for example,
    // bit-ops.c

    return (packageName + "." + sanitize(className)).replace('.', '/');
  }

  @VisibleForTesting
  static String sanitize(String name) {
    Preconditions.checkArgument(name.length() >= 1);

    StringBuilder className = new StringBuilder();

    int i = 0;
    if(Character.isJavaIdentifierStart(name.charAt(0))) {
      className.append(name.charAt(0));
      i++;
    } else {
      className.append('_');
    }

    for(;i<name.length();++i) {
      char c = name.charAt(i);
      if(Character.isJavaIdentifierPart(c)) {
        className.append(c);
      } else {
        className.append("_");
      }
    }
    return className.toString();
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  private void transform(List<GimpleCompilationUnit> units) {

    for (GimpleCompilationUnit unit : units) {
      if(TRACE) {
        System.out.println(unit);
      }
      for (GimpleFunction function : unit.getFunctions()) {
        if(!function.isEmpty()) {
          transformFunctionBody(rootLogger.branch("Transforming " + function.getName()), unit, function);
        }
      }
    }
  }

  private void transformFunctionBody(TreeLogger parentLogger, GimpleCompilationUnit unit, GimpleFunction function) {
    boolean updated;
    do {
      updated = false;
      for(FunctionBodyTransformer transformer : functionBodyTransformers) {
        TreeLogger logger = parentLogger.branch(TreeLogger.Level.DEBUG, "Running " + transformer.getClass().getSimpleName());
        if(transformer.transform(logger, unit, function)) {
          updated = true;
        }
      }
    } while(updated);
  }


  /**
   * Write a "trampoline class" that contains references to all the exported methods
   */
  private void writeTrampolineClass() throws IOException {

    TrampolineClassGenerator classGenerator =
        new TrampolineClassGenerator(getInternalClassName(trampolineClassName));

    Set<String> names = Sets.newHashSet();

    for (Map.Entry<String, CallGenerator> entry : globalSymbolTable.getFunctions()) {
      if (entry.getValue() instanceof FunctionCallGenerator) {
        FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) entry.getValue();
        if (functionCallGenerator.getStrategy() instanceof FunctionGenerator) {
          FunctionGenerator functionGenerator = (FunctionGenerator) functionCallGenerator.getStrategy();
          if(!names.contains(functionGenerator.getMangledName())) {
            classGenerator.emitTrampolineMethod(functionGenerator);
            names.add(functionGenerator.getMangledName());
          }
        }
      }
    }

    writeClass(getInternalClassName(trampolineClassName), classGenerator.generateClassFile());
  }


  private void writeClass(String internalName, byte[] classByteArray) throws IOException {
    File classFile = new File(outputDirectory.getAbsolutePath() + File.separator + internalName + ".class");
    if(!classFile.getParentFile().exists()) {
      boolean created = classFile.getParentFile().mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory for class file: " + classFile.getParentFile());
      }
    }
    Files.write(classByteArray, classFile);
  }

  public void addVariable(String globalVariableName, Class<?> declaringClass) {
    try {
      addVariable(globalVariableName, declaringClass.getField(globalVariableName));
    } catch (NoSuchFieldException e) {
      throw new InternalCompilerException("Cannot find field", e);
    }
  }

  public void addVariable(String name, Field field) {
    providedVariables.put(name, new ProvidedGlobalVarField(field));

  }

  public String getRecordClassPrefix() {
    return recordClassPrefix;
  }

  public void setRecordClassPrefix(String recordClassPrefix) {
    this.recordClassPrefix = recordClassPrefix;
  }

  public void setLinkClassLoader(ClassLoader linkClassLoader) {
    this.globalSymbolTable.setLinkClassLoader(linkClassLoader);
  }


}
