/*
    java copyright claim:

    This software is based on the public domain UNCMIN routines.
    It was translated from FORTRAN to Java by a US government employee
    on official time.  Thus this software is also in the public domain.


    The translator's mail address is:

    Steve Verrill
    USDA Forest Products Laboratory
    1 Gifford Pinchot Drive
    Madison, Wisconsin
    53705


    The translator's e-mail address is:

    steve@www1.fpl.fs.fed.us


***********************************************************************

DISCLAIMER OF WARRANTIES:

THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.
THE TRANSLATOR DOES NOT WARRANT, GUARANTEE OR MAKE ANY REPRESENTATIONS
REGARDING THE SOFTWARE OR DOCUMENTATION IN TERMS OF THEIR CORRECTNESS,
RELIABILITY, CURRENCY, OR OTHERWISE. THE ENTIRE RISK AS TO
THE RESULTS AND PERFORMANCE OF THE SOFTWARE IS ASSUMED BY YOU.
IN NO CASE WILL ANY PARTY INVOLVED WITH THE CREATION OR DISTRIBUTION
OF THE SOFTWARE BE LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE
OF THIS SOFTWARE.

Sorry about that.

***********************************************************************


History:

Date        Translator        Changes

4/14/98     Steve Verrill     Translated

*/


package org.renjin.stats.internals.optimize;


/**
 * <p/>
 * This class contains Java translations of the UNCMIN unconstrained
 * optimization routines.  See R.B. Schnabel, J.E. Koontz, and B.E.
 * Weiss, <i>A Modular System
 * of Algorithms for Unconstrained Minimization</i>, Report CU-CS-240-82,
 * Comp. Sci. Dept., University of Colorado at Boulder, 1982.
 * <p/>
 * <p/>
 * <b>IMPORTANT:</b>  The "_f77" suffixes indicate that these routines use
 * FORTRAN style indexing.  For example, you will see
 * <pre>
 *   for (i = 1; i <= n; i++)
 * </pre>
 * rather than
 * <pre>
 *   for (i = 0; i < n; i++)
 * </pre>
 * To use the "_f77" routines you will have to declare your vectors
 * and matrices to be one element larger (e.g., v[101] rather than
 * v[100], and a[101][101] rather than a[100][100]), and you will have
 * to fill elements 1 through n rather than elements 0 through n - 1.
 * Versions of these programs that use C/Java style indexing will
 * eventually be available.  They will end with the suffix "_j".
 * <p/>
 * <p/>
 * This class was translated by a statistician from a FORTRAN version of
 * UNCMIN.  It is NOT an official translation.  It wastes
 * memory by failing to use the first elements of vectors.  When
 * public domain Java optimization routines become available
 * from the people who produced UNCMIN, then <b>THE CODE PRODUCED
 * BY THE NUMERICAL ANALYSTS SHOULD BE USED</b>.
 * <p/>
 * <p/>
 * Meanwhile, if you have suggestions for improving this
 * code, please contact Steve Verrill at steve@www1.fpl.fs.fed.us.
 *
 * @author (translator)Steve Verrill
 * @version .5 --- April 14, 1998
 */
class Uncmin {

  public enum Method {
    LINE_SEARCH,
    DOUBLE_DOGLEG,
    MORE_HEBDON
  }

  /**
   * Minimizes a smooth nonlinear function of n variables.
   * A method that computes the function value at any point
   * must be supplied.  (See Uncmin_methods.java and UncminTest.java.)
   * Derivative values are not required.
   * The optif9 method provides complete user access to the UNCMIN
   * minimization routines.  The user has full control over options.
   * For details, see the Schnabel et al reference and the comments in the code.
   * <p/>
   * Translated by Steve Verrill, August 4, 1998.
   *
   * @param n        The number of arguments of the function to minimize
   * @param x        The initial estimate of the minimum point
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param typsiz   Typical size for each component of x
   * @param fscale   Estimate of the scale of the objective function
   * @param method   Algorithm to use to solve the minimization problem
   *                 = 1  line search
   *                 = 2  double dogleg
   *                 = 3  More-Hebdon
   * @param iexp     = 1 if the optimization function f_to_minimize
   *                 is expensive to evaluate, = 0 otherwise.  If iexp = 1,
   *                 then the Hessian will be evaluated by secant update
   *                 rather than analytically or by finite differences.
   * @param msg      Message to inhibit certain automatic checks and output
   * @param ndigit   Number of good digits in the minimization function
   * @param itnlim   Maximum number of allowable iterations
   * @param iagflg   = 0 if an analytic gradient is not supplied
   * @param iahflg   = 0 if an analytic Hessian is not supplied
   * @param dlt      Trust region radius
   * @param gradtl   Tolerance at which the gradient is considered close enough
   *                 to zero to terminate the algorithm
   * @param stepmx   Maximum allowable step size
   * @param steptl   Relative step size at which successive iterates
   *                 are considered close enough to terminate the algorithm
   * @param xpls     The final estimate of the minimum point
   * @param fpls     The value of f_to_minimize at xpls
   * @param gpls     The gradient at the local minimum xpls
   * @param itrmcd   Termination code
   *                 ITRMCD =  0:  Optimal solution found
   *                 ITRMCD =  1:  Terminated with gradient small,
   *                 X is probably optimal
   *                 ITRMCD =  2:  Terminated with stepsize small,
   *                 X is probably optimal
   *                 ITRMCD =  3:  Lower point cannot be found,
   *                 X is probably optimal
   *                 ITRMCD =  4:  Iteration limit (150) exceeded
   *                 ITRMCD =  5:  Too many large steps,
   *                 function may be unbounded
   * @param a        Workspace for the Hessian (or its estimate)
   *                 and its Cholesky decomposition
   * @param udiag    Workspace for the diagonal of the Hessian
   */

  public static void optif9_f77(int n, double x[], UncminFunction minclass,
                                double typsiz[], double fscale, Method method,
                                boolean iexp, int msg[], int ndigit[], int itnlim[],
                                int iagflg[], int iahflg[], double dlt[],
                                double gradtl[], double stepmx[], double steptl[],
                                double xpls[], double fpls[], double gpls[],
                                int itrmcd[], double a[][], double udiag[]) {



// CHECK FUNCTION SCALE

    if (fscale == 0) {
      fscale = 1.0;
    }
    if (fscale < 0.0) {
      fscale = -fscale;
    }


    double g[] = new double[n + 1];
    double p[] = new double[n + 1];
    double sx[] = new double[n + 1];
    double wrk0[] = new double[n + 1];
    double wrk1[] = new double[n + 1];
    double wrk2[] = new double[n + 1];
    double wrk3[] = new double[n + 1];

// MINIMIZE FUNCTION

    optdrv_f77(n, x, minclass, typsiz, fscale, method,
        iexp, msg, ndigit, itnlim, iagflg, iahflg,
        dlt, gradtl, stepmx, steptl, xpls,
        fpls, gpls, itrmcd, a, udiag, g, p, sx,
        wrk0, wrk1, wrk2, wrk3);

    if (itrmcd[1] == 1) {

//         System.out.print("\nUncmin WARNING --- itrmcd = 1, " +
//                          "probably converged, gradient small\n\n");

    } else if (itrmcd[1] == 2) {

//         System.out.print("\nUncmin WARNING --- itrmcd = 2, " +
//                          "probably converged, stepsize small\n\n");

    } else if (itrmcd[1] == 3) {

//         System.out.print("\nUncmin WARNING --- itrmcd = 3, " +
//                          "cannot find lower point\n\n");

    } else if (itrmcd[1] == 4) {

//         System.out.print("\nUncmin WARNING --- itrmcd = 4, " +
//                          "too many iterations\n\n");

    } else if (itrmcd[1] == 5) {

//         System.out.print("\nUncmin WARNING --- itrmcd = 5, " +
//                          "too many large steps, " +
//                          "possibly unbounded\n\n");

    }
  }


  /**
   * Solves Ax = b where A is an upper triangular
   * matrix.  Note that A is input as a lower triangular matrix and
   * this method takes its transpose implicitly.
   * <p/>
   * Translated by Steve Verrill, April 14, 1998.
   *
   * @param n Dimension of the problem
   * @param a n by n lower triangular matrix (preserved)
   * @param x The solution vector
   * @param b The right-hand side vector
   */

  public static void bakslv_f77(int n, double a[][], double x[], double b[]) {

    int i, ip1, j;
    double sum;

// SOLVE (L-TRANSPOSE)X=B. (BACK SOLVE)

    i = n;
    x[i] = b[i] / a[i][i];

    while (i > 1) {

      ip1 = i;
      i--;

      sum = 0.0;

      for (j = ip1; j <= n; j++) {
        sum += a[j][i] * x[j];
      }

      x[i] = (b[i] - sum) / a[i][i];
    }
  }


  /**
   * <p/>
   * The chlhsn_f77 method finds
   * "THE L(L-TRANSPOSE) [WRITTEN LL+] DECOMPOSITION OF THE PERTURBED
   * MODEL HESSIAN MATRIX A+MU*I(WHERE MU\0 AND I IS THE IDENTITY MATRIX)
   * WHICH IS SAFELY POSITIVE DEFINITE.  IF A IS SAFELY POSITIVE DEFINITE
   * UPON ENTRY, THEN MU=0."
   * <p/>
   * Translated by Steve Verrill, April 14, 1998.
   *
   * @param n     Dimension of the problem
   * @param a     On entry: A is the model Hessian (only the lower
   *              triangle and diagonal stored)
   *              On exit: A contains L of the LL+ decomposition of
   *              the perturbed model Hessian in the lower triangle
   *              and diagonal, and contains the Hessian in the upper
   *              triangle and udiag
   * @param epsm  Machine epsilon
   * @param sx    Scaling vector for x
   * @param udiag On exit: Contains the diagonal of the Hessian
   */

  public static void chlhsn_f77(int n, double a[][], double epsm,
                                double sx[], double udiag[]) {

/*C INTERNAL VARIABLES
C ------------------
C TOL              TOLERANCE
C DIAGMN           MINIMUM ELEMENT ON DIAGONAL OF A
C DIAGMX           MAXIMUM ELEMENT ON DIAGONAL OF A
C OFFMAX           MAXIMUM OFF-DIAGONAL ELEMENT OF A
C OFFROW           SUM OF OFF-DIAGONAL ELEMENTS IN A ROW OF A
C EVMIN            MINIMUM EIGENVALUE OF A
C EVMAX            MAXIMUM EIGENVALUE OF A
C
C DESCRIPTION
C -----------
C 1. IF "A" HAS ANY NEGATIVE DIAGONAL ELEMENTS, THEN CHOOSE MU>0
C SUCH THAT THE DIAGONAL OF A:=A+MU*I IS ALL POSITIVE
C WITH THE RATIO OF ITS SMALLEST TO LARGEST ELEMENT ON THE
C ORDER OF SQRT(EPSM).
C
C 2. "A" UNDERGOES A PERTURBED CHOLESKY DECOMPOSITION WHICH
C RESULTS IN AN LL+ DECOMPOSITION OF A+D, WHERE D IS A
C NON-NEGATIVE DIAGONAL MATRIX WHICH IS IMPLICITLY ADDED TO
C "A" DURING THE DECOMPOSITION IF "A" IS NOT POSITIVE DEFINITE.
C "A" IS RETAINED AND NOT CHANGED DURING THIS PROCESS BY
C COPYING L INTO THE UPPER TRIANGULAR PART OF "A" AND THE
C DIAGONAL INTO UDIAG.  THEN THE CHOLESKY DECOMPOSITION ROUTINE
C IS CALLED.  ON RETURN, ADDMAX CONTAINS MAXIMUM ELEMENT OF D.
C
C 3. IF ADDMAX=0, "A" WAS POSITIVE DEFINITE GOING INTO STEP 2
C AND RETURN IS MADE TO CALLING PROGRAM.  OTHERWISE,
C THE MINIMUM NUMBER SDD WHICH MUST BE ADDED TO THE
C DIAGONAL OF A TO MAKE IT SAFELY STRICTLY DIAGONALLY DOMINANT
C IS CALCULATED.  SINCE A+ADDMAX*I AND A+SDD*I ARE SAFELY
C POSITIVE DEFINITE, CHOOSE MU=MIN(ADDMAX,SDD) AND DECOMPOSE
C A+MU*I TO OBTAIN L.
C

*/

    int i, j, im1, jm1;

    /**
     * tolerance
     */
    double tol;

    double diagmx, diagmn, posmax, amu, offmax,
        evmin, evmax, offrow, sdd;

    double addmax[] = new double[2];

// SCALE HESSIAN
// PRE- AND POST- MULTIPLY "A" BY INV(SX)

    for (j = 1; j <= n; j++) {

      for (i = j; i <= n; i++) {

        a[i][j] /= (sx[i] * sx[j]);

      }

    }

// STEP1
// -----
// NOTE:  IF A DIFFERENT TOLERANCE IS DESIRED THROUGHOUT THIS
// ALGORITHM, CHANGE TOLERANCE HERE:

    tol = Math.sqrt(epsm);

    diagmx = a[1][1];
    diagmn = a[1][1];

    for (i = 2; i <= n; i++) {

      if (a[i][i] < diagmn) diagmn = a[i][i];
      if (a[i][i] > diagmx) diagmx = a[i][i];

    }

    posmax = Math.max(diagmx, 0.0);

// DIAGMN .LE. 0

    if (diagmn <= posmax * tol) {

      amu = tol * (posmax - diagmn) - diagmn;

      if (amu == 0.0) {

// FIND LARGEST OFF-DIAGONAL ELEMENT OF A

        offmax = 0.0;

        for (i = 2; i <= n; i++) {

          im1 = i - 1;

          for (j = 1; j <= im1; j++) {

            if (Math.abs(a[i][j]) > offmax) offmax =
                Math.abs(a[i][j]);

          }

        }

        amu = offmax;

        if (amu == 0.0) {

          amu = 1.0;

        } else {

          amu *= 1.0 + tol;

        }

      }

// A = A + MU*I

      for (i = 1; i <= n; i++) {

        a[i][i] += amu;

      }

      diagmx += amu;

    }

// STEP2
// -----
// COPY LOWER TRIANGULAR PART OF "A" TO UPPER TRIANGULAR PART
// AND DIAGONAL OF "A" TO UDIAG


    for (j = 1; j <= n; j++) {

      udiag[j] = a[j][j];

      for (i = j + 1; i <= n; i++) {

        a[j][i] = a[i][j];

      }

    }

    choldc_f77(n, a, diagmx, tol, addmax);

// STEP3
// -----
// IF ADDMAX=0, "A" WAS POSITIVE DEFINITE GOING INTO STEP 2,
// THE LL+ DECOMPOSITION HAS BEEN DONE, AND WE RETURN.
// OTHERWISE, ADDMAX>0.  PERTURB "A" SO THAT IT IS SAFELY
// DIAGONALLY DOMINANT AND FIND LL+ DECOMPOSITION

    if (addmax[1] > 0.0) {

// RESTORE ORIGINAL "A" (LOWER TRIANGULAR PART AND DIAGONAL)

      for (j = 1; j <= n; j++) {

        a[j][j] = udiag[j];

        for (i = j + 1; i <= n; i++) {

          a[i][j] = a[j][i];

        }

      }

// FIND SDD SUCH THAT A+SDD*I IS SAFELY POSITIVE DEFINITE
// NOTE:  EVMIN<0 SINCE A IS NOT POSITIVE DEFINITE;

      evmin = 0.0;
      evmax = a[1][1];

      for (i = 1; i <= n; i++) {

        offrow = 0.0;
        im1 = i - 1;

        for (j = 1; j <= im1; j++) {

          offrow += Math.abs(a[i][j]);

        }

        for (j = i + 1; j <= n; j++) {

          offrow += Math.abs(a[j][i]);

        }

        evmin = Math.min(evmin, a[i][i] - offrow);
        evmax = Math.max(evmax, a[i][i] + offrow);

      }

      sdd = tol * (evmax - evmin) - evmin;

// PERTURB "A" AND DECOMPOSE AGAIN

      amu = Math.min(sdd, addmax[1]);

      for (i = 1; i <= n; i++) {

        a[i][i] += amu;
        udiag[i] = a[i][i];

      }

// "A" NOW GUARANTEED SAFELY POSITIVE DEFINITE

      choldc_f77(n, a, 0.0, tol, addmax);

    }

// UNSCALE HESSIAN AND CHOLESKY DECOMPOSITION MATRIX

    for (j = 1; j <= n; j++) {

      for (i = j; i <= n; i++) {

        a[i][j] *= sx[i];

      }

      jm1 = j - 1;

      for (i = 1; i <= jm1; i++) {

        a[i][j] *= sx[i] * sx[j];

      }

      udiag[j] *= sx[j] * sx[j];

    }
  }


  /**
   * <p/>
   * The choldc_f77 method finds
   * "THE PERTURBED L(L-TRANSPOSE) [WRITTEN LL+] DECOMPOSITION
   * OF A+D, WHERE D IS A NON-NEGATIVE DIAGONAL MATRIX ADDED TO A IF
   * NECESSARY TO ALLOW THE CHOLESKY DECOMPOSITION TO CONTINUE."
   * <p/>
   * Translated by Steve Verrill, April 15, 1998.
   *
   * @param n      Dimension of the problem
   * @param a      On entry: matrix for which to find the perturbed
   *               Cholesky decomposition
   *               On exit: contains L of the LL+ decomposition
   *               in lower triangle
   * @param diagmx Maximum diagonal element of "A"
   * @param tol    Tolerance
   * @param addmax Maximum amount implicitly added to diagonal
   *               of "A" in forming the Cholesky decomposition
   *               of A+D
   */

