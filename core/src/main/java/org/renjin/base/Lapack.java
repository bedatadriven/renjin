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
package org.renjin.base;


import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;
import org.apache.commons.math.complex.Complex;
import org.netlib.util.doubleW;
import org.netlib.util.intW;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.ComplexGroup;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * R Bindings for the LAPACK library, used
 * by the base package. 
 */
public class Lapack {

  public static final BytePtr UPPER_TRIANGLE = BytePtr.asciiString("U");

  /**
   * Invert a symmetric, positive definite square matrix from its Choleski decomposition, using
   * the {@code dpotri} routine.
   *
   * @param a
   * @param sz the number of columns of x containing the Choleski decomposition.
   * @return
   */
  @Internal
  public static DoubleVector La_chol2inv(DoubleVector a, int sz) {

    if(IntVector.isNA(sz) || sz < 1) {
      throw new EvalException("'size' argument must be a positive integer");
    }

    int m = 1, n = 1;

    if (sz == 1 && !Types.isMatrix(a)) {
      /* nothing to do; m = n = 1; ... */
      return a;

    } else if (Types.isMatrix(a)) {
      Vector adims = a.getAttributes().getDim();
      m = adims.getElementAsInt(0);
      n = adims.getElementAsInt(1);
    } else {
      throw new EvalException("'a' must be a numeric matrix");
    }

    if (sz > n) {
      throw new EvalException("'size' cannot exceed ncol(x) = %d", n);
    }
    if (sz > m) {
      throw new EvalException("'size' cannot exceed nrow(x) = %d", m);
    }

    double result[] = new double[sz * sz];
    for (int j = 0; j < sz; j++) {
      for (int i = 0; i <= j; i++) {
        result[i + j * sz] = a.getElementAsDouble(i + j * m);
      }
    }

    intW resultCode = new intW(0);
    LAPACK.getInstance().dpotri("Upper", sz, result, sz, resultCode);

    if (resultCode.val != 0) {
      if (resultCode.val > 0) {
        throw new EvalException("element (%d, %d) is zero, so the inverse cannot be computed",
            resultCode.val, resultCode.val);
      } else {
        throw new EvalException("argument %d of Lapack routine %s had invalid value", -resultCode.val, "dpotri");
      }
    }

    for (int j = 0; j < sz; j++) {
      for (int i = j+1; i < sz; i++) {
        result[i + j * sz] = result[j + i * sz];
      }
    }

    return DoubleArrayVector.unsafe(result, AttributeMap.builder().setDim(sz, sz));
  }


