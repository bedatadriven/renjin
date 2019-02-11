/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.packaging;

import org.json.JSONArray;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.Closure;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class ExecuteMetadataBuilderTest {

  @Test
  public void testMetadata() {
    Session session = SessionBuilder.buildDefault();
    session.getTopLevelContext().evaluate(RParser.parseSource("execute <- function(\n" +
        "input = fileParameter(filter = '*.csv'),\n" +
        "targetProfitMargin = numberParameter(min = 0, max = 1)) { }"));

    SEXP execute = session.getGlobalEnvironment().getVariableUnsafe(Symbol.get("execute"));

    JSONArray metadata = ExecuteMetadataBuilder.composeParameterMetadata( (Closure) execute);

    System.out.println(metadata.toString(4));
  }


}