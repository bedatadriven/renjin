package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.objectweb.asm.Type;
import org.renjin.gcc.analysis.AddressableFinder;
import org.renjin.gcc.analysis.FunctionBodyTransformer;
import org.renjin.gcc.analysis.ResultDeclRewriter;
import org.renjin.gcc.analysis.VoidPointerTypeDeducer;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.MainClassGenerator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.call.FunctionTable;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Compiles a set of Gimple functions to jvm class file
 * 
 */
public class GimpleCompiler  {

  private File outputDirectory;
  
  private String packageName;

  private String className;

  private boolean verbose;

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());

  private FunctionTable functionTable;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();
  
  private final GeneratorFactory generatorFactory = new GeneratorFactory();
  
  private final Map<String, Class> providedRecordTypes = Maps.newHashMap();
  
  
  public GimpleCompiler() {
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(AddressableFinder.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionTable = new FunctionTable(generatorFactory);
    functionTable.addDefaults();
  }

  public void setPackageName(String name) {
    this.packageName = name;
  }

  public void setOutputDirectory(File directory) {
    this.outputDirectory = directory;
  }

  public void setClassName(String className) {
    this.className = className;
  }


  public void addReferenceClass(Class<?> clazz) {
    functionTable.addMethods(clazz);
  }

  public void addMathLibrary() {
    functionTable.addMethod("log", Math.class);
    functionTable.addMethod("exp", Math.class);
  }

  public void addMethod(String functionName, Class declaringClass, String methodName) {
    functionTable.addMethod(functionName, declaringClass, methodName);
  }
  
  public void addRecordClass(String typeName, Class recordClass) {
    providedRecordTypes.put(typeName, recordClass);
  }

  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    // First apply any transformations needed by the code generation process
    transform(units);
    compileRecords(units);

    MainClassGenerator generator = new MainClassGenerator(generatorFactory, functionTable, getInternalClassName());
    generator.emit(units);
    writeClass(getInternalClassName(), generator.toByteArray());
  }

  private void compileRecords(List<GimpleCompilationUnit> units) throws IOException {
    
    // Enumerate record types before writing, so that records can reference each other
    List<RecordClassGenerator> recordsToWrite = Lists.newArrayList();
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
        System.out.println(recordTypeDef);
        
        if(isProvided(recordTypeDef)) {
          
          // Map this record type to an existing JVM class
          Class existingClass = providedRecordTypes.get(recordTypeDef.getName());
          
          generatorFactory.addRecordType(recordTypeDef, 
              new RecordClassGenerator(generatorFactory, Type.getInternalName(existingClass), recordTypeDef));
          
        } else {
          // Create a new JVM class for this record type
          
          String recordClassName;
          if (recordTypeDef.getName() != null) {
            recordClassName = recordTypeDef.getName();
          } else {
            recordClassName = String.format("%s$Record%d", className, recordsToWrite.size());
          }
          RecordClassGenerator recordGenerator =
              new RecordClassGenerator(generatorFactory, getInternalClassName(recordClassName), recordTypeDef);

          generatorFactory.addRecordType(recordTypeDef, recordGenerator);
          recordsToWrite.add(recordGenerator);
        }
      }
    }

    // Now write out the record class files
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

  private String getInternalClassName() {
    return getInternalClassName(className);
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

}
