package r.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

import r.jvmi.wrapper.WrapperGenerator;
import r.lang.Closure;
import r.lang.Context;
import r.lang.Environment;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.CharStreams;

public class PackageCompiler {

  private static final Logger LOGGER = Logger.getLogger(PackageCompiler.class.getName());
  private File rootDir;
  private File outDir;
  private String packageName = "org/renjin/base";
  private Environment packageEnvironment;
  private File packageDir;
  
  public PackageCompiler(File rootDir) throws IOException {
    this.rootDir = rootDir;
  }
  
  public void setOutputDir(File outputDir) {
    this.outDir = outputDir;
  }

  public void compile() throws FileNotFoundException, IOException {

    this.packageDir = new File(outDir, packageName);
    packageDir.mkdirs();

    evaluatePackageBody();
    compileClosures();    
    compileLoader(); 
  }

  private void compileClosures() throws FileNotFoundException, IOException {
    for(Symbol name : packageEnvironment.getSymbolNames()) {
      SEXP value = packageEnvironment.getVariable(name);
      if(value instanceof Closure) {
        try { 
          compileClosure(WrapperGenerator.toJavaName(name.getPrintName()), (Closure)value);
        } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "Error compiling closure '" + name + "'", e);
          throw new RuntimeException("Error compiling closure '" + name + "'", e);
        }
      }
    }
  }

  private void compileClosure(String name, Closure closure)
      throws FileNotFoundException, IOException {
   
    File classFile = new File(packageDir, name + ".class");
    FileOutputStream fos = new FileOutputStream(classFile);
    fos.write(ClosureCompiler.compile(packageName + "/" + name, closure));
    fos.close();
  }
  
  private void compileLoader() throws IOException {
    File classFile = new File(packageDir, "Loader");
    FileOutputStream fos = new FileOutputStream(classFile);
    fos.write(PackageLoaderCompiler.compile(packageName, packageEnvironment));
    fos.close();
  }
  
  private void evaluatePackageBody() throws FileNotFoundException,
      IOException {

    Context context = Context.newTopLevelContext();
    
    List<File> srcFiles = findSourceFiles();

    for(File srcFile : srcFiles) {
      LOGGER.fine("Evaluating " + srcFile.getName());
      Reader reader = new InputStreamReader(new FileInputStream(srcFile));
      SEXP srcBody = RParser.parseSource(CharStreams.toString(reader) + "\n");
      reader.close();
      context.evaluate(srcBody);
    }
    this.packageEnvironment = context.getEnvironment();
  }

  private List<File> findSourceFiles() {
    List<File> srcFiles = Lists.newArrayList();
    for(File file : this.rootDir.listFiles()) {
      if(file.getName().toLowerCase().endsWith(".r")) {
        srcFiles.add(file);
      }
    }
    
    Collections.sort(srcFiles, Ordering.natural().onResultOf(new Function<File, Comparable>() {
      @Override
      public Comparable apply(File input) {
        return input.getName();
      }
    }));
   
    return srcFiles;
  }
  
  public static void main(String[] args) throws IOException {
    PackageCompiler compiler = new PackageCompiler(new File(args[0]));
    compiler.setOutputDir(new File(args[1]));
    compiler.compile();
  }
  
}
