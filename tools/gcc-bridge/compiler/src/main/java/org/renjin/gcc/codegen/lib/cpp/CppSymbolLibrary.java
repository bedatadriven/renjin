/*
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
package org.renjin.gcc.codegen.lib.cpp;

import org.renjin.gcc.codegen.call.FreeCallGenerator;
import org.renjin.gcc.codegen.call.MallocCallGenerator;
import org.renjin.gcc.codegen.lib.SymbolFunction;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.SymbolMethod;
import org.renjin.gcc.codegen.type.TypeOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CppSymbolLibrary implements SymbolLibrary {

  @Override
  public List<SymbolFunction> getFunctions(TypeOracle typeOracle) {
    List<SymbolFunction> functions = new ArrayList<SymbolFunction>();
    functions.add(new SymbolFunction("_Znwj", new MallocCallGenerator(typeOracle)));
    functions.add(new SymbolFunction("operator delete", new FreeCallGenerator()));
    functions.add(new SymbolFunction("__comp_ctor ", new CtorCallGenerator())); // complete constructor
    functions.add(new SymbolFunction("__comp_dtor ", new DtorCallGenerator())); // complete destructor
    return functions;
  }

  @Override
  public List<SymbolMethod> getMethods() {
    return Collections.emptyList();
  }
}