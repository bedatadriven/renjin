/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import r.lang.primitive.FunctionTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
  private Map<String, SymbolExp> table = new HashMap<String, SymbolExp>();

  private SymbolExp bracket2Symbol;
  private SymbolExp bracketSymbol;
  private SymbolExp braceSymbol;
  private SymbolExp classSymbol;
  private SymbolExp deviceSymbol;
  private SymbolExp levelsSymbol;
  private SymbolExp dimnamesSymbol;
  private SymbolExp dimSymbol;
  private SymbolExp dollarSymbol;
  private SymbolExp dotsSymbol;
  private SymbolExp modeSymbol;
  private SymbolExp nameSymbol;
  private SymbolExp packageSymbol;
  private SymbolExp namesSymbol;
  private SymbolExp seedsSymbol;
  private SymbolExp sourceSymbol;
  private SymbolExp naRmSymbol;
  private SymbolExp tspSymbol;
  private SymbolExp lastValueSymbol;
  private SymbolExp dropSymbol;
  private SymbolExp rowNamesSymbol;
  private SymbolExp quoteSymbol;

  /**
   * Creates the Symbol Table and fills it with
   * builtin functions
   */
  public SymbolTable() {
    setupShortcuts();
    installFunctionTable();
    installPlatform();
  }

  /**
   * install - probe the symbol table
   * If "name" is not found, it is installed in the symbol table.
   * The symbol corresponding to the string "name" is returned.
   */
  public SymbolExp install(String name) {
    if (table.containsKey(name)) {
      return table.get(name);
    } else {
      SymbolExp sexp = new SymbolExp(name);
      table.put(name, sexp);
      return sexp;
    }
  }

  private void setupShortcuts() {
    bracket2Symbol = install("[[");
    bracketSymbol = install("[");
    braceSymbol = install("{");
    classSymbol = install("class");
    deviceSymbol = install(".Device");
    dimnamesSymbol = install("dimnames");
    dimSymbol = install("dim");
    dollarSymbol = install("$");
    dotsSymbol = install("...");
    dropSymbol = install("drop");
    lastValueSymbol = install(".Last.value");
    levelsSymbol = install("levels");
    modeSymbol = install("mode");
    nameSymbol = install("name");
    namesSymbol = install("names");
    naRmSymbol = install("na.rm");
    packageSymbol = install("package");
    quoteSymbol = install("quote");
    rowNamesSymbol = install("row.names");
    seedsSymbol = install(".Random.seed");
    sourceSymbol = install("source");
    tspSymbol = install("tsp");
  }


  public List<String> getBoundSymbolNames() {
    List<String> names = new ArrayList<String>();
    for(SymbolExp symbol : table.values()) {
      if(symbol.getValue() != SymbolExp.UNBOUND_VALUE) {
        names.add(symbol.getPrintName());
      }
    }
    return names;
  }


  private void installFunctionTable() {
    for (FunctionTable.Entry entry : FunctionTable.ENTRIES) {
      SymbolExp symbol = install(entry.name);
      PrimitiveExp primitive;

      if (entry.eval % 10 != 0) {
        primitive = new BuiltinExp(entry);
      } else {
        primitive = new SpecialExp(entry);
      }

      if ((entry.eval % 100) / 10 != 0) {
        symbol.setInternal(primitive);
      } else {
        symbol.setValue(primitive);
      }
    }
  }

  private void installPlatform() {
    SymbolExp platform = install(".Platform");
    platform.setValue( PairListExp.buildList()
        .add(new StringExp(resolveOsName())).taggedWith(install("OS.type"))
        .add(new StringExp("/")).taggedWith(install("file.sep"))
        .add(new StringExp("unknown")).taggedWith(install("GUI"))
        .add(new StringExp("big")).taggedWith(install("endian"))
        .add(new StringExp("source")).taggedWith(install("pkgType"))
        .add(new StringExp("")).taggedWith(install("r_arch")).list() );
  }

  private String resolveOsName() {
    return System.getProperty("os.name").contains("windows") ? "windows" : "unix";
  }


  /**
   * "[["
   */
  private SymbolExp getBracket2Symbol() {
    return bracket2Symbol;
  }

  /**
   * "["
   */
  public SymbolExp getBracketSymbol() {
    return bracketSymbol;
  }

  /**
   * "{"
   */
  public SymbolExp getBraceSymbol() {
    return braceSymbol;
  }

  /**
   * "class"
   */
  public SymbolExp getClassSymbol() {
    return classSymbol;
  }

  /**
   * ".Device"
   */
  public SymbolExp getDeviceSymbol() {
    return deviceSymbol;
  }

  /**
   * "dimnames"
   */
  public SymbolExp getDimNamesSymbol() {
    return dimnamesSymbol;
  }


  /**
   * "dim"
   */
  public SymbolExp getDimSymbol() {
    return dimSymbol;
  }

  /**
   * "$"
   */
  public SymbolExp getDollarSymbol() {
    return dollarSymbol;
  }

  /**
   * "..."
   */
  public SymbolExp getDotsSymbol() {
    return dotsSymbol;
  }

  /**
   * "drop"
   */
  public SymbolExp getDropSymbol() {
    return dropSymbol;
  }

  /**
   * ".Last.value"
   */
  public SymbolExp getLastValueSymbol() {
    return lastValueSymbol;
  }

  /**
   * "levels"
   */
  public SymbolExp getLevelsSymbol() {
    return levelsSymbol;
  }

  /**
   * "mode"
   */
  public SymbolExp getModeSymbol() {
    return modeSymbol;
  }

  /**
   * "name"
   */
  public SymbolExp getNameSymbol() {
    return nameSymbol;
  }

  /**
   * "names"
   */
  public SymbolExp getNamesSymbol() {
    return namesSymbol;
  }

  /**
   * "na.rm"
   */
  public SymbolExp getNaRmSymbol() {
    return naRmSymbol;
  }

  /**
   * "package"
   */
  public SymbolExp getPackageSymbol() {
    return packageSymbol;
  }

  /**
   * "quote"
   */
  public SymbolExp getQuoteSymbol() {
    return quoteSymbol;
  }

  /**
   * "row.names"
   */
  public SymbolExp getRowNamesSymbol() {
    return rowNamesSymbol;
  }

  /**
   * ".Random.seed"
   */
  public SymbolExp getSeedsSymbol() {
    return seedsSymbol;
  }

  /**
   * "source"
   */
  public SymbolExp getSourceSymbol() {
    return sourceSymbol;
  }

  /**
   * "tsp"
   */
  public SymbolExp getTspSymbol() {
    return tspSymbol;
  }

}
