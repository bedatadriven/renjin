package org.renjin.primitives.packaging;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.S3;
import org.renjin.primitives.annotations.SessionScoped;
import org.renjin.primitives.packaging.NamespaceDef.S3Export;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Session-level registry of namespaces
 *
 */
@SessionScoped
public class NamespaceRegistry {

  private static final Symbol BASE = Symbol.get("base");
  
  private PackageLoader loader;
	private Map<Symbol, Namespace> namespaces = Maps.newIdentityHashMap();
	private Map<Environment, Namespace> envirMap = Maps.newIdentityHashMap();
	
  private Context context;
	
	public NamespaceRegistry(PackageLoader loader, Context context, Environment baseNamespaceEnv) {
	  this.loader = loader;
	  this.context = context;
	  Namespace baseNamespace = new BaseNamespace(baseNamespaceEnv);
    namespaces.put(BASE, baseNamespace);
	  envirMap.put(baseNamespaceEnv, baseNamespace);
	}

	public Namespace getBaseNamespace() {
	  return namespaces.get(BASE);
	}
	
	public Environment getBaseNamespaceEnv() {
	  return getBaseNamespace().getNamespaceEnvironment();
	}
	
	public Namespace getNamespace(Environment envir) {
	  Namespace ns = envirMap.get(envir);
	  if(ns == null) {
	    throw new IllegalArgumentException();
	  }
	  return ns;
	}
	
	public Iterable<Symbol> getLoadedNamespaces() {
	  return namespaces.keySet();
	}
	
  public Namespace getNamespace(Symbol name) {
	  Namespace namespace = namespaces.get(name);
	  if(namespace == null) {
	    try {
        namespace = load(name);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
	    namespaces.put(name, namespace);
	  }
	  return namespace;
	}

  public Namespace getNamespace(String name) {
    return getNamespace(Symbol.get(name));
  }
  
  private Namespace load(Symbol name) throws IOException {
    
    Package pkg = loader.load(name.getPrintName());
    
    // load the serialized functions/values from the classpath
    // and add them to our private namespace environment
    Namespace namespace = createNamespace(pkg, name.getPrintName());
    for(NamedValue value : pkg.loadSymbols(context)) {
      namespace.getNamespaceEnvironment().setVariable(Symbol.get(value.getName()), value.getValue());
    }
    
    // import foreign symbols
    Environment importsEnv = namespace.getNamespaceEnvironment().getParent();
    setupImports(pkg.getNamespaceDef(), importsEnv);

    Set<Symbol> groups = Sets.newHashSet(Symbol.get("Ops"), Symbol.get("Math"), Symbol.get("Summary"));
    
    // we need to export S3 methods to the namespaces to which
    // the generic functions were defined
    for(S3Export export : namespace.getDef().getS3Exports()) {
      SEXP methodExp =  namespace.getNamespaceEnvironment().getVariable(export.getMethod());
      if(methodExp == Symbol.UNBOUND_VALUE) {
        throw new EvalException("Missing export: " + export.getMethod() + " from namespace " + name);
      }
      if(!(methodExp instanceof Function)) {
        throw new IllegalStateException(export.getMethod() + ": expected function but was " + methodExp.getClass().getName());
      }
      Function method = (Function) methodExp;
      Environment definitionEnv;
      if(groups.contains(export.getGenericFunction())) {
        definitionEnv = getBaseNamespaceEnv();
      } else {
        SEXP genericFunction = namespace.getNamespaceEnvironment().findFunction(context, export.getGenericFunction());
        if(genericFunction == null) {
          System.err.println("Cannot find S3 method definition '" + export.getGenericFunction() + "'");
          for(Symbol sym : namespace.getNamespaceEnvironment().getParent().getSymbolNames()) {
            System.err.println("imported: " + sym);
          }
          throw new EvalException("Cannot find S3 method definition '" + export.getGenericFunction() + "'");
        }
        definitionEnv = getDefinitionEnv( genericFunction );
      }
      if(!definitionEnv.hasVariable(S3.METHODS_TABLE)) {
        definitionEnv.setVariable(S3.METHODS_TABLE, Environment.createChildEnvironment(context.getBaseEnvironment()));
      }
      Environment methodsTable = (Environment) definitionEnv.getVariable(S3.METHODS_TABLE);
      methodsTable.setVariable(export.getMethod(), method);
    }

    if(namespace.getNamespaceEnvironment().hasVariable(Symbol.get(".onLoad"))) {
      StringVector nameArgument = StringVector.valueOf(name.getPrintName());
      context.evaluate(FunctionCall.newCall(Symbol.get(".onLoad"), nameArgument, nameArgument), 
          namespace.getNamespaceEnvironment());
    }
    return namespace;
  }

  private void setupImports(NamespaceDef namespaceDef, Environment importsEnv) {
    for(NamespaceDef.NamespaceImport importDef : namespaceDef.getImports() ) {
      Namespace importedNamespace = getNamespace(importDef.getNamespace());
      if(importDef.isImportAll()) {
        importedNamespace.copyExportsTo(importsEnv);
      } else {
        for(Symbol importedSymbol : importDef.getSymbols()) {
          SEXP importedExp = importedNamespace.getExport(importedSymbol);
          importsEnv.setVariable(importedSymbol, importedExp);
        }
      }
    }
  }
  
  private Environment getDefinitionEnv(SEXP genericFunction) {
    if(genericFunction instanceof Closure) {
      return ((Closure) genericFunction).getEnclosingEnvironment();
    } else if(genericFunction instanceof PrimitiveFunction) {
      return getBaseNamespaceEnv();
    } else {
      throw new IllegalArgumentException(genericFunction.getClass().getName());
    }
  }

  public boolean isRegistered(Symbol name) {
    return namespaces.containsKey(name);
  }
  
  public Namespace getBase() {
    return namespaces.get(BASE);
  }

  /**
   * Creates a new empty namespace 
   * @param namespaceDef 
   * @param namespaceName
   * @return
   */
  public Namespace createNamespace(Package pkg, String localName) {
    // each namespace has environment which is the leaf in a hierarchy that
    // looks like this:
    // BASE-NS -> IMPORTS -> ENVIRONMENT
    
    Environment imports = Environment.createNamedEnvironment(getBaseNamespaceEnv(), "imports:" + localName);
    setupImports(pkg.getNamespaceDef(), imports);
    
    Environment namespaceEnv = Environment.createNamedEnvironment(imports, "namespace:" + localName);
    Namespace namespace = new Namespace(pkg, localName, namespaceEnv);
    namespaces.put(Symbol.get(localName), namespace);
    envirMap.put(namespaceEnv, namespace);
    
    // save the name to the environment
    namespaceEnv.setVariable(".packageName", StringVector.valueOf(localName));
    return namespace;
  }

  public boolean isNamespaceEnv(Environment envir) {
    return envirMap.containsKey(envir);
  }
}
