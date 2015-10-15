package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.renjin.gcc.analysis.FunctionBodyTransformer;
import org.renjin.gcc.analysis.VoidPointerTypeDeducer;
import org.renjin.gcc.codegen.ClassGenerator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.translate.FunctionTranslator;
import org.renjin.gcc.translate.MethodTable;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.type.struct.ImRecordType;

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

  private File jimpleOutputDirectory;

  private File outputDirectory;
  
  private String packageName;

  private String className;

  private boolean verbose;

  private List<File> classPaths = Lists.newArrayList();

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());

  private MethodTable methodTable = new MethodTable();

  private Map<String, ImRecordType> providedTypes = Maps.newHashMap();

  private List<FunctionBodyTransformer> functionBodyTransformers = Lists.newArrayList();
  

  public GimpleCompiler() {
    functionBodyTransformers.add(VoidPointerTypeDeducer.INSTANCE);
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

  public MethodTable getMethodTable() {
    return methodTable;
  }

  public void compile(List<GimpleCompilationUnit> units) throws Exception {

    File packageFolder = getPackageFolder();
    packageFolder.mkdirs();

    ClassGenerator generator = new ClassGenerator(getInternalClassName());
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

  public void provideType(String typeName, ImRecordType type) {
    providedTypes.put(typeName, type);
  }

  protected JimpleOutput translate(List<GimpleCompilationUnit> units) throws IOException {

    JimpleOutput jimple = new JimpleOutput();

    JimpleClassBuilder mainClass = jimple.newClass();
    mainClass.setClassName(className);
    mainClass.setPackageName(packageName);

    TranslationContext context = new TranslationContext(mainClass, methodTable, providedTypes, units);
    for(GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        
        transformFunctionBody(unit, function);
        
        FunctionTranslator translator = new FunctionTranslator(context);
        translator.translate(function);
      }
    }
    return jimple;
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

  public void setJimpleOutputDirectory(File tempDir) {
    // NOOP
  }
}
