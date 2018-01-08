/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.studio.console;


import org.renjin.sexp.Environment;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;

public class SymbolCompletion implements NameCompletion {

  private final Environment global;

  public SymbolCompletion(Environment global) {
    this.global = global;
  }

  @Override
  public String[] completeName(String part) {
    ArrayList<String> list = new ArrayList<String>();
    for(Environment env : global.parents()) {
      for(Symbol name : env.getSymbolNames()) {
        if(name.getPrintName().startsWith(part)) {
          list.add(name.getPrintName());
        }
      }
    }
    return list.toArray(new String[list.size()]);
  }
}
