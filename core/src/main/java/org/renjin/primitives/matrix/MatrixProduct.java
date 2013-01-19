package org.renjin.primitives.matrix;

import org.netlib.blas.BLAS;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


class MatrixProduct {

  // TODO:
  // 1. Code cleanup
  // 2. Complex matrixes implementation
  // 3. dim names (etc)
  
  public static final int PROD = 0;
  public static final int CROSSPROD = 1;
  public static final int TCROSSPROD = 2;

  private AtomicVector x;
  private AtomicVector y;

  private int nrx=0, ncx=0, nry=0, ncy=0;
  private int primop;
  private AtomicVector xdims;
  private AtomicVector ydims;
  private int ldx;
  private int ldy;

  private boolean sym;
  
  private ListVector.Builder dimnames = new ListVector.Builder(2);

  public MatrixProduct(int primop, AtomicVector x, AtomicVector y) {
    super();
    this.x = x;
    this.y = y;

    sym = (y == Null.INSTANCE);
    if (sym && (primop > 0)) {
      this.y = x;
    }

    this.primop = primop;

    computeMatrixDims();
  }


  private void computeMatrixDims() {

    xdims = (AtomicVector)x.getAttribute(Symbols.DIM);
    ydims = (AtomicVector)y.getAttribute(Symbols.DIM);
    ldx = xdims.length();
    ldy = ydims.length();


    if (ldx != 2 && ldy != 2) {         /* x and y non-matrices */
      if (primop == PROD) {
        nrx = 1;
        ncx = x.length();
      }
      else {
        nrx = x.length();
        ncx = 1;
      }
      nry = y.length();
      ncy = 1;
    }
    else if (ldx != 2) {                /* x not a matrix */
      nry = ydims.getElementAsInt(0);
      ncy = ydims.getElementAsInt(1);
      nrx = 0;
      ncx = 0;
      if (primop == 0) {
        if (x.length() == nry) {     /* x as row vector */
          nrx = 1;
          ncx = nry; /* == x.length() */
        }
        else if (nry == 1) {        /* x as col vector */
          nrx = x.length();
          ncx = 1;
        }
      }
      else if (primop == 1) { /* crossprod() */
        if (x.length() == nry) {     /* x is a col vector */
          nrx = nry; /* == x.length() */
          ncx = 1;
        }
        /* else if (nry == 1) ... not being too tolerant
               to treat x as row vector, as t(x) *is* row vector */
      }
      else { /* tcrossprod */
        if (x.length() == ncy) {     /* x as row vector */
          nrx = 1;
          ncx = ncy; /* == x.length() */
        }
        else if (ncy == 1) {        /* x as col vector */
          nrx = x.length();
          ncx = 1;
        }
      }
    }
    else if (ldy != 2) {                /* y not a matrix */
      nrx = xdims.getElementAsInt(0);
      ncx = xdims.getElementAsInt(1);
      nry = 0;
      ncy = 0;
      if (primop == 0) {
        if (y.length() == ncx) {     /* y as col vector */
          nry = ncx;
          ncy = 1;
        }
        else if (ncx == 1) {        /* y as row vector */
          nry = 1;
          ncy = y.length();
        }
      }
      else if (primop == 1) { /* crossprod() */
        if (y.length() == nrx) {     /* y is a col vector */
          nry = nrx;
          ncy = 1;
        }
      }
      else { /* tcrossprod --         y is a col vector */
        nry = y.length();
        ncy = 1;
      }
    }
    else {                              /* x and y matrices */
      nrx = xdims.getElementAsInt(0);
      ncx = xdims.getElementAsInt(1);
      nry = ydims.getElementAsInt(0);
      ncy = ydims.getElementAsInt(1);
    }
    

    if ( ((primop == 0) && (ncx != nry)) ||
         ((primop == 1) && (nrx != nry)) ||
         ((primop == 2) && (ncx != ncy)) ) {
            throw new EvalException("non-conformable arguments");
    }
  }


