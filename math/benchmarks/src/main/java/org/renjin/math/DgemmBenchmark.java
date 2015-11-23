package org.renjin.math;

import org.netlib.blas.Dgemm;
import org.openjdk.jmh.annotations.*;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Matrix multiplication benchmark
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DgemmBenchmark {

  @Param({"16", "32", "64" /* "128", "1000", "10000" */})
  public int m;
  
  private double[] a;
  private double[] b;
  
  @Setup
  public void setupMatrix() {

    a = randomArray(m * m);
    b = randomArray(m * m);
  }

  private double[] randomArray(int n) {
    assert n > 0;
    Random random = new Random();
    double[] array = new double[n];
    for (int i = 0; i < n; i++) {
      array[i] = random.nextGaussian();
    }
    return array;
  }

  @Benchmark
  public double[] netlib() {
    double[] c = new double[m * m];
    dgemm("N", "N", m, m, m, 1, a, m, b, m, 0, c, m);
    return c;
  }

  public void dgemm(String arg1, String arg2, int arg3, int arg4, int arg5, double arg6, double[] arg7, int arg9, double[] arg10, int arg12, double arg13, double[] arg14, int arg16) {
    Dgemm.dgemm(arg1, arg2, arg3, arg4, arg5, arg6, arg7, 0, arg9, arg10, 0, arg12, arg13, arg14, 0, arg16);
  }

  @Benchmark
  public double[] gccBridge() {
    double[] c = new double[m * m];
    IntPtr mPtr = new IntPtr(m);
    // TRANSA,TRANSB,M,N,K,ALPHA,A,LDA,B,LDB,BETA,C,LDC)
    org.renjin.math.Blas.dgemm_(
        /* TRANSA */ BytePtr.asciiString("N"), 
        /* TRANSB */ BytePtr.asciiString("N"),
        /* M */ mPtr,
        /* N */ mPtr,
        /* K */ mPtr,
        /* ALPHA */ new DoublePtr(1),
        /* A */ new DoublePtr(a),
        /* LDA */ mPtr,
        /* B */ new DoublePtr(b),
        /* LDB */ mPtr,
        /* BETA */ new DoublePtr(0),
        /* C */ new DoublePtr(c),
        /* LDC */ mPtr,
        /* LEN(TRANSA) */ 1,
        /* LEN(TRANSB) */ 1 );
    
    return c;
  }

}
