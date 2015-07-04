package org.renjin.primitives;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Implements the identical() function
 */
public class Identical {


  @Internal
  public static boolean identical(SEXP x, SEXP y, boolean numericallyEqual,
      boolean singleNA, boolean attributesAsSet, boolean ignoreByteCode) {
    if (!attributesAsSet) {
      throw new EvalException(
          "identical implementation only supports attrib.as.set = TRUE");
    }

    return identical(x,y, !numericallyEqual, !singleNA);
  }

  public static boolean identical(SEXP x, SEXP y) {
    return identical(x, y, false, false);
  }

  private static boolean identical(SEXP x, SEXP y, boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {
    if(x == y) {
      return true;
    }
    if(x.length() != y.length()) {
      return false;
    }
    if(!x.getTypeName().equals(y.getTypeName())) {
      return false;
    }
    if(x instanceof AtomicVector) {
      return identicalAttributes(x,y ) &&
             identicalElements((AtomicVector)x, (AtomicVector)y, bitwiseComparisonNumbers, bitwiseComparisonNaN);

    } else if(x instanceof ExpressionVector) {
      return identicalAttributes(x, y) &&
             identicalElements((ListVector)x, (ListVector)y, bitwiseComparisonNumbers, bitwiseComparisonNaN);

    } else if(x instanceof ListVector) {
      return identicalAttributes(x, y) &&
          identicalElements((ListVector)x, (ListVector)y, bitwiseComparisonNumbers, bitwiseComparisonNaN);

    } else if(x instanceof FunctionCall) {
      return identicalAttributes(x, y) &&
          identicalElements((PairList)x, (PairList)y);

    } else if(x instanceof PairList.Node) {
      return identicalAttributes(x, y) &&
          identicalElements((PairList)x, (PairList)y);

    } else if(x instanceof S4Object) {
      return identicalAttributes(x, y);

    } else if(x instanceof ExternalPtr) {
      return identicalPointers((ExternalPtr) x, (ExternalPtr) y);

    } else if(x instanceof Symbol || x instanceof Environment || x instanceof Function) {
      return x == y;

    } else {
      throw new UnsupportedOperationException("x = " + x.getClass() + ", y = " + y.getClass());
    }
  }

  private static boolean identicalPointers(ExternalPtr x, ExternalPtr y) {
    return Objects.equal(x, y);
  }

  /**
   * Returns true if all elements in the list are identical
   */
  private static boolean identicalElements(ListVector x, ListVector y,
                                           boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {
    assert x.length() == y.length();

    for(int i=0;i!=x.length();++i) {
      if(!identical(x.getElementAsSEXP(i), y.getElementAsSEXP(i),
              bitwiseComparisonNumbers, bitwiseComparisonNaN)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if all the elements in the AtomicVectors are equal using the criteria provided
   */
  private static boolean identicalElements(AtomicVector x, AtomicVector y,
                                           boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {
    assert x.length() == y.length();

    Vector.Type vectorType = x.getVectorType();
    if(y.getVectorType() != vectorType) {
      return false;
    }
    if(x instanceof DoubleVector) {
      return identicalDoubleElements((DoubleVector) x, (DoubleVector) y, bitwiseComparisonNumbers, bitwiseComparisonNaN);
    } else if(x instanceof LogicalVector || x instanceof IntVector) {
      return identicalIntegerElements(x, y);
    } else if(x instanceof StringVector) {
      return identicalStringElements(x, y);
    } else if(x instanceof ComplexVector) {
      return identicalComplexElements(x, y, bitwiseComparisonNumbers, bitwiseComparisonNaN);
    }

    for(int i=0;i!=x.length();++i) {
      if(x.isElementNA(i) && y.isElementNA(i)) {
        continue;
      }
      if(!vectorType.elementsEqual(x, i, y, i)) {
        return false;
      }
    }
    return true;
  }


  private static boolean identicalStringElements(AtomicVector x, AtomicVector y) {
    assert x.length() == y.length();

    for(int i=0;i!=x.length();++i) {
      String sx = x.getElementAsString(i);
      String sy = y.getElementAsString(i);
      if(!java.util.Objects.equals(sx, sy)) {
        return false;
      }
    }
    return true;

  }

  private static boolean identicalIntegerElements(AtomicVector x, AtomicVector y) {
    assert x.length() == y.length();

    for(int i=0;i!=x.length();++i) {
      if(x.getElementAsInt(i) != y.getElementAsInt(i)) {
        return false;
      }
    }
    return true;
  }

  private static boolean identicalDoubleElements(DoubleVector x, DoubleVector y,
                                                 boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {
    assert x.length() == y.length();

    for(int i=0;i!=x.length();++i) {
      if(!equals(x.getElementAsDouble(i), y.getElementAsDouble(i), bitwiseComparisonNumbers, bitwiseComparisonNaN)) {
        return false;
      }
    }
    return true;
  }

  private static boolean identicalComplexElements(AtomicVector x, AtomicVector y,
                                                  boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {

    assert x.length() == y.length();

    for(int i=0;i!=x.length();++i) {
      Complex xi = x.getElementAsComplex(i);
      Complex yi = y.getElementAsComplex(i);
      if(!equals(xi.getReal(), yi.getReal(), bitwiseComparisonNumbers, bitwiseComparisonNaN) ||
         !equals(xi.getImaginary(), yi.getImaginary(), bitwiseComparisonNumbers, bitwiseComparisonNaN)) {
        return false;
      }
    }
    return true;
  }

  private static boolean identicalElements(PairList x, PairList y) {
    assert x.length() == y.length();

    Iterator<PairList.Node> xi = x.nodes().iterator();
    Iterator<PairList.Node> yi = y.nodes().iterator();
    while(xi.hasNext()) {
      PairList.Node xni = xi.next();
      PairList.Node yni = yi.next();
      if(xni.getRawTag() != yni.getRawTag() ||
          !identical(xni.getValue(), yni.getValue(), false, false)) {
        return false;
      }
    }
    return true;
  }

  private static boolean identicalAttributes(SEXP x, SEXP y) {
    AttributeMap xa = x.getAttributes();
    AttributeMap ya = y.getAttributes();
    if(xa==ya) {
      return true;
    }
    Set<Symbol> xan = Sets.newHashSet(xa.names());
    Set<Symbol> yan = Sets.newHashSet(ya.names());
    if(xan.size() != yan.size()) {
      return false;
    }
    for(Symbol name : xan) {
      if(!identical(xa.get(name), ya.get(name), false, false)) {
        return false;
      }
    }
    return true;
  }

  /**
   *  Not Equal  (x, y)   <==>   x  "!="  y
   *  where the NA/NaN and "-0." / "+0." cases treatment depend on 'str'.
   *
   * @param bitwiseComparisonNumbers  if true, then (x != y) is used when both are not NA or NaN.
   *  If true, will differentiate between '+0.' and '-0.'.
   *
   * @param bitwiseComparisonNaN if true, then
   * @return
   */
  private static boolean equals(double x, double y, boolean bitwiseComparisonNumbers, boolean bitwiseComparisonNaN) {

    if(Double.isNaN(x) || Double.isNaN(y)) {

      if(bitwiseComparisonNaN) {
        return Double.doubleToRawLongBits(x) == Double.doubleToRawLongBits(y);

      } else {
        // only consider the NaN payload in the case of our special NA value
        if(DoubleVector.isNA(x)) {
          return DoubleVector.isNA(y);
        } else if(DoubleVector.isNA(y)) {
          return DoubleVector.isNA(y);
        } else {
          return Double.isNaN(x) && Double.isNaN(y);
        }
      }

    } else {
      // In the case of having both valid numbers, we can either compare bitwise,
      // or by the number

      if(bitwiseComparisonNumbers) {
        return Double.doubleToRawLongBits(x) == Double.doubleToRawLongBits(y);
      } else {
        return x == y;
      }
    }
  }

}