  public Vector matprod() {

    double ans[] = new double[nrx*ncy];


    //    if (mode == CPLXSXP)
    //        cmatprod(COMPLEX(CAR(args)), nrx, ncx,
    //                 COMPLEX(y), nry, ncy, COMPLEX(ans));
    //    else

    matprod(getXArray(), nrx, ncx,
        getYArray(), nry, ncy, ans);
    
    
    Vector xdimnames = (Vector) x.getAttribute(Symbols.DIMNAMES);
    
    if (xdimnames != Null.INSTANCE) {
        if (ldx == 2 || ncx == 1) {
          dimnames.set(0, xdimnames.getElementAsSEXP(0));
//            dnx = getAttrib(xdims, R_NamesSymbol);
//            if(!isNull(dnx))
//                SET_STRING_ELT(dimnamesnames, 0, STRING_ELT(dnx, 0));
        }
    }

    ydimsEtcetera();

    return makeMatrix(ans, nrx, ncy);
  }

  private DoubleVector makeMatrix(double[] values, int nr, int nc) {
    AttributeMap.Builder attributes = AttributeMap.builder();
    attributes.setDim(nr, nc);
    attributes.set(Symbols.DIMNAMES, buildDimnames());

    return new DoubleArrayVector(values, attributes.build());
  }

  private Vector buildDimnames() {
    ListVector vector = dimnames.build();
    if(vector.getElementAsSEXP(0) != Null.INSTANCE ||
       vector.getElementAsSEXP(1) != Null.INSTANCE) {
      return vector;
    } else {
      return Null.INSTANCE;
    }
  }
  
  public DoubleVector crossprod() {
    double ans[] = new double[ncx * ncy];
    //    if (mode == CPLXSXP)
    //        if(sym)
    //            ccrossprod(COMPLEX(CAR(args)), nrx, ncx,
    //                       COMPLEX(CAR(args)), nry, ncy, COMPLEX(ans));
    //        else
    //            ccrossprod(COMPLEX(CAR(args)), nrx, ncx,
    //                       COMPLEX(y), nry, ncy, COMPLEX(ans));
    //    else {
    if(sym)
      symcrossprod(getXArray(), nrx, ncx, ans);
    else
      crossprod(getXArray(), nrx, ncx,
          getYArray(), nry, ncy, ans);


    //    PROTECT(xdims = getAttrib(CAR(args), R_DimNamesSymbol));
    //    if (sym)
    //        PROTECT(ydims = xdims);
    //    else
    //        PROTECT(ydims = getAttrib(y, R_DimNamesSymbol));
    //
    //    if (xdims != Null.INSTANCE || ydims != Null.INSTANCE) {
    //        SEXP dimnames, dimnamesnames, dnx=Null.INSTANCE, dny=Null.INSTANCE;
    //
    //        /* allocate dimnames and dimnamesnames */
    //
    //        PROTECT(dimnames = allocVector(VECSXP, 2));
    //        PROTECT(dimnamesnames = allocVector(STRSXP, 2));
    //
    //        if (xdims != Null.INSTANCE) {
    //            if (ldx == 2) {/* not nrx==1 : .. fixed, ihaka 2003-09-30 */
    //                SET_VECTOR_ELT(dimnames, 0, VECTOR_ELT(xdims, 1));
    //                dnx = getAttrib(xdims, R_NamesSymbol);
    //                if(!isNull(dnx))
    //                    SET_STRING_ELT(dimnamesnames, 0, STRING_ELT(dnx, 1));
    //            }
    //        }
    //
    //        ydimsEtcetera();
    //    }
    
    return makeMatrix(ans, ncx, ncy);
  }

  private void ydimsEtcetera() {
    
    Vector ydimnames = (Vector) y.getAttribute(Symbols.DIMNAMES);
        if (ydimnames != Null.INSTANCE) {                                 
            if (ldy == 2) { 
              dimnames.set(1, ydimnames.getElementAsSEXP(1));
//                AtomicVector dny = getAttrib(ydims, R_NamesSymbol);             
//                if(dny != Null.INSTANCE)                                   
//                    SET_STRING_ELT(dimnamesnames, 1, STRING_ELT(dny, 1));
            } else if (nry == 1) {                                 
              dimnames.set(1, ydimnames.getElementAsSEXP(0)); 
//                dny = getAttrib(ydims, R_NamesSymbol);             
//                if(!isNull(dny))                                   
//                    SET_STRING_ELT(dimnamesnames, 1, STRING_ELT(dny, 0));
            }                                                      
        }                                                          
//                                                                   
//        /* We sometimes attach a dimnames attribute                
//         * whose elements are all NULL ...                         
//         * This is ugly but causes no real damage.                 
//         * Now (2.1.0 ff), we don't anymore: */                    
//        if (VECTOR_ELT(dimnames,0) != Null.INSTANCE ||                
//            VECTOR_ELT(dimnames,1) != Null.INSTANCE) {                
//            if (dnx != Null.INSTANCE || dny != Null.INSTANCE)            
//                setAttrib(dimnames, R_NamesSymbol, dimnamesnames); 
//            setAttrib(ans, R_DimNamesSymbol, dimnames);            
//        }                      
  }

  public DoubleVector tcrossprod() {
    {                                      /* op == 2: tcrossprod() */

      double[] ans = new double[nrx * nry];
      
      //      if (mode == CPLXSXP)
      //          if(sym)
      //              tccrossprod(COMPLEX(CAR(args)), nrx, ncx,
      //                          COMPLEX(CAR(args)), nry, ncy, COMPLEX(ans));
      //          else
      //              tccrossprod(COMPLEX(CAR(args)), nrx, ncx,
      //                          COMPLEX(y), nry, ncy, COMPLEX(ans));

      if(sym)
        symtcrossprod(getXArray(), nrx, ncx, ans);
      else
        tcrossprod(getXArray(), nrx, ncx,
            getYArray(), nry, ncy, ans);


      return makeMatrix(ans, nrx, nry);
    }

    //      PROTECT(xdims = getAttrib(CAR(args), R_DimNamesSymbol));
    //      if (sym)
    //          PROTECT(ydims = xdims);
    //      else
    //          PROTECT(ydims = getAttrib(y, R_DimNamesSymbol));
    //
    //      if (xdims != Null.INSTANCE || ydims != Null.INSTANCE) {
    //          SEXP dimnames, dimnamesnames, dnx=Null.INSTANCE, dny=Null.INSTANCE;
    //
    //          /* allocate dimnames and dimnamesnames */
    //
    //          PROTECT(dimnames = allocVector(VECSXP, 2));
    //          PROTECT(dimnamesnames = allocVector(STRSXP, 2));
    //
    //          if (xdims != Null.INSTANCE) {
    //              if (ldx == 2) {
    //                  SET_VECTOR_ELT(dimnames, 0, VECTOR_ELT(xdims, 0));
    //                  dnx = getAttrib(xdims, R_NamesSymbol);
    //                  if(!isNull(dnx))
    //                      SET_STRING_ELT(dimnamesnames, 0, STRING_ELT(dnx, 0));
    //              }
    //          }
    //          if (ydims != Null.INSTANCE) {
    //              if (ldy == 2) {
    //                  SET_VECTOR_ELT(dimnames, 1, VECTOR_ELT(ydims, 0));
    //                  dny = getAttrib(ydims, R_NamesSymbol);
    //                  if(!isNull(dny))
    //                      SET_STRING_ELT(dimnamesnames, 1, STRING_ELT(dny, 0));
    //              }
    //          }
    //          if (VECTOR_ELT(dimnames,0) != Null.INSTANCE ||
    //              VECTOR_ELT(dimnames,1) != Null.INSTANCE) {
    //              if (dnx != Null.INSTANCE || dny != Null.INSTANCE)
    //                  setAttrib(dimnames, R_NamesSymbol, dimnamesnames);
    //              setAttrib(ans, R_DimNamesSymbol, dimnames);
    //          }

  }

