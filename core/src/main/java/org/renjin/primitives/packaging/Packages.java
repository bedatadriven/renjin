package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.sexp.*;

import java.io.IOException;

public class Packages {

  public static final FqPackageName METHODS_NAMESPACE = new FqPackageName("org.renjin", "methods");

  @Internal
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      String packageName) throws IOException {


    Namespace namespace = namespaceRegistry.getNamespace(context, packageName);
    
    // Check to see if already on the search path...
    if(isAttached(context, namespace)) {
      return;
    }
    
    // Add "Depends" packages to the global search path
    // (But not "Imports" !)
    for(String dependencyName : namespace.getPackage().getPackageDependencies()) {
      context.getSession().getStdOut().println("Loading required package: " + dependencyName);
      library(context, namespaceRegistry, dependencyName);
    }
    
    // Create the package environment
    Environment packageEnv = context.getGlobalEnvironment().insertAbove(new HashFrame());
    packageEnv.setAttribute(Symbols.NAME,  StringVector.valueOf("package:" + namespace.getFullyQualifiedName().getPackageName()));
    packageEnv.setAttribute(Symbols.FQNAME,  StringVector.valueOf("package:" + namespace.getFullyQualifiedName().toString()));

    // Copy in the namespace's exports
    namespace.copyExportsTo(context, packageEnv);
    
    // Load dataset objects as promises
    for(Dataset dataset : namespace.getPackage().getDatasets()) {
      for(String objectName : dataset.getObjectNames()) {
        packageEnv.setVariable(objectName, new DatasetObjectPromise(dataset, objectName));
      }
    }
    
    if(!namespace.getFullyQualifiedName().equals(METHODS_NAMESPACE)) {
      maybeUpdateS4MetadataCache(context, namespace);
    }
    
    context.setInvisibleFlag();
  }

  private static boolean isAttached(Context context, Namespace namespace) {
    
    String expected = "package:" + namespace.getFullyQualifiedName().toString();
    
    Environment env = context.getGlobalEnvironment();
    while(env != Environment.EMPTY) {
      SEXP fqNameSexp = env.getAttribute(Symbols.FQNAME);
      if(fqNameSexp instanceof StringVector && fqNameSexp.length() == 1) {
        String fqName = ((StringVector) fqNameSexp).getElementAsString(0);
        if(expected.equals(fqName)) {
          return true;
        }
      }
      env = env.getParent();
    }
    return false;
  }

  private static void maybeUpdateS4MetadataCache(Context context, Namespace namespace) {
    //methods:::cacheMetaData(ns, TRUE, ns)
    Optional<Namespace> methods = context.getNamespaceRegistry()
        .getNamespaceIfPresent(Symbol.get("methods"));
    if(methods.isPresent()) {
      SEXP cacheFunction = methods.get().getEntry(Symbol.get("cacheMetaData"));
      FunctionCall cacheCall = FunctionCall.newCall(cacheFunction, 
          namespace.getNamespaceEnvironment(),
          LogicalVector.TRUE,
          namespace.getNamespaceEnvironment());
      
      context.evaluate(cacheCall);
    }
  }

  @Internal
  @Invisible
  public static boolean require(@Current Context context,
                                @Current NamespaceRegistry registry, 
                                String packageName) {
    try {
      library(context, registry, packageName);
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}
