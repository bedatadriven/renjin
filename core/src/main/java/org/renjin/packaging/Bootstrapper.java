package org.renjin.packaging;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.renjin.RVersion;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Bootstraps the packaging of the default R packages
 *
 */
public class Bootstrapper {

  private File srcRoot = new File("src/library");
  private File destRoot = new File("target/classes/org/renjin/library");
  
  public Bootstrapper() throws IOException, ScriptException {

    try {
      
      serializeBaseEnv();
      
    } catch(Exception e) {
      System.out.println("R language package build failed, expect subsequent test failures...");
      e.printStackTrace();
    }
    
  }
  
  private void copyProfile() throws IOException {
    File profileScript = file(destRoot, "base", "R", "Rprofile");
    Files.copy(file(srcRoot, "profile", "Common.R"), profileScript);
    Files.append(
        Files.toString(
             file(srcRoot, "profile", "Renjin.R"), Charsets.UTF_8),
         profileScript, Charsets.UTF_8);
  } 
  
  private void installPackageSources(String packageName) throws IOException {
    List<File> sources = PackagingUtils.findSourceFiles(
        file(srcRoot, packageName));
    
    file(destRoot, packageName).mkdirs();
    
    copyDescription(packageName);
    
    File namespaceFile = file(srcRoot, packageName, "NAMESPACE");
    if(namespaceFile.exists()) {
      Files.copy(namespaceFile, file(destRoot, packageName, "NAMESPACE"));
    }
    
    PackagingUtils.concatSources(sources, destRoot, packageName);
  }

  protected void copyDescription(String packageName) throws IOException {
    File src = file(srcRoot, packageName, "DESCRIPTION");
    File dest = file(destRoot, packageName, "DESCRIPTION");
    Files.copy(src, dest);
    Files.append(String.format("Build: R %s; ; %s; unix", RVersion.STRING, new Date().toString()),
        dest, Charsets.UTF_8);
  }
  
  private void createBasePackageDatabase() throws IOException, ScriptException {
    evalWithoutDefaultPackages(file(srcRoot, "base", "makebasedb.R"));
    
    Files.copy(file(srcRoot, "base", "baseloader.R"), 
          file(destRoot, "base", "R", "base"));
  }

  private void evalWithoutDefaultPackages(File file) throws IOException,
      ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.withOptions().withNoDefaultPackages().get();
    engine.eval(file);
  }

  private void eval(String source) throws ScriptException, IOException {
    //System.out.println(source);
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.withOptions().withNoDefaultPackages().get();
    engine.eval(source); 
    engine.printWarnings();
  }
  
  private void bootstrapTools() throws IOException, ScriptException {
    System.out.println("Building tools package...");

    installPackageSources("tools");
    eval(String.format("tools:::.install_package_description('%s', '%s')",
        escape(file(srcRoot, "tools").getAbsolutePath()),
        escape(file(destRoot, "tools").getAbsolutePath())));
    
    String lazyLoadScript = Files.toString(file(srcRoot, "tools", "R", "makeLazyLoad.R"), Charsets.UTF_8);
    lazyLoadScript += "\n" + "makeLazyLoading('tools')\n";
    
    eval(lazyLoadScript);
  }


  private String escape(String s) {
    return s.replace("\\", "\\\\");
  }

  private void buildPackage(String packageName) throws IOException, ScriptException {
    System.out.println(String.format("Building package '%s'...", packageName));

    installPackageSources(packageName);
    eval(
      String.format("tools:::.install_package_description('%s', '%s')\n" +
                    "tools:::makeLazyLoading('%s')\n",
        escape(file(srcRoot, packageName).getAbsolutePath()),
        escape(file(destRoot, packageName).getAbsolutePath()),
        packageName));

    try {     
      NativeSourcesCompiler compiler = new NativeSourcesCompiler();
      compiler.setPackageName(packageName);
      compiler.addSources(file(srcRoot, packageName, "src"));
      compiler.setVerbose(false);
      compiler.compile();
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println("WARNING: failed to compile native sources for " + packageName + ", some functions may be missing...");
    }
  }
  
  private void buildMethodsPackage() throws IOException, ScriptException {
    // the methods package has a special one-time loading step
    // that writes out the lazy loaded db on completion
    String packageName = "methods";
    System.out.println(String.format("Building package 'methods'...", packageName));

    installPackageSources(packageName);
    eval(
      String.format("tools:::.install_package_description('%s', '%s')\n",
        escape(file(srcRoot, packageName).getAbsolutePath()),
        escape(file(destRoot, packageName).getAbsolutePath()),
        packageName));

    eval("library(methods)");
    
    // copy the namespace loader 
    File loaderScript = file(destRoot,  packageName, "R", packageName);
    if(!loaderScript.delete()) {
      throw new IOException("Could not remove " +  loaderScript.getAbsolutePath());
    }
    Files.copy(new File("src/main/resources/org/renjin/share/R/nspackloader.R"), loaderScript);
  }
  
  private static File file(File parent, String... children) {
    File file = parent;
    for(String child : children) {
      file = new File(file, child);
    }
    return file;
  }

  public static void main(String[] args) throws IOException, ScriptException {
      new Bootstrapper();
  }
 
  public void serializeBaseEnv() throws IOException {
    
  // Evaluate the basesources into the base namespace environment
  
    Context context = Context.newTopLevelContext();
    Environment baseNamespaceEnv = context.getNamespaceRegistry().getBase().getNamespaceEnvironment();
    Context evalContext = context.beginEvalContext(baseNamespaceEnv);
    
    File baseSourceRoot = new File("src/main/R/base");
    List<File> baseSources = Lists.newArrayList();
    for(File sourceFile : baseSourceRoot.listFiles()) {
      if(sourceFile.getName().endsWith(".R")) {
        baseSources.add(sourceFile);
      }
    }
    Collections.sort(baseSources);
    
    for(File baseSource : baseSources) {
      FileReader reader = new FileReader(baseSource);
      SEXP expr = RParser.parseAllSource(reader);
      reader.close();
      evalContext.evaluate(expr);
    }
    
    // now serialize them to a lazy-loadable frame
    
    final List<String> omit = Lists.newArrayList(
        ".Last.value", ".AutoloadEnv", ".BaseNamespaceEnv", 
        ".Device", ".Devices", ".Machine", ".Options", ".Platform");
    
    new LazyLoadFrameBuilder(context)
    .outputTo(new File("target/classes/org/renjin/baseNamespace"))
    .filter(new Predicate<NamedValue>() {
      public boolean apply(NamedValue namedValue) {
        if(omit.contains(namedValue.getName())) {
          return false;
        }
        if(namedValue.getValue() instanceof PrimitiveFunction) {
          return false;
        }
        return true;
      }
    })
    .build(baseNamespaceEnv);
  

      
  }
  
}
