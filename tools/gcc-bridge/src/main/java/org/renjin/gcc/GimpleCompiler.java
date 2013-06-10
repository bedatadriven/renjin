package org.renjin.gcc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.translate.FunctionTranslator;
import org.renjin.gcc.translate.MethodTable;
import org.renjin.gcc.translate.TranslationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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

  public void setPackageName(String name) {
    this.packageName = name;
  }

  public void setJimpleOutputDirectory(File directory) {
    this.jimpleOutputDirectory = directory;
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

    JimpleOutput output = translate(units);

    output.write(jimpleOutputDirectory);

    compileJimple(output.getClassNames());
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

  private void compileJimple(Set<String> classNames) throws IOException, InterruptedException {
    List<String> options = Lists.newArrayList();
    if (verbose) {
      options.add("-v");
    }
    options.add("-pp");
    options.add("-cp");
    options.add(sootClassPath());
    options.add("-src-prec");
    options.add("jimple");
    options.add("-keep-line-number");
    options.add("-output-dir");
    options.add(outputDirectory.getAbsolutePath());
    options.addAll(classNames);

    LOGGER.info("Running Soot " + Joiner.on(" ").join(options));

    soot.G.reset();
    soot.Main.main(options.toArray(new String[0]));
  }

  private String sootClassPath() {
    StringBuilder paths = new StringBuilder();
    paths.append(jimpleOutputDirectory.getAbsolutePath());
    for (File path : classPaths) {
      paths.append(File.pathSeparatorChar);
      paths.append(path.getAbsolutePath());
    }
    return paths.toString();
  }

  protected JimpleOutput translate(List<GimpleCompilationUnit> units) throws IOException {

    JimpleOutput jimple = new JimpleOutput();

    JimpleClassBuilder mainClass = jimple.newClass();
    mainClass.setClassName(className);
    mainClass.setPackageName(packageName);

    TranslationContext context = new TranslationContext(mainClass, methodTable, units);
    for(GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        FunctionTranslator translator = new FunctionTranslator(context);
        translator.translate(function);
      }
    }

    return jimple;
  }

  private File getPackageFolder() {
    return new File(outputDirectory, packageName.replace('.', File.separatorChar));
  }

}
