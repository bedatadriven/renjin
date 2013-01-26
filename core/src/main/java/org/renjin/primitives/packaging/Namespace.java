package org.renjin.primitives.packaging;

import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class Namespace {
  
  private String name;
  private final NamespaceDef def;
  private final Environment namespaceEnvironment;
  private Package pkg;
    
  public Namespace(Package pkg, String localName, Environment namespaceEnvironment) {
    this.name = localName;
    this.pkg = pkg;
    this.def = pkg.getNamespaceDef();
    this.namespaceEnvironment = namespaceEnvironment;
  }
  
  public String getName() {
    return name;
  }

  public SEXP getEntry(Symbol entry) {
    SEXP value = namespaceEnvironment.getVariable(entry);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Namespace " + name + " has no symbol named '" + entry + "'");
    }
    return value;
  }

  public SEXP getExport(Symbol entry) {
    if(name.equals("base")) {
      return getEntry(entry);
    }
    if(def.getExports().contains(entry)) {
      return this.namespaceEnvironment.getVariable(entry);
    }
    throw new EvalException("Namespace " + name + " has no exported symbol named '" + entry.getPrintName() + "'");
  }

  public Environment getImportsEnvironment() {
    return this.namespaceEnvironment.getParent();
  }
  
  /**
   *
   * @return the private environment for this namespace
   */
  public Environment getNamespaceEnvironment() {
    return this.namespaceEnvironment;
  }

  /**
   * Copies the exported (public) symbols from our namespace environment
   * to the given package environment
   * 
   * @param packageEnv
   */
  public void copyExportsTo(Environment packageEnv) {
    for(Symbol name : def.getExports()) {
      packageEnv.setVariable(name, namespaceEnvironment.getVariable(name));
    }
  }

  /**
   * 
   * @return the namespace definition (from the NAMESPACE file)
   */
  public NamespaceDef getDef() {
    return def;
  }

  public Package getPackage() {
    return pkg;
  }
  
}
