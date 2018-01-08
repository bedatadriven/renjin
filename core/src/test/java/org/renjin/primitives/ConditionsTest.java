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
package org.renjin.primitives;


import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.junit.Assert.assertThat;

public class ConditionsTest extends EvalTestCase {

  @Test
  public void catchStop() {

    assertThat(eval("tryCatch(stop('foo'), error = function(...) 41)"), elementsIdenticalTo(c(41)));
    assertThat(eval("tryCatch(nonExistantVar + 1, error = function(...) 42)"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void supressWarnings() {

    
    eval("x <- suppressWarnings({ warning('foo'); 42 })");
   
    assertThat(eval("x"), elementsIdenticalTo(c(42)));
  }
}
