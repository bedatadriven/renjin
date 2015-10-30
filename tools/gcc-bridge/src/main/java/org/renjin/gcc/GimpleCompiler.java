package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.renjin.gcc.analysis.AddressableFinder;
import org.renjin.gcc.analysis.FunctionBodyTransformer;
import org.renjin.gcc.analysis.ResultDeclRewriter;
import org.renjin.gcc.analysis.VoidPointerTypeDeducer;
import org.renjin.gcc.codegen.MainClassGenerator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.call.FunctionTable;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.translate.MethodTable;

import java.io.File;
import java.util.List;
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

  private List<File> classPaths = Lists.newArrayList();

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());

  private FunctionTable functionTable;

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();
  
  public GimpleCompiler() {
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
    functionBodyTransformers.add(AddressableFinder.INSTANCE);
    functionBodyTransformers.add(ResultDeclRewriter.INSTANCE);
    functionTable = new FunctionTable();
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
  
  public MethodTable getMethodTable() {
    throw new UnsupportedOperationException();
  }

  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    // First apply any transformations needed by the code generation process
    transform(units);

    // Now emit byte code
    File packageFolder = getPackageFolder();
    packageFolder.mkdirs();

    int recordTypeIndex = 1;
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
        String recordClassName = String.format("%s$record%d", className, recordTypeIndex++);
        System.out.println(recordTypeDef);
        RecordClassGenerator recordType = new RecordClassGenerator(packageName.replace('.', '/') + "/" + recordClassName);
        recordType.emit(recordTypeDef);
        byte[] recordTypeClass = recordType.toByteArray();
        Files.write(recordTypeClass, new File(packageFolder, recordClassName + ".class"));
      }
    }

    MainClassGenerator generator = new MainClassGenerator(functionTable, getInternalClassName());
    generator.emit(units);

    byte[] classFile = generator.toByteArray();

    Files.write(classFile, new File(packageFolder, className + ".class"));
  }

  private String getInternalClassName() {
    return (packageName + "." + className).replace('.', '/');
    
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void addSootClassPaths(List<File> classPaths) {
    this.classPaths.addAll(classPaths);
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

  private File getPackageFolder() {
    return new File(outputDirectory, packageName.replace('.', File.separatorChar));
  }

}
