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

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;

public class DtorCallGenerator implements CallGenerator {

  /**
   * Constructors and destructors basically works the same way.
   * Source:
   * http://d3s.mff.cuni.cz/software/gmc/download/Thesis-JanSebetovsky-C++Support.pdf#chapter.4
   * __comp_dtor should call a "complete destructor" if it exists. Calls "base destructor" otherwise.
   * Source:
   * http://d3s.mff.cuni.cz/software/gmc/download/Thesis-JanSebetovsky-C++Support.pdf#section.4.1
   */
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    GimpleFunctionRef baseConstructor = new GimpleFunctionRef();
    baseConstructor.setName("__base_dtor ");
    GimpleAddressOf functionExpr = new GimpleAddressOf();
    functionExpr.setValue(baseConstructor);
    CallGenerator baseCtorCallGenerator = exprFactory.findCallGenerator(functionExpr);
    baseCtorCallGenerator.emitCall(mv, exprFactory, call);
  }
}