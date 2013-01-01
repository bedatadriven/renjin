package org.renjin.primitives.packaging;

import java.io.IOException;
import java.util.Map;

import org.renjin.eval.Context;
import org.renjin.primitives.annotations.SessionScoped;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Maps;

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
	  Namespace baseNamespace = new Namespace(new NamespaceDef(), "base", baseNamespaceEnv);
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
    
    Namespace namespace = createNamespace(pkg.getNamespaceDef(), name.getPrintName());
    for(NamedValue value : pkg.loadSymbols(context)) {
      namespace.getNamespaceEnvironment().setVariable(Symbol.get(value.getName()), value.getValue());
    }
    
    return namespace;
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
  public Namespace createNamespace(NamespaceDef namespaceDef, String namespaceName) {
    // each namespace has environment which is the leaf in a hierarchy that
    // looks like this:
    // BASE-NS -> IMPORTS -> ENVIRONMENT
    
    Environment imports = Environment.createNamedEnvironment(getBaseNamespaceEnv(), "imports:" + namespaceName);
    Environment namespaceEnv = Environment.createNamedEnvironment(imports, "namespace:" + namespaceName);
    Namespace namespace = new Namespace(namespaceDef, namespaceName, namespaceEnv);
    namespaces.put(Symbol.get(namespaceName), namespace);
    envirMap.put(namespaceEnv, namespace);
    return namespace;
  }

  public boolean isNamespaceEnv(Environment envir) {
    return envirMap.containsKey(envir);
  }
}
