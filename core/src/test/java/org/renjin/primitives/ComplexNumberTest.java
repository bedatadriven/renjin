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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;

import static org.junit.Assert.assertThat;


public class ComplexNumberTest extends EvalTestCase {
  
  @Test
  public void imaginaryPartOfConvertedDoubleShouldBeZero() throws IOException {
    assertThat( eval("Im(as.complex(1))"), elementsIdenticalTo(c(0)) );
  }
  
  @Test
  public void realPartOfConvertedDoubleShouldMatch() throws IOException{
    assertThat(eval("Re(as.complex(1))"), elementsIdenticalTo(c(1)));
    assertThat(eval("Re(as.complex(2))"), elementsIdenticalTo(c(2)));
  }
  
  @Test
  public void vectorizedReal() throws IOException{
    eval("sqrt(c(1,4,9))");
    assertThat(eval("Re(c(as.complex(1),as.complex(2)))[1]"), elementsIdenticalTo(c(1)));
    assertThat(eval("Re(c(as.complex(1),as.complex(2)))[2]"), elementsIdenticalTo(c(2)));
  }
  
  @Test
  public void sizeAt0_1() throws IOException{

    //    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(0,1))"), elementsIdenticalTo(c(1)));
    assertThat(eval("Mod(complex(0,9))"), elementsIdenticalTo(c(9)));
  }
  
  @Test
  public void argumentAt0_1() throws IOException{

//    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Arg(complex(real=0, i=1))/pi"), elementsIdenticalTo(c(0.5)));
  }
  
  @Test
  public void polarCoordinatesAt1_0() throws IOException{

    //    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(real=1,i=0))"), elementsIdenticalTo(c(1)));
    assertThat(eval("Mod(complex(real=9,i=0))"), elementsIdenticalTo(c(9)));
  }
  
  @Test
  public void complexConjugate() throws IOException{

    assertThat(eval("Im(Conj(complex(real=0,i=1)))"), elementsIdenticalTo(c(-1)));
  }
}
