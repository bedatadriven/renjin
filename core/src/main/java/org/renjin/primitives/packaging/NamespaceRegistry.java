package org.renjin.primitives.packaging;

import java.util.Map;

import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.SessionScoped;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Maps;

/**
 * Session-level registry of namespaces
 *
 */
@SessionScoped
public class NamespaceRegistry {

  private static final Symbol BASE = Symbol.get("base");
  
	private Map<Symbol, Namespace> namespaces = Maps.newIdentityHashMap();
	
	public NamespaceRegistry(Environment baseNamespaceEnv) {
	  namespaces.put(BASE, new Namespace(baseNamespaceEnv));
	}

  public Namespace getNamespace(Symbol name) {
	  Namespace namespace = namespaces.get(name);
	  if(namespace == null) {
	    throw new EvalException("Cannot find namespace '" + name + "'");
	  }
	  return namespace;
	}

  public Namespace getNamespace(String name) {
    return getNamespace(Symbol.get(name));
  }
  
  public boolean isRegistered(Symbol name) {
    return namespaces.containsKey(name);
  }

  public Namespace getBase() {
    return namespaces.get(BASE);
  }
}
