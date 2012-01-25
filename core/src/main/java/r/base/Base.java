/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.complex.Complex;
import org.netlib.lapack.LAPACK;
import org.netlib.util.doubleW;
import org.netlib.util.intW;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import r.base.matrix.Matrix;
import r.base.matrix.MatrixBuilder;
import r.lang.AtomicVector;
import r.lang.ComplexVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * Implementation of routines from the base dll.
 * 
 * <p>The functions implemented here are distinct from the collection of
 * primitives which make up most of the base package; these functions are
 * called from R code using the .Call("methodname", arg1, arg2, ... argn, PACKAGE="base") syntax.
 * 
 * <p>Also note that many methods use a convention whereby the values to be 
 * returned are allocated by the calling R code to save the C implementors the
 * trouble of dealing with memory management (I think), so many of these methods 
 * take what are, in renjin's context, unused arguments. 
 * 
 */
public class Base {

  private Base() { }
  
  public static boolean R_isMethodsDispatchOn() {
    return false;
  }

  public static ListVector R_getSymbolInfo(String sname, SEXP spackage, boolean withRegistrationInfo) {

    ListVector.Builder result = new ListVector.Builder();
    result.setAttribute(Symbols.CLASS, new StringVector("CRoutine"));

    return result.build();

  }

  public static ListVector R_getRegisteredRoutines(String dll) {
    ListVector.Builder builder = new ListVector.Builder();
    return builder.build();
  }

  /** @return  n if the data frame 'vec' has c(NA, n) rownames;
   *         nrow(.) otherwise;  note that data frames with nrow(.) == 0
   *          have no row.names.
   * ==> is also used in dim.data.frame()
   *
   * AB Note: I have no idea what this function really does but it is
   * so opaque that it is first against the wall when the revolution comes.
   */
  public static SEXP R_shortRowNames(SEXP vector, int type) {
    SEXP s =  vector.getAttribute(Symbols.ROW_NAMES);
    SEXP ans = s;

    if( type < 0 || type > 2) {
      throw new EvalException("invalid 'type' argument");
    }

    if(type >= 1) {
      int n;
      if (s instanceof IntVector && s.length() == 2 && ((IntVector) s).isElementNA(0)) {
        n = ((IntVector) s).getElementAsInt(1);
      } else {
        if (s == Null.INSTANCE) {
          n = 0;
        } else {
          n = s.length();
        }
      }
      if (type == 1) {
        ans = new IntVector(n);
      } else {
        ans = new IntVector(Math.abs(n));
      }
    }
    return ans;
  }
  
  /**
   * 
   * "native" implementation called by tabulate(), which 
   * takes the integer-valued vector bin and counts the number of times each integer occurs in it.
   * 
   * <p>There is a bin for each of the values 1, ..., nbins; values outside that range 
   * and NAs are (silently) ignored.
   * 
   * @param bin the integer vector to bin
   * @param length the length of bin
   * @param nbins the number of bins
   * @param ans not used
   * @return 
   */
  public static PairList R_tabulate(IntVector bin, int length, int nbins, SEXP ans) {
    int counts[] = new int[nbins];
    for(int i=0;i!=length;++i) {
      if(!bin.isElementNA(i)) {
        int value = bin.getElementAsInt(i);
        if(value >= 1 && value <= nbins) {
          counts[value-1]++;
        }
      }
    }
    return PairList.Node.singleton("ans", new IntVector(counts));
  }
  
  public static SEXP Rrowsum_df(ListVector x, int ncol, Vector group, SEXP ugroup, boolean naRm) {
    throw new EvalException("nyi");
  }

  public static Vector Rrowsum_matrix(Vector x, int ncol, AtomicVector groups, AtomicVector ugroup, boolean naRm) {
    
    int numGroups = ugroup.length();
    
    Matrix source = new Matrix(x, ncol);
    MatrixBuilder result = source.newBuilder(numGroups, ncol);
    
    for(int col=0;col!=ncol;++col) {
      
      // sum the rows in this column by group
      
      double groupSums[] = new double[numGroups];
      for(int row=0;row!=source.getNumRows();++row) {
        int group = ugroup.indexOf(groups, row, 0);
        groupSums[group] += source.getElementAsDouble(row, col);
      }
      
      // copy sums to matrix
      for(int group=0;group!=ugroup.length();++group) {
        result.setValue(group, col, groupSums[group]);
      }
      
    }
    return result.build();
  }  
    
