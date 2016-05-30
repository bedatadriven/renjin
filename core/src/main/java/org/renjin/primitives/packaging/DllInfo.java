package org.renjin.primitives.packaging;

import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

/**
 * 
 */
public class DllInfo {
  private final String libraryName;
  private final List<DllSymbol> symbols = Lists.newArrayList();


  public DllInfo(String libraryName) {
    this.libraryName = libraryName;
  }

  public String getLibraryName() {
    return libraryName;
  }

  public void addSymbol(DllSymbol symbol) {
    symbols.add(symbol);
  }
  
  public List<DllSymbol> getSymbols() {
    return symbols;
  }
}
