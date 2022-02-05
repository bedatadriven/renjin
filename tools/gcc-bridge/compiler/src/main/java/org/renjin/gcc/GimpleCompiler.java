/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.*;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.logging.LogManager;
import org.renjin.gcc.runtime.*;
import org.renjin.gcc.symbols.GlobalSymbolTable;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Ordering;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.Files;
import soot.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

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

  public static boolean IGNORE_ERRORS = "TRUE".equals(System.getenv("GCC_BRIDGE_IGNORE_ERRORS"));

  public static boolean TRACE = false;

  private File outputDirectory;

  private File javadocOutputDirectory;

  private String packageName;

  private boolean verbose;

  private ClassLoader linkClassLoader = getClass().getClassLoader();

  private GlobalSymbolTable globalSymbolTable;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();

  private final TypeOracle typeOracle = new TypeOracle();

  private final Map<String, ProvidedGlobalVar> providedVariables = Maps.newHashMap();

  private final List<GimpleCompilerPlugin> plugins = new ArrayList<>();

  private final List<String> classesWritten = new ArrayList<>();

  private String trampolineClassName;
  private String recordClassPrefix = "record";

  private final LogManager logManager = new LogManager(System.err);
  private String runtimeClasspath;
  private boolean byteCodeOptimizationDisabled = false;


  public GimpleCompiler() {
    functionBodyTransformers.add(AddressableSimplifier.INSTANCE);
    functionBodyTransformers.add(FunctionCallPruner.INSTANCE);
    functionBodyTransformers.add(LocalVariablePruner.INSTANCE);
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionBodyTransformers.add(LocalVariableInitializer.INSTANCE);
    globalSymbolTable = new GlobalSymbolTable(typeOracle, providedVariables);
    globalSymbolTable.addDefaults();
    addReferenceClass(Builtins.class);
    addReferenceClass(Stdlib.class);
    addReferenceClass(Stdlib2.class);
    addReferenceClass(Mathlib.class);
    addReferenceClass(Std.class);
    addReferenceClass(PosixThreads.class);
  }


  public static boolean ignoreCompilerErrors() {
    return IGNORE_ERRORS;
  }

  public LogManager getLogManager() {
    return logManager;
  }

  public void setLoggingDirectory(File logDir) {
    logManager.setLoggingDirectory(logDir);
  }

  /**
   * Sets the package name to use for the compiled JVM classes.
   *
   * @param name the package name, separated by dots. For example "com.acme"
   */
  public void setPackageName(String name) {
    this.packageName = name;
  }

  public String getPackageName() {
    return packageName;
  }

  /**
   * Sets the output directory to place compiled class files.
   *
   */
  public void setOutputDirectory(File directory) {
    this.outputDirectory = directory;
  }


  public void setRuntimeClasspath(String runtimeClasspath) {
    this.runtimeClasspath = runtimeClasspath;
  }

  public String getRuntimeClasspath() {
    return runtimeClasspath;
  }

  public boolean isByteCodeOptimizationDisabled() {
    return byteCodeOptimizationDisabled;
  }

  public void setByteCodeOptimizationDisabled(boolean byteCodeOptimizationDisabled) {
    this.byteCodeOptimizationDisabled = byteCodeOptimizationDisabled;
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

  /**
   * Sets the name of the trampoline class that contains a static wrapper method for all 'extern' functions.
   */
  public void setClassName(String className) {
    this.trampolineClassName = className;
  }

  public void setIgnoreErrors(boolean ignoreErrors) {
    IGNORE_ERRORS = ignoreErrors;
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
      if(Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &&
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

  public void addPlugin(GimpleCompilerPlugin plugin) {
    plugins.add(plugin);
  }

  public void addMethod(String functionName, Class declaringClass, String methodName) {
    globalSymbolTable.addMethod(functionName, declaringClass, methodName);
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

      typeOracle.initRecords(units, linkClassLoader);

      // First apply any transformations needed by the code generation process
      transform(units);

      // Identify variables and fields that must be addressable
      AddressableFinder addressableFinder = new AddressableFinder(units);
      addressableFinder.mark();

      // Queue up the global variable transforms
      List<GlobalVarTransformer> globalVarTransformers = new ArrayList<>();
      globalVarTransformers.add(new ProvidedVarTransformer(typeOracle, providedVariables));
      for (GimpleCompilerPlugin plugin : plugins) {
        globalVarTransformers.addAll(plugin.createGlobalVarTransformers());
      }

      // Next, do a round of compilation units to make sure all externally visible functions and
      // symbols are added to the global symbol table.
      // This allows us to effectively do linking at the same time as code generation
      List<UnitClassGenerator> unitClassGenerators = Lists.newArrayList();
      Map<GimpleCompilationUnit, String> unitNames = nameCompilationUnits(units);
      for (GimpleCompilationUnit unit : units) {
        UnitClassGenerator generator = new  UnitClassGenerator(
            typeOracle,
            globalSymbolTable,
            globalVarTransformers,
            unit,
            this::writeResourcePrefix,
            unitNames.get(unit));
        unitClassGenerators.add(generator);
      }

      // Finally, run code generation
      Map<GimpleCompilationUnit, SymbolTable> symbolTableMap = new HashMap<>();
      for (UnitClassGenerator generator : unitClassGenerators) {
        generator.emit(logManager);
        writeClass(generator.getClassName(), generator.toByteArray());

        if(trampolineClassName == null && javadocOutputDirectory != null) {
          generator.emitJavaDoc(javadocOutputDirectory);
        }

        symbolTableMap.put(generator.getUnit(), generator.getSymbolTable());
      }

      // Write link metadata to META-INF/org.renjin.gcc.symbols
      writeLinkMetadata(unitClassGenerators);

      // If requested, generate a single class that wraps all exported functions
      if (trampolineClassName != null) {
        writeTrampolineClass();
      }

      writePluginClasses(globalSymbolTable, symbolTableMap);

    } finally {
      try {
        logManager.finish();
      } catch (Exception e) {
        System.out.println("Failed to write logs");
        e.printStackTrace();
      }
    }

    optimizeClassfiles();
  }


  private void writeResourcePrefix(String resourceName, byte[] bytes) throws IOException {
    writeResource(packageName.replace('.', '/') + "/" + resourceName, bytes);
  }

  private Map<GimpleCompilationUnit, String> nameCompilationUnits(List<GimpleCompilationUnit> units) {
    // First sort compilation units by path so we get consistent names between compilations
    units.sort(Ordering.natural().onResultOf(u -> u.getSourceFile().getAbsolutePath()));

    Map<String, Integer> nameCounter = new HashMap<>();
    Map<GimpleCompilationUnit, String> nameMap = new HashMap<>();
    for (GimpleCompilationUnit unit : units) {
      String className = classNameForUnit(unit.getName());
      int duplicateIndex = nameCounter.getOrDefault(className, 0);
      String uniqueClassName;
      if (duplicateIndex == 0) {
        uniqueClassName = className;
      } else {
        uniqueClassName = className + "$" + duplicateIndex;
      }
      nameMap.put(unit, uniqueClassName);
      nameCounter.put(className, duplicateIndex + 1);
    }

    return nameMap;

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
          transformFunctionBody(logManager, unit, function);
        }
      }
    }
  }

  private void transformFunctionBody(LogManager logger, GimpleCompilationUnit unit, GimpleFunction function) {
    boolean updated;
    do {
      updated = false;
      for(FunctionBodyTransformer transformer : functionBodyTransformers) {
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
            classGenerator.emitTrampolineMethod(plugins, functionGenerator);
            names.add(functionGenerator.getMangledName());
          }
        }
      }
    }

    writeClass(getInternalClassName(trampolineClassName), classGenerator.generateClassFile());
  }


  private void writePluginClasses(GlobalSymbolTable globalSymbolTable,
                                  Map<GimpleCompilationUnit, SymbolTable> symbolTableMap) throws IOException {
    CodeGenerationContext context = new CodeGenerationContext() {
      @Override
      public TypeOracle getTypeOracle() {
        return typeOracle;
      }

      @Override
      public GlobalSymbolTable getGlobalSymbolTable() {
        return globalSymbolTable;
      }

      @Override
      public SymbolTable getSymbolTable(GimpleCompilationUnit unit) {
        return symbolTableMap.get(unit);
      }

      @Override
      public void writeClassFile(Type className, byte[] bytes) throws IOException {
        GimpleCompiler.this.writeClass(className.getInternalName(), bytes);
      }

      @Override
      public void writeResourceFile(String resourceName, byte[] bytes) throws IOException {
        GimpleCompiler.this.writeResource(resourceName, bytes);
      }
    };

    for (GimpleCompilerPlugin plugin : plugins) {
      plugin.writeClasses(context);
    }
  }

  private void writeClass(String internalName, byte[] classByteArray) throws IOException {
    writeResource(internalName + ".class", classByteArray);
    classesWritten.add(internalName.replace('/', '.'));
  }

  private void writeResource(String resourceName, byte[] content) throws IOException {
    File classFile = new File(outputDirectory.getAbsolutePath() + File.separator + resourceName);
    if (!classFile.getParentFile().exists()) {
      boolean created = classFile.getParentFile().mkdirs();
      if (!created) {
        throw new IOException("Failed to create directory for class file: " + classFile.getParentFile());
      }
    }
    Files.write(content, classFile);
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

  public void setRecordClassPrefix(String recordClassPrefix) {
    this.recordClassPrefix = recordClassPrefix;
  }

  public void setLinkClassLoader(ClassLoader linkClassLoader) {
    this.linkClassLoader = linkClassLoader;
    this.globalSymbolTable.setLinkClassLoader(linkClassLoader);
  }

  private void optimizeClassfiles() {
    if(byteCodeOptimizationDisabled) {
      return;
    }
    List<String> args = new ArrayList<>();
    // Prepend the existing classpath with below
    args.add("-pp");

    // Classpath for soot analysis
    args.add("-cp");
    args.add(getSootClasspath());

    args.add("-java-version");
    args.add("1.8");

    args.add("-debug");

    // Add classes to optimize
    args.add("-O");
    args.addAll(classesWritten);

    // Write out to build directory and overwrite existing classfiles
    args.add("-d");
    args.add(outputDirectory.getAbsolutePath());

    try {
      Main.v().run(args.toArray(new String[0]));
    } catch (Throwable e) {
      System.err.println("WARNING: Soot failed to complete.");
      e.printStackTrace(System.err);
    }

    // When soot hits an error, it can set the interrupt flag for this
    // thread.
    Thread.interrupted();
  }

  private String getSootClasspath() {
    if(runtimeClasspath != null) {
      return runtimeClasspath + ":" + outputDirectory.getAbsolutePath();
    } else {
      return findClasspath();
    }
  }

  private String findClasspath() {
    if(!(Build.class.getClassLoader() instanceof URLClassLoader)) {
      throw new RuntimeException("Cannot get classpath from class loader, which is " + Build.class.getClassLoader().getClass().getName());
    }
    List<String> paths = new ArrayList<>();
    paths.add(outputDirectory.getAbsolutePath());
    URLClassLoader classLoader = (URLClassLoader) Build.class.getClassLoader();
    for (URL url : classLoader.getURLs()) {
      if(url.getProtocol().equals("file")) {
        File dir = new File(url.getPath());
        if(dir.exists()) {
          paths.add(url.getPath());
        }
      } else {
        paths.add(url.toString());
      }
    }
    return paths.stream().collect(Collectors.joining(":"));
  }


  public static void main(String[] args) throws Exception {

    GimpleCompiler compiler = new GimpleCompiler();

    List<GimpleCompilationUnit> units = new ArrayList<>();

    for (String arg : args) {
      if(arg.startsWith("--")) {
        String[] parts = arg.split("=", 2);
        String option = parts[0];
        String value = parts[1];
        switch (option) {
          case "--package":
            compiler.setPackageName(value);
            break;
          case "--class":
            compiler.setClassName(value);
            break;
          case "--runtime-classpath":
            compiler.setRuntimeClasspath(value);
            break;
          case "--output-dir":
            compiler.setOutputDirectory(new File(value));
            break;
          case "--log-dir":
            compiler.setLoggingDirectory(new File(value));
            break;
          case "--ignore-errors":
            compiler.setIgnoreErrors("true".equalsIgnoreCase(value));
            break;
          default:
            throw new RuntimeException("Unknown option " + arg);
        }
      } else {
        File input = new File(arg);
        if(!input.exists()) {
          throw new RuntimeException("The input '" + input + "' does not exist.");
        }
        if(input.isDirectory()) {
          for (File gimpleFile : input.listFiles()) {
            units.add(Gcc.parseGimple(gimpleFile));
          }
        } else {
          units.add(Gcc.parseGimple(input));
        }
      }
    }

    compiler.compile(units);
  }
}
