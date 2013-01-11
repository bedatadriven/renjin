package org.renjin.maven.namespace;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrameBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.packaging.NamespaceDef;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;

public class NamespaceBuilder {

  private String namespaceName;
  private File sourceDirectory;
  private File environmentFile;

  public void build(String namespaceName, File sourceDirectory,
      File environmentFile) {
   
    this.namespaceName = namespaceName;
    this.sourceDirectory = sourceDirectory;
    this.environmentFile = environmentFile;
    
    compileNamespaceEnvironment();
  }


  private void compileNamespaceEnvironment() {
    List<File> sources = getRSources();
    if(isUpToDate(sources)) {
      return;
    }
    
    Context context = initContext();
  
    Namespace namespace = context.getNamespaceRegistry().createNamespace(new NamespaceDef(), namespaceName);
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
      System.out.println("namespaceEnvironment is up to date, skipping compiliation");
      return true;
    }
    
    return false;
  }

  

  private Context initContext()  {
    try {
      Context context = Context.newTopLevelContext();
      context.init();
      return context;
    } catch(IOException e) {
      throw new RuntimeException("Could not initialize R top level context", e);
    }
  }

  private List<File> getRSources() {
    List<File> list = Lists.newArrayList();
    if(sourceDirectory.listFiles() != null) {
      list.addAll(Arrays.asList(sourceDirectory.listFiles()));
    }
    Collections.sort(list);
    return list;
  }


  private void evaluateSources(Context context, List<File> sources, Environment namespaceEnvironment) {
    for(File sourceFile : sources) {
      if(sourceFile.getName().toUpperCase().endsWith(".R")) {
        System.err.println("Evaluating '" + sourceFile + "'");
        try {
          FileReader reader = new FileReader(sourceFile);
          SEXP expr = RParser.parseAllSource(reader);
          reader.close();
          
          context.evaluate(expr, namespaceEnvironment);
          
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
      builder.outputTo(environmentFile);
      builder.build(namespaceEnv);
    } catch(IOException e) {
      throw new RuntimeException("Exception encountered serializing namespace environment", e);
    }
  }

}
