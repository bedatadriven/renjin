package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.objectweb.asm.Type;
import org.renjin.gcc.analysis.*;
import org.renjin.gcc.codegen.*;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.symbols.GlobalSymbolTable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Compiles a set of Gimple functions to jvm class file
 *
 */
public class GimpleCompiler  {

  public static boolean TRACE = false;

  private File outputDirectory;

  private String packageName;

  private boolean verbose;

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());

  private GlobalSymbolTable globalSymbolTable;

  private Collection<GimpleRecordTypeDef> recordTypeDefs;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();

  private final GeneratorFactory generatorFactory = new GeneratorFactory();

  private final Map<String, Class> providedRecordTypes = Maps.newHashMap();
  private final Map<String, Field> providedVariables = Maps.newHashMap();

  private String trampolineClassName;
  private String recordClassPrefix = "record";

  public GimpleCompiler() {
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(AddressableFinder.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionBodyTransformers.add(LocalVariableInitializer.INSTANCE);
    functionBodyTransformers.add(TreeBuilder.INSTANCE);
    globalSymbolTable = new GlobalSymbolTable(generatorFactory);
    globalSymbolTable.addDefaults();
  }

  public void setPackageName(String name) {
    this.packageName = name;
  }

  public void setOutputDirectory(File directory) {
    this.outputDirectory = directory;
  }

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

  public void addMethod(String functionName, Class declaringClass, String methodName) {
    globalSymbolTable.addMethod(functionName, declaringClass, methodName);
  }

  public void addRecordClass(String typeName, Class recordClass) {
    providedRecordTypes.put(typeName, recordClass);
  }

  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    // create the mapping from the compilation unit's version of the record types
    // to the canonical version shared by all compilation units
    recordTypeDefs = RecordTypeDefCanonicalizer.canonicalize(units);

    // First apply any transformations needed by the code generation process
    transform(units);

    // Compile the record types so they are available to functions and variables
    compileRecords(units);

    // Next, do a round of compilation units to make sure all externally visible functions and 
    // symbols are added to the global symbol table.
    // This allows us to effectively do linking at the same time as code generation
    List<UnitClassGenerator> unitClassGenerators = Lists.newArrayList();
    for (GimpleCompilationUnit unit : units) {
      String className = getInternalClassName(unit.getName());
      UnitClassGenerator generator = new UnitClassGenerator(
          generatorFactory,
          globalSymbolTable,
          providedVariables,
          unit, className);
      unitClassGenerators.add(generator);
    }

    // Finally, run code generation
    for (UnitClassGenerator generator : unitClassGenerators) {
      generator.emit();
      writeClass(generator.getClassName(), generator.toByteArray());
    }

    // Also store an index to symbols in this library
    if(trampolineClassName != null) {
      writeTrampolineClass();
    }
  }

  private void compileRecords(List<GimpleCompilationUnit> units) throws IOException {

    List<RecordClassGenerator> recordsToWrite = Lists.newArrayList();
    List<RecordClassGenerator> recordsToLink = Lists.newArrayList();


    // Enumerate record types before writing, so that records can reference each other
    for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
      
      try {
        RecordClassGenerator recordGenerator;

        if (GimpleCompiler.TRACE) {
          System.out.println(recordTypeDef);
        }
        if (isProvided(recordTypeDef)) {

          // Map this record type to an existing JVM class
          Class existingClass = providedRecordTypes.get(recordTypeDef.getName());
          recordGenerator = new RecordClassGenerator(generatorFactory, Type.getInternalName(existingClass), recordTypeDef);

        } else {
          // Create a new JVM class for this record type

          String recordClassName;
          if (recordTypeDef.getName() != null) {
            recordClassName = recordTypeDef.getName();
          } else {
            recordClassName = String.format("%s$Record%d", recordClassPrefix, recordsToWrite.size());
          }
          recordGenerator =
              new RecordClassGenerator(generatorFactory, getInternalClassName(recordClassName), recordTypeDef);
          
          recordsToWrite.add(recordGenerator);
        }

        generatorFactory.addRecordType(recordTypeDef, recordGenerator);
        recordsToLink.add(recordGenerator);
        
        
      } catch (Exception e) {
        throw new InternalCompilerException("Exception compiling record " + recordTypeDef.getName());
      }
    }


    // Now that the record types are all registered, we can link the fields to their
    // FieldGenerators
    for (RecordClassGenerator recordClassGenerator : recordsToLink) {
      try {
        recordClassGenerator.linkFields();
      } catch (Exception e) {
        throw new InternalCompilerException(String.format("Exception linking record %s: %s",
            recordClassGenerator.getTypeDef().getName(),
            e.getMessage()), e);
      }
    }


    // Finally write out the record class files for those records which are  not provided
    for (RecordClassGenerator recordClassGenerator : recordsToWrite) {
      writeClass(recordClassGenerator.getClassName(), recordClassGenerator.generateClassFile());
    }
  }

  private boolean isProvided(GimpleRecordTypeDef recordTypeDef) {
    if(recordTypeDef.getName() == null) {
      return false;
    } else {
      return providedRecordTypes.containsKey(recordTypeDef.getName());
    }
  }

  private String getInternalClassName(String className) {
    return (packageName + "." + className).replace('.', '/');
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
        transformFunctionBody(unit, function);
      }
    }
  }

  private void transformFunctionBody(GimpleCompilationUnit unit, GimpleFunction function) {
    boolean updated;
    do {
      updated = false;
      for(FunctionBodyTransformer transformer : functionBodyTransformers) {
        if(transformer.transform(unit, function)) {
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

    for (Map.Entry<String, CallGenerator> entry : globalSymbolTable.getFunctions()) {
      if(entry.getValue() instanceof FunctionCallGenerator) {
        FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) entry.getValue();
        FunctionGenerator functionGenerator = functionCallGenerator.getFunctionGenerator();

        classGenerator.emitTrampolineMethod(functionGenerator);
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
}
