package org.renjin.math.blas;

import org.junit.Test;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

/**
 * Verifies that the compiled BLAS methods actually run
 */
public class BlasTest {

  @Test
  public void testDcabs() {
    org.renjin.math.Blas.dcabs1_(new DoublePtr(new double[4], 0));
  }

  @Test
  public void testDgemm() {
    int m = 2;
    double a[] = new double[] { 1, 2, 3, 4};
    double b[] = new double[] { 5, 6, 7, 8};
    double c[] = new double[m*m];
    
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

  }
}
