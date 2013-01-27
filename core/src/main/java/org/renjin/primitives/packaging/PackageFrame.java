package org.renjin.primitives.packaging;

import java.io.IOException;
import java.util.Set;

import org.renjin.eval.EvalException;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Sets;

/**
 * Specialized backing frame for package environments
 * that supports lazy loading of package datasets
 *
 */
public class PackageFrame extends HashFrame {

  private Package pkg;
  private Set<Symbol> datasets = Sets.newHashSet();
  
  public PackageFrame(Package pkg) {
    this.pkg = pkg;
    for(String dataset : pkg.getDatasets()) {
      System.out.println("inserting dataset: " + dataset);
      Symbol name = Symbol.get(dataset);
      setVariable(name, Symbol.UNBOUND_VALUE);
      datasets.add(name);
    }
  }
  
  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = super.getVariable(name);
    if(value == Symbol.UNBOUND_VALUE && datasets.contains(name)) {
      System.out.println("lazy loading dataset" + name);
      try {
        value = pkg.loadDataset(name.getPrintName());
      } catch (IOException e) {
        throw new EvalException(e);
      }
      setVariable(name, value);
    }
    return value;
  }
}