  public static void choldc_f77(int n, double a[][], double diagmx,
                                double tol, double addmax[]) {

/*

Here is a copy of the choldc FORTRAN documentation:

      SUBROUTINE CHOLDC(NR,N,A,DIAGMX,TOL,ADDMAX)

C
C PURPOSE
C -------
C FIND THE PERTURBED L(L-TRANSPOSE) [WRITTEN LL+] DECOMPOSITION
C OF A+D, WHERE D IS A NON-NEGATIVE DIAGONAL MATRIX ADDED TO A IF
C NECESSARY TO ALLOW THE CHOLESKY DECOMPOSITION TO CONTINUE.
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)      <--> ON ENTRY: MATRIX FOR WHICH TO FIND PERTURBED
C                       CHOLESKY DECOMPOSITION
C                  ON EXIT:  CONTAINS L OF LL+ DECOMPOSITION
C                  IN LOWER TRIANGULAR PART AND DIAGONAL OF "A"
C DIAGMX       --> MAXIMUM DIAGONAL ELEMENT OF "A"
C TOL          --> TOLERANCE
C ADDMAX      <--  MAXIMUM AMOUNT IMPLICITLY ADDED TO DIAGONAL OF "A"
C                  IN FORMING THE CHOLESKY DECOMPOSITION OF A+D
C INTERNAL VARIABLES
C ------------------
C AMINL    SMALLEST ELEMENT ALLOWED ON DIAGONAL OF L
C AMNLSQ   =AMINL**2
C OFFMAX   MAXIMUM OFF-DIAGONAL ELEMENT IN COLUMN OF A
C
C
C DESCRIPTION
C -----------
C THE NORMAL CHOLESKY DECOMPOSITION IS PERFORMED.  HOWEVER, IF AT ANY
C POINT THE ALGORITHM WOULD ATTEMPT TO SET L(I,I)=SQRT(TEMP)
C WITH TEMP < TOL*DIAGMX, THEN L(I,I) IS SET TO SQRT(TOL*DIAGMX)
C INSTEAD.  THIS IS EQUIVALENT TO ADDING TOL*DIAGMX-TEMP TO A(I,I)
C
C

*/

    int i, j, jm1, jp1, k;
    double aminl, amnlsq, offmax, sum, temp;

    addmax[1] = 0.0;
    aminl = Math.sqrt(diagmx * tol);
    amnlsq = aminl * aminl;

// FORM COLUMN J OF L

    for (j = 1; j <= n; j++) {

// FIND DIAGONAL ELEMENTS OF L

      sum = 0.0;
      jm1 = j - 1;
      jp1 = j + 1;

      for (k = 1; k <= jm1; k++) {

        sum += a[j][k] * a[j][k];

      }

      temp = a[j][j] - sum;

      if (temp >= amnlsq) {
        a[j][j] = Math.sqrt(temp);
      } else {

// FIND MAXIMUM OFF-DIAGONAL ELEMENT IN COLUMN

        offmax = 0.0;

        for (i = jp1; i <= n; i++) {
          if (Math.abs(a[i][j]) > offmax) {
            offmax = Math.abs(a[i][j]);
          }
        }

        if (offmax <= amnlsq) offmax = amnlsq;

// ADD TO DIAGONAL ELEMENT  TO ALLOW CHOLESKY DECOMPOSITION TO CONTINUE

        a[j][j] = Math.sqrt(offmax);
        addmax[1] = Math.max(addmax[1], offmax - temp);

      }

// FIND I,J ELEMENT OF LOWER TRIANGULAR MATRIX

      for (i = jp1; i <= n; i++) {
        sum = 0.0;

        for (k = 1; k <= jm1; k++) {
          sum += a[i][k] * a[j][k];
        }

        a[i][j] = (a[i][j] - sum) / a[j][j];
      }
    }
  }


  /**
   * <p/>
   * The dogdrv_f77 method finds the next Newton iterate (xpls) by the double dogleg
   * method.  It drives dogstp_f77.
   * <p/>
   * Translated by Steve Verrill, April 15, 1998.
   *
   * @param n        Dimension of the problem
   * @param x        The old iterate
   * @param f        Function value at the old iterate
   * @param g        Gradient or approximation at the old iterate
   * @param a        Cholesky decomposition of Hessian
   *                 in lower triangular part and diagonal
   * @param p        Newton step
   * @param xpls     The new iterate
   * @param fpls     Function value at the new iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param sx       Scaling vector for x
   * @param stepmx   Maximum allowable step size
   * @param steptl   Relative step size at which successive iterates
   *                 are considered close enough to terminate the
   *                 algorithm
   * @param dlt      Trust region radius (value needs to be retained
   *                 between successive calls)
   * @param iretcd   Return code:
   *                 0 --- satisfactory xpls found
   *                 1 --- failed to find satisfactory xpls
   *                 sufficently distinct from x
   * @param mxtake   Boolean flag indicating that a step of maximum
   *                 length was used length
   * @param sc       Workspace (current step)
   * @param wrk1     Workspace (and place holding argument to tregup)
   * @param wrk2     Workspace
   * @param wrk3     Workspace
   */