  /**
   * Singular Value Decomposition implemented with Jlapack, a mechanical Fortran-to-java translation 
   * of the same lapack library that R uses.
   * 
   */
  public static SEXP La_svd(String jobu, String jobv, DoubleVector x, DoubleVector  sexp,
      DoubleVector uexp, DoubleVector vexp, String method ) {
    
    IntVector xdims = (IntVector) x.getAttribute(Symbols.DIM);
    int n = xdims.getElementAsInt(0);
    int p = xdims.getElementAsInt(1);

    double xvals[] = x.toDoubleArray();
    
    int ldu =  ((IntVector)uexp.getAttribute(Symbols.DIM)).getElementAsInt(0);
    int ldvt = ((IntVector)vexp.getAttribute(Symbols.DIM)).getElementAsInt(0);
    
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
      .add("d", new DoubleVector(s, sexp.getAttributes()))
      .add("u", new DoubleVector(u, uexp.getAttributes()))
      .add("vt", new DoubleVector(v, vexp.getAttributes()))
      .build();
  }
 
  // nicer API but output does not match R-2.10's result at all--
  // do i misunderstand something??
//  public static SEXP La_svd(String jobu, String jobv, DoubleVector x, DoubleVector  sexp,
//      DoubleVector uexp, DoubleVector vexp, String method ) {
//    
//    SingularValueDecomposition svd = new SingularValueDecompositionImpl(CommonsMath.asRealMatrix(x));
//        
//    ListVector.Builder val = new ListVector.Builder();
//    val.add("d", new DoubleVector(svd.getSingularValues()));
//    val.add("u", CommonsMath.asDoubleVector(svd.getU()));
//    val.add("vt", CommonsMath.asDoubleVector(svd.getVT()));
//    
//    return val.build();
//  }
  
  
  
