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

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.GroupGeneric;
import org.renjin.invoke.annotations.PreserveAttributeStyle;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.vector.ConvertingDoubleVector;
import org.renjin.primitives.vector.ImaginaryVector;
import org.renjin.sexp.*;


@GroupGeneric("Complex")
public class ComplexGroup {

  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL)
  public static double Mod(Complex z) {
    return z.abs();
  }

  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double Mod(double x) {
    return Math.abs(x);
  }

  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double Arg(Complex z) {
    if(ComplexVector.isNA(z)) {
      return DoubleVector.NA;
    }
    return z.getArgument();
  }
  

  @Builtin
  public static DoubleVector Re(AtomicVector x) {
    if(x instanceof ComplexVector) {
      return new ConvertingDoubleVector(x, x.getAttributes());
      
    } else if(x instanceof DoubleVector || x instanceof IntVector || x instanceof LogicalVector) {
      return DoubleVector.VECTOR_TYPE.to(x);

    } else {
      throw new EvalException("non-numeric argument to function");
    }
  }
  
  @Builtin
  public static DoubleVector Im(AtomicVector x) {
    if(x instanceof ComplexVector) {
      return new ImaginaryVector((ComplexVector) x, x.getAttributes());

    } else if(x instanceof DoubleVector || x instanceof IntVector || x instanceof LogicalVector) {
      if(x.length() < 10) {
        return new DoubleArrayVector(new double[x.length()], x.getAttributes());
      } else {
        return new RepDoubleVector(DoubleVector.valueOf(0), x.length(), 1, x.getAttributes());
      }
      
    } else {
      throw new EvalException("non-numeric argument to function");
    }
  }

  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double Conj(double x) {
    return x;
  }
  
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex Conj(Complex z) {
    return ComplexVector.complex(z.getReal(),-1*z.getImaginary());
  }
}