  /**
   * Computes the singular value decomposition (SVD) of an n-columns by m-row array as the product of 
   * orthogonal and diagonal arrays.
   */
  public static ListVector svd(String jobu, String jobv, DoubleVector x, DoubleVector  sexp,
                               DoubleVector uexp, DoubleVector vexp, String method ) {

    Vector xdims = x.getAttributes().getDim();
    int n = xdims.getElementAsInt(0);
    int p = xdims.getElementAsInt(1);

    double xvals[] = x.toDoubleArray();

    int ldu = uexp.getAttributes().getDim().getElementAsInt(0);
    int ldvt = vexp.getAttributes().getDim().getElementAsInt(0);

    double s[] = sexp.toDoubleArray();
    double u[] = uexp.toDoubleArray();
    double v[] = vexp.toDoubleArray();
    double tmp[] = new double[1];

    int iwork[] = new int[8*(n<p ? n : p)];

    LAPACK lapack = LAPACK.getInstance();
   
    /* ask for optimal size of work array */
    int lwork = -1;
    intW info = new intW(0);
    lapack.dgesdd(jobu, n, p, xvals, n,  s, u, ldu, v, ldvt, tmp, lwork, iwork, info);

    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info.val, "dgesdd");
    }

    lwork = (int) tmp[0];
    double work[] = new double[lwork];

    lapack.dgesdd(jobu, n, p, xvals, n, s, u, ldu, v, ldvt, work, lwork, iwork, info);

    return ListVector.newNamedBuilder()
        .add("d", DoubleArrayVector.unsafe(s, sexp.getAttributes()))
        .add("u", DoubleArrayVector.unsafe(u, uexp.getAttributes()))
        .add("vt", DoubleArrayVector.unsafe(v, vexp.getAttributes()))
        .build();
  }

  /**
   * Computes the solution to a real system of linear equations
   *     A * X = B,
   *  where A is an N-by-N matrix and X and B are N-by-NRHS matrices. 
   *
   * The LU decomposition with partial pivoting and row interchanges is
   * used to factor A as
   * A = P * L * U,
   * where P is a permutation matrix, L is unit lower triangular, and U is
   * upper triangular.  The factored form of A is then used to solve the 
   * system of equations A * X = B.
   */
  public static SEXP dgesv(DoubleVector A, DoubleVector B, double tolerance) {
    double anorm;
    doubleW rcond = new doubleW(0);

    if (!Types.isMatrix(A)) {
      throw new EvalException("'a' must be a numeric matrix");
    }
    if (!Types.isMatrix(B)) {
      throw new EvalException("'b' must be a numeric matrix");
    }

    Vector Adims = A.getAttributes().getDim();
    Vector Bdims = B.getAttributes().getDim();

    int n = Adims.getElementAsInt(0);
    if (n == 0) {
      throw new EvalException("'a' is 0-diml");
    }
    int p = Bdims.getElementAsInt(1);
    if (p == 0) {
      throw new EvalException("no right-hand side in 'b'");
    }
    if (Adims.getElementAsInt(1) != n) {
      throw new EvalException("'a' (" + n + " x " + Adims.getElementAsInt(1) + ") must be square");
    }
    if (Bdims.getElementAsInt(0) != n) {
      throw new EvalException("'b' (" + Bdims.getElementAsInt(0) + " x " + p + ") must be compatible with 'a' (" + n + " x " + n + ")");
    }

    int ipiv[] = new int[n];
    double avals[] = A.toDoubleArray();

    LAPACK lapack = LAPACK.getInstance();
    intW info = new intW(0);

    double[] result = B.toDoubleArray();

    lapack.dgesv(n, p, avals, n, ipiv, result, n, info);

    if (info.val < 0) {
      throw new EvalException("argument -" + info.val + " of Lapack routine 'dgsv' had invalid value");
    }
    if (info.val > 0) {
      throw new EvalException("Lapack routine dgesv: system is exactly singular");
    }

    anorm = lapack.dlange("1", n, n, A.toDoubleArray(), n, null);

    double[] arrWork = new double[4 * n];
    lapack.dgecon("1", n, avals, n, anorm, rcond, arrWork, ipiv, info);

    if (rcond.val < tolerance) {
      throw new EvalException("system is computationally singular: reciprocal condition number = " + rcond.val);
    }
    return DoubleArrayVector.unsafe(result, B.getAttributes());
  }


  /**
   * Ca
   * @param x
   * @param ov
   * @return
   */
  public static SEXP rs(DoubleVector x, boolean ov) {
//      int *xdims, n, lwork, info = 0, ov;
//      char jobv[1], uplo[1], range[1];
//      SEXP values, ret, nm, x, z = R_NilValue;
//      double *work, *rx, *rvalues, tmp, *rz = NULL;
//      int liwork, *iwork, itmp, m;

    double vl = 0.0, vu = 0.0, abstol = 0.0;
      /* valgrind seems to think vu should be set, but it is documented
         not to be used if range='a' */

    int il=0, iu=0;

    String uplo = "L";
    int n = getSquareMatrixSize(x);

    double rx[] = x.toDoubleArray();
    double rvalues[] = new double[n];

    String range = "A";
    double rz[] = null;
    if (!ov) {
      rz = new double[n*n];
    }

    String jobv = ov ? "N" : "V";


    int isuppz[] = new int[2*n];
      /* ask for optimal size of work arrays */

    int lwork = -1;
    int liwork = -1;
    intW m = new intW(0);
    int itmp[] = new int[1];

    double tmp[] = new double[1];

    LAPACK lapack = LAPACK.getInstance();
    intW info = new intW(0);
    lapack.dsyevr(jobv, range, uplo, n, rx, n,
        vl, vu, il, iu, abstol, m, rvalues,
        rz, n, isuppz,
        tmp, lwork, itmp, liwork, info);
    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info, "dsyevr");
    }

    lwork = (int) tmp[0];
    liwork = itmp[0];

    double work[] =  new double[lwork];
    int iwork[] = new int[liwork];

    lapack.dsyevr(jobv, range, uplo, n, rx, n,
        vl, vu, il, iu, abstol, m, rvalues,
        rz, n, isuppz,
        work, lwork, iwork, liwork, info);
    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info, "dsyevr");
    }

    ListVector.NamedBuilder ret = ListVector.newNamedBuilder();
    ret.add("values", new DoubleArrayVector(rvalues));
    if (!ov) {
      ret.add("vectors", DoubleArrayVector.newMatrix(rz, n, n));
    }
    return ret.build();
  }

  /**
   * Compute the Choleski factorization of a real symmetric
   * positive-definite square matrix.
   *
   * @param context the current evaluation context
   * @param a a square, real-valued matrix
   * @param pivot should pivoting be used?
   * @param tol a numeric tolerance for use with 'pivot = TRUE'
   */
  @Internal
  public static SEXP La_chol(@Current Context context, SEXP a, int pivot, double tol) {

    if (!Types.isMatrix(a)) {
      throw new EvalException("'a' must be a numeric matrix");
    }

    double matrix[] = ((AtomicVector) a).toDoubleArray();
    int[] dim = a.getAttributes().getDimArray();

    int m = dim[0];
    int n = dim[1];

    if (m != n) {
      throw new EvalException("'a' must be a square matrix");
    }
    if (m <= 0) {
      throw new EvalException("'a' must have dims > 0");
    }

    /* zero the lower triangle */
    int N = dim[0];
    for (int j = 0; j < n; j++) {
      for (int i = j + 1; i < n; i++) {
        matrix[i + N * j] = 0.;
      }
    }
    if (pivot != 0 && pivot != 1) {
      throw new EvalException("invalid 'pivot' value");
    }
    if (pivot == 0) {
      intW info = new intW(0);
      LAPACK.getInstance().dpotrf("Upper", m, matrix, m, info);
      if (info.val != 0) {
        if (info.val > 0) {
          throw new EvalException("the leading minor of order %d is not positive definite", info);
        } else {
          throw new EvalException("argument %d of Lapack routine %s had invalid value", info, "dpotrf");
        }
      }
      return DoubleArrayVector.unsafe(matrix, a.getAttributes());

    } else {

      int pivoti[] = new int[m];
      double work[] = new double[m * 2];
      IntPtr rank = new IntPtr(0);
      IntPtr info = new IntPtr(0);
      org.renjin.math.Lapack.dpstrf_(
          /* UPLO = */ UPPER_TRIANGLE,        // (in) Specifies whether the upper or lower triangular part of th
          /* N = */    new IntPtr(m),         // (in) The order of the matrix A.  N >= 0.
          /* A = */    new DoublePtr(matrix), // (in, out) The symmetric matrix A
          /* LDA = */  new IntPtr(m),         // (in) The leading dimension of the array A. LDA >= max(1,N)
          /* PIV = */  new IntPtr(pivoti),    // (out) PIV is such that the nonzero entries are P( PIV(K), K ) = 1
          /* RANK = */ rank,                  // (out) rank of A given by the number of steps the algorithm compl.
          /* TOL = */  new DoublePtr(tol),    // (in) User defined tolerance.
          /* WORK = */ new DoublePtr(work),   // (out) Work space. dimension (2*N)
          /* INFO = */ info,                  // (out)
          /* LEN(UPLO) = */ 1);

      if (info.get() != 0) {
        if (info.get() > 0) {
          context.warn("the matrix is either rank-deficient or indefinite");
        } else {
          throw new EvalException("argument %d of Lapack routine %s had invalid value", -info.get(), "dpstrf");
        }
      }

      return DoubleArrayVector.unsafe(matrix,
          a.getAttributes().copy()
              .set("pivot", IntArrayVector.unsafe(pivoti))
              .set("rank", IntArrayVector.unsafe(rank.array))
              .setDimNames(pivotColumnNames(a.getAttributes().getDimNames(), pivoti))
              .build());
    }
  }

  private static Vector pivotColumnNames(Vector dimNames, int[] pivoti) {
    if(dimNames == Null.INSTANCE) {
      return Null.INSTANCE;
    }
    Vector colNames = dimNames.getElementAsSEXP(1);
    if(colNames == Null.INSTANCE) {
      return dimNames;
    }
    ListVector dimNamesList = (ListVector) dimNames;
    StringVector.Builder pivotedCallNames = new StringVector.Builder(colNames.length());
    for (int i = 0; i < pivoti.length; i++) {
      pivotedCallNames.set(i, colNames.getElementAsString(pivoti[i] - 1));
    }

    return dimNamesList.newCopyBuilder()
        .set(1, pivotedCallNames.build())
        .build();
  }


  /**
   * Solves a triangular system of linear equations.

   * @param r an upper (or lower) triangular matrix giving the coefficients for the system to be solved.
   *          Values below (above) the diagonal are ignored.
   * @param b a matrix whose columns give the right-hand sides for the equations.
   * @param k The number of columns of ‘r’ and rows of ‘x’ to use.
   * @param upper if true, the upper triangle part of {@code r} is used. Otherwise, the lower one.
   * @param trans if ‘TRUE’, solve r' * y = x for y, i.e., ‘t(r) %*% y == x’.
   */
  @Internal
  public static SEXP backsolve(AtomicVector r, AtomicVector b, int k, boolean upper, boolean trans) {


    int nrr = r.getAttributes().getDimArray()[0];
    int nrb = b.getAttributes().getDimArray()[0];
    int ncb = b.getAttributes().getDimArray()[1];

    /* k is the number of rows to be used: there must be at least that
       many rows and cols in the rhs and at least that many rows on
       the rhs.
    */
    if (IntVector.isNA(k) || k <= 0 || k > nrr || k > r.getAttributes().getDimArray()[1] || k > nrb) {
      throw new EvalException("invalid k argument");
    }

    double rr[] = r.toDoubleArray();

    /* check for zeros on diagonal of r: only k row/cols are used. */
    int incr = nrr + 1;
    for(int i = 0; i < k; i++) { /* check for zeros on diagonal */
      if (rr[i * incr] == 0.0) {
        throw new EvalException("singular matrix in 'backsolve'. First zero in diagonal [%d]", i + 1);
      }
    }

    double ans[] = new double[k * ncb];
    double[] ba = b.toDoubleArray();
    double one = 1.0;

    if (k > 0 && ncb > 0) {
       /* copy (part) cols of b to ans */
      for (int j = 0; j < ncb; j++) {
        System.arraycopy(
            /* Source = */ ba,
            /* Source offset = */ j * nrb,
            /* Destination = */ ans,
            /* Destination offset = */ j * k,
            /* Length = */ k);
      }
      BLAS.getInstance().dtrsm(
          /* SIDE = */    "L",
          /* UPLO = */    upper ? "U" : "L",
          /* TRANSA = */  trans ? "T" : "N", "N",
          k,
          ncb,
          one,
          rr,
          nrr,
          ans,
          k);
    }

    return DoubleArrayVector.unsafe(ans, AttributeMap.builder().setDim(k, ncb));
  }

  /**
   * Returns the value of the 1-norm, Frobenius norm, infinity-norm, or the largest absolute value of any
   * element of a general rectangular matrix.
   *
   * @param A the real-valued matrix
   * @param type the type of norm to return
   * @see <a href="http://www.netlib.org/lapack/explore-3.1.1-html/dlange.f.html">LAPACK Reference</a>
   */
  @Internal
  public static double La_dlange(AtomicVector A, StringVector type) {

    if(!Types.isMatrix(A)) {
      throw new EvalException("'A' must be a numeric matrix");
    }

    double a[] = A.toDoubleArray();

    int xdims[] = A.getAttributes().getDimArray();
    int m = xdims[0];
    int n = xdims[1]; /* m x n  matrix {using Lapack naming convention} */

    double work[] = null;

    String normalizationType = La_norm_type(type);
    if(normalizationType.equals("I")) {
      work = new double[m];
    }

    return LAPACK.getInstance().dlange(normalizationType, m, n, a, m, work);
  }

  private static String La_norm_type(StringVector typeVector) {

    String type = Strings.nullToEmpty(typeVector.getElementAsString(0)).toUpperCase();
    switch (type) {
      case "1":       // alias for "one norm"
        return "O";
      case "E":       // alias for Frobenius norm
        return "F";
      case "M":
      case "O":
      case "I":
      case "F":
        return type;

      default:
        throw new EvalException("argument type[1]='%s' must be one of 'M','1','O','I','F' or 'E'", type);
    }
  }

  private static String La_rcond_type(StringVector typeVector) {
    String type = Strings.nullToEmpty(typeVector.getElementAsString(0)).toUpperCase();
    switch (type) {
      case "1":
        return "O";
      case "O":
      case "I":
        return type;
      default:
        throw new EvalException("argument type[1]='%s' must be one of '1','O', or 'I'", type);
    }
  }


  /**
   * Estimates the reciprocal of the condition number of a
   * triangular matrix A, in either the 1-norm or the infinity-norm.
   *
   * @param A a real-valued matrix
   * @param typeVector the type, either the 1-norm ("O") or the infinity-norm ("I")
   * @see <a href="http://www.netlib.org/lapack/explore-3.1.1-html/dtrcon.f.html">LAPACK Reference</a>
   */
  @Internal
  public static double La_dtrcon(AtomicVector A, StringVector typeVector) {

    if(!Types.isMatrix(A)) {
      throw new EvalException("'A' must be a numeric matrix");
    }

    double a[] = A.toDoubleArray();

    int xdims[] = A.getAttributes().getDimArray();
    int n = xdims[0];
    int m = xdims[1];

    if(n != m) {
      throw new EvalException("'A' must be a *square* matrix");
    }

    String type = La_rcond_type(typeVector);

    doubleW val = new doubleW(0);
    intW info = new intW(0);

    LAPACK.getInstance().dtrcon(
        /* NORM = */  type, // Specifies whether the 1-norm condition number ("O") or the
                            // infinity-norm condition ("I") number is required
        /* UPLO = */  "U",  // Upper triangle
        /* DIAG = */  "N",  // Non-unit triangular
        /* N = */     n,    // The order of the matrix
        /* A = */     a,    // The triangular matrix A. Only the upper part is referenced
        /* LDA = */   n,    // The leading dimension of the array A
        /* RCOND = */ val,  // (output) The reciprocal of the condition number of the matrix A,
                            //          computed as RCOND = 1/(norm(A) * norm(inv(A))).
        new double[n],      // (workspace) DOUBLE PRECISION array, dimension (3*N)
        new int[n],         // (workspace) INTEGER array, dimension (N)
        info);              // (output)  successful exit (0) or error code

    if (info.val != 0) {
      throw new EvalException("error [%d] from Lapack 'dtrcon()'", info.val);
    }

    return val.val;
  }

  /**
   * Estimates the reciprocal of the condition number of a general
   * real matrix A, in either the 1-norm or the infinity-norm, using
   * the LU factorization computed by DGETRF.
   *
   * An estimate is obtained for norm(inv(A)), and the reciprocal of the
   * condition number is computed as
   * {@code RCOND = 1 / ( norm(A) * norm(inv(A)) )}
   */
  @Internal
  public static double La_dgecon(AtomicVector A, StringVector norm) {

    if(!Types.isMatrix(A)) {
      throw new EvalException("'A' must be a numeric matrix");
    }

    int xdims[] = A.getAttributes().getDimArray();
    int m = xdims[0];
    int n = xdims[1];

    String typeNorm = La_rcond_type(norm);

    double[] work;
    if(typeNorm.equals("I") && m > 4*n) {
      work = new double[m];
    } else {
      work = new double[4*n];
    }

    double a[] = A.toDoubleArray();
    int iwork[] = new int[m];

    double anorm = LAPACK.getInstance().dlange(
        /* NORM = */  typeNorm,
        /* M = */     m,        // (input) The number of rows of the matrix A.  M >= 0.
        /* N = */     n,        // (input) The number of columns of the matrix A.  N >= 0.
        /* A = */     a,        // (input) DOUBLE PRECISION array, dimension (LDA,N)
        /* LDA = */   m,        // (input) DOUBLE PRECISION array, dimension (LDA,N)
        /* WORK = */  work);    // (workspace) DOUBLE PRECISION array, dimension (MAX(1,LWORK)),


    // Compute the LU-decomposition and overwrite 'A' with result :
    intW info = new intW(0);
    LAPACK.getInstance().dgetrf(
        /* M = */     m,        // (input)  The number of rows of the matrix A.  M >= 0.
        /* N = */     n,        // (input)  The number of columns of the matrix A.  N >= 0.
        /* A = */     a,        // (input/output) array, dimension (LDA,N)
        /* LDA = */   m,        // (input)  The leading dimension of the array A.  LDA >= max(1,M).
        /* IPIV = */  iwork,    // (output) The pivot indices; for 1 <= i <= min(M,N), row i of the matrix was
                                //          interchanged with row IPIV(i).
        /* INFO = */  info);    // (output) = 0:  successful exit

    if (info.val != 0) {
      if (info.val < 0) {
        throw new EvalException("error [%d] from Lapack 'dgetrf()'", info.val);
      } else {
        // i := info > 0:  LU decomp. is completed, but  U[i,i] = 0
        //   <==> singularity
        return 0;
      }
    }

    doubleW val = new doubleW(0);
    LAPACK.getInstance().dgecon(
        /* NORM = */  typeNorm,
        /* N = */     n,
        /* A = */     a,
        /* LDA = */   n,
        /* ANORM = */ anorm,
        /* RCOND = */ val,
        /* WORK = */  work,
        /* IWORK = */ iwork,
        /* INFO = */  info);

    if (info.val != 0) {
      throw new EvalException("error [%d] from Lapack 'dgecon()'", info.val);
    }
    return val.val;
  }

  @Internal
  public static SEXP La_zgecon(SEXP x, StringVector norm) {
    throw new EvalException("TODO");
  }

  @Internal
  public static SEXP La_ztrcon(SEXP x, StringVector norm) {
    throw new EvalException("TODO");
  }


  /**
   * Represents an eigenvalue-eigenvector pair
   * @author jamie
   *
   */
  private static class ComplexEntry implements Comparable<ComplexEntry>{
    public Complex z;
    public double[] vector;
    public ComplexEntry(Complex z, double[] vector){
      this.z=z;
      this.vector=vector;
    }
    /**
     * TODO ensure ordering of the two complex conjugates (if there are any)
     */
    @Override
    public int compareTo(ComplexEntry that) {
      Complex o1=this.z;
      Complex o2=that.z;
      if(o1.getImaginary()==0 && o2.getImaginary()!=0){
        return -1;
      }else if(o1.getImaginary()!=0 && o2.getImaginary()==0){
        return 1;
      }else{
        return (int)(ComplexGroup.Mod(o2)-ComplexGroup.Mod(o1));
      }
    }

    public static Complex[] getEigenvalues(int n, List<ComplexEntry> complex) {
      Complex[] eigenvalues= new Complex[n];
      for(int i=0; i<n; i++){
        eigenvalues[i]=complex.get(i).z;
      }
      return eigenvalues;
    }

    /**
     * For an explanation of the logic of parsing LAPACK results, see
     * http://www.netlib.org/lapack/explore-html/d9/d28/dgeev_8f_source.html
     * @param complexes
     * @return
     */
    public static Complex[] getEigenvectors(List<ComplexEntry> complexes){
      int n = complexes.size();
      Complex[] result = new Complex[n*n];
      for(int j=0; j<n; j++){
        Complex z = complexes.get(j).z;
        if(z.getImaginary()==0){
          // v(j) = VR(:,j)
          for(int index=0; index<n; index++){
            result[n*j+index]=complex(complexes.get(j).vector[index]);
          }
        }else if(j+1<n && isConjugate(z,complexes.get(j+1).z)){
//          v(j) = VR(:,j) + i*VR(:,j+1) and
          for(int index=0; index<n; index++){
            result[n*j+index]=complex(complexes.get(j).vector[index],complexes.get(j+1).vector[index]);
          }
        }else if(j>0 && isConjugate(z,complexes.get(j-1).z)){
//          v(j+1) = VR(:,j) - i*VR(:,j+1)
          for(int index=0; index<n; index++){
            result[n*j+index]=complex(complexes.get(j-1).vector[index],-1*complexes.get(j).vector[index]);
          }
        }else{
          assert false :"This should never happen!";
        }
      }
      return result;
    }
    private static boolean isConjugate(Complex z, Complex w) {
      return z.getReal()==w.getReal() && z.getImaginary()==-1*w.getImaginary();
    }

  }

  public static SEXP rg(SEXP x, boolean ov) {

    int lwork;
    double work[], tmp[];
    String jobVL, jobVR;

    int n = getSquareMatrixSize(x);

      /* work on a copy of x */
    double xvals[] = ((DoubleVector)x).toDoubleArray();

    boolean vectors = !ov;
    jobVL = jobVR = "N";
    // TODO: left = right = (double *) 0;

    double left[] = null;
    double right[] = null;

    if (vectors) {
      jobVR = "V";
      jobVL="V";
      right = new double[n*n];
      left=new double[n*n];
    }
    double wR[] = new double[n];
    double wI[] = new double[n];
      
      /* ask for optimal size of work array */
    lwork = -1;

    LAPACK lapack = LAPACK.getInstance();

    tmp = new double[1];
    intW info = new intW(0);
    lapack.dgeev(jobVL, jobVR, n, xvals, n, wR, wI, left, n, right, n, tmp, lwork, info);

    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info, "dgeev");
    }

    lwork = (int) tmp[0];
    work =  new double[lwork];
    lapack.dgeev(jobVL, jobVR, n, xvals, n, wR, wI, left, n, right, n, work, lwork, info);

    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info, "dgeev");
    }

    ListVector.NamedBuilder ret = new ListVector.NamedBuilder();

    if (thereAreComplexResults(n, wR, wI)) {

      //Step 1: Get different eigenvalues
      List<ComplexEntry> complex = new ArrayList<ComplexEntry>();
      for(int i=0; i<n; i++ ){
        complex.add(new ComplexEntry(complex(wR[i],wI[i]),Arrays.copyOfRange(right, n*i, n*(i+1))));
      }
      Collections.sort(complex);
      Complex[] eigenvalues = ComplexEntry.getEigenvalues(n, complex);
      ComplexArrayVector values = new ComplexArrayVector(eigenvalues);

      ret.add("values",values);

      ret.add("vectors", vectors ? ComplexArrayVector.newMatrix(ComplexEntry.getEigenvectors(complex), n, n) : Null.INSTANCE);
//    
//        val = allocVector(CPLXSXP, n);
//          for (i = 0; i < n; i++) {
//              COMPLEX(val)[i].r = wR[i];
//              COMPLEX(val)[i].i = wI[i];
//          }
//          SET_VECTOR_ELT(ret, 0, val);

//        ret.add("values", new DoubleVector(new double[n]));
//        
//        if (vectors) {
////        SET_VECTOR_ELT(ret, 1, unscramble(wI, n, right));
//        }
    } else {
      ret.add("values", new DoubleArrayVector(wR));
      ret.add("vectors", vectors ? DoubleArrayVector.newMatrix(right, n, n) : Null.INSTANCE);
    }
    return ret.build();
  }



  private static boolean thereAreComplexResults(int n, double[] wR, double[] wI) {
    boolean complexValues = false;
    for (int i = 0; i < n; i++) {
      /* This test used to be !=0 for R < 2.3.0.  This is OK for 0+0i */
      if (Math.abs(wI[i]) >  10 * DoubleVector.EPSILON * Math.abs(wR[i])) {
        complexValues = true;
        break;
      }
    }
    return complexValues;
  }


  public static ComplexVector c(double... d){
    ComplexArrayVector.Builder builder = new ComplexArrayVector.Builder();
    for(double di : d){
      builder.add(ComplexVector.complex(di));
    }
    return builder.build();
  }

  public static Complex complex(double x,double y){
    return ComplexVector.complex(x,y);
  }

  public static Complex complex(double x){
    return complex(x,0);
  }

  protected static Complex[] row(Complex... z){
    return z;
  }

  protected static SEXP matrix(Complex[]... rows) {
    ComplexArrayVector.Builder matrix = new ComplexArrayVector.Builder();
    int nrows = rows.length;
    int ncols = rows[0].length;

    for(int j=0;j!=ncols;++j) {
      for(int i=0;i!=nrows;++i) {
        matrix.add(rows[i][j]);
      }
    }
    return matrix.build();
  }

  private static int getSquareMatrixSize(SEXP x) {
    Vector xdims =  (Vector)x.getAttribute(Symbols.DIM);
    if(xdims.length() != 2 || xdims.getElementAsInt(0) != xdims.getElementAsInt(1)) {
      throw new EvalException("'x' must be a square numeric matrix");
    }
    int n = xdims.getElementAsInt(0);
    return n;
  }


}