  private void symcrossprod(double x[], int nr, int nc, double z[]) {
    String trans = "T";
    String uplo = "U";
    double one = 1.0, zero = 0.0;
    int i, j;
    if (nr > 0 && nc > 0) {
      BLAS.getInstance().dsyrk(uplo, trans, nc, nr, one, x, nr, zero, z, nc);  
      for (i = 1; i < nc; i++)
        for (j = 0; j < i; j++) z[i + nc *j] = z[j + nc * i];
    } else { /* zero-extent operations should return zeroes */
      for(i = 0; i < nc*nc; i++) z[i] = 0;
    }
  }

  private double[] getXArray() {
    return x.toDoubleArray();
  }

  private double[] getYArray() {
    return y.toDoubleArray();
  }

  private void matprod(double x[], int nrx, int ncx,
      double y[], int nry, int ncy, double z[])
  {
    String transa = "N";
    String transb = "N";
    int i,  j, k;
    double one = 1.0, zero = 0.0;
    double sum;
    boolean have_na = false;

    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {
      /* Don't trust the BLAS to handle NA/NaNs correctly: PR#4582
       * The test is only O(n) here
       */

      for (i = 0; i < nrx*ncx; i++) {
        if (Double.isNaN(x[i])) {
          have_na = true;
          break;
        }
      }

      if (!have_na) {
        for (i = 0; i < nry*ncy; i++) {
          if (Double.isNaN(y[i])) {
            have_na = true; 
            break;
          }
        }
      }

      if (have_na) {
        for (i = 0; i < nrx; i++) {
          for (k = 0; k < ncy; k++) {
            sum = 0.0;
            for (j = 0; j < ncx; j++) {
              sum +=  x[i + j * nrx] * y[j + k * nry];
            }
            z[i + k * nrx] = sum;
          }
        }
      } else {
        BLAS.getInstance().dgemm(transa, transb, nrx, ncy, ncx, one,
            x, nrx, y, nry, zero, z, nrx);
      }
    } else { /* zero-extent operations should return zeroes */
      for(i = 0; i < nrx*ncy; i++) {
        z[i] = 0;
      }
    }
  }


  private void symtcrossprod(double[] x, int nr, int nc, double[] z)
  {
    String trans = "N";
    String uplo = "U";
    double one = 1.0, zero = 0.0;
    int i, j;
    if (nr > 0 && nc > 0) {
      BLAS.getInstance().dsyrk(uplo, trans, nr, nc, one, x, nr, zero, z, nr);
      for (i = 1; i < nr; i++) {
        for (j = 0; j < i; j++) {
          z[i + nr *j] = z[j + nr * i];
        }
      }
    } else { /* zero-extent operations should return zeroes */
      for(i = 0; i < nr*nr; i++) {
        z[i] = 0;
      }
    }
  }


  private void tcrossprod(double x[], int nrx, int ncx,
      double y[], int nry, int ncy, double z[])
  {
    String transa = "N";
    String transb = "T";
    double one = 1.0, zero = 0.0;
    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {
      BLAS.getInstance().dgemm(transa, transb, nrx, nry, ncx, one,
          x, nrx, y, nry, zero, z, nrx);
    } else { /* zero-extent operations should return zeroes */
      int i;
      for(i = 0; i < nrx*nry; i++) {
        z[i] = 0;
      }
    }
  }


  private void crossprod(double x[], int nrx, int ncx,
      double y[], int nry, int ncy, double z[])
  {
    String transa = "T";
    String transb = "N";
    double one = 1.0, zero = 0.0;
    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {
      BLAS.getInstance().dgemm(transa, transb, ncx, ncy, nrx, one,
          x, nrx, y, nry, zero, z, ncx);
    } else { /* zero-extent operations should return zeroes */
      int i;
      for(i = 0; i < ncx*ncy; i++) {
        z[i] = 0;
      }
    }
  }
}
