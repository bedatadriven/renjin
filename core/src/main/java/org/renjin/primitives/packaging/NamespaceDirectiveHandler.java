package org.renjin.primitives.packaging;


import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;

import java.util.List;

public interface NamespaceDirectiveHandler {



  public static class DynlibEntry {
    private String alias;
    private String symbolName;

    public DynlibEntry(String alias, String symbolName) {
      this.alias = alias;
      this.symbolName = symbolName;
    }

    public DynlibEntry(String symbolName) {
      this.symbolName = symbolName;
    }

    public String getAlias() {
      return alias;
    }

    public String getSymbolName() {
      return symbolName;
    }
  }

  void export(List<Symbol> symbols);

  void exportPattern(String pattern);

  void import_(List<String> packageNames);

  void importFrom(String packageName, List<Symbol> symbols);

  void importFromClass(String className, List<Symbol> methods);

  /**
   * Exports an S3 method
   *
   * @param genericName the name of the generic, for example "print" or "summary"
   * @param className the class, e.g. "dist" or "numeric"
   * @param function the implementing function, for example ".print.via.format"
   */
  void S3method(Symbol genericName, String className, Symbol function);

  /**
   * Exports an S3 method genericName.className
   *
   * @param genericName the name of the generic, for example "print" or "summary"
   * @param className  the class, e.g. "dist" or "numeric"
   */
  void S3method(Symbol genericName, String className);


  void useDynlib(String libraryName, List<DynlibEntry> entries, boolean register, String fixes);
}