  /*
   * Converted directly from the C code, src/modules/lapack/Lapack.c
   */
  public static SEXP La_dgesv(DoubleVector A, DoubleVector Bin, DoubleVector tolin) {
    int n, p;
    IntVector ipiv, Adims, Bdims;
    IntVector.Builder ipivBuilder;
    DoubleVector avals;
    DoubleVector.Builder avalsBuilder;
    org.netlib.util.intW info;

    double anorm;
    doubleW rcond = new doubleW(0);

    DoubleVector tol = tolin, work;
    DoubleVector B;
    double[] Bcontent;

    if (!(Types.isMatrix(A) && Types.isDouble(A))) {
      throw new EvalException("'a' must be a numeric matrix");
    }
    if (!(Types.isMatrix(Bin) && Types.isDouble(Bin))) {
      throw new EvalException("'b' must be a numeric matrix");
    }
    //PROTECT(B = duplicate(Bin));
    DoubleVector.Builder bu = new DoubleVector.Builder();
    for (int i = 0; i < Bin.length(); i++) {
      bu.add(Bin.get(i));
    }    
    bu.copyAttributesFrom(Bin);
    B = bu.build();
      

    //Adims = INTEGER(coerceVector(getAttrib(A, R_DimSymbol), INTSXP));
    //Bdims = INTEGER(coerceVector(getAttrib(B, R_DimSymbol), INTSXP));
    Adims = (IntVector) A.getAttribute(Symbols.DIM);
    Bdims = (IntVector) B.getAttribute(Symbols.DIM);

    n = Adims.getElementAsInt(0);
    if (n == 0) {
      throw new EvalException("'a' is 0-diml");
    }
    p = Bdims.getElementAsInt(1);
    if (p == 0) {
      throw new EvalException("no right-hand side in 'b'");
    }
    if (Adims.getElementAsInt(1) != n) {
      throw new EvalException("'a' (" + n + " x " + Adims.getElementAsInt(1) + ") must be square");
    }
    if (Bdims.getElementAsInt(0) != n) {
      throw new EvalException("'b' (" + Bdims.getElementAsInt(0) + " x " + p + ") must be compatible with 'a' (" + n + " x " + n + ")");
    }

    //ipiv = new IntVector(); //Will be size of n
    ipivBuilder = new IntVector.Builder();
    for (int i = 0; i < n; i++) {
      ipivBuilder.add(0);
    }
    ipiv = ipivBuilder.build();

    //avals = (double *) R_alloc(n * n, sizeof(double));
    avalsBuilder = new DoubleVector.Builder();
    //Memcpy(avals, REAL(A), n * n);
    for (int i = 0; i < A.length(); i++) {
      avalsBuilder.add(A.get(i));
    }
    avalsBuilder.copyAttributesFrom(A);
    avals = avalsBuilder.build();

    //F77_CALL(dgesv)(&n, &p, avals, &n, ipiv, REAL(B), &n, &info);
    LAPACK lapack = LAPACK.getInstance();
    info = new intW(0);
    Bcontent = B.toDoubleArray();
    lapack.dgesv(n, p, avals.toDoubleArray(), n, ipiv.toIntArray(), Bcontent, n, info);

    if (info.val < 0) {
      throw new EvalException("argument -" + info.val + " of Lapack routine 'dgsv' had invalid value");
    }
    if (info.val > 0) {
      throw new EvalException("Lapack routine dgesv: system is exactly singular");
    }

    //anorm = F77_CALL(dlange)("1", &n, &n, REAL(A), &n, (double*) NULL);
    anorm = lapack.dlange("1", n, n, A.toDoubleArray(), n, null);
    //work = (double *) R_alloc(4*n, sizeof(double));
    double[] arrWork = new double[4 * n];
    //F77_CALL(dgecon)("1", &n, avals, &n, &anorm, &rcond, work, ipiv, &info);

    lapack.dgecon("1", n, avals.toDoubleArray(), n, anorm, rcond, arrWork, ipiv.toIntArray(), info);

    if (rcond.val < tol.get(0)) {
      throw new EvalException("system is computationally singular: reciprocal condition number = " + rcond.val);
    }
    return new DoubleVector(Bcontent);
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
  
  public static SEXP La_rg(SEXP x, boolean ov)
  {
  
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
        print(wR);
        print(wI);
        print(right);
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
        ret.add("values", new DoubleVector(wR));
        ret.add("vectors", vectors ? DoubleVector.newMatrix(right, n, n) : Null.INSTANCE);
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
  
  private static void print(ComplexVector values) {
    java.lang.System.out.println(values);
    
  }

  private static void print(double[] right) {
    List<Double> list = new ArrayList<Double>();
    if (right != null) {
      for (double x : right) {
        list.add(x);
      }
    }
    java.lang.System.out.println(list);
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
  
  
  public static SEXP La_rs(DoubleVector x, boolean ov)
  {
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
      ret.add("values", new DoubleVector(rvalues));
      if (!ov) {
        ret.add("vectors", DoubleVector.newMatrix(rz, n, n));
      }
      return ret.build();
  }
  
  public static SEXP R_copyDFattr(SEXP in, SEXP out) { 
    
    // NOTE: the equivalent C code actuallly modifies 'out' which
    // is not actually possible in Renjin-- we can only return a modified
    // copy. Not clear whether this will
    // have consequences -- this is called from [.data.frame
    ListVector attributesToCopy;
    if(in.hasAttributes()) {
      attributesToCopy = (ListVector)in.getAttributes().toVector();
    } else {
      attributesToCopy = new ListVector();
    }
    //IS_S4_OBJECT(in) ?  SET_S4_OBJECT(out) : UNSET_S4_OBJECT(out);
    //SET_OBJECT(out, OBJECT(in));

    return out.setAttributes(attributesToCopy);  
  }
}
