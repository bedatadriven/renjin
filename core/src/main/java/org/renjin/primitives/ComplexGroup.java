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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double Mod(Complex z) {
    return z.abs();
  }

  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double Mod(double x) {
    return Math.abs(x);
  }

  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double Arg(Complex z) {
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double Conj(double x) {
    return x;
  }
  
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex Conj(Complex z) {
    return ComplexVector.complex(z.getReal(),-1*z.getImaginary());
  }
}
