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
package org.renjin.invoke.codegen;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;

public class IfElseBuilder {

  private final JBlock block;
  private JConditional conditional;

  public IfElseBuilder(JBlock block) {
    this.block = block;
  }

  public JBlock _if(JExpression expr) {
    if(conditional == null) {
      conditional = block._if(expr);
    } else {
      conditional = conditional._elseif(expr);
    }
    return conditional._then();
  }

  public JBlock _else() {
    if(conditional == null) {
      return block;
    } else {
      return conditional._else();
    }
  }
}