  public static void dogdrv_f77(int n, double x[], double f[], double g[],
                                double a[][], double p[], double xpls[],
                                double fpls[], UncminFunction minclass,
                                double sx[], double stepmx[], double steptl[],
                                double dlt[], int iretcd[], boolean mxtake[],
                                double sc[], double wrk1[], double wrk2[],
                                double wrk3[]) {

/*

Here is a copy of the dogdrv FORTRAN documentation:

      SUBROUTINE DOGDRV(NR,N,X,F,G,A,P,XPLS,FPLS,FCN,SX,STEPMX,
     +     STEPTL,DLT,IRETCD,MXTAKE,SC,WRK1,WRK2,WRK3,IPR)

C
C PURPOSE
C -------
C FIND A NEXT NEWTON ITERATE (XPLS) BY THE DOUBLE DOGLEG METHOD
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE X[K-1]
C F            --> FUNCTION VALUE AT OLD ITERATE, F(X)
C G(N)         --> GRADIENT  AT OLD ITERATE, G(X), OR APPROXIMATE
C A(N,N)       --> CHOLESKY DECOMPOSITION OF HESSIAN
C                  IN LOWER TRIANGULAR PART AND DIAGONAL
C P(N)         --> NEWTON STEP
C XPLS(N)     <--  NEW ITERATE X[K]
C FPLS        <--  FUNCTION VALUE AT NEW ITERATE, F(XPLS)
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C DLT         <--> TRUST REGION RADIUS
C                  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C IRETCD      <--  RETURN CODE
C                    =0 SATISFACTORY XPLS FOUND
C                    =1 FAILED TO FIND SATISFACTORY XPLS SUFFICIENTLY
C                       DISTINCT FROM X
C MXTAKE      <--  BOOLEAN FLAG INDICATING STEP OF MAXIMUM LENGTH USED
C SC(N)        --> WORKSPACE [CURRENT STEP]
C WRK1(N)      --> WORKSPACE (AND PLACE HOLDING ARGUMENT TO TREGUP)
C WRK2(N)      --> WORKSPACE
C WRK3(N)      --> WORKSPACE
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    int i;
    double tmp, rnwtln;
    double fplsp[] = new double[2];
    double cln[] = new double[2];
    double eta[] = new double[2];
    boolean fstdog[] = new boolean[2];
    boolean nwtake[] = new boolean[2];


    iretcd[1] = 4;
    fstdog[1] = true;
    tmp = 0.0;

    for (i = 1; i <= n; i++) {

      tmp += sx[i] * sx[i] * p[i] * p[i];

    }

    rnwtln = Math.sqrt(tmp);

    while (iretcd[1] > 1) {

// FIND NEW STEP BY DOUBLE DOGLEG ALGORITHM

      dogstp_f77(n, g, a, p, sx, rnwtln, dlt, nwtake, fstdog,
          wrk1, wrk2, cln, eta, sc, stepmx);

// CHECK NEW POINT AND UPDATE TRUST REGION

      tregup_f77(n, x, f, g, a, minclass, sc, sx, nwtake, stepmx,
          steptl, dlt, iretcd, wrk3, fplsp, xpls, fpls,
          mxtake, 2, wrk1);
    }
  }


  /**
   * <p/>
   * The dogstp_f77 method finds the new step by the double dogleg
   * appproach.
   * <p/>
   * Translated by Steve Verrill, April 21, 1998.
   *
   * @param n      DIMENSION OF PROBLEM
   * @param g      GRADIENT AT CURRENT ITERATE, G(X)
   * @param a      CHOLESKY DECOMPOSITION OF HESSIAN IN
   *               LOWER PART AND DIAGONAL
   * @param p      NEWTON STEP
   * @param sx     Scaling vector for x
   * @param rnwtln NEWTON STEP LENGTH
   * @param dlt    TRUST REGION RADIUS
   * @param nwtake BOOLEAN, = true IF NEWTON STEP TAKEN
   * @param fstdog BOOLEAN, = true IF ON FIRST LEG OF DOGLEG
   * @param ssd    WORKSPACE [CAUCHY STEP TO THE MINIMUM OF THE
   *               QUADRATIC MODEL IN THE SCALED STEEPEST DESCENT
   *               DIRECTION] [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
   * @param v      WORKSPACE  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
   * @param cln    CAUCHY LENGTH
   *               [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
   * @param eta    [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
   * @param sc     CURRENT STEP
   * @param stepmx MAXIMUM ALLOWABLE STEP SIZE
   */


  public static void dogstp_f77(int n, double g[], double a[][], double p[],
                                double sx[], double rnwtln, double dlt[],
                                boolean nwtake[], boolean fstdog[], double ssd[],
                                double v[], double cln[], double eta[],
                                double sc[], double stepmx[]) {

/*

Here is a copy of the dogstp FORTRAN documentation:

C
C PURPOSE
C -------
C FIND NEW STEP BY DOUBLE DOGLEG ALGORITHM
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C G(N)         --> GRADIENT AT CURRENT ITERATE, G(X)
C A(N,N)       --> CHOLESKY DECOMPOSITION OF HESSIAN IN
C                  LOWER PART AND DIAGONAL
C P(N)         --> NEWTON STEP
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C RNWTLN       --> NEWTON STEP LENGTH
C DLT         <--> TRUST REGION RADIUS
C NWTAKE      <--> BOOLEAN, =.TRUE. IF NEWTON STEP TAKEN
C FSTDOG      <--> BOOLEAN, =.TRUE. IF ON FIRST LEG OF DOGLEG
C SSD(N)      <--> WORKSPACE [CAUCHY STEP TO THE MINIMUM OF THE
C                  QUADRATIC MODEL IN THE SCALED STEEPEST DESCENT
C                  DIRECTION] [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C V(N)        <--> WORKSPACE  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C CLN         <--> CAUCHY LENGTH
C                  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C ETA              [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C SC(N)       <--  CURRENT STEP
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C
C INTERNAL VARIABLES
C ------------------
C CLN              LENGTH OF CAUCHY STEP
C

*/

    double alpha, beta, tmp, dot1, dot2, alam;
    int i, j;

// CAN WE TAKE NEWTON STEP

    if (rnwtln <= dlt[1]) {

      nwtake[1] = true;

      for (i = 1; i <= n; i++) {

        sc[i] = p[i];

      }

      dlt[1] = rnwtln;

    } else {

// NEWTON STEP TOO LONG
// CAUCHY STEP IS ON DOUBLE DOGLEG CURVE

      nwtake[1] = false;

      if (fstdog[1]) {

// CALCULATE DOUBLE DOGLEG CURVE (SSD)

        fstdog[1] = false;
        alpha = 0.0;

        for (i = 1; i <= n; i++) {

          alpha += (g[i] * g[i]) / (sx[i] * sx[i]);

        }

        beta = 0.0;

        for (i = 1; i <= n; i++) {

          tmp = 0.0;

          for (j = i; j <= n; j++) {

            tmp += (a[j][i] * g[j]) / (sx[j] * sx[j]);

          }

          beta += tmp * tmp;

        }

        for (i = 1; i <= n; i++) {

          ssd[i] = -(alpha / beta) * g[i] / sx[i];

        }

        cln[1] = alpha * Math.sqrt(alpha) / beta;

        eta[1] = .2 +
            (.8 * alpha * alpha) / (-beta * Blas_f77.ddot_f77(n, g, 1, p, 1));

        for (i = 1; i <= n; i++) {

          v[i] = eta[1] * sx[i] * p[i] - ssd[i];

        }

        if (dlt[1] == -1.0) dlt[1] = Math.min(cln[1], stepmx[1]);

      }

      if (eta[1] * rnwtln <= dlt[1]) {

// TAKE PARTIAL STEP IN NEWTON DIRECTION

        for (i = 1; i <= n; i++) {

          sc[i] = (dlt[1] / rnwtln) * p[i];

        }

      } else {

        if (cln[1] >= dlt[1]) {

// TAKE STEP IN STEEPEST DESCENT DIRECTION

          for (i = 1; i <= n; i++) {

            sc[i] = (dlt[1] / cln[1]) * ssd[i] / sx[i];

          }

        } else {

// CALCULATE CONVEX COMBINATION OF SSD AND ETA*P
// WHICH HAS SCALED LENGTH DLT

          dot1 = Blas_f77.ddot_f77(n, v, 1, ssd, 1);
          dot2 = Blas_f77.ddot_f77(n, v, 1, v, 1);

          alam = (-dot1 + Math.sqrt((dot1 * dot1) -
              dot2 * (cln[1] * cln[1] - dlt[1] * dlt[1]))) / dot2;

          for (i = 1; i <= n; i++) {

            sc[i] = (ssd[i] + alam * v[i]) / sx[i];

          }
        }
      }
    }
  }


  /**
   * <p/>
   * The forslv_f77 method solves Ax = b where A is a lower triangular matrix.
   * <p/>
   * Translated by Steve Verrill, April 21, 1998.
   *
   * @param n The dimension of the problem
   * @param a The lower triangular matrix (preserved)
   * @param x The solution vector
   * @param b The right-hand side vector
   */

  public static void forslv_f77(int n, double a[][], double x[],
                                double b[]) {

/*

Here is a copy of the forslv FORTRAN documentation:

      SUBROUTINE FORSLV(NR,N,A,X,B)

C
C PURPOSE
C -------
C SOLVE  AX=B  WHERE A IS LOWER TRIANGULAR MATRIX
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)       --> LOWER TRIANGULAR MATRIX (PRESERVED)
C X(N)        <--  SOLUTION VECTOR
C B(N)         --> RIGHT-HAND SIDE VECTOR
C
C NOTE
C ----
C IF B IS NO LONGER REQUIRED BY CALLING ROUTINE,
C THEN VECTORS B AND X MAY SHARE THE SAME STORAGE.
C

*/

    int i, im1, j;
    double sum;

// SOLVE LX=B. (FORWARD SOLVE)

    x[1] = b[1] / a[1][1];

    for (i = 2; i <= n; i++) {

      sum = 0.0;
      im1 = i - 1;

      for (j = 1; j <= im1; j++) {

        sum += a[i][j] * x[j];

      }

      x[i] = (b[i] - sum) / a[i][i];

    }

    return;

  }


  /**
   * <p/>
   * The fstocd_f77 method finds a central difference approximation to the
   * gradient of the function to be minimized.
   * <p/>
   * Translated by Steve Verrill, April 21, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        The point at which the gradient is to be approximated
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param sx       Scaling vector for x
   * @param rnoise   Relative noise in the function to be minimized
   * @param g        A central difference approximation to the gradient
   */


  public static void fstocd_f77(int n, double x[], UncminFunction minclass,
                                double sx[], double rnoise, double g[]) {

/*

Here is a copy of the fstocd FORTRAN documentation:

      SUBROUTINE FSTOCD (N, X, FCN, SX, RNOISE, G)

C PURPOSE
C -------
C FIND CENTRAL DIFFERENCE APPROXIMATION G TO THE FIRST DERIVATIVE
C (GRADIENT) OF THE FUNCTION DEFINED BY FCN AT THE POINT X.
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF PROBLEM
C X            --> POINT AT WHICH GRADIENT IS TO BE APPROXIMATED.
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION.
C SX           --> DIAGONAL SCALING MATRIX FOR X.
C RNOISE       --> RELATIVE NOISE IN FCN [F(X)].
C G           <--  CENTRAL DIFFERENCE APPROXIMATION TO GRADIENT.
C
C

*/

    double stepi, xtempi, fplus, fminus, xmult;
    int i;

// FIND I-TH  STEPSIZE, EVALUATE TWO NEIGHBORS IN DIRECTION OF I-TH
// UNIT VECTOR, AND EVALUATE I-TH  COMPONENT OF GRADIENT.

    xmult = Math.pow(rnoise, 1.0 / 3.0);

    for (i = 1; i <= n; i++) {

      stepi = xmult * Math.max(Math.abs(x[i]), 1.0 / sx[i]);
      xtempi = x[i];

      x[i] = xtempi + stepi;
      fplus = minclass.apply(x);

      x[i] = xtempi - stepi;
      fminus = minclass.apply(x);

      x[i] = xtempi;

      g[i] = (fplus - fminus) / (2.0 * stepi);

    }

    return;

  }


  /**
   * <p/>
   * This version of the fstofd_f77 method finds a finite difference approximation
   * to the Hessian.
   * <p/>
   * <p/>
   * Translated by Steve Verrill, April 22, 1998.
   *
   * @param n        The dimension of the problem
   * @param xpls     New iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param fpls     fpls[1] -- fpls[n] contains the gradient
   *                 of the function to minimize
   * @param a        "FINITE DIFFERENCE APPROXIMATION.  ONLY
   *                 LOWER TRIANGULAR MATRIX AND DIAGONAL ARE RETURNED"
   * @param sx       Scaling vector for x
   * @param rnoise   Relative noise in the function to be minimized
   * @param fhat     Workspace
   */

  public static void fstofd_f77(int n, double xpls[], UncminFunction minclass,
                                double fpls[], double a[][], double sx[],
                                double rnoise, double fhat[]) {

/*

Here is a copy of the fstofd FORTRAN documentation.  This
is not entirely relevant as this Java version of fstofd
only estimates the Hessian.

      SUBROUTINE FSTOFD(NR,M,N,XPLS,FCN,FPLS,A,SX,RNOISE,FHAT,ICASE)

C PURPOSE
C -------
C FIND FIRST ORDER FORWARD FINITE DIFFERENCE APPROXIMATION "A" TO THE
C FIRST DERIVATIVE OF THE FUNCTION DEFINED BY THE SUBPROGRAM "FNAME"
C EVALUATED AT THE NEW ITERATE "XPLS".
C
C
C FOR OPTIMIZATION USE THIS ROUTINE TO ESTIMATE:
C 1) THE FIRST DERIVATIVE (GRADIENT) OF THE OPTIMIZATION FUNCTION "FCN
C    ANALYTIC USER ROUTINE HAS BEEN SUPPLIED;
C 2) THE SECOND DERIVATIVE (HESSIAN) OF THE OPTIMIZATION FUNCTION
C    IF NO ANALYTIC USER ROUTINE HAS BEEN SUPPLIED FOR THE HESSIAN BUT
C    ONE HAS BEEN SUPPLIED FOR THE GRADIENT ("FCN") AND IF THE
C    OPTIMIZATION FUNCTION IS INEXPENSIVE TO EVALUATE
C
C NOTE
C ----
C _M=1 (OPTIMIZATION) ALGORITHM ESTIMATES THE GRADIENT OF THE FUNCTION
C      (FCN).   FCN(X) # F: R(N)-->R(1)
C _M=N (SYSTEMS) ALGORITHM ESTIMATES THE JACOBIAN OF THE FUNCTION
C      FCN(X) # F: R(N)-->R(N).
C _M=N (OPTIMIZATION) ALGORITHM ESTIMATES THE HESSIAN OF THE OPTIMIZATIO
C      FUNCTION, WHERE THE HESSIAN IS THE FIRST DERIVATIVE OF "FCN"
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C M            --> NUMBER OF ROWS IN A
C N            --> NUMBER OF COLUMNS IN A; DIMENSION OF PROBLEM
C XPLS(N)      --> NEW ITERATE:  X[K]
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C FPLS(M)      --> _M=1 (OPTIMIZATION) FUNCTION VALUE AT NEW ITERATE:
C                       FCN(XPLS)
C                  _M=N (OPTIMIZATION) VALUE OF FIRST DERIVATIVE
C                       (GRADIENT) GIVEN BY USER FUNCTION FCN
C                  _M=N (SYSTEMS)  FUNCTION VALUE OF ASSOCIATED
C                       MINIMIZATION FUNCTION
C A(NR,N)     <--  FINITE DIFFERENCE APPROXIMATION (SEE NOTE).  ONLY
C                  LOWER TRIANGULAR MATRIX AND DIAGONAL ARE RETURNED
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C RNOISE       --> RELATIVE NOISE IN FCN [F(X)]
C FHAT(M)      --> WORKSPACE
C ICASE        --> =1 OPTIMIZATION (GRADIENT)
C                  =2 SYSTEMS
C                  =3 OPTIMIZATION (HESSIAN)
C
C INTERNAL VARIABLES
C ------------------
C STEPSZ - STEPSIZE IN THE J-TH VARIABLE DIRECTION
C

*/

    double xmult, stepsz, xtmpj;
    int i, j, nm1;

    xmult = Math.sqrt(rnoise);


    for (j = 1; j <= n; j++) {

      stepsz = xmult * Math.max(Math.abs(xpls[j]), 1.0 / sx[j]);
      xtmpj = xpls[j];
      xpls[j] = xtmpj + stepsz;

      minclass.applyGradient(xpls, fhat);

      xpls[j] = xtmpj;

      for (i = 1; i <= n; i++) {

        a[i][j] = (fhat[i] - fpls[i]) / stepsz;

      }

    }

    nm1 = n - 1;

    for (j = 1; j <= nm1; j++) {

      for (i = j + 1; i <= n; i++) {

        a[i][j] = (a[i][j] + a[j][i]) / 2.0;

      }

    }

    return;

  }


  /**
   * <p/>
   * This version of the fstofd_f77 method finds first order finite difference
   * approximations for gradients.
   * <p/>
   * Translated by Steve Verrill, April 22, 1998.
   *
   * @param n        The dimension of the problem
   * @param xpls     New iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param fpls     fpls contains the value of the
   *                 function to minimize at the new iterate
   * @param g        finite difference approximation to the gradient
   * @param sx       Scaling vector for x
   * @param rnoise   Relative noise in the function to be minimized
   */


  public static void fstofd_f77(int n, double xpls[], UncminFunction minclass,
                                double fpls[], double g[], double sx[],
                                double rnoise) {

/*

Here is a copy of the fstofd FORTRAN documentation.  It
is not entirely relevant here as this particular
Java method is only used for gradient calculations.

      SUBROUTINE FSTOFD(NR,M,N,XPLS,FCN,FPLS,A,SX,RNOISE,FHAT,ICASE)

C PURPOSE
C -------
C FIND FIRST ORDER FORWARD FINITE DIFFERENCE APPROXIMATION "A" TO THE
C FIRST DERIVATIVE OF THE FUNCTION DEFINED BY THE SUBPROGRAM "FNAME"
C EVALUATED AT THE NEW ITERATE "XPLS".
C
C
C FOR OPTIMIZATION USE THIS ROUTINE TO ESTIMATE:
C 1) THE FIRST DERIVATIVE (GRADIENT) OF THE OPTIMIZATION FUNCTION "FCN
C    ANALYTIC USER ROUTINE HAS BEEN SUPPLIED;
C 2) THE SECOND DERIVATIVE (HESSIAN) OF THE OPTIMIZATION FUNCTION
C    IF NO ANALYTIC USER ROUTINE HAS BEEN SUPPLIED FOR THE HESSIAN BUT
C    ONE HAS BEEN SUPPLIED FOR THE GRADIENT ("FCN") AND IF THE
C    OPTIMIZATION FUNCTION IS INEXPENSIVE TO EVALUATE
C
C NOTE
C ----
C _M=1 (OPTIMIZATION) ALGORITHM ESTIMATES THE GRADIENT OF THE FUNCTION
C      (FCN).   FCN(X) # F: R(N)-->R(1)
C _M=N (SYSTEMS) ALGORITHM ESTIMATES THE JACOBIAN OF THE FUNCTION
C      FCN(X) # F: R(N)-->R(N).
C _M=N (OPTIMIZATION) ALGORITHM ESTIMATES THE HESSIAN OF THE OPTIMIZATIO
C      FUNCTION, WHERE THE HESSIAN IS THE FIRST DERIVATIVE OF "FCN"
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C M            --> NUMBER OF ROWS IN A
C N            --> NUMBER OF COLUMNS IN A; DIMENSION OF PROBLEM
C XPLS(N)      --> NEW ITERATE:  X[K]
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C FPLS(M)      --> _M=1 (OPTIMIZATION) FUNCTION VALUE AT NEW ITERATE:
C                       FCN(XPLS)
C                  _M=N (OPTIMIZATION) VALUE OF FIRST DERIVATIVE
C                       (GRADIENT) GIVEN BY USER FUNCTION FCN
C                  _M=N (SYSTEMS)  FUNCTION VALUE OF ASSOCIATED
C                       MINIMIZATION FUNCTION
C A(NR,N)     <--  FINITE DIFFERENCE APPROXIMATION (SEE NOTE).  ONLY
C                  LOWER TRIANGULAR MATRIX AND DIAGONAL ARE RETURNED
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C RNOISE       --> RELATIVE NOISE IN FCN [F(X)]
C FHAT(M)      --> WORKSPACE
C ICASE        --> =1 OPTIMIZATION (GRADIENT)
C                  =2 SYSTEMS
C                  =3 OPTIMIZATION (HESSIAN)
C
C INTERNAL VARIABLES
C ------------------
C STEPSZ - STEPSIZE IN THE J-TH VARIABLE DIRECTION
C

*/

    double xmult, stepsz, xtmpj, fhat;
    int j;

    xmult = Math.sqrt(rnoise);

// gradient

    for (j = 1; j <= n; j++) {

      stepsz = xmult * Math.max(Math.abs(xpls[j]), 1.0 / sx[j]);
      xtmpj = xpls[j];
      xpls[j] = xtmpj + stepsz;

      fhat = minclass.apply(xpls);

      xpls[j] = xtmpj;

      g[j] = (fhat - fpls[1]) / stepsz;

    }

    return;

  }


  /**
   * <p/>
   * The grdchk_f77 method checks the analytic gradient supplied
   * by the user.
   * <p/>
   * Translated by Steve Verrill, April 22, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        The location at which the gradient is to be checked
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param f        Function value
   * @param g        Analytic gradient
   * @param typsiz   Typical size for each component of x
   * @param sx       Scaling vector for x:  sx[i] = 1.0/typsiz[i]
   * @param fscale   Estimate of scale of f_to_minimize
   * @param rnf      Relative noise in f_to_minimize
   * @param analtl   Tolerance for comparison of estimated and
   *                 analytical gradients
   * @param gest     Finite difference gradient
   */

  public static void grdchk_f77(int n, double x[], UncminFunction minclass,
                                double f[], double g[], double typsiz[],
                                double sx[], double fscale, double rnf,
                                double analtl, double gest[]) {

/*

Here is a copy of the grdchk FORTRAN documentation:

      SUBROUTINE GRDCHK(N,X,FCN,F,G,TYPSIZ,SX,FSCALE,RNF,
     +     ANALTL,WRK1,MSG,IPR)

C
C PURPOSE
C -------
C CHECK ANALYTIC GRADIENT AGAINST ESTIMATED GRADIENT
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ESTIMATE TO A ROOT OF FCN
C FCN          --> NAME OF SUBROUTINE TO EVALUATE OPTIMIZATION FUNCTION
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C                       FCN:  R(N) --> R(1)
C F            --> FUNCTION VALUE:  FCN(X)
C G(N)         --> GRADIENT:  G(X)
C TYPSIZ(N)    --> TYPICAL SIZE FOR EACH COMPONENT OF X
C SX(N)        --> DIAGONAL SCALING MATRIX:  SX(I)=1./TYPSIZ(I)
C FSCALE       --> ESTIMATE OF SCALE OF OBJECTIVE FUNCTION FCN
C RNF          --> RELATIVE NOISE IN OPTIMIZATION FUNCTION FCN
C ANALTL       --> TOLERANCE FOR COMPARISON OF ESTIMATED AND
C                  ANALYTICAL GRADIENTS
C WRK1(N)      --> WORKSPACE
C MSG         <--  MESSAGE OR ERROR CODE
C                    ON OUTPUT: =-21, PROBABLE CODING ERROR OF GRADIENT
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    double gs;
    int ker, i;

// COMPUTE FIRST ORDER FINITE DIFFERENCE GRADIENT AND COMPARE TO
// ANALYTIC GRADIENT.

    fstofd_f77(n, x, minclass, f, gest, sx, rnf);

    ker = 0;

    for (i = 1; i <= n; i++) {

      gs = Math.max(Math.abs(f[1]), fscale) / Math.max(Math.abs(x[i]), typsiz[i]);

      if (Math.abs(g[i] - gest[i]) >
          Math.max(Math.abs(g[i]), gs) * analtl) ker = 1;

    }

    if (ker == 0) return;

    StringBuilder errmsg = new StringBuilder();
    errmsg.append("\nThere appears to be an error in the coding");
    errmsg.append(" of the gradient method.\n\n\n");
    errmsg.append("Component   Analytic   Finite Difference\n\n");

    for (i = 1; i <= n; i++) {

      errmsg.append(i + "  " + g[i] + "  " + gest[i]);

    }

    throw new IllegalArgumentException(errmsg.toString());

  }


  /**
   * <p/>
   * The heschk_f77 method checks the analytic Hessian supplied
   * by the user.
   * <p/>
   * Translated by Steve Verrill, April 23, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        The location at which the Hessian is to be checked
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param f        Function value
   * @param g        Gradient
   * @param a        On exit: Hessian in lower triangle
   * @param typsiz   Typical size for each component of x
   * @param sx       Scaling vector for x:  sx[i] = 1.0/typsiz[i]
   * @param rnf      Relative noise in f_to_minimize
   * @param analtl   Tolerance for comparison of estimated and
   *                 analytic gradients
   * @param iagflg   = 1 if an analytic gradient is supplied
   * @param udiag    Workspace
   * @param wrk1     Workspace
   * @param wrk2     Workspace
   */

  public static void heschk_f77(int n, double x[], UncminFunction minclass,
                                double f[], double g[], double a[][],
                                double typsiz[],
                                double sx[], double rnf,
                                double analtl, int iagflg[],
                                double udiag[], double wrk1[],
                                double wrk2[]) {

/*

Here is a copy of the heschk FORTRAN documentation:

      SUBROUTINE HESCHK(NR,N,X,FCN,D1FCN,D2FCN,F,G,A,TYPSIZ,SX,RNF,
     +     ANALTL,IAGFLG,UDIAG,WRK1,WRK2,MSG,IPR)

C
C PURPOSE
C -------
C CHECK ANALYTIC HESSIAN AGAINST ESTIMATED HESSIAN
C  (THIS MAY BE DONE ONLY IF THE USER SUPPLIED ANALYTIC HESSIAN
C   D2FCN FILLS ONLY THE LOWER TRIANGULAR PART AND DIAGONAL OF A)
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ESTIMATE TO A ROOT OF FCN
C FCN          --> NAME OF SUBROUTINE TO EVALUATE OPTIMIZATION FUNCTION
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C                       FCN:  R(N) --> R(1)
C D1FCN        --> NAME OF SUBROUTINE TO EVALUATE GRADIENT OF FCN.
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C D2FCN        --> NAME OF SUBROUTINE TO EVALUATE HESSIAN OF FCN.
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C F            --> FUNCTION VALUE:  FCN(X)
C G(N)        <--  GRADIENT:  G(X)
C A(N,N)      <--  ON EXIT:  HESSIAN IN LOWER TRIANGULAR PART AND DIAG
C TYPSIZ(N)    --> TYPICAL SIZE FOR EACH COMPONENT OF X
C SX(N)        --> DIAGONAL SCALING MATRIX:  SX(I)=1./TYPSIZ(I)
C RNF          --> RELATIVE NOISE IN OPTIMIZATION FUNCTION FCN
C ANALTL       --> TOLERANCE FOR COMPARISON OF ESTIMATED AND
C                  ANALYTICAL GRADIENTS
C IAGFLG       --> =1 IF ANALYTIC GRADIENT SUPPLIED
C UDIAG(N)     --> WORKSPACE
C WRK1(N)      --> WORKSPACE
C WRK2(N)      --> WORKSPACE
C MSG         <--> MESSAGE OR ERROR CODE
C                    ON INPUT : IF =1XX DO NOT COMPARE ANAL + EST HESS
C                    ON OUTPUT: =-22, PROBABLE CODING ERROR OF HESSIAN
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    int i, j, ker;
    double hs;


// COMPUTE FINITE DIFFERENCE APPROXIMATION H TO THE HESSIAN.

    if (iagflg[1] == 1) fstofd_f77(n, x, minclass, g, a, sx, rnf, wrk1);

    if (iagflg[1] != 1) sndofd_f77(n, x, minclass, f, a, sx, rnf, wrk1, wrk2);

    ker = 0;

// COPY LOWER TRIANGULAR PART OF H TO UPPER TRIANGULAR PART
// AND DIAGONAL OF H TO UDIAG

    for (j = 1; j <= n; j++) {

      udiag[j] = a[j][j];

      for (i = j + 1; i <= n; i++) {

        a[j][i] = a[i][j];

      }

    }

// COMPUTE ANALYTIC HESSIAN AND COMPARE TO FINITE DIFFERENCE
// APPROXIMATION.

    minclass.applyHessian(x, a);

    for (j = 1; j <= n; j++) {

      hs = Math.max(Math.abs(g[j]), 1.0) / Math.max(Math.abs(x[j]), typsiz[j]);

      if (Math.abs(a[j][j] - udiag[j]) >
          Math.max(Math.abs(udiag[j]), hs) * analtl) ker = 1;

      for (i = j + 1; i <= n; i++) {

        if (Math.abs(a[i][j] - a[j][i]) >
            Math.max(Math.abs(a[i][j]), hs) * analtl) ker = 1;

      }

    }

    if (ker == 0) return;

    StringBuilder sb = new StringBuilder();
    sb.append("\nThere appears to be an error in the coding");
    sb.append(" of the Hessian method.\n\n\n");
    sb.append("Row   Column   Analytic   Finite Difference\n\n");

    for (i = 1; i <= n; i++) {

      for (j = 1; j < i; j++) {

        sb.append(i + "  " + j + "  " + a[i][j] + "  " + a[j][i] + "\n");

      }

      sb.append(i + "  " + i + "  " + a[i][i] + "  " + udiag[i] + "\n");

    }

    throw new IllegalArgumentException(sb.toString());

  }


  /**
   * <p/>
   * The hookdr_f77 method finds a next Newton iterate (xpls) by the More-Hebdon
   * technique.  It drives hookst_f77.
   * <p/>
   * Translated by Steve Verrill, April 23, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        The old iterate
   * @param f        The function value at the old iterate
   * @param g        Gradient or approximation at old iterate
   * @param a        Cholesky decomposition of Hessian in lower triangle
   *                 and diagonal.  Hessian in upper triangle and udiag.
   * @param udiag    Diagonal of Hessian in a
   * @param p        Newton step
   * @param xpls     New iterate
   * @param fpls     Function value at the new iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param sx       Scaling vector for x
   * @param stepmx   Maximum allowable step size
   * @param steptl   Relative step size at which consecutive iterates
   *                 are considered close enough to terminate the algorithm
   * @param dlt      Trust region radius
   * @param iretcd   Return code
   *                 = 0  satisfactory xpls found
   *                 = 1  failed to find satisfactory xpls
   *                 sufficiently distinct from x
   * @param mxtake   Boolean flag indicating step of maximum length used
   * @param amu      [Retain value between successive calls]
   * @param dltp     [Retain value between successive calls]
   * @param phi      [Retain value between successive calls]
   * @param phip0    [Retain value between successive calls]
   * @param sc       Workspace
   * @param xplsp    Workspace
   * @param wrk0     Workspace
   * @param epsm     Machine epsilon
   * @param itncnt   Iteration count
   */

  public static void hookdr_f77(int n, double x[], double f[], double g[],
                                double a[][], double udiag[],
                                double p[], double xpls[], double fpls[],
                                UncminFunction minclass,
                                double sx[], double stepmx[],
                                double steptl[], double dlt[],
                                int iretcd[], boolean mxtake[],
                                double amu[], double dltp[],
                                double phi[], double phip0[],
                                double sc[], double xplsp[],
                                double wrk0[], double epsm,
                                int itncnt[]) {

/*

Here is a copy of the hookdr FORTRAN documentation:

      SUBROUTINE HOOKDR(NR,N,X,F,G,A,UDIAG,P,XPLS,FPLS,FCN,SX,STEPMX,
     +     STEPTL,DLT,IRETCD,MXTAKE,AMU,DLTP,PHI,PHIP0,
     +     SC,XPLSP,WRK0,EPSM,ITNCNT,IPR)

C
C PURPOSE
C -------
C FIND A NEXT NEWTON ITERATE (XPLS) BY THE MORE-HEBDON METHOD
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE X[K-1]
C F            --> FUNCTION VALUE AT OLD ITERATE, F(X)
C G(N)         --> GRADIENT AT OLD ITERATE, G(X), OR APPROXIMATE
C A(N,N)       --> CHOLESKY DECOMPOSITION OF HESSIAN IN LOWER
C                  TRIANGULAR PART AND DIAGONAL.
C                  HESSIAN IN UPPER TRIANGULAR PART AND UDIAG.
C UDIAG(N)     --> DIAGONAL OF HESSIAN IN A(.,.)
C P(N)         --> NEWTON STEP
C XPLS(N)     <--  NEW ITERATE X[K]
C FPLS        <--  FUNCTION VALUE AT NEW ITERATE, F(XPLS)
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C DLT         <--> TRUST REGION RADIUS
C IRETCD      <--  RETURN CODE
C                    =0 SATISFACTORY XPLS FOUND
C                    =1 FAILED TO FIND SATISFACTORY XPLS SUFFICIENTLY
C                       DISTINCT FROM X
C MXTAKE      <--  BOOLEAN FLAG INDICATING STEP OF MAXIMUM LENGTH USED
C AMU         <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C DLTP        <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C PHI         <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C PHIP0       <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C SC(N)        --> WORKSPACE
C XPLSP(N)     --> WORKSPACE
C WRK0(N)      --> WORKSPACE
C EPSM         --> MACHINE EPSILON
C ITNCNT       --> ITERATION COUNT
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/


    int i, j;
    boolean fstime[] = new boolean[2];
    boolean nwtake[] = new boolean[2];
    double tmp, rnwtln, alpha, beta;

    double fplsp[] = new double[2];

    iretcd[1] = 4;
    fstime[1] = true;

    tmp = 0.0;

    for (i = 1; i <= n; i++) {

      tmp += sx[i] * sx[i] * p[i] * p[i];

    }

    rnwtln = Math.sqrt(tmp);

    if (itncnt[1] == 1) {

      amu[1] = 0.0;

// IF FIRST ITERATION AND TRUST REGION NOT PROVIDED BY USER,
// COMPUTE INITIAL TRUST REGION.

      if (dlt[1] == -1.0) {

        alpha = 0.0;

        for (i = 1; i <= n; i++) {

          alpha += (g[i] * g[i]) / (sx[i] * sx[i]);

        }

        beta = 0.0;

        for (i = 1; i <= n; i++) {

          tmp = 0.0;

          for (j = i; j <= n; j++) {

            tmp += (a[j][i] * g[j]) / (sx[j] * sx[j]);

          }

          beta += tmp * tmp;

        }

        dlt[1] = alpha * Math.sqrt(alpha) / beta;
        dlt[1] = Math.min(dlt[1], stepmx[1]);

      }

    }

    while (iretcd[1] > 1) {

// FIND NEW STEP BY MORE-HEBDON ALGORITHM

      hookst_f77(n, g, a, udiag, p, sx, rnwtln, dlt,
          amu, dltp, phi, phip0,
          fstime, sc, nwtake, wrk0, epsm);

      dltp[1] = dlt[1];

// CHECK NEW POINT AND UPDATE TRUST REGION

      tregup_f77(n, x, f, g, a, minclass, sc, sx, nwtake, stepmx,
          steptl, dlt, iretcd, xplsp, fplsp, xpls, fpls,
          mxtake, 3, udiag);

    }

    return;

  }


  /**
   * <p/>
   * The hookst_f77 method finds a new step by the More-Hebdon algorithm.
   * It is driven by hookdr_f77.
   * <p/>
   * Translated by Steve Verrill, April 24, 1998.
   *
   * @param n      The dimension of the problem
   * @param g      The gradient at the current iterate
   * @param a      Cholesky decomposition of the Hessian in
   *               the lower triangle and diagonal.  Hessian
   *               or approximation in upper triangle (and udiag).
   * @param p      Newton step
   * @param sx     Scaling vector for x
   * @param rnwtln Newton step length
   * @param dlt    Trust region radius
   * @param amu    Retain value between successive calls
   * @param dltp   Trust region radius at last exit from
   *               this routine
   * @param phi    Retain value between successive calls
   * @param phip0  Retain value between successive calls
   * @param fstime "= true if first entry to this routine
   *               during the k-th iteration"
   * @param sc     Current step
   * @param nwtake = true if Newton step taken
   * @param wrk0   Workspace
   * @param epsm   Machine epsilon
   * @udiag Diagonal of Hessian in a
   */

  public static void hookst_f77(int n, double g[], double a[][], double udiag[],
                                double p[], double sx[], double rnwtln,
                                double dlt[], double amu[], double dltp[],
                                double phi[], double phip0[],
                                boolean fstime[], double sc[], boolean nwtake[],
                                double wrk0[], double epsm) {

/*

Here is a copy of the hookst FORTRAN documentation:

      SUBROUTINE HOOKST(NR,N,G,A,UDIAG,P,SX,RNWTLN,DLT,AMU,
     +     DLTP,PHI,PHIP0,FSTIME,SC,NWTAKE,WRK0,EPSM,IPR)

C
C PURPOSE
C -------
C FIND NEW STEP BY MORE-HEBDON ALGORITHM
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C G(N)         --> GRADIENT AT CURRENT ITERATE, G(X)
C A(N,N)       --> CHOLESKY DECOMPOSITION OF HESSIAN IN
C                  LOWER TRIANGULAR PART AND DIAGONAL.
C                  HESSIAN OR APPROX IN UPPER TRIANGULAR PART
C UDIAG(N)     --> DIAGONAL OF HESSIAN IN A(.,.)
C P(N)         --> NEWTON STEP
C SX(N)        --> DIAGONAL SCALING MATRIX FOR N
C RNWTLN       --> NEWTON STEP LENGTH
C DLT         <--> TRUST REGION RADIUS
C AMU         <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C DLTP         --> TRUST REGION RADIUS AT LAST EXIT FROM THIS ROUTINE
C PHI         <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C PHIP0       <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C FSTIME      <--> BOOLEAN. =.TRUE. IF FIRST ENTRY TO THIS ROUTINE
C                  DURING K-TH ITERATION
C SC(N)       <--  CURRENT STEP
C NWTAKE      <--  BOOLEAN, =.TRUE. IF NEWTON STEP TAKEN
C WRK0         --> WORKSPACE
C EPSM         --> MACHINE EPSILON
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    double hi, alo;
    double phip, amulo, amuup, stepln;
    int i, j;
    boolean done;

    double addmax[] = new double[2];

// HI AND ALO ARE CONSTANTS USED IN THIS ROUTINE.
// CHANGE HERE IF OTHER VALUES ARE TO BE SUBSTITUTED.

    phip = 0.0;
    hi = 1.5;
    alo = .75;

    if (rnwtln <= hi * dlt[1]) {

//       TAKE NEWTON STEP

      nwtake[1] = true;

      for (i = 1; i <= n; i++) {

        sc[i] = p[i];

      }

      dlt[1] = Math.min(dlt[1], rnwtln);
      amu[1] = 0.0;

      return;

    } else {

// NEWTON STEP NOT TAKEN

      nwtake[1] = false;

      if (amu[1] > 0.0) {

        amu[1] -= (phi[1] + dltp[1]) * ((dltp[1] - dlt[1]) + phi[1]) / (dlt[1] * phip);

      }

      phi[1] = rnwtln - dlt[1];

      if (fstime[1]) {

        for (i = 1; i <= n; i++) {

          wrk0[i] = sx[i] * sx[i] * p[i];

        }

// SOLVE L*Y = (SX**2)*P

        forslv_f77(n, a, wrk0, wrk0);
        phip0[1] = -Math.pow(Blas_f77.dnrm2_f77(n, wrk0, 1), 2) / rnwtln;
        fstime[1] = false;

      }

      phip = phip0[1];
      amulo = -phi[1] / phip;

      amuup = 0.0;

      for (i = 1; i <= n; i++) {

        amuup += (g[i] * g[i]) / (sx[i] * sx[i]);

      }

      amuup = Math.sqrt(amuup) / dlt[1];

      done = false;

// TEST VALUE OF AMU; GENERATE NEXT AMU IF NECESSARY

      while (!done) {

        if (amu[1] < amulo || amu[1] > amuup) {

          amu[1] = Math.max(Math.sqrt(amulo * amuup), amuup * .001);

        }

// COPY (H,UDIAG) TO L
// WHERE H <-- H+AMU*(SX**2) [DO NOT ACTUALLY CHANGE (H,UDIAG)]

        for (j = 1; j <= n; j++) {

          a[j][j] = udiag[j] + amu[1] * sx[j] * sx[j];

          for (i = j + 1; i <= n; i++) {

            a[i][j] = a[j][i];

          }

        }

// FACTOR H=L(L+)

        choldc_f77(n, a, 0.0, Math.sqrt(epsm), addmax);

// SOLVE H*P = L(L+)*SC = -G

        for (i = 1; i <= n; i++) {

          wrk0[i] = -g[i];

        }

        lltslv_f77(n, a, sc, wrk0);

// RESET H.  NOTE SINCE UDIAG HAS NOT BEEN DESTROYED WE NEED DO
// NOTHING HERE.  H IS IN THE UPPER PART AND IN UDIAG, STILL INTACT

        stepln = 0.0;

        for (i = 1; i <= n; i++) {

          stepln += sx[i] * sx[i] * sc[i] * sc[i];

        }

        stepln = Math.sqrt(stepln);
        phi[1] = stepln - dlt[1];

        for (i = 1; i <= n; i++) {

          wrk0[i] = sx[i] * sx[i] * sc[i];

        }

        forslv_f77(n, a, wrk0, wrk0);

        phip = -Math.pow(Blas_f77.dnrm2_f77(n, wrk0, 1), 2) / stepln;

        if ((alo * dlt[1] <= stepln && stepln <= hi * dlt[1]) ||
            (amuup - amulo <= 0.0)) {

// SC IS ACCEPTABLE HOOKSTEP

          done = true;

        } else {

// SC NOT ACCEPTABLE HOOKSTEP.  SELECT NEW AMU

          amulo = Math.max(amulo, amu[1] - (phi[1] / phip));
          if (phi[1] < 0.0) amuup = Math.min(amuup, amu[1]);
          amu[1] -= (stepln * phi[1]) / (dlt[1] * phip);

        }

      }

      return;

    }

  }


  /**
   * <p/>
   * The hsnint_f77 method provides the initial Hessian when secant
   * updates are being used.
   * <p/>
   * Translated by Steve Verrill, April 27, 1998.
   *
   * @param n      The dimension of the problem
   * @param a      Initial Hessian (lower triangular matrix)
   * @param sx     Scaling vector for x
   * @param method Algorithm to use to solve the minimization problem
   *               1,2 --- factored secant method
   *               3 --- unfactored secant method
   */

  public static void hsnint_f77(int n, double a[][], double sx[],
                                Method method) {

/*

Here is a copy of the hsnint FORTRAN documentation:

      SUBROUTINE HSNINT(NR,N,A,SX,METHOD)

C
C PURPOSE
C -------
C PROVIDE INITIAL HESSIAN WHEN USING SECANT UPDATES
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)      <--  INITIAL HESSIAN (LOWER TRIANGULAR MATRIX)
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C METHOD       --> ALGORITHM TO USE TO SOLVE MINIMIZATION PROBLEM
C                    =1,2 FACTORED SECANT METHOD USED
C                    =3   UNFACTORED SECANT METHOD USED
C

*/

    int i, j;

    for (j = 1; j <= n; j++) {

      if (method == Method.MORE_HEBDON) {

        a[j][j] = sx[j] * sx[j];

      } else {

        a[j][j] = sx[j];

      }

      for (i = j + 1; i <= n; i++) {

        a[i][j] = 0.0;

      }

    }

    return;

  }

  /**
   * <p/>
   * The lltslv_f77 method solves Ax = b where A has the form L(L transpose)
   * but only the lower triangular part, L, is stored.
   * <p/>
   * Translated by Steve Verrill, April 27, 1998.
   *
   * @param n The dimension of the problem
   * @param a Matrix of form L(L transpose).
   *          On return a is unchanged.
   * @param x The solution vector
   * @param b The right-hand side vector
   */


  public static void lltslv_f77(int n, double a[][], double x[],
                                double b[]) {

/*

Here is a copy of the lltslv FORTRAN documentation:

      SUBROUTINE LLTSLV(NR,N,A,X,B)

C
C PURPOSE
C -------
C SOLVE AX=B WHERE A HAS THE FORM L(L-TRANSPOSE)
C BUT ONLY THE LOWER TRIANGULAR PART, L, IS STORED.
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)       --> MATRIX OF FORM L(L-TRANSPOSE).
C                  ON RETURN A IS UNCHANGED.
C X(N)        <--  SOLUTION VECTOR
C B(N)         --> RIGHT-HAND SIDE VECTOR
C
C NOTE
C ----
C IF B IS NOT REQUIRED BY CALLING PROGRAM, THEN
C B AND X MAY SHARE THE SAME STORAGE.
C

*/

// FORWARD SOLVE, RESULT IN X

    forslv_f77(n, a, x, b);

// BACK SOLVE, RESULT IN X

    bakslv_f77(n, a, x, x);

    return;

  }


  /**
   * <p/>
   * The lnsrch_f77 method finds a next Newton iterate by line search.
   * <p/>
   * Translated by Steve Verrill, May 15, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        Old iterate
   * @param f        Function value at old iterate
   * @param g        Gradient or approximation at old iterate
   * @param p        Non-zero Newton step
   * @param xpls     New iterate
   * @param fpls     Function value at new iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param mxtake   Boolean flag indicating whether the step of
   *                 maximum length was used
   * @param iretcd   Return code
   * @param stepmx   Maximum allowable step size
   * @param steptl   Relative step size at which successive iterates
   *                 are considered close enough to terminate the
   *                 algorithm
   * @param sx       Scaling vector for x
   */


  public static void lnsrch_f77(int n, double x[], double f[],
                                double g[], double p[], double xpls[],
                                double fpls[], UncminFunction minclass,
                                boolean mxtake[], int iretcd[], double stepmx[],
                                double steptl[], double sx[]) {

/*

Here is a copy of the lnsrch FORTRAN documentation:

      SUBROUTINE LNSRCH(N,X,F,G,P,XPLS,FPLS,FCN,MXTAKE,
     +   IRETCD,STEPMX,STEPTL,SX,IPR)

C PURPOSE
C -------
C FIND A NEXT NEWTON ITERATE BY LINE SEARCH.
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE:   X[K-1]
C F            --> FUNCTION VALUE AT OLD ITERATE, F(X)
C G(N)         --> GRADIENT AT OLD ITERATE, G(X), OR APPROXIMATE
C P(N)         --> NON-ZERO NEWTON STEP
C XPLS(N)     <--  NEW ITERATE X[K]
C FPLS        <--  FUNCTION VALUE AT NEW ITERATE, F(XPLS)
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C IRETCD      <--  RETURN CODE
C MXTAKE      <--  BOOLEAN FLAG INDICATING STEP OF MAXIMUM LENGTH USED
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C
C INTERNAL VARIABLES
C ------------------
C SLN              NEWTON LENGTH
C RLN              RELATIVE LENGTH OF NEWTON STEP
C

*/

    int i;
    double tmp, sln, scl, slp, rln, rmnlmb, almbda, tlmbda;
    double t1, t2, t3, a, b, disc, pfpls, plmbda;

    pfpls = 0.0;
    plmbda = 0.0;

    mxtake[1] = false;
    iretcd[1] = 2;

    tmp = 0.0;

    for (i = 1; i <= n; i++) {

      tmp += sx[i] * sx[i] * p[i] * p[i];

    }

    sln = Math.sqrt(tmp);

    if (sln > stepmx[1]) {

// NEWTON STEP LONGER THAN MAXIMUM ALLOWED

      scl = stepmx[1] / sln;
      sclmul_f77(n, scl, p, p);
      sln = stepmx[1];

    }

    slp = Blas_f77.ddot_f77(n, g, 1, p, 1);
    rln = 0.0;

    for (i = 1; i <= n; i++) {

      rln =
          Math.max(rln, Math.abs(p[i]) / Math.max(Math.abs(x[i]), 1.0 / sx[i]));

    }

    rmnlmb = steptl[1] / rln;
    almbda = 1.0;

// LOOP
// CHECK IF NEW ITERATE SATISFACTORY.  GENERATE NEW LAMBDA IF NECESSARY.

    while (iretcd[1] >= 2) {

      for (i = 1; i <= n; i++) {

        xpls[i] = x[i] + almbda * p[i];

      }

      fpls[1] = minclass.apply(xpls);

      if (fpls[1] <= (f[1] + slp * .0001 * almbda)) {

// SOLUTION FOUND

        iretcd[1] = 0;
        if (almbda == 1.0 && sln > .99 * stepmx[1]) mxtake[1] = true;

      } else {

// SOLUTION NOT (YET) FOUND

        if (almbda < rmnlmb) {

// NO SATISFACTORY XPLS FOUND SUFFICIENTLY DISTINCT FROM X

          iretcd[1] = 1;

        } else {

// CALCULATE NEW LAMBDA

          if (almbda == 1.0) {

// FIRST BACKTRACK: QUADRATIC FIT

            tlmbda = -slp / (2.0 * (fpls[1] - f[1] - slp));

          } else {

// ALL SUBSEQUENT BACKTRACKS: CUBIC FIT

            t1 = fpls[1] - f[1] - almbda * slp;
            t2 = pfpls - f[1] - plmbda * slp;
            t3 = 1.0 / (almbda - plmbda);
            a = t3 * (t1 / (almbda * almbda) - t2 / (plmbda * plmbda));
            b = t3 * (t2 * almbda / (plmbda * plmbda)
                - t1 * plmbda / (almbda * almbda));
            disc = b * b - 3.0 * a * slp;

            if (disc > b * b) {

// ONLY ONE POSITIVE CRITICAL POINT, MUST BE MINIMUM

              tlmbda = (-b +
                  Blas_f77.sign_f77(1.0, a) * Math.sqrt(disc)) / (3.0 * a);

            } else {

// BOTH CRITICAL POINTS POSITIVE, FIRST IS MINIMUM

              tlmbda = (-b -
                  Blas_f77.sign_f77(1.0, a) * Math.sqrt(disc)) / (3.0 * a);

            }

            if (tlmbda > .5 * almbda) tlmbda = .5 * almbda;

          }

          plmbda = almbda;
          pfpls = fpls[1];

          if (tlmbda < almbda / 10.0) {

            almbda *= .1;

          } else {

            almbda = tlmbda;

          }
        }
      }
    }
  }


  /**
   * Computes y = Lx where L is a lower
   * triangular matrix stored in A.
   * <p/>
   * Translated by Steve Verrill, April 27, 1998.
   *
   * @param n The dimension of the problem
   * @param a Lower triangular matrix
   * @param x Operand vector
   * @param y Result vector
   */

  public static void mvmltl_f77(int n, double a[][], double x[],
                                double y[]) {
/*

Here is a copy of the mvmltl FORTRAN documentation:

      SUBROUTINE MVMLTL(NR,N,A,X,Y)
C
C PURPOSE
C -------
C COMPUTE Y=LX
C WHERE L IS A LOWER TRIANGULAR MATRIX STORED IN A
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)       --> LOWER TRIANGULAR (N*N) MATRIX
C X(N)         --> OPERAND VECTOR
C Y(N)        <--  RESULT VECTOR
C
C NOTE
C ----
C X AND Y CANNOT SHARE STORAGE
C

*/

    double sum;
    int i, j;

    for (i = 1; i <= n; i++) {

      sum = 0.0;

      for (j = 1; j <= i; j++) {

        sum += a[i][j] * x[j];

      }

      y[i] = sum;

    }

    return;

  }


  /**
   * The mvmlts_f77 method computes y = Ax where A is a symmetric matrix
   * stored in its lower triangular part.
   * <p/>
   * Translated by Steve Verrill, April 27, 1998.
   *
   * @param n The dimension of the problem
   * @param a The symmetric matrix
   * @param x Operand vector
   * @param y Result vector
   */

  public static void mvmlts_f77(int n, double a[][], double x[],
                                double y[]) {
/*

Here is a copy of the mvmlts FORTRAN documentation:

      SUBROUTINE MVMLTS(NR,N,A,X,Y)

C
C PURPOSE
C -------
C COMPUTE Y=AX
C WHERE "A" IS A SYMMETRIC (N*N) MATRIX STORED IN ITS LOWER
C TRIANGULAR PART AND X,Y ARE N-VECTORS
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)       --> SYMMETRIC (N*N) MATRIX STORED IN
C                  LOWER TRIANGULAR PART AND DIAGONAL
C X(N)         --> OPERAND VECTOR
C Y(N)        <--  RESULT VECTOR
C
C NOTE
C ----
C X AND Y CANNOT SHARE STORAGE.
C

*/

    double sum;
    int i, j;

    for (i = 1; i <= n; i++) {

      sum = 0.0;

      for (j = 1; j <= i; j++) {

        sum += a[i][j] * x[j];

      }

      for (j = i + 1; j <= n; j++) {

        sum += a[j][i] * x[j];

      }

      y[i] = sum;
    }
  }


  /**
   * <p/>
   * The mvmltu_f77 method computes Y = (L transpose)X where L is a
   * lower triangular matrix stored in A (L transpose
   * is taken implicitly).
   * <p/>
   * Translated by Steve Verrill, April 27, 1998.
   *
   * @param n The dimension of the problem
   * @param a The lower triangular matrix
   * @param x Operand vector
   * @param y Result vector
   */

  public static void mvmltu_f77(int n, double a[][], double x[],
                                double y[]) {
/*

Here is a copy of the mvmltu FORTRAN documentation:

      SUBROUTINE MVMLTU(NR,N,A,X,Y)

C
C PURPOSE
C -------
C COMPUTE Y=(L+)X
C WHERE L IS A LOWER TRIANGULAR MATRIX STORED IN A
C (L-TRANSPOSE (L+) IS TAKEN IMPLICITLY)
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(NR,1)       --> LOWER TRIANGULAR (N*N) MATRIX
C X(N)         --> OPERAND VECTOR
C Y(N)        <--  RESULT VECTOR
C
C NOTE
C ----
C X AND Y CANNOT SHARE STORAGE
C

*/

    double sum;
    int i, j;

    for (i = 1; i <= n; i++) {

      sum = 0.0;

      for (j = i; j <= n; j++) {

        sum += a[j][i] * x[j];

      }

      y[i] = sum;

    }

    return;

  }


  /**
   * <p/>
   * The optchk_f77 method checks the input for reasonableness.
   * <p/>
   * Translated by Steve Verrill, May 12, 1998.
   *
   * @param n      The dimension of the problem
   * @param x      On entry, estimate of the root of f_to_minimize
   * @param typsiz Typical size of each component of x
   * @param sx     Scaling vector for x
   * @param fscale Estimate of scale of objective function
   * @param gradtl Tolerance at which the gradient is considered
   *               close enough to zero to terminate the algorithm
   * @param itnlim Maximum number of allowable iterations
   * @param ndigit Number of good digits in the optimization function
   * @param epsm   Machine epsilon
   * @param dlt    Trust region radius
   * @param iagflg = 1 if an analytic gradient is supplied
   * @param iahflg = 1 if an analytic Hessian is supplied
   * @param stepmx Maximum step size
   * @param msg    Message and error code
   */

  public static void optchk_f77(int n, double x[], double typsiz[],
                                double sx[], double fscale, double gradtl[],
                                int itnlim[], int ndigit[], double epsm,
                                double dlt[],
                                int iagflg[], int iahflg[], double stepmx[],
                                int msg[]) {

/*

Here is a copy of the optchk FORTRAN documentation:


      SUBROUTINE OPTCHK(N,X,TYPSIZ,SX,FSCALE,GRADTL,ITNLIM,NDIGIT,EPSM,
     +     DLT,METHOD,IEXP,IAGFLG,IAHFLG,STEPMX,MSG,IPR)
C
C PURPOSE
C -------
C CHECK INPUT FOR REASONABLENESS
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ON ENTRY, ESTIMATE TO ROOT OF FCN
C TYPSIZ(N)   <--> TYPICAL SIZE OF EACH COMPONENT OF X
C SX(N)       <--  DIAGONAL SCALING MATRIX FOR X
C FSCALE      <--> ESTIMATE OF SCALE OF OBJECTIVE FUNCTION FCN
C GRADTL       --> TOLERANCE AT WHICH GRADIENT CONSIDERED CLOSE
C                  ENOUGH TO ZERO TO TERMINATE ALGORITHM
C ITNLIM      <--> MAXIMUM NUMBER OF ALLOWABLE ITERATIONS
C NDIGIT      <--> NUMBER OF GOOD DIGITS IN OPTIMIZATION FUNCTION FCN
C EPSM         --> MACHINE EPSILON
C DLT         <--> TRUST REGION RADIUS
C METHOD      <--> ALGORITHM INDICATOR
C IEXP        <--> EXPENSE FLAG
C IAGFLG      <--> =1 IF ANALYTIC GRADIENT SUPPLIED
C IAHFLG      <--> =1 IF ANALYTIC HESSIAN SUPPLIED
C STEPMX      <--> MAXIMUM STEP SIZE
C MSG         <--> MESSAGE AND ERROR CODE
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    int i;
    double stpsiz;


// CHECK THAT PARAMETERS ONLY TAKE ON ACCEPTABLE VALUES.
// IF NOT, SET THEM TO DEFAULT VALUES.

    if (iagflg[1] != 1) iagflg[1] = 0;
    if (iahflg[1] != 1) iahflg[1] = 0;

    if ((msg[1] / 2) % 2 == 1 && iagflg[1] == 0) {

      throw new IllegalArgumentException("User requests that analytic gradient" +
          " be accepted as properly coded, msg = " + msg + ", but an analytic gradient is not supplied," +
          " iagflag = " + iagflg[1]);

    }

    if ((msg[1] / 4) % 2 == 1 && iahflg[1] == 0) {

//      System.out.print("\n\nOPTCHK   User requests that analytic Hessian");
//      System.out.print(" be accepted as properly coded,\n");
//      System.out.print("OPTCHK   msg = " + msg + ",\n");
//      System.out.print("OPTCHK   but an analytic Hessian is not");
//      System.out.print(" supplied,\n");
//      System.out.print("OPTCHK   iahflg = " + iahflg[1] + ".\n\n");

      throw new IllegalArgumentException();
    }

// CHECK DIMENSION OF PROBLEM

    if (n <= 0) {

      throw new IllegalArgumentException("Illegal dimension, n = " + n);

    }

    if (n == 1 && msg[1] % 2 == 0) {

      System.out.print("\n\nOPTCHK   !!!WARNING!!!  This class is ");
      System.out.print("inefficient for problems of size 1.\n");
      System.out.print("OPTCHK   You might want to try Fmin instead.\n\n");

      msg[1] = -2;

    }

// COMPUTE SCALE MATRIX

    for (i = 1; i <= n; i++) {

      if (typsiz[i] == 0) typsiz[i] = 1.0;
      if (typsiz[i] < 0.0) typsiz[i] = -typsiz[i];
      sx[i] = 1.0 / typsiz[i];

    }

// CHECK MAXIMUM STEP SIZE

    if (stepmx[1] <= 0.0) {

      stpsiz = 0.0;

      for (i = 1; i <= n; i++) {

        stpsiz += x[i] * x[i] * sx[i] * sx[i];

      }

      stpsiz = Math.sqrt(stpsiz);

      stepmx[1] = Math.max(1000.0 * stpsiz, 1000.0);

    }

// CHECK GRADIENT TOLERANCE

    if (gradtl[1] < 0.0) {

      throw new IllegalArgumentException("Illegal tolerance, gradtl = " + gradtl[1]);

    }

// CHECK ITERATION LIMIT

    if (itnlim[1] < 0) {
      throw new IllegalArgumentException("Illegal iteration limit, itnlim = " + itnlim[1]);
    }

// CHECK NUMBER OF DIGITS OF ACCURACY IN FUNCTION FCN

    if (ndigit[1] == 0) {
      throw new IllegalArgumentException("Minimization function has no good digits, ndigit = " + ndigit);
    }

    if (ndigit[1] < 0) ndigit[1] = (int) (-Math.log(epsm) / Math.log(10.0));

// CHECK TRUST REGION RADIUS

    if (dlt[1] <= 0.0) {
      dlt[1] = -1.0;
    }
    if (dlt[1] > stepmx[1]) {
      dlt[1] = stepmx[1];
    }
  }


  /**
   * <p/>
   * The optdrv_f77 method is the driver for the nonlinear optimization problem.
   * <p/>
   * Translated by Steve Verrill, May 18, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        On entry, estimate of the location of a minimum
   *                 of f_to_minimize
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param typsiz   Typical size of each component of x
   * @param fscale   Estimate of scale of objective function
   * @param method   Algorithm indicator
   *                 1 -- line search
   *                 2 -- double dogleg
   *                 3 -- More-Hebdon
   * @param iexp     Expense flag.
   *                 1 -- optimization function, f_to_minimize,
   *                 is expensive to evaluate
   *                 0 -- otherwise
   *                 If iexp = 1, the Hessian will be evaluated
   *                 by secant update rather than analytically or
   *                 by finite differences.
   * @param msg      On input: (> 0) message to inhibit certain
   *                 automatic checks
   *                 On output: (< 0) error code (= 0, no error)
   * @param ndigit   Number of good digits in the optimization function
   * @param itnlim   Maximum number of allowable iterations
   * @param iagflg   = 1 if an analytic gradient is supplied
   * @param iahflg   = 1 if an analytic Hessian is supplied
   * @param dlt      Trust region radius
   * @param gradtl   Tolerance at which the gradient is considered
   *                 close enough to zero to terminate the algorithm
   * @param stepmx   Maximum step size
   * @param steptl   Relative step size at which successive iterates
   *                 are considered close enough to terminate the
   *                 algorithm
   * @param xpls     On exit: xpls is a local minimum
   * @param fpls     On exit: function value at xpls
   * @param gpls     On exit: gradient at xpls
   * @param itrmcd   Termination code
   * @param a        workspace for Hessian (or its approximation)
   *                 and its Cholesky decomposition
   * @param udiag    workspace (for diagonal of Hessian)
   * @param g        workspace (for gradient at current iterate)
   * @param p        workspace for step
   * @param sx       workspace (for scaling vector)
   * @param wrk0     workspace
   * @param wrk1     workspace
   * @param wrk2     workspace
   * @param wrk3     workspace
   */

  public static void optdrv_f77(int n, double x[], UncminFunction minclass,
                                double typsiz[], double fscale, Method method,
                                boolean iexp, int msg[], int ndigit[], int itnlim[],
                                int iagflg[], int iahflg[], double dlt[],
                                double gradtl[], double stepmx[], double steptl[],
                                double xpls[], double fpls[], double gpls[],
                                int itrmcd[], double a[][], double udiag[],
                                double g[], double p[], double sx[],
                                double wrk0[], double wrk1[], double wrk2[],
                                double wrk3[]) {

/*

Here is a copy of the optdrv FORTRAN documentation:


      SUBROUTINE OPTDRV(NR,N,X,FCN,D1FCN,D2FCN,TYPSIZ,FSCALE,
     +     METHOD,IEXP,MSG,NDIGIT,ITNLIM,IAGFLG,IAHFLG,IPR,
     +     DLT,GRADTL,STEPMX,STEPTL,
     +     XPLS,FPLS,GPLS,ITRMCD,
     +     A,UDIAG,G,P,SX,WRK0,WRK1,WRK2,WRK3)

C
C PURPOSE
C -------
C DRIVER FOR NON-LINEAR OPTIMIZATION PROBLEM
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ON ENTRY: ESTIMATE TO A ROOT OF FCN
C FCN          --> NAME OF SUBROUTINE TO EVALUATE OPTIMIZATION FUNCTION
C                  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C                            FCN: R(N) --> R(1)
C D1FCN        --> (OPTIONAL) NAME OF SUBROUTINE TO EVALUATE GRADIENT
C                  OF FCN.  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C D2FCN        --> (OPTIONAL) NAME OF SUBROUTINE TO EVALUATE HESSIAN OF
C                  OF FCN.  MUST BE DECLARED EXTERNAL IN CALLING ROUTINE
C TYPSIZ(N)    --> TYPICAL SIZE FOR EACH COMPONENT OF X
C FSCALE       --> ESTIMATE OF SCALE OF OBJECTIVE FUNCTION
C METHOD       --> ALGORITHM TO USE TO SOLVE MINIMIZATION PROBLEM
C                    =1 LINE SEARCH
C                    =2 DOUBLE DOGLEG
C                    =3 MORE-HEBDON
C IEXP         --> =1 IF OPTIMIZATION FUNCTION FCN IS EXPENSIVE TO
C                  EVALUATE, =0 OTHERWISE.  IF SET THEN HESSIAN WILL
C                  BE EVALUATED BY SECANT UPDATE INSTEAD OF
C                  ANALYTICALLY OR BY FINITE DIFFERENCES
C MSG         <--> ON INPUT:  (.GT.0) MESSAGE TO INHIBIT CERTAIN
C                    AUTOMATIC CHECKS
C                  ON OUTPUT: (.LT.0) ERROR CODE; =0 NO ERROR
C NDIGIT       --> NUMBER OF GOOD DIGITS IN OPTIMIZATION FUNCTION FCN
C ITNLIM       --> MAXIMUM NUMBER OF ALLOWABLE ITERATIONS
C IAGFLG       --> =1 IF ANALYTIC GRADIENT SUPPLIED
C IAHFLG       --> =1 IF ANALYTIC HESSIAN SUPPLIED
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C DLT          --> TRUST REGION RADIUS
C GRADTL       --> TOLERANCE AT WHICH GRADIENT CONSIDERED CLOSE
C                  ENOUGH TO ZERO TO TERMINATE ALGORITHM
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C XPLS(N)     <--> ON EXIT:  XPLS IS LOCAL MINIMUM
C FPLS        <--> ON EXIT:  FUNCTION VALUE AT SOLUTION, XPLS
C GPLS(N)     <--> ON EXIT:  GRADIENT AT SOLUTION XPLS
C ITRMCD      <--  TERMINATION CODE
C A(N,N)       --> WORKSPACE FOR HESSIAN (OR ESTIMATE)
C                  AND ITS CHOLESKY DECOMPOSITION
C UDIAG(N)     --> WORKSPACE [FOR DIAGONAL OF HESSIAN]
C G(N)         --> WORKSPACE (FOR GRADIENT AT CURRENT ITERATE)
C P(N)         --> WORKSPACE FOR STEP
C SX(N)        --> WORKSPACE (FOR DIAGONAL SCALING MATRIX)
C WRK0(N)      --> WORKSPACE
C WRK1(N)      --> WORKSPACE
C WRK2(N)      --> WORKSPACE
C WRK3(N)      --> WORKSPACE
C
C
C INTERNAL VARIABLES
C ------------------
C ANALTL           TOLERANCE FOR COMPARISON OF ESTIMATED AND
C                  ANALYTICAL GRADIENTS AND HESSIANS
C EPSM             MACHINE EPSILON
C F                FUNCTION VALUE: FCN(X)
C ITNCNT           CURRENT ITERATION, K
C RNF              RELATIVE NOISE IN OPTIMIZATION FUNCTION FCN.
C                       NOISE=10.**(-NDIGIT)
C

*/

    boolean noupdt[] = new boolean[2];
    boolean mxtake[] = new boolean[2];

    int i, j, num5, remain, ilow, ihigh;
    int icscmx[] = new int[2];
    int iretcd[] = new int[2];
    int itncnt[] = new int[2];

    double rnf, analtl, dltsav,
        amusav, dlpsav, phisav, phpsav;

    double f[] = new double[2];

    double amu[] = new double[2];
    double dltp[] = new double[2];
    double phi[] = new double[2];
    double phip0[] = new double[2];

    double epsm;

    dltsav = amusav = dlpsav = phisav = phpsav = 0.0;


// INITIALIZATION

    for (i = 1; i <= n; i++) {

      p[i] = 0.0;

    }

    itncnt[1] = 0;
    iretcd[1] = -1;

    epsm = 1.12e-16;

    optchk_f77(n, x, typsiz, sx, fscale, gradtl, itnlim, ndigit,
        epsm, dlt, iagflg, iahflg, stepmx,
        msg);

//   removed because don't want to stop if msg = -2
//   and other negative messages are now handled where they are generated
//      if (msg[1] < 0) return;

    rnf = Math.max(Math.pow(10.0, -ndigit[1]), epsm);
    analtl = Math.max(.01, Math.sqrt(rnf));

    if ((msg[1] / 8) % 2 != 1) {

      num5 = n / 5;
      remain = n % 5;

      //System.out.print("\n\nOPTDRV          Typical x\n\n");

      ilow = -4;
      ihigh = 0;

      for (i = 1; i <= num5; i++) {

        ilow += 5;
        ihigh += 5;

        //System.out.print(ilow + "--" + ihigh + "     ");

        for (j = 1; j <= 5; j++) {

          System.out.print(typsiz[ilow + j - 1] + "  ");

        }

        //System.out.print("\n");

      }

      ilow += 5;
      ihigh = ilow + remain - 1;

      System.out.print(ilow + "--" + ihigh + "     ");

      for (j = 1; j <= remain; j++) {

        System.out.print(typsiz[ilow + j - 1] + "  ");

      }

      System.out.print("\n");


      System.out.print("\n\nOPTDRV      Scaling vector for x\n\n");

      ilow = -4;
      ihigh = 0;

      for (i = 1; i <= num5; i++) {

        ilow += 5;
        ihigh += 5;

        System.out.print(ilow + "--" + ihigh + "     ");

        for (j = 1; j <= 5; j++) {

          System.out.print(sx[ilow + j - 1] + "  ");

        }

        System.out.print("\n");

      }

      ilow += 5;
      ihigh = ilow + remain - 1;

      System.out.print(ilow + "--" + ihigh + "     ");

      for (j = 1; j <= remain; j++) {

        System.out.print(sx[ilow + j - 1] + "  ");

      }

      System.out.print("\n");


      System.out.println("\n\nOPTDRV      Typical f = " + fscale);
      System.out.print("OPTDRV      Number of good digits in");
      System.out.println(" f_to_minimize = " + ndigit[1]);
      System.out.print("OPTDRV      Gradient flag");
      System.out.println(" = " + iagflg[1]);
      System.out.print("OPTDRV      Hessian flag");
      System.out.println(" = " + iahflg[1]);
      System.out.print("OPTDRV      Expensive function calculation flag");
      System.out.println(" = " + iexp);
      System.out.print("OPTDRV      Method to use");
      System.out.println(" = " + method);
      System.out.print("OPTDRV      Iteration limit");
      System.out.println(" = " + itnlim[1]);
      System.out.print("OPTDRV      Machine epsilon");
      System.out.println(" = " + epsm);
      System.out.print("OPTDRV      Maximum step size");
      System.out.println(" = " + stepmx[1]);
      System.out.print("OPTDRV      Step tolerance");
      System.out.println(" = " + steptl[1]);
      System.out.print("OPTDRV      Gradient tolerance");
      System.out.println(" = " + gradtl[1]);
      System.out.print("OPTDRV      Trust region radius");
      System.out.println(" = " + dlt[1]);
      System.out.print("OPTDRV      Relative noise in");
      System.out.println(" f_to_minimize = " + rnf);
      System.out.print("OPTDRV      Analytical fd tolerance");
      System.out.println(" = " + analtl);

    }

// EVALUATE FCN(X)

    f[1] = minclass.apply(x);

// EVALUATE ANALYTIC OR FINITE DIFFERENCE GRADIENT AND CHECK ANALYTIC
// GRADIENT, IF REQUESTED.

    if (iagflg[1] == 0) {

      fstofd_f77(n, x, minclass, f, g, sx, rnf);

    } else {

      minclass.applyGradient(x, g);

      if ((msg[1] / 2) % 2 == 0) {

        grdchk_f77(n, x, minclass, f, g, typsiz,
            sx, fscale, rnf, analtl,
            wrk1);

      }

    }


    optstp_f77(n, x, f, g, wrk1, itncnt, icscmx,
        itrmcd, gradtl, steptl, sx, fscale,
        itnlim, iretcd, mxtake, msg);

    if (itrmcd[1] != 0) {

      fpls[1] = f[1];

      for (i = 1; i <= n; i++) {

        xpls[i] = x[i];
        gpls[i] = g[i];

      }

    } else {

      if (iexp) {

// IF OPTIMIZATION FUNCTION EXPENSIVE TO EVALUATE (IEXP=1), THEN
// HESSIAN WILL BE OBTAINED BY SECANT UPDATES.  GET INITIAL HESSIAN.

        hsnint_f77(n, a, sx, method);

      } else {

// EVALUATE ANALYTIC OR FINITE DIFFERENCE HESSIAN AND CHECK ANALYTIC
// HESSIAN IF REQUESTED (ONLY IF USER-SUPPLIED ANALYTIC HESSIAN
// ROUTINE minclass.hessian FILLS ONLY LOWER TRIANGULAR PART AND DIAGONAL OF A).

        if (iahflg[1] == 0) {

          if (iagflg[1] == 1) {

            fstofd_f77(n, x, minclass, g, a, sx, rnf, wrk1);

          } else {

            sndofd_f77(n, x, minclass, f, a, sx, rnf, wrk1, wrk2);

          }

        } else {

          if ((msg[1] / 4) % 2 == 1) {

            minclass.applyHessian(x, a);

          } else {

            heschk_f77(n, x, minclass, f, g, a, typsiz,
                sx, rnf, analtl, iagflg, udiag,
                wrk1, wrk2);

// HESCHK EVALUATES minclass.hessian AND CHECKS IT AGAINST THE FINITE
// DIFFERENCE HESSIAN WHICH IT CALCULATES BY CALLING FSTOFD
// (IF IAGFLG .EQ. 1) OR SNDOFD (OTHERWISE).

          }

        }

      }

      if ((msg[1] / 8) % 2 == 0) result_f77(n, x, f, g, a,
          p, itncnt, 1);

// ITERATION

      while (itrmcd[1] == 0) {

        itncnt[1]++;

// FIND PERTURBED LOCAL MODEL HESSIAN AND ITS LL+ DECOMPOSITION
// (SKIP THIS STEP IF LINE SEARCH OR DOGSTEP TECHNIQUES BEING USED WITH
// SECANT UPDATES.  CHOLESKY DECOMPOSITION L ALREADY OBTAINED FROM
// SECFAC.)

        if ( ! iexp || method == Method.MORE_HEBDON) {

          chlhsn_f77(n, a, epsm, sx, udiag);

        }

// SOLVE FOR NEWTON STEP:  AP=-G

        for (i = 1; i <= n; i++) {
          wrk1[i] = -g[i];
        }

        lltslv_f77(n, a, p, wrk1);

// DECIDE WHETHER TO ACCEPT NEWTON STEP  XPLS=X + P
// OR TO CHOOSE XPLS BY A GLOBAL STRATEGY.

        if (iagflg[1] == 0 && method != Method.LINE_SEARCH) {

          dltsav = dlt[1];

          if (method != Method.DOUBLE_DOGLEG) {

            amusav = amu[1];
            dlpsav = dltp[1];
            phisav = phi[1];
            phpsav = phip0[1];

          }

        }

        if (method == Method.LINE_SEARCH) {

          lnsrch_f77(n, x, f, g, p, xpls, fpls, minclass,
              mxtake, iretcd, stepmx, steptl,
              sx);

        } else if (method == Method.DOUBLE_DOGLEG) {

          dogdrv_f77(n, x, f, g, a, p, xpls, fpls, minclass,
              sx, stepmx, steptl, dlt, iretcd,
              mxtake, wrk0, wrk1, wrk2, wrk3);

        } else {

          hookdr_f77(n, x, f, g, a, udiag, p, xpls, fpls,
              minclass, sx, stepmx, steptl,
              dlt, iretcd, mxtake, amu,
              dltp, phi, phip0, wrk0, wrk1,
              wrk2, epsm, itncnt);

        }

// IF COULD NOT FIND SATISFACTORY STEP AND FORWARD DIFFERENCE
// GRADIENT WAS USED, RETRY USING CENTRAL DIFFERENCE GRADIENT.

        if (iretcd[1] == 1 && iagflg[1] == 0) {

// SET IAGFLG FOR CENTRAL DIFFERENCES

          iagflg[1] = -1;

          System.out.print("\nOPTDRV      Shift from forward to central");
          System.out.print(" differences");
          System.out.print("\nOPTDRV      in iteration " + itncnt[1]);
          System.out.print("\n");

          fstocd_f77(n, x, minclass, sx, rnf, g);

          if (method == Method.LINE_SEARCH) {

// SOLVE FOR NEWTON STEP:  AP=-G

            for (i = 1; i <= n; i++) {

              wrk1[i] = -g[i];

            }

            lltslv_f77(n, a, p, wrk1);

            lnsrch_f77(n, x, f, g, p, xpls, fpls, minclass,
                mxtake, iretcd, stepmx, steptl,
                sx);

          } else {

            dlt[1] = dltsav;
            if (method == Method.DOUBLE_DOGLEG) {


// SOLVE FOR NEWTON STEP:  AP=-G

              for (i = 1; i <= n; i++) {

                wrk1[i] = -g[i];

              }

              lltslv_f77(n, a, p, wrk1);

              dogdrv_f77(n, x, f, g, a, p, xpls, fpls, minclass,
                  sx, stepmx, steptl, dlt, iretcd,
                  mxtake, wrk0, wrk1, wrk2, wrk3);


            } else {

              amu[1] = amusav;
              dltp[1] = dlpsav;
              phi[1] = phisav;
              phip0[1] = phpsav;

              chlhsn_f77(n, a, epsm, sx, udiag);

// SOLVE FOR NEWTON STEP:  AP=-G

              for (i = 1; i <= n; i++) {

                wrk1[i] = -g[i];

              }

              lltslv_f77(n, a, p, wrk1);

              hookdr_f77(n, x, f, g, a, udiag, p, xpls, fpls,
                  minclass, sx, stepmx, steptl,
                  dlt, iretcd, mxtake, amu,
                  dltp, phi, phip0, wrk0, wrk1,
                  wrk2, epsm, itncnt);

            }

          }

        }

// CALCULATE STEP FOR OUTPUT

        for (i = 1; i <= n; i++) {

          p[i] = xpls[i] - x[i];

        }

// CALCULATE GRADIENT AT XPLS

        if (iagflg[1] == -1) {

// CENTRAL DIFFERENCE GRADIENT

          fstocd_f77(n, xpls, minclass, sx, rnf, gpls);

        } else if (iagflg[1] == 0) {

// FORWARD DIFFERENCE GRADIENT

          fstofd_f77(n, xpls, minclass, fpls, gpls, sx, rnf);

        } else {

// ANALYTIC GRADIENT

          minclass.applyGradient(xpls, gpls);

        }

// CHECK WHETHER STOPPING CRITERIA SATISFIED

        optstp_f77(n, xpls, fpls, gpls, x, itncnt, icscmx,
            itrmcd, gradtl, steptl, sx, fscale,
            itnlim, iretcd, mxtake, msg);

        if (itrmcd[1] == 0) {

// EVALUATE HESSIAN AT XPLS

          if ( iexp ) {

            if (method == Method.MORE_HEBDON) {

              secunf_f77(n, x, g, a, udiag, xpls, gpls, epsm,
                  itncnt, rnf, iagflg, noupdt,
                  wrk1, wrk2, wrk3);

            } else {

              secfac_f77(n, x, g, a, xpls, gpls, epsm, itncnt,
                  rnf, iagflg, noupdt, wrk0, wrk1,
                  wrk2, wrk3);

            }

          } else {

            if (iahflg[1] == 1) {

              minclass.applyHessian(xpls, a);

            } else {

              if (iagflg[1] == 1) {

                fstofd_f77(n, xpls, minclass, gpls, a,
                    sx, rnf, wrk1);

              } else {

                sndofd_f77(n, xpls, minclass, fpls, a,
                    sx, rnf, wrk1, wrk2);

              }

            }


          }

          if ((msg[1] / 16) % 2 == 1) {

            result_f77(n, xpls, fpls, gpls, a, p, itncnt, 1);

          }

// X <-- XPLS  AND  G <-- GPLS  AND  F <-- FPLS

          f[1] = fpls[1];

          for (i = 1; i <= n; i++) {

            x[i] = xpls[i];
            g[i] = gpls[i];

          }

        }

      }

// TERMINATION
// -----------
// RESET XPLS,FPLS,GPLS,  IF PREVIOUS ITERATE SOLUTION
//

      if (itrmcd[1] == 3) {

        fpls[1] = f[1];

        for (i = 1; i <= n; i++) {

          xpls[i] = x[i];
          gpls[i] = g[i];

        }

      }

    }

// PRINT RESULTS

    if ((msg[1] / 8) % 2 == 0) {

      result_f77(n, xpls, fpls, gpls, a, p, itncnt, 0);

    }

    msg[1] = 0;
    return;

  }


  /**
   * <p/>
   * The optstp_f77 method determines whether the algorithm should
   * terminate due to any of the following:
   * 1) problem solved within user tolerance
   * 2) convergence within user tolerance
   * 3) iteration limit reached
   * 4) divergence or too restrictive maximum step (stepmx)
   * suspected
   * <p/>
   * Translated by Steve Verrill, May 12, 1998.
   *
   * @param n      The dimension of the problem
   * @param xpls   New iterate
   * @param fpls   Function value at new iterate
   * @param gpls   Gradient or approximation at new iterate
   * @param x      Old iterate
   * @param itncnt Current iteration
   * @param icscmx Number of consecutive steps >= stepmx
   *               (retain between successive calls)
   * @param itrmcd Termination code
   * @param gradtl Tolerance at which the relative gradient is considered
   *               close enough to zero to terminate the algorithm
   * @param steptl Relative step size at which successive iterates
   *               are considered close enough to terminate the algorithm
   * @param sx     Scaling vector for x
   * @param fscale Estimate of the scale of the objective function
   * @param itnlim Maximum number of allowable iterations
   * @param iretcd Return code
   * @param mxtake Boolean flag indicating step of
   *               maximum length was used
   * @param msg    If msg includes a term 8, suppress output
   */

  public static void optstp_f77(int n, double xpls[], double fpls[],
                                double gpls[], double x[], int itncnt[],
                                int icscmx[], int itrmcd[], double gradtl[],
                                double steptl[], double sx[], double fscale,
                                int itnlim[], int iretcd[], boolean mxtake[],
                                int msg[]) {
/*

Here is a copy of the optstp FORTRAN documentation:

      SUBROUTINE OPTSTP(N,XPLS,FPLS,GPLS,X,ITNCNT,ICSCMX,
     +      ITRMCD,GRADTL,STEPTL,SX,FSCALE,ITNLIM,IRETCD,MXTAKE,IPR,MSG)

C
C UNCONSTRAINED MINIMIZATION STOPPING CRITERIA
C --------------------------------------------
C FIND WHETHER THE ALGORITHM SHOULD TERMINATE, DUE TO ANY
C OF THE FOLLOWING:
C 1) PROBLEM SOLVED WITHIN USER TOLERANCE
C 2) CONVERGENCE WITHIN USER TOLERANCE
C 3) ITERATION LIMIT REACHED
C 4) DIVERGENCE OR TOO RESTRICTIVE MAXIMUM STEP (STEPMX) SUSPECTED
C
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF PROBLEM
C XPLS(N)      --> NEW ITERATE X[K]
C FPLS         --> FUNCTION VALUE AT NEW ITERATE F(XPLS)
C GPLS(N)      --> GRADIENT AT NEW ITERATE, G(XPLS), OR APPROXIMATE
C X(N)         --> OLD ITERATE X[K-1]
C ITNCNT       --> CURRENT ITERATION K
C ICSCMX      <--> NUMBER CONSECUTIVE STEPS .GE. STEPMX
C                  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C ITRMCD      <--  TERMINATION CODE
C GRADTL       --> TOLERANCE AT WHICH RELATIVE GRADIENT CONSIDERED CLOSE
C                  ENOUGH TO ZERO TO TERMINATE ALGORITHM
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C FSCALE       --> ESTIMATE OF SCALE OF OBJECTIVE FUNCTION
C ITNLIM       --> MAXIMUM NUMBER OF ALLOWABLE ITERATIONS
C IRETCD       --> RETURN CODE
C MXTAKE       --> BOOLEAN FLAG INDICATING STEP OF MAXIMUM LENGTH USED
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C MSG          --> IF MSG INCLUDES A TERM 8, SUPPRESS OUTPUT
C
C

*/

    int i;
    double d, rgx, relgrd, rsx, relstp;

    itrmcd[1] = 0;

// LAST GLOBAL STEP FAILED TO LOCATE A POINT LOWER THAN X

    if (iretcd[1] == 1) {

      itrmcd[1] = 3;

      if ((msg[1] / 8) % 2 == 0) {

        System.out.print("\n\nOPTSTP    The last global step failed");
        System.out.print(" to locate a point lower than x.\n");
        System.out.print("OPTSTP    Either x is an approximate local");
        System.out.print(" minimum of the function,\n");
        System.out.print("OPTSTP    the function is too nonlinear for");
        System.out.print(" this algorithm, or\n");
        System.out.print("OPTSTP    steptl is too large.\n");

      }

      return;

    } else {

// FIND DIRECTION IN WHICH RELATIVE GRADIENT MAXIMUM.
// CHECK WHETHER WITHIN TOLERANCE

      d = Math.max(Math.abs(fpls[1]), fscale);
      rgx = 0.0;

      for (i = 1; i <= n; i++) {

        relgrd = Math.abs(gpls[i]) * Math.max(Math.abs(xpls[i]), 1.0 / sx[i]) / d;
        rgx = Math.max(rgx, relgrd);

      }

      if (rgx <= gradtl[1]) {

        itrmcd[1] = 1;

        if ((msg[1] / 8) % 2 == 0) {

          System.out.print("\n\nOPTSTP    The relative gradient is close");
          System.out.print(" to zero.\n");
          System.out.print("OPTSTP    The current iterate is probably");
          System.out.print(" a solution.\n");

        }

        return;

      }

      if (itncnt[1] == 0) return;

// FIND DIRECTION IN WHICH RELATIVE STEPSIZE MAXIMUM
// CHECK WHETHER WITHIN TOLERANCE.

      rsx = 0.0;

      for (i = 1; i <= n; i++) {

        relstp = Math.abs(xpls[i] - x[i]) /
            Math.max(Math.abs(xpls[i]), 1.0 / sx[i]);

        rsx = Math.max(rsx, relstp);

      }

      if (rsx <= steptl[1]) {

        itrmcd[1] = 2;

        if ((msg[1] / 8) % 2 == 0) {

          System.out.print("\n\nOPTSTP    Successive iterates are within");
          System.out.print(" steptl.\n");
          System.out.print("OPTSTP    The current iterate is probably");
          System.out.print(" a solution.\n");

        }

        return;

      }


// CHECK ITERATION LIMIT

      if (itncnt[1] >= itnlim[1]) {

        itrmcd[1] = 4;

        if ((msg[1] / 8) % 2 == 0) {

          System.out.print("\n\nOPTSTP    The iteration limit was reached.\n");
          System.out.print("OPTSTP    The algorithm failed.\n");

        }

        return;

      }

// CHECK NUMBER OF CONSECUTIVE STEPS \ STEPMX

      if (!mxtake[1]) {

        icscmx[1] = 0;

        return;

      }

      if ((msg[1] / 8) % 2 == 0) {

        System.out.print("\n\nOPTSTP    Step of maximum length (stepmx)");
        System.out.print(" taken.\n");

      }

      icscmx[1]++;

      if (icscmx[1] < 5) return;

      itrmcd[1] = 5;

      if ((msg[1] / 8) % 2 == 0) {

        System.out.print("\n\nOPTSTP    Maximum step size exceeded");
        System.out.print(" five consecutive times.\n");
        System.out.print("OPTSTP    Either the function is unbounded");
        System.out.print(" below,\n");
        System.out.print("OPTSTP    becomes asymptotic to a finite value");
        System.out.print(" from above in some direction, or\n");
        System.out.print("OPTSTP    stepmx is too small.\n");

      }

    }

  }


  /**
   * <p/>
   * The qraux1_f77 method interchanges rows i,i+1 of the upper
   * Hessenberg matrix r, columns i to n.
   * <p/>
   * Translated by Steve Verrill, April 29, 1998.
   *
   * @param n The dimension of the matrix
   * @param r Upper Hessenberg matrix
   * @param i Index of row to interchange (i < n)
   */

  public static void qraux1_f77(int n, double r[][], int i) {

/*

Here is a copy of the qraux1 FORTRAN documentation:

      SUBROUTINE QRAUX1(NR,N,R,I)
C
C PURPOSE
C -------
C INTERCHANGE ROWS I,I+1 OF THE UPPER HESSENBERG MATRIX R,
C COLUMNS I TO N
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF MATRIX
C R(N,N)      <--> UPPER HESSENBERG MATRIX
C I            --> INDEX OF ROW TO INTERCHANGE (I.LT.N)
C

*/

    double tmp;
    int j, ip1;

    ip1 = i + 1;

    for (j = i; j <= n; j++) {

      tmp = r[i][j];
      r[i][j] = r[ip1][j];
      r[ip1][j] = tmp;

    }

    return;

  }


  /**
   * <p/>
   * The qraux2_f77 method pre-multiplies r by the Jacobi rotation j(i,i+1,a,b).
   * <p/>
   * Translated by Steve Verrill, April 29, 1998.
   *
   * @param n The dimension of the matrix
   * @param r Upper Hessenberg matrix
   * @param i Index of row
   * @param a scalar
   * @param b scalar
   */

  public static void qraux2_f77(int n, double r[][], int i,
                                double a, double b) {

/*

Here is a copy of the qraux2 FORTRAN documentation:

      SUBROUTINE QRAUX2(NR,N,R,I,A,B)
C
C PURPOSE
C -------
C PRE-MULTIPLY R BY THE JACOBI ROTATION J(I,I+1,A,B)
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF MATRIX
C R(N,N)      <--> UPPER HESSENBERG MATRIX
C I            --> INDEX OF ROW
C A            --> SCALAR
C B            --> SCALAR
C

*/

    double den, c, s, y, z;
    int j, ip1;

    ip1 = i + 1;

    den = Math.sqrt(a * a + b * b);
    c = a / den;
    s = b / den;

    for (j = i; j <= n; j++) {

      y = r[i][j];
      z = r[ip1][j];
      r[i][j] = c * y - s * z;
      r[ip1][j] = s * y + c * z;

    }

    return;

  }


  /**
   * <p/>
   * The qrupdt_f77 method finds an orthogonal n by n matrix, Q*,
   * and an upper triangular n by n matrix, R*, such that
   * (Q*)(R*) = R+U(V+).
   * <p/>
   * Translated by Steve Verrill, May 11, 1998.
   *
   * @param n The dimension of the problem
   * @param a On input: contains R
   *          On output: contains R*
   * @param u Vector
   * @param v Vector
   */

  public static void qrupdt_f77(int n, double a[][],
                                double u[], double v[]) {

/*

Here is a copy of the qrupdt FORTRAN documentation:


      SUBROUTINE QRUPDT(NR,N,A,U,V)
C
C PURPOSE
C -------
C FIND AN ORTHOGONAL (N*N) MATRIX (Q*) AND AN UPPER TRIANGULAR (N*N)
C MATRIX (R*) SUCH THAT (Q*)(R*)=R+U(V+)
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C A(N,N)      <--> ON INPUT:  CONTAINS R
C                  ON OUTPUT: CONTAINS (R*)
C U(N)         --> VECTOR
C V(N)         --> VECTOR
C

*/

    int k, km1, ii, i, j;
    double t1, t2;

// DETERMINE LAST NON-ZERO IN U(.)

    k = n;

    while (u[k] == 0 && k > 1) {

      k--;

    }

// (K-1) JACOBI ROTATIONS TRANSFORM
// R + U(V+) --> (R*) + (U(1)*E1)(V+)
// WHICH IS UPPER HESSENBERG

    km1 = k - 1;

    for (ii = 1; ii <= km1; ii++) {

      i = km1 - ii + 1;

      if (u[i] == 0.0) {

        qraux1_f77(n, a, i);
        u[i] = u[i + 1];

      } else {

        qraux2_f77(n, a, i, u[i], -u[i + 1]);
        u[i] = Math.sqrt(u[i] * u[i] + u[i + 1] * u[i + 1]);

      }

    }

// R <-- R + (U(1)*E1)(V+)

    for (j = 1; j <= n; j++) {

      a[1][j] += u[1] * v[j];

    }

// (K-1) JACOBI ROTATIONS TRANSFORM UPPER HESSENBERG R
// TO UPPER TRIANGULAR (R*)

    km1 = k - 1;

    for (i = 1; i <= km1; i++) {

      if (a[i][i] == 0.0) {

        qraux1_f77(n, a, i);

      } else {

        t1 = a[i][i];
        t2 = -a[i + 1][i];

        qraux2_f77(n, a, i, t1, t2);

      }

    }

    return;

  }


  /**
   * <p/>
   * The result_f77 method prints information.
   * <p/>
   * Translated by Steve Verrill, May 11, 1998.
   *
   * @param n      The dimension of the problem
   * @param x      Estimate of the location of a minimum at iteration k
   * @param f      function value at x
   * @param g      gradient at x
   * @param a      Hessian at x
   * @param p      Step taken
   * @param itncnt Iteration number (k)
   * @param iflg   Flag controlling the information to print
   */

  public static void result_f77(int n, double x[], double f[],
                                double g[], double a[][],
                                double p[], int itncnt[], int iflg) {

/*

Here is a copy of the result FORTRAN documentation:


      SUBROUTINE RESULT(NR,N,X,F,G,A,P,ITNCNT,IFLG,IPR)
C
C PURPOSE
C -------
C PRINT INFORMATION
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> ITERATE X[K]
C F            --> FUNCTION VALUE AT X[K]
C G(N)         --> GRADIENT AT X[K]
C A(N,N)       --> HESSIAN AT X[K]
C P(N)         --> STEP TAKEN
C ITNCNT       --> ITERATION NUMBER K
C IFLG         --> FLAG CONTROLLING INFO TO PRINT
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C

*/

    int i, j, iii, num5, remain, iii5, iiir;
    int ilow, ihigh;

    num5 = n / 5;
    remain = n % 5;


// PRINT ITERATION NUMBER

    System.out.print("\n\nRESULT      Iterate k = " + itncnt[1] + "\n");

    if (iflg != 0) {

// PRINT STEP

      System.out.print("\n\nRESULT      Step\n\n");

      ilow = -4;
      ihigh = 0;

      for (i = 1; i <= num5; i++) {

        ilow += 5;
        ihigh += 5;

        System.out.print(ilow + "--" + ihigh + "     ");

        for (j = 1; j <= 5; j++) {

          System.out.print(p[ilow + j - 1] + "  ");

        }

        System.out.print("\n");

      }

      ilow += 5;
      ihigh = ilow + remain - 1;

      System.out.print(ilow + "--" + ihigh + "     ");

      for (j = 1; j <= remain; j++) {

        System.out.print(p[ilow + j - 1] + "  ");

      }

      System.out.print("\n");

    }

// PRINT CURRENT ITERATE

    System.out.print("\n\nRESULT      Current x\n\n");

    ilow = -4;
    ihigh = 0;

    for (i = 1; i <= num5; i++) {

      ilow += 5;
      ihigh += 5;

      System.out.print(ilow + "--" + ihigh + "     ");

      for (j = 1; j <= 5; j++) {

        System.out.print(x[ilow + j - 1] + "  ");

      }

      System.out.print("\n");

    }

    ilow += 5;
    ihigh = ilow + remain - 1;

    System.out.print(ilow + "--" + ihigh + "     ");

    for (j = 1; j <= remain; j++) {

      System.out.print(x[ilow + j - 1] + "  ");

    }

    System.out.print("\n");


// PRINT FUNCTION VALUE

    System.out.print("\n\nRESULT      f_to_minimize at x = " + f[1] + "\n");


// PRINT GRADIENT

    System.out.print("\n\nRESULT      Gradient at x\n\n");

    ilow = -4;
    ihigh = 0;

    for (i = 1; i <= num5; i++) {

      ilow += 5;
      ihigh += 5;

      System.out.print(ilow + "--" + ihigh + "     ");

      for (j = 1; j <= 5; j++) {

        System.out.print(g[ilow + j - 1] + "  ");

      }

      System.out.print("\n");

    }

    ilow += 5;
    ihigh = ilow + remain - 1;

    System.out.print(ilow + "--" + ihigh + "     ");

    for (j = 1; j <= remain; j++) {

      System.out.print(g[ilow + j - 1] + "  ");

    }

    System.out.print("\n");


// PRINT HESSIAN FROM ITERATION K

    if (iflg != 0) {

      System.out.print("\n\nRESULT      Hessian at x\n\n");

      for (iii = 1; iii <= n; iii++) {

        iii5 = iii / 5;
        iiir = iii % 5;

        ilow = -4;
        ihigh = 0;

        for (i = 1; i <= iii5; i++) {

          ilow += 5;
          ihigh += 5;

          System.out.print("i = " + iii + ", j = ");
          System.out.print(ilow + "--" + ihigh + "     ");

          for (j = 1; j <= 5; j++) {

            System.out.print(a[iii][ilow + j - 1] + "  ");

          }

          System.out.print("\n");

        }

        ilow += 5;
        ihigh = ilow + iiir - 1;

        System.out.print("i = " + iii + ", j = ");
        System.out.print(ilow + "--" + ihigh + "     ");

        for (j = 1; j <= iiir; j++) {

          System.out.print(a[iii][ilow + j - 1] + "  ");

        }

        System.out.print("\n");

      }

    }

    return;

  }


  /**
   * <p/>
   * The sclmul_f77 method multiplies a vector by a scalar.
   * <p/>
   * Translated by Steve Verrill, May 8, 1998.
   *
   * @param n The dimension of the problem
   * @param s The scalar
   * @param v Operand vector
   * @param z Result vector
   */

  public static void sclmul_f77(int n, double s, double v[],
                                double z[]) {

/*

Here is a copy of the sclmul FORTRAN documentation:

      SUBROUTINE SCLMUL(N,S,V,Z)
C
C PURPOSE
C -------
C MULTIPLY VECTOR BY SCALAR
C RESULT VECTOR MAY BE OPERAND VECTOR
C
C PARAMETERS
C ----------
C N            --> DIMENSION OF VECTORS
C S            --> SCALAR
C V(N)         --> OPERAND VECTOR
C Z(N)        <--  RESULT VECTOR

*/

    int i;
    for (i = 1; i <= n; i++) {
      z[i] = s * v[i];
    }
  }


  /**
   * <p/>
   * The secfac_f77 method updates the Hessian by the BFGS factored technique.
   * <p/>
   * Translated by Steve Verrill, May 14, 1998.
   *
   * @param n      The dimension of the problem
   * @param x      Old iterate
   * @param g      Gradient or approximation at the old iterate
   * @param a      On entry: Cholesky decomposition of Hessian
   *               in lower triangle and diagonal
   *               On exit: Updated Cholesky decomposition of
   *               Hessian in lower triangle and diagonal
   * @param xpls   New iterate
   * @param gpls   Gradient or approximation at the new iterate
   * @param epsm   Machine epsilon
   * @param itncnt Iteration count
   * @param rnf    Relative noise in optimization function f_to_minimize
   * @param iagflg 1 if an analytic gradient is supplied, 0 otherwise
   * @param noupdt Boolean: no update yet (retain value between
   *               successive calls)
   * @param s      Workspace
   * @param y      Workspace
   * @param u      Workspace
   * @param w      Workspace
   */

  public static void secfac_f77(int n, double x[], double g[],
                                double a[][], double xpls[], double gpls[],
                                double epsm, int itncnt[], double rnf,
                                int iagflg[], boolean noupdt[], double s[],
                                double y[], double u[], double w[]) {

/*

Here is a copy of the secfac FORTRAN documentation:

      SUBROUTINE SECFAC(NR,N,X,G,A,XPLS,GPLS,EPSM,ITNCNT,RNF,
     +     IAGFLG,NOUPDT,S,Y,U,W)
C
C PURPOSE
C -------
C UPDATE HESSIAN BY THE BFGS FACTORED METHOD
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE, X[K-1]
C G(N)         --> GRADIENT OR APPROXIMATE AT OLD ITERATE
C A(N,N)      <--> ON ENTRY: CHOLESKY DECOMPOSITION OF HESSIAN IN
C                    LOWER PART AND DIAGONAL.
C                  ON EXIT:  UPDATED CHOLESKY DECOMPOSITION OF HESSIAN
C                    IN LOWER TRIANGULAR PART AND DIAGONAL
C XPLS(N)      --> NEW ITERATE, X[K]
C GPLS(N)      --> GRADIENT OR APPROXIMATE AT NEW ITERATE
C EPSM         --> MACHINE EPSILON
C ITNCNT       --> ITERATION COUNT
C RNF          --> RELATIVE NOISE IN OPTIMIZATION FUNCTION FCN
C IAGFLG       --> =1 IF ANALYTIC GRADIENT SUPPLIED, =0 ITHERWISE
C NOUPDT      <--> BOOLEAN: NO UPDATE YET
C                  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C S(N)         --> WORKSPACE
C Y(N)         --> WORKSPACE
C U(N)         --> WORKSPACE
C W(N)         --> WORKSPACE
C

*/

    boolean skpupd;
    int i, j, im1;
    double den1, snorm2, ynrm2, den2, alp, reltol;

    if (itncnt[1] == 1) noupdt[1] = true;

    for (i = 1; i <= n; i++) {

      s[i] = xpls[i] - x[i];
      y[i] = gpls[i] - g[i];

    }

    den1 = Blas_f77.ddot_f77(n, s, 1, y, 1);
    snorm2 = Blas_f77.dnrm2_f77(n, s, 1);
    ynrm2 = Blas_f77.dnrm2_f77(n, y, 1);

    if (den1 >= Math.sqrt(epsm) * snorm2 * ynrm2) {

      mvmltu_f77(n, a, s, u);
      den2 = Blas_f77.ddot_f77(n, u, 1, u, 1);

// L <-- SQRT(DEN1/DEN2)*L

      alp = Math.sqrt(den1 / den2);

      if (noupdt[1]) {

        for (j = 1; j <= n; j++) {

          u[j] *= alp;

          for (i = j; i <= n; i++) {

            a[i][j] *= alp;

          }

        }

        noupdt[1] = false;
        den2 = den1;
        alp = 1.0;

      }

      skpupd = true;

// W = L(L+)S = HS

      mvmltl_f77(n, a, u, w);

      i = 1;

      if (iagflg[1] == 0) {

        reltol = Math.sqrt(rnf);

      } else {

        reltol = rnf;

      }

      while (i <= n && skpupd) {

        if (Math.abs(y[i] - w[i]) >=
            reltol * Math.max(Math.abs(g[i]), Math.abs(gpls[i]))) {

          skpupd = false;

        } else {

          i++;

        }

      }

      if (!skpupd) {

// W=Y-ALP*L(L+)S

        for (i = 1; i <= n; i++) {

          w[i] = y[i] - alp * w[i];

        }

// ALP=1/SQRT(DEN1*DEN2)

        alp /= den1;

// U=(L+)/SQRT(DEN1*DEN2) = (L+)S/SQRT((Y+)S * (S+)L(L+)S)

        for (i = 1; i <= n; i++) {

          u[i] *= alp;

        }

// COPY L INTO UPPER TRIANGULAR PART.  ZERO L.

        for (i = 2; i <= n; i++) {

          im1 = i - 1;

          for (j = 1; j <= im1; j++) {

            a[j][i] = a[i][j];
            a[i][j] = 0.0;

          }

        }

// FIND Q, (L+) SUCH THAT  Q(L+) = (L+) + U(W+)

        qrupdt_f77(n, a, u, w);

// UPPER TRIANGULAR PART AND DIAGONAL OF A NOW CONTAIN UPDATED
// CHOLESKY DECOMPOSITION OF HESSIAN.  COPY BACK TO LOWER
// TRIANGULAR PART.

        for (i = 2; i <= n; i++) {

          im1 = i - 1;

          for (j = 1; j <= im1; j++) {

            a[i][j] = a[j][i];

          }

        }

      }

    }

    return;

  }


  /**
   * <p/>
   * The secunf_f77 method updates the Hessian by the BFGS unfactored approach.
   * <p/>
   * Translated by Steve Verrill, May 8, 1998.
   *
   * @param n      The dimension of the problem
   * @param x      The old iterate
   * @param g      The gradient or an approximation at the old iterate
   * @param a      On entry: Approximate Hessian at the old iterate
   *               in the upper triangular part (and udiag)
   *               On exit:  Updated approximate Hessian at the new
   *               iterate in the lower triangular part and
   *               diagonal
   * @param udiag  On entry: Diagonal of Hessian
   * @param xpls   New iterate
   * @param gpls   Gradient or approximation at the new iterate
   * @param epsm   Machine epsilon
   * @param itncnt Iteration count
   * @param rnf    Relative noise in the optimization function,
   *               f_to_minimize
   * @param iagflg = 1 if an analytic gradient is supplied,
   *               = 0 otherwise
   * @param noupdt Boolean: no update yet (retain value between calls)
   * @param s      workspace
   * @param y      workspace
   * @param t      workspace
   */

  public static void secunf_f77(int n, double x[], double g[], double
      a[][], double udiag[], double xpls[],
                                double gpls[], double epsm, int itncnt[],
                                double rnf, int iagflg[], boolean noupdt[],
                                double s[], double y[], double t[]) {

/*

Here is a copy of the secunf FORTRAN documentation:

      SUBROUTINE SECUNF(NR,N,X,G,A,UDIAG,XPLS,GPLS,EPSM,ITNCNT,
     +     RNF,IAGFLG,NOUPDT,S,Y,T)
C
C PURPOSE
C -------
C UPDATE HESSIAN BY THE BFGS UNFACTORED METHOD
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE, X[K-1]
C G(N)         --> GRADIENT OR APPROXIMATE AT OLD ITERATE
C A(N,N)      <--> ON ENTRY: APPROXIMATE HESSIAN AT OLD ITERATE
C                    IN UPPER TRIANGULAR PART (AND UDIAG)
C                  ON EXIT:  UPDATED APPROX HESSIAN AT NEW ITERATE
C                    IN LOWER TRIANGULAR PART AND DIAGONAL
C                  [LOWER TRIANGULAR PART OF SYMMETRIC MATRIX]
C UDIAG        --> ON ENTRY: DIAGONAL OF HESSIAN
C XPLS(N)      --> NEW ITERATE, X[K]
C GPLS(N)      --> GRADIENT OR APPROXIMATE AT NEW ITERATE
C EPSM         --> MACHINE EPSILON
C ITNCNT       --> ITERATION COUNT
C RNF          --> RELATIVE NOISE IN OPTIMIZATION FUNCTION FCN
C IAGFLG       --> =1 IF ANALYTIC GRADIENT SUPPLIED, =0 OTHERWISE
C NOUPDT      <--> BOOLEAN: NO UPDATE YET
C                  [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C S(N)         --> WORKSPACE
C Y(N)         --> WORKSPACE
C T(N)         --> WORKSPACE
C

*/

    double den1, snorm2, ynrm2, den2, gam, tol;
    int i, j;
    boolean skpupd;


// COPY HESSIAN IN UPPER TRIANGULAR PART AND UDIAG TO
// LOWER TRIANGULAR PART AND DIAGONAL

    for (j = 1; j <= n; j++) {

      a[j][j] = udiag[j];

      for (i = j + 1; i <= n; i++) {

        a[i][j] = a[j][i];

      }

    }

    if (itncnt[1] == 1) noupdt[1] = true;

    for (i = 1; i <= n; i++) {

      s[i] = xpls[i] - x[i];
      y[i] = gpls[i] - g[i];

    }

    den1 = Blas_f77.ddot_f77(n, s, 1, y, 1);

    snorm2 = Blas_f77.dnrm2_f77(n, s, 1);

    ynrm2 = Blas_f77.dnrm2_f77(n, y, 1);

    if (den1 >= Math.sqrt(epsm) * snorm2 * ynrm2) {

      mvmlts_f77(n, a, s, t);

      den2 = Blas_f77.ddot_f77(n, s, 1, t, 1);

      if (noupdt[1]) {

// H <-- [(S+)Y/(S+)HS]H

        gam = den1 / den2;

        den2 = gam * den2;

        for (j = 1; j <= n; j++) {

          t[j] *= gam;

          for (i = j; i <= n; i++) {

            a[i][j] *= gam;

          }

        }

        noupdt[1] = false;

      }

      skpupd = true;

// CHECK UPDATE CONDITION ON ROW I

      for (i = 1; i <= n; i++) {

        tol = rnf * Math.max(Math.abs(g[i]), Math.abs(gpls[i]));
        if (iagflg[1] == 0) tol /= Math.sqrt(rnf);

        if (Math.abs(y[i] - t[i]) >= tol) {

          skpupd = false;
          break;

        }

      }

      if (!skpupd) {

// BFGS UPDATE

        for (j = 1; j <= n; j++) {

          for (i = j; i <= n; i++) {

            a[i][j] += y[i] * y[j] / den1 - t[i] * t[j] / den2;

          }

        }

      }

    }

    return;

  }


  /**
   * <p/>
   * The sndofd_f77 method finds second order forward finite difference
   * approximations to the Hessian.  For optimization use this
   * method to estimate the Hessian of the optimization function
   * if no analytical user function has been supplied for either
   * the gradient or the Hessian, and the optimization function
   * is inexpensive to evaluate.
   * <p/>
   * Translated by Steve Verrill, May 8, 1998.
   *
   * @param n        The dimension of the problem
   * @param xpls     New iterate
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param fpls     Function value at the new iterate
   * @param a        "FINITE DIFFERENCE APPROXIMATION TO HESSIAN.  ONLY
   *                 LOWER TRIANGULAR MATRIX AND DIAGONAL ARE RETURNED"
   * @param sx       Scaling vector for x
   * @param rnoise   Relative noise in the function to be minimized
   * @param stepsz   Workspace (stepsize in i-th component direction)
   * @param anbr     Workspace (neighbor in i-th direction)
   */

  public static void sndofd_f77(int n, double xpls[], UncminFunction minclass,
                                double fpls[], double a[][], double sx[],
                                double rnoise, double stepsz[], double anbr[]) {

/*

Here is a copy of the sndofd FORTRAN documentation.

      SUBROUTINE SNDOFD(NR,N,XPLS,FCN,FPLS,A,SX,RNOISE,STEPSZ,ANBR)

C PURPOSE
C -------
C FIND SECOND ORDER FORWARD FINITE DIFFERENCE APPROXIMATION "A"
C TO THE SECOND DERIVATIVE (HESSIAN) OF THE FUNCTION DEFINED BY THE SUBP
C "FCN" EVALUATED AT THE NEW ITERATE "XPLS"
C
C FOR OPTIMIZATION USE THIS ROUTINE TO ESTIMATE
C 1) THE SECOND DERIVATIVE (HESSIAN) OF THE OPTIMIZATION FUNCTION
C    IF NO ANALYTICAL USER FUNCTION HAS BEEN SUPPLIED FOR EITHER
C    THE GRADIENT OR THE HESSIAN AND IF THE OPTIMIZATION FUNCTION
C    "FCN" IS INEXPENSIVE TO EVALUATE.
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C XPLS(N)      --> NEW ITERATE:   X[K]
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C FPLS         --> FUNCTION VALUE AT NEW ITERATE, F(XPLS)
C A(N,N)      <--  FINITE DIFFERENCE APPROXIMATION TO HESSIAN
C                  ONLY LOWER TRIANGULAR MATRIX AND DIAGONAL
C                  ARE RETURNED
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C RNOISE       --> RELATIVE NOISE IN FNAME [F(X)]
C STEPSZ(N)    --> WORKSPACE (STEPSIZE IN I-TH COMPONENT DIRECTION)
C ANBR(N)      --> WORKSPACE (NEIGHBOR IN I-TH DIRECTION)
C
C

*/

    double xmult, xtmpi, xtmpj, fhat;
    int i, j;

// FIND I-TH STEPSIZE AND EVALUATE NEIGHBOR IN DIRECTION
// OF I-TH UNIT VECTOR

    xmult = Math.pow(rnoise, 1.0 / 3.0);

    for (i = 1; i <= n; i++) {

      stepsz[i] = xmult * Math.max(Math.abs(xpls[i]), 1.0 / sx[i]);
      xtmpi = xpls[i];
      xpls[i] = xtmpi + stepsz[i];

      anbr[i] = minclass.apply(xpls);
      xpls[i] = xtmpi;

    }

// CALCULATE COLUMN I OF A

    for (i = 1; i <= n; i++) {

      xtmpi = xpls[i];
      xpls[i] = xtmpi + 2.0 * stepsz[i];
      fhat = minclass.apply(xpls);
      a[i][i] = ((fpls[1] - anbr[i]) + (fhat - anbr[i])) /
          (stepsz[i] * stepsz[i]);

// CALCULATE SUB-DIAGONAL ELEMENTS OF COLUMN

      if (i != n) {

        xpls[i] = xtmpi + stepsz[i];

        for (j = i + 1; j <= n; j++) {

          xtmpj = xpls[j];
          xpls[j] = xtmpj + stepsz[j];
          fhat = minclass.apply(xpls);
          a[j][i] = ((fpls[1] - anbr[i]) + (fhat - anbr[j])) /
              (stepsz[i] * stepsz[j]);
          xpls[j] = xtmpj;

        }

      }

      xpls[i] = xtmpi;

    }

    return;

  }


  /**
   * <p/>
   * The tregup_f77 method decides whether to accept xpls = x + sc as the next
   * iterate and update the trust region dlt.
   * <p/>
   * Translated by Steve Verrill, May 11, 1998.
   *
   * @param n        The dimension of the problem
   * @param x        Old iterate
   * @param f        Function value at old iterate
   * @param g        Gradient or approximation at old iterate
   * @param a        Cholesky decomposition of Hessian in
   *                 lower triangular part and diagonal.
   *                 Hessian or approximation in upper triangular part.
   * @param minclass A class that implements the Uncmin_methods
   *                 interface (see the definition in
   *                 Uncmin_methods.java).  See UncminTest_f77.java for an
   *                 example of such a class.  The class must define:
   *                 1.) a method, f_to_minimize, to minimize.
   *                 f_to_minimize must have the form
   *                 <p/>
   *                 public static double f_to_minimize(double x[])
   *                 <p/>
   *                 where x is the vector of arguments to the function
   *                 and the return value is the value of the function
   *                 evaluated at x.
   *                 2.) a method, gradient, that has the form
   *                 <p/>
   *                 public static void gradient(double x[],
   *                 double g[])
   *                 <p/>
   *                 where g is the gradient of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the gradient.
   *                 3.) a method, hessian, that has the form
   *                 <p/>
   *                 public static void hessian(double x[],
   *                 double h[][])
   *                 where h is the Hessian of f evaluated at x.  This
   *                 method will have an empty body if the user
   *                 does not wish to provide an analytic estimate
   *                 of the Hessian.  If the user wants Uncmin
   *                 to check the Hessian, then the hessian method
   *                 should only fill the lower triangle (and diagonal)
   *                 of h.
   * @param sc       Current step
   * @param sx       Scaling vector for x
   * @param nwtake   Boolean, = true if Newton step taken
   * @param stepmx   Maximum allowable step size
   * @param steptl   Relative step size at which successive iterates
   *                 are considered close enough to terminate
   *                 the algorithm
   * @param dlt      Trust region radius
   * @param iretcd   Return code
   *                 = 0  xpls accepted as next iterate, dlt is the
   *                 trust region radius for the next iteration
   *                 = 1  xpls unsatisfactory but accepted as next
   *                 iterate because xpls - x is less than the
   *                 smallest allowable step length
   *                 = 2  f(xpls) too large.  Continue current
   *                 iteration with new reduced dlt.
   *                 = 3  f(xpls) sufficiently small, but quadratic
   *                 model predicts f(xpls) sufficiently
   *                 well to continue current iteration
   *                 with new doubled dlt.
   * @param xplsp    Workspace (value needs to be retained between
   *                 successive calls of k-th global step)
   * @param fplsp    Retain between successive calls
   * @param xpls     New iterate
   * @param fpls     Function value at new iterate
   * @param mxtake   Boolean flag indicating step of maximum length used
   * @param method   Algorithm to use to solve minimization problem
   *                 = 1  Line search
   *                 = 2  Double dogleg
   *                 = 3  More-Hebdon
   * @param udiag    Diagonal of Hessian in a
   */


  public static void tregup_f77(int n, double x[], double f[], double g[],
                                double a[][], UncminFunction minclass,
                                double sc[], double sx[], boolean nwtake[],
                                double stepmx[], double steptl[], double dlt[],
                                int iretcd[], double xplsp[], double fplsp[],
                                double xpls[], double fpls[], boolean mxtake[],
                                int method, double udiag[]) {

/*

Here is a copy of the tregup FORTRAN documentation.


      SUBROUTINE TREGUP(NR,N,X,F,G,A,FCN,SC,SX,NWTAKE,STEPMX,STEPTL,
     +     DLT,IRETCD,XPLSP,FPLSP,XPLS,FPLS,MXTAKE,IPR,METHOD,UDIAG)
C
C PURPOSE
C -------
C DECIDE WHETHER TO ACCEPT XPLS=X+SC AS THE NEXT ITERATE AND UPDATE THE
C TRUST REGION DLT.
C
C PARAMETERS
C ----------
C NR           --> ROW DIMENSION OF MATRIX
C N            --> DIMENSION OF PROBLEM
C X(N)         --> OLD ITERATE X[K-1]
C F            --> FUNCTION VALUE AT OLD ITERATE, F(X)
C G(N)         --> GRADIENT AT OLD ITERATE, G(X), OR APPROXIMATE
C A(N,N)       --> CHOLESKY DECOMPOSITION OF HESSIAN IN
C                  LOWER TRIANGULAR PART AND DIAGONAL.
C                  HESSIAN OR APPROX IN UPPER TRIANGULAR PART
C FCN          --> NAME OF SUBROUTINE TO EVALUATE FUNCTION
C SC(N)        --> CURRENT STEP
C SX(N)        --> DIAGONAL SCALING MATRIX FOR X
C NWTAKE       --> BOOLEAN, =.TRUE. IF NEWTON STEP TAKEN
C STEPMX       --> MAXIMUM ALLOWABLE STEP SIZE
C STEPTL       --> RELATIVE STEP SIZE AT WHICH SUCCESSIVE ITERATES
C                  CONSIDERED CLOSE ENOUGH TO TERMINATE ALGORITHM
C DLT         <--> TRUST REGION RADIUS
C IRETCD      <--> RETURN CODE
C                    =0 XPLS ACCEPTED AS NEXT ITERATE;
C                       DLT TRUST REGION FOR NEXT ITERATION.
C                    =1 XPLS UNSATISFACTORY BUT ACCEPTED AS NEXT ITERATE
C                       BECAUSE XPLS-X .LT. SMALLEST ALLOWABLE
C                       STEP LENGTH.
C                    =2 F(XPLS) TOO LARGE.  CONTINUE CURRENT ITERATION
C                       WITH NEW REDUCED DLT.
C                    =3 F(XPLS) SUFFICIENTLY SMALL, BUT QUADRATIC MODEL
C                       PREDICTS F(XPLS) SUFFICIENTLY WELL TO CONTINUE
C                       CURRENT ITERATION WITH NEW DOUBLED DLT.
C XPLSP(N)    <--> WORKSPACE [VALUE NEEDS TO BE RETAINED BETWEEN
C                  SUCCESIVE CALLS OF K-TH GLOBAL STEP]
C FPLSP       <--> [RETAIN VALUE BETWEEN SUCCESSIVE CALLS]
C XPLS(N)     <--  NEW ITERATE X[K]
C FPLS        <--  FUNCTION VALUE AT NEW ITERATE, F(XPLS)
C MXTAKE      <--  BOOLEAN FLAG INDICATING STEP OF MAXIMUM LENGTH USED
C IPR          --> DEVICE TO WHICH TO SEND OUTPUT
C METHOD       --> ALGORITHM TO USE TO SOLVE MINIMIZATION PROBLEM
C                    =1 LINE SEARCH
C                    =2 DOUBLE DOGLEG
C                    =3 MORE-HEBDON
C UDIAG(N)     --> DIAGONAL OF HESSIAN IN A(.,.)
C

*/

    int i, j;
    double rln, temp, dltf, slp, dltmp, dltfp;

    mxtake[1] = false;

    for (i = 1; i <= n; i++) {

      xpls[i] = x[i] + sc[i];

    }

    fpls[1] = minclass.apply(xpls);

    dltf = fpls[1] - f[1];

    slp = Blas_f77.ddot_f77(n, g, 1, sc, 1);

    if (iretcd[1] == 4) fplsp[1] = 0.0;

    if ((iretcd[1] == 3) && ((fpls[1] >= fplsp[1]) ||
        (dltf > .0001 * slp))) {

// RESET XPLS TO XPLSP AND TERMINATE GLOBAL STEP

      iretcd[1] = 0;

      for (i = 1; i <= n; i++) {

        xpls[i] = xplsp[i];

      }

      fpls[1] = fplsp[1];
      dlt[1] *= .5;

    } else {

// FPLS TOO LARGE

      if (dltf > .0001 * slp) {

        rln = 0.0;

        for (i = 1; i <= n; i++) {

          rln = Math.max(rln, Math.abs(sc[i]) /
              Math.max(Math.abs(xpls[i]), 1.0 / sx[i]));

        }

        if (rln < steptl[1]) {

// CANNOT FIND SATISFACTORY XPLS SUFFICIENTLY DISTINCT FROM X

          iretcd[1] = 1;

        } else {

// REDUCE TRUST REGION AND CONTINUE GLOBAL STEP

          iretcd[1] = 2;

          dltmp = -slp * dlt[1] / (2.0 * (dltf - slp));

          if (dltmp < .1 * dlt[1]) {

            dlt[1] *= .1;

          } else {

            dlt[1] = dltmp;

          }

        }

      } else {

// FPLS SUFFICIENTLY SMALL

        dltfp = 0.0;

        if (method == 2) {

          for (i = 1; i <= n; i++) {

            temp = 0.0;

            for (j = i; j <= n; j++) {

              temp += a[j][i] * sc[j];

            }

            dltfp += temp * temp;

          }

        } else {

          for (i = 1; i <= n; i++) {

            dltfp += udiag[i] * sc[i] * sc[i];
            temp = 0.0;

            for (j = i + 1; j <= n; j++) {

              temp += a[i][j] * sc[i] * sc[j];

            }

            dltfp += 2.0 * temp;

          }

        }

        dltfp = slp + dltfp / 2.0;

        if ((iretcd[1] != 2) && (Math.abs(dltfp - dltf) <=
            .1 * Math.abs(dltf)) && (!nwtake[1]) &&
            (dlt[1] <= .99 * stepmx[1])) {

// DOUBLE TRUST REGION AND CONTINUE GLOBAL STEP

          iretcd[1] = 3;

          for (i = 1; i <= n; i++) {

            xplsp[i] = xpls[i];

          }

          fplsp[1] = fpls[1];
          dlt[1] = Math.min(2.0 * dlt[1], stepmx[1]);

        } else {

// ACCEPT XPLS AS NEXT ITERATE.  CHOOSE NEW TRUST REGION.

          iretcd[1] = 0;

          if (dlt[1] > .99 * stepmx[1]) mxtake[1] = true;

          if (dltf >= .1 * dltfp) {

// DECREASE TRUST REGION FOR NEXT ITERATION

            dlt[1] *= .5;

          } else {

// CHECK WHETHER TO INCREASE TRUST REGION FOR NEXT ITERATION

            if (dltf <= .75 * dltfp) dlt[1] = Math.min(2.0 * dlt[1], stepmx[1]);

          }

        }

      }

    }
  }

  static double[][] f77_array(int size_1, int size_2) {
    return new double[size_1 + 1][size_1 + 1];
  }

  static double[] f77_array(int size) {
    return new double[size + 1];
  }

  static double[] from_f77(double[] src) {
    double[] dst = new double[src.length - 1];
    System.arraycopy(src, 1, dst, 0, src.length - 1);
    return dst;
  }

  static double[] to_f77(double[] src) {
    double[] copy = new double[src.length + 1];
    System.arraycopy(src, 0, copy, 1, src.length);
    return copy;
  }
}