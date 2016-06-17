package org.renjin.gcc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.objectweb.asm.Type;
import org.renjin.gcc.analysis.*;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.TrampolineClassGenerator;
import org.renjin.gcc.codegen.UnitClassGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.record.RecordArrayTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.symbols.GlobalSymbolTable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
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

  private String packageName;

  private boolean verbose;

  private GlobalSymbolTable globalSymbolTable;

  private Collection<GimpleRecordTypeDef> recordTypeDefs;
  private RecordUsageAnalyzer recordUsage;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();

  private final TypeOracle typeOracle = new TypeOracle();

  private final Map<String, Class> providedRecordTypes = Maps.newHashMap();
  private final Map<String, Field> providedVariables = Maps.newHashMap();

  private String trampolineClassName;
  private String recordClassPrefix = "record";
  private int nextRecordIndex = 0;
  
  private TreeLogger rootLogger = new NullTreeLogger();

  public GimpleCompiler() {
    functionBodyTransformers.add(FunctionCallPruner.INSTANCE);
    functionBodyTransformers.add(LocalVariablePruner.INSTANCE);
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(AddressableFinder.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionBodyTransformers.add(LocalVariableInitializer.INSTANCE);
    globalSymbolTable = new GlobalSymbolTable(typeOracle);
    globalSymbolTable.addDefaults();
    providedRecordTypes.put("tm", org.renjin.gcc.runtime.tm.class);
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
   * Sets the name of the trampoline class that contains a static wrapper method for all 'extern' functions.
   */
  public void setClassName(String className) {
    this.trampolineClassName = className;
  }

  public void addReferenceClass(Class<?> clazz) {
    globalSymbolTable.addMethods(clazz);

    for (Field field : clazz.getFields()) {
      if(Modifier.isStatic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
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

  public void addMethod(String functionName, Class declaringClass, String methodName) {
    globalSymbolTable.addMethod(functionName, declaringClass, methodName);
  }

  public void addRecordClass(String typeName, Class recordClass) {
    providedRecordTypes.put(typeName, recordClass);
  }

  /**
   * Compiles the given {@link GimpleCompilationUnit}s to JVM class files.
   */
  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    try {
      
      GlobalVarMerger.merge(units);
      
      // create the mapping from the compilation unit's version of the record types
      // to the canonical version shared by all compilation units
      recordTypeDefs = RecordTypeDefCanonicalizer.canonicalize(rootLogger, units);
      if (verbose) {
        for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
          System.out.println(recordTypeDef);
        }
      }

      // Prune unused functions 
      SymbolPruner.prune(rootLogger, units);

      // First apply any transformations needed by the code generation process
      transform(units);

      // Analyze record type usage to determine strategy for generate code involving records
      // (must be done after void ptr inference)

      recordUsage = new RecordUsageAnalyzer(recordTypeDefs);
      recordUsage.analyze(units);

      // Compile the record types so they are available to functions and variables
      compileRecords(units);

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
      }

      // Write link metadata to META-INF/org.renjin.gcc.symbols
      writeLinkMetadata();

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

  private void writeLinkMetadata() throws IOException {

    for (Map.Entry<String, CallGenerator> entry : globalSymbolTable.getFunctions()) {
      if (entry.getValue() instanceof FunctionCallGenerator) {
        FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) entry.getValue();
        if (functionCallGenerator.getStrategy() instanceof FunctionGenerator) {
          FunctionGenerator functionGenerator = (FunctionGenerator) functionCallGenerator.getStrategy();
          for (String mangledName : functionGenerator.getMangledNames()) {
            LinkSymbol symbol = LinkSymbol.forFunction(mangledName, functionGenerator.getMethodHandle());
            symbol.write(outputDirectory);
          }
        }
      }
    }
  }

  private void compileRecords(List<GimpleCompilationUnit> units) throws IOException {

    TreeLogger recordLogger = rootLogger.branch("Compiling record types...");
    
    // Enumerate record types before writing, so that records can reference each other
    for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
      typeOracle.addRecordType(recordTypeDef, strategyFor(recordLogger, recordTypeDef));
    }

    // Now that the record types are all registered, we can link the fields to their
    // FieldGenerators
    for (RecordTypeStrategy recordTypeStrategy : typeOracle.getRecordTypes()) {
      try {
        recordTypeStrategy.linkFields(typeOracle);
      } catch (Exception e) {
        throw new InternalCompilerException(String.format("Exception linking record %s: %s",
            recordTypeStrategy.getRecordTypeDef().getName(),
            e.getMessage()), e);
      }
    }

    // Finally write out the record class files for those records which are  not provided
    for (RecordTypeStrategy recordTypeStrategy : typeOracle.getRecordTypes()) {
      recordTypeStrategy.writeClassFiles(outputDirectory);
    }
  }

  private RecordTypeStrategy strategyFor(TreeLogger parentLogger, GimpleRecordTypeDef recordTypeDef) {

    TreeLogger logger = parentLogger.branch(String.format("Building strategy for %s [%s]", 
        recordTypeDef.getName(), recordTypeDef.getId()));

    logger.debug("RecordTypeDef:", recordTypeDef);

    if(isProvided(recordTypeDef)) {
      Type providedType = Type.getType(providedRecordTypes.get(recordTypeDef.getName()));
      logger.debug("Provided: " + providedType.getInternalName());
      
      RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
      strategy.setProvided(true);
      strategy.setJvmType(providedType);

      strategy.setUnitPointer(recordUsage.unitPointerAssumptionsHoldFor(recordTypeDef));

      return strategy;

    } else if(recordTypeDef.getFields().isEmpty()) {
      logger.debug("Using EmptyRecordTypeStrategy");

      RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
      strategy.setUnitPointer(recordUsage.unitPointerAssumptionsHoldFor(recordTypeDef));
      strategy.setJvmType(Type.getType(Object.class));
      strategy.setProvided(true);

      return strategy;
      
    } else if(RecordArrayTypeStrategy.accept(recordTypeDef)) {
      logger.debug("Using RecordArrayTypeStrategy");

      return new RecordArrayTypeStrategy(recordTypeDef);

    } else {
      logger.debug("Using RecordClassStrategy");

      RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
      strategy.setUnitPointer(recordUsage.unitPointerAssumptionsHoldFor(recordTypeDef));
      String recordClassName;
      if (recordTypeDef.getName() != null) {
        recordClassName =  String.format("%s$%s", recordClassPrefix, recordTypeDef.getName() + (nextRecordIndex++));
      } else {
        recordClassName = String.format("%s$Record%d", recordClassPrefix, nextRecordIndex++);
      }
      strategy.setJvmType(Type.getType("L" + getInternalClassName(recordClassName) + ";"));

      logger.debug("unitPointer = " + strategy.isUnitPointer());
      logger.debug("jvmType = " + strategy.getJvmType().getInternalName());
      
      return strategy;
    }
  }

  private boolean isProvided(GimpleRecordTypeDef recordTypeDef) {
    if(recordTypeDef.getName() == null) {
      return false;
    } else {
      return providedRecordTypes.containsKey(recordTypeDef.getName());
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
        
        transformFunctionBody(rootLogger.branch("Transforming " + function.getName()), unit, function);
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
    providedVariables.put(name, field);

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
