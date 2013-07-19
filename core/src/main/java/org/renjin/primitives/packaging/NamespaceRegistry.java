package org.renjin.primitives.packaging;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.SessionScoped;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
        if(namespace == null) {
          throw new EvalException("Could not find package " + name);
        }
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
    if(pkg == null) {
      throw new EvalException(name + " could not be found");
    }
    
    // load the serialized functions/values from the classpath
    // and add them to our private namespace environment
    Namespace namespace = createNamespace(pkg, name.getPrintName());

    // set up the namespace
    populateNamespace(pkg, namespace);

    // setup namespace from NAMESPACE file
    setupImportsExports(pkg, namespace);

    // invoke the .onLoad hook
    if(namespace.getNamespaceEnvironment().hasVariable(Symbol.get(".onLoad"))) {
      StringVector nameArgument = StringVector.valueOf(name.getPrintName());
      context.evaluate(FunctionCall.newCall(Symbol.get(".onLoad"), nameArgument, nameArgument), 
          namespace.getNamespaceEnvironment());
    }
    return namespace;
  }

  /**
   * Populates the namespace from the R-language functions and expressions defined
   * in this namespace.
   *
   */
  private void populateNamespace(Package pkg, Namespace namespace) throws IOException {
    for(NamedValue value : pkg.loadSymbols(context)) {
      namespace.getNamespaceEnvironment().setVariable(Symbol.get(value.getName()), value.getValue());
    }
  }

  /**
   * Sets up imports and exports defined in the NAMESPACE file.
   *
   */
  private void setupImportsExports(Package pkg, Namespace namespace) throws IOException {

    InputSupplier<InputStream> namespaceFile = pkg.getResource("NAMESPACE");
    NamespaceDirectiveParser.parse(
        CharStreams.newReaderSupplier(namespaceFile, Charsets.UTF_8),
        new NamespaceInitHandler(context, this, namespace));
  }


  public boolean isRegistered(Symbol name) {
    return namespaces.containsKey(name);
  }
  
  public Namespace getBase() {
    return namespaces.get(BASE);
  }

  /**
   * Creates a new empty namespace
   */
  public Namespace createNamespace(Package pkg, String localName) {
    // each namespace has environment which is the leaf in a hierarchy that
    // looks like this:
    // BASE-NS -> IMPORTS -> ENVIRONMENT

    Environment imports = Environment.createNamedEnvironment(getBaseNamespaceEnv(), "imports:" + localName);

    Environment namespaceEnv = Environment.createNamedEnvironment(imports, localName);
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
