package org.renjin.primitives.packaging;

import com.google.common.collect.Lists;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;

public class Namespace {

  List<Symbol> exports = Lists.newArrayList();
  private final Environment namespaceEnvironment;
  private Package pkg;

  public Namespace(Package pkg, Environment namespaceEnvironment) {
    this.pkg = pkg;
    this.namespaceEnvironment = namespaceEnvironment;
  }
  
  public String getName() {
    return pkg.getName().getPackageName();
  }

  public FqPackageName getFullyQualifiedName() {
    return pkg.getName();
  }

  public SEXP getEntry(Symbol entry) {
    SEXP value = namespaceEnvironment.getVariable(entry);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Namespace " + pkg.getName() + " has no symbol named '" + entry + "'");
    }
    return value;
  }

  public SEXP getExport(Symbol entry) {
    // the base package's namespace is treated specially for historical reasons:
    // all symbols are considered to be exported.
    if(FqPackageName.BASE.equals(pkg.getName())) {
      return getEntry(entry);
    }
    if(exports.contains(entry)) {
      return this.namespaceEnvironment.getVariable(entry);
    }
    throw new EvalException("Namespace " + pkg.getName() + " has no exported symbol named '" + entry.getPrintName() + "'");
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
    for(Symbol name : exports) {
      packageEnv.setVariable(name, namespaceEnvironment.getVariable(name));
    }
  }

  public void addExport(Symbol export) {
    exports.add(export);
  }

  public Package getPackage() {
    return pkg;
  }
  
}
