package org.renjin.primitives;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import org.apache.commons.math.complex.Complex;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Vector;

public class FFT {

  @Primitive
  public static ComplexVector fft(IntVector x, boolean inverse) {
    return realFFT(x, inverse);
  }
  
  @Primitive
  public static ComplexVector fft(DoubleVector x, boolean inverse) {
    return realFFT(x, inverse);
  }

  @Primitive
  public static ComplexVector fft(ComplexVector x, boolean inverse) {
    DoubleFFT_1D fft = new DoubleFFT_1D(x.length());
    
    double array[] = new double[x.length() * 2];
    for(int i=0;i!=x.length();++i) {
      array[i*2] = x.getElementAsComplex(i).getReal();
      array[i*2+1] = x.getElementAsComplex(i).getImaginary();
    }
    if(inverse) {
      fft.complexInverse(array, false);
    } else {
      fft.complexForward(array);
    }
    return toComplex(array);
  }
  
  private static ComplexVector realFFT(Vector x, boolean inverse) {
    
    DoubleFFT_1D fft = new DoubleFFT_1D(x.length());
    
    double array[] = new double[x.length() * 2];
    for(int i=0;i!=x.length();++i) {
      array[i] = x.getElementAsDouble(i);
    }
    
    if(inverse) {
      fft.realInverse(array, false);
    } else {
      fft.realForwardFull(array);
    }
    return toComplex(array);
  }

  private static ComplexVector toComplex(double[] array) {
    int n = array.length / 2;
    ComplexVector.Builder result = new ComplexVector.Builder(0, n);
    for(int i=0;i!=n;++i) {
      result.add(new Complex(array[i*2], array[i*2+1]));
    }
    return result.build();
  }
    
  private static boolean isPowerOfTwo(int n) {
    return ((n!=0) && (n&(n-1))==0);
  }
  
}
