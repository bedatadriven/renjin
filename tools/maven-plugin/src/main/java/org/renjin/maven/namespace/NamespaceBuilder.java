package org.renjin.maven.namespace;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.SessionBuilder;
import org.renjin.maven.PackageDescription;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

import com.google.common.collect.Lists;

public class NamespaceBuilder {

  private FqPackageName name;
  private File sourceDirectory;
  private File environmentFile;
  private List<String> defaultPackages;

  public void build(String groupId, String namespaceName, File sourceDirectory,
      File environmentFile, List<String> defaultPackages) throws IOException, MojoExecutionException {

    this.name = new FqPackageName(groupId, namespaceName);
    this.sourceDirectory = sourceDirectory;
    this.environmentFile = environmentFile;
    this.defaultPackages = defaultPackages;

    compileNamespaceEnvironment();
  }


  private void compileNamespaceEnvironment() throws MojoExecutionException {
    List<File> sources = getRSources();
    if(isUpToDate(sources)) {
      return;
    }
    
    Context context = initContext();

    Namespace namespace = context.getNamespaceRegistry().createNamespace(new InitializingPackage(name));
    evaluateSources(context, getRSources(), namespace.getNamespaceEnvironment());
    serializeEnvironment(context, namespace.getNamespaceEnvironment(), environmentFile);
  }

  private boolean isUpToDate(List<File> sources) {
    long lastModified = 0;
    for(File source : sources) {
      if(source.lastModified() > lastModified) {
        lastModified = source.lastModified();
      }
    }
    
    if(lastModified < environmentFile.lastModified()) {
      System.out.println("namespaceEnvironment is up to date, skipping compilation");
      return true;
    }
    
    return false;
  }

  private Context initContext()  {
    SessionBuilder builder = new SessionBuilder();
    Context context = builder.build().getTopLevelContext();
    if(defaultPackages != null) {
      for(String name : defaultPackages) {
        context.evaluate(FunctionCall.newCall(Symbol.get("library"), StringVector.valueOf(name)));
      }
    }
    return context;
  }

  private List<File> getRSources() {
    List<File> list = Lists.newArrayList();
    if(sourceDirectory.listFiles() != null) {
      list.addAll(Arrays.asList(sourceDirectory.listFiles()));
    }
    Collections.sort(list);
    return list;
  }


  private void evaluateSources(Context context, List<File> sources, Environment namespaceEnvironment) throws MojoExecutionException {
    for(File sourceFile : sources) {
      String nameUpper = sourceFile.getName().toUpperCase();
      if(nameUpper.endsWith(".R") ||
         nameUpper.endsWith(".S") ||
         nameUpper.endsWith(".Q")) {
        System.err.println("Evaluating '" + sourceFile + "'");
        try {
          FileReader reader = new FileReader(sourceFile);
          SEXP expr = RParser.parseAllSource(reader);
          reader.close();

          context.evaluate(expr, namespaceEnvironment);
        
        } catch (EvalException e) {
          System.out.println("ERROR: " + e.getMessage());
          e.printRStackTrace(System.out);
          throw new MojoExecutionException("Error evaluating package source: " + sourceFile.getName(), e);
        } catch (Exception e) {
          throw new RuntimeException("Exception evaluating " + sourceFile.getName(), e);
        }
      }
    }
    
    // some packages (methods) have a routine to do initialization 
    // before serializing the environment
    if(namespaceEnvironment.hasVariable(Symbol.get("..First.lib"))) {
      PairList.Builder args = new PairList.Builder();
      args.add("where", namespaceEnvironment);
      
      context.evaluate(new FunctionCall(Symbol.get("..First.lib"), args.build()), namespaceEnvironment);
    }
  }
  
  private void serializeEnvironment(Context context, Environment namespaceEnv, File environmentFile) {
    
    System.out.println("Writing namespace environment to " + environmentFile);
    try {
      LazyLoadFrameBuilder builder = new LazyLoadFrameBuilder(context);
      builder.outputTo(environmentFile.getParentFile());
      builder.build(namespaceEnv);
    } catch(IOException e) {
      throw new RuntimeException("Exception encountered serializing namespace environment", e);
    }
  }

}

