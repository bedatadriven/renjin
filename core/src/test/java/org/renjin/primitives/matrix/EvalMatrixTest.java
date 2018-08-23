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
package org.renjin.primitives.matrix;

import org.junit.Test;
import org.renjin.EvalTestCase;

public class EvalMatrixTest extends EvalTestCase {

  @Test
  public void bigMatrixAllocTest() {
    eval("matrix(nrow=10^8, ncol=1)");
  }

  @Test
  public void matrixSubscriptTest() {
    eval("df <- data.frame(a=1:10000,b=1:10000)");
    System.out.println("now");
    eval("mm <- as.matrix(df)");
    eval("rs <- mm[,1]");

  }

  @Test
  public void vecAssignTest() {
    eval("df <- matrix(1.0,nrow=10000,ncol=10)");
    eval("df[,3] <- 1:10000");
  }
}
