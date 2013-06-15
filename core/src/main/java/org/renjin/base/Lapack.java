package org.renjin.base;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.complex.Complex;
import org.netlib.lapack.LAPACK;
import org.netlib.util.doubleW;
import org.netlib.util.intW;
import org.renjin.eval.EvalException;
import org.renjin.primitives.ComplexGroup;
import org.renjin.primitives.Types;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

/**
 * R Bindings for the LAPACK library, used
 * by the base package. 
 */
public class Lapack {

  /**
   * Invert a symmetric, positive definite square matrix from its Choleski decomposition, using
   * the {@code dpotri} routine.
   * 
   * @param a 
   * @param sz the number of columns of x containing the Choleski decomposition.
   * @return 
   */
  public static DoubleVector chol2inv(DoubleVector a, int sz) {

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
    
    return DoubleArrayVector.unsafe(result, AttributeMap.builder().setDim(sz, sz).build());
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
      if (info.val != 0)
        throw new EvalException("error code %d from Lapack routine '%s'", info, "dsyevr");
      
      lwork = (int) tmp[0];
      liwork = itmp[0];
  
      double work[] =  new double[lwork];
      int iwork[] = new int[liwork];

      lapack.dsyevr(jobv, range, uplo, n, rx, n,
                       vl, vu, il, iu, abstol, m, rvalues,
                       rz, n, isuppz,
                       work, lwork, iwork, liwork, info);
      if (info.val != 0)
        throw new EvalException("error code %d from Lapack routine '%s'", info, "dsyevr");
  
      ListVector.NamedBuilder ret = ListVector.newNamedBuilder();
      ret.add("values", new DoubleArrayVector(rvalues));
      if (!ov) {
        ret.add("vectors", DoubleArrayVector.newMatrix(rz, n, n));
      }
      return ret.build();
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

      if (info.val != 0)
          throw new EvalException("error code %d from Lapack routine '%s'", info, "dgeev");
      
      lwork = (int) tmp[0];
      work =  new double[lwork];
      lapack.dgeev(jobVL, jobVR, n, xvals, n, wR, wI, left, n, right, n, work, lwork, info);

      if (info.val != 0)
          throw new EvalException("error code %d from Lapack routine '%s'", info, "dgeev");

      ListVector.NamedBuilder ret = new ListVector.NamedBuilder();
      
      if (thereAreComplexResults(n, wR, wI)) {
        
        //Step 1: Get different eigenvalues
        List<ComplexEntry> complex = new ArrayList<ComplexEntry>();
        for(int i=0; i<n; i++ ){
          complex.add(new ComplexEntry(complex(wR[i],wI[i]),Arrays.copyOfRange(right, n*i, n*(i+1))));
        }
        Collections.sort(complex);
        Complex[] eigenvalues = ComplexEntry.getEigenvalues(n, complex);
        ComplexVector values = new ComplexVector(eigenvalues);
        
        ret.add("values",values);

        ret.add("vectors", vectors ? ComplexVector.newMatrix(ComplexEntry.getEigenvectors(complex), n, n) : Null.INSTANCE);
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
    ComplexVector.Builder builder = new ComplexVector.Builder();  
    for(double di : d){
        builder.add(new Complex(di,0));
    }
      return builder.build();
  }
  
  public static Complex complex(double x,double y){
    return new Complex(x,y);
  }
  
  public static Complex complex(double x){
    return complex(x,0);
  }
  
  protected static Complex[] row(Complex... z){
    return z;
  }
  
  protected static SEXP matrix(Complex[]... rows) {
    ComplexVector.Builder matrix = new ComplexVector.Builder();
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
