/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
