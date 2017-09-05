/*
 *
 *  Copyright (C) 1997-1998 Ross Ihaka
 *  Copyright (C) 1999-2001 R Core Team
 *
 *	cpoly finds the zeros of a complex polynomial.
 *
 *	On Entry
 *
 *	opr, opi      -	 double precision vectors of real and
 *			 imaginary parts of the coefficients in
 *			 order of decreasing powers.
 *
 *	degree	      -	 int degree of polynomial.
 *
 *
 *	On Return
 *
 *	zeror, zeroi  -	 output double precision vectors of
 *			 real and imaginary parts of the zeros.
 *
 *	fail	      -	 output int parameter,	true  only if
 *			 leading coefficient is zero or if cpoly
 *			 has found fewer than degree zeros.
 *
 *	The program has been written to reduce the chance of overflow
 *	occurring. If it does occur, there is still a possibility that
 *	the zerofinder will work provided the overflowed quantity is
 *	replaced by a large number.
 *
 *	This is a C translation of the following.
 *
 *	TOMS Algorithm 419
 *	Jenkins and Traub.
 *	Comm. ACM 15 (1972) 97-99.
 *
 *	Ross Ihaka
 *	February 1997
 *
 *  The C code was updated to be thread-safe and Renjin-compatible.
 *
 *  Alex Bertram
 *  August 2017
 */

#include <stddef.h>
#include <float.h> /* for FLT_RADIX */
#include <math.h>

typedef int Rboolean;
#define TRUE 1
#define FALSE 0

static const double eta =  DBL_EPSILON;
static const double are = /* eta = */DBL_EPSILON;
static const double mre = 2. * M_SQRT2 * /* eta, i.e. */DBL_EPSILON;
static const double infin = DBL_MAX;
static const double smalno = DBL_MIN;
static const double base = (double)FLT_RADIX;
static const double cosr =/* cos 94 */ -0.06975647374412529990;
static const double sinr =/* sin 94 */  0.99756405025982424767;

#define R_PosInf INFINITY

/* Refactored global variables into work struct */

typedef struct {
    int nn;
    double *pr, *pi, *hr, *hi, *qpr, *qpi, *qhr, *qhi, *shr, *shi;
    double sr, si;
    double tr, ti;
    double pvr, pvi;
} work_t;

static void calct(work_t *, Rboolean *);
static Rboolean fxshft(work_t *, int, double *, double *);
static Rboolean vrshft(work_t *, int, double *, double *);
static void nexth(work_t *, Rboolean);
static void noshft(work_t *, int);

static void polyev(int, double, double,
		   double *, double *, double *, double *, double *, double *);
static double errev(int, double *, double *, double, double, double, double);
static double cpoly_cauchy(int, double *, double *);
static double cpoly_scale(int, double *, double, double, double, double);
static void cdivid(double, double, double, double, double *, double *);


void cpolyroot(double *opr, double *opi, int *degree,
			double *zeror, double *zeroi, Rboolean *fail)
{

    work_t work;
    static int d_n, i, i1, i2;
    static double zi, zr, xx, yy;
    static double bnd, xxx;
    Rboolean conv;
    int d1;
    double *tmp;

    xx = M_SQRT1_2;/* 1/sqrt(2) = 0.707.... */

    yy = -xx;
    *fail = FALSE;

    work.nn = *degree;
    d1 = work.nn - 1;

    /* algorithm fails if the leading coefficient is zero. */

    if (opr[0] == 0. && opi[0] == 0.) {
	*fail = TRUE;
	return;
    }

    /* remove the zeros at the origin if any. */

    while (opr[work.nn] == 0. && opi[work.nn] == 0.) {
	d_n = d1-work.nn+1;
	zeror[d_n] = 0.;
	zeroi[d_n] = 0.;
	work.nn--;
    }
    work.nn++;
    /*-- Now, global var.  work.nn := #{coefficients} = (relevant degree)+1 */

    if (work.nn == 1) return;

    /* Use a single allocation as these as small */
    tmp = (double *) malloc((size_t) (10*work.nn) * sizeof(double));
    work.pr = tmp; work.pi = tmp + work.nn; work.hr = tmp + 2*work.nn; work.hi = tmp + 3*work.nn;
    work.qpr = tmp + 4*work.nn; work.qpi = tmp + 5*work.nn; work.qhr = tmp + 6*work.nn; work.qhi = tmp + 7*work.nn;
    work.shr = tmp + 8*work.nn; work.shi = tmp + 9*work.nn;

    /* make a copy of the coefficients and work.shr[] = | p[] | */
    for (i = 0; i < work.nn; i++) {
	work.pr[i] = opr[i];
	work.pi[i] = opi[i];
	work.shr[i] = hypot(work.pr[i], work.pi[i]);
    }

    /* scale the polynomial with factor 'bnd'. */
    bnd = cpoly_scale(work.nn, work.shr, eta, infin, smalno, base);
    if (bnd != 1.) {
	for (i=0; i < work.nn; i++) {
	    work.pr[i] *= bnd;
	    work.pi[i] *= bnd;
	}
    }

    /* start the algorithm for one zero */

    while (work.nn > 2) {

	/* calculate bnd, a lower bound on the modulus of the zeros. */

	for (i=0 ; i < work.nn ; i++)
	    work.shr[i] = hypot(work.pr[i], work.pi[i]);
	bnd = cpoly_cauchy(work.nn, work.shr, work.shi);

	/* outer loop to control 2 major passes */
	/* with different sequences of shifts */

	for (i1 = 1; i1 <= 2; i1++) {

	    /* first stage calculation, no shift */

	    noshft(&work, 5);

	    /*	inner loop to select a shift */
	    for (i2 = 1; i2 <= 9; i2++) {

		/* shift is chosen with modulus bnd */
		/* and amplitude rotated by 94 degrees */
		/* from the previous shift */

		xxx= cosr * xx - sinr * yy;
		yy = sinr * xx + cosr * yy;
		xx = xxx;
		work.sr = bnd * xx;
		work.si = bnd * yy;

		/*  second stage calculation, fixed shift */

		conv = fxshft(&work, i2 * 10, &zr, &zi);
		if (conv)
		    goto L10;
	    }
	}

	/* the zerofinder has failed on two major passes */
	/* return empty handed */

	*fail = TRUE;
	return;

	/* the second stage jumps directly to the third stage iteration.
	 * if successful, the zero is stored and the polynomial deflated.
	 */
    L10:
	d_n = d1+2 - work.nn;
	zeror[d_n] = zr;
	zeroi[d_n] = zi;
	--work.nn;
	for (i=0; i < work.nn ; i++) {
	    work.pr[i] = work.qpr[i];
	    work.pi[i] = work.qpi[i];
	}
    }/*while*/

    /*	calculate the final zero and return */
    cdivid(-work.pr[1], -work.pi[1], work.pr[0], work.pi[0], &zeror[d1], &zeroi[d1]);

    return;
}


/*  Computes the derivative polynomial as the initial
 *  polynomial and computes l1 no-shift h polynomials.	*/

static void noshft(work_t *pwork, int l1)
{
    int i, j, jj, n = pwork->nn - 1, nm1 = n - 1;

    double t1, t2, xni;

    for (i=0; i < n; i++) {
	xni = (double)(pwork->nn - i - 1);
	pwork->hr[i] = xni * pwork->pr[i] / n;
	pwork->hi[i] = xni * pwork->pi[i] / n;
    }

    for (jj = 1; jj <= l1; jj++) {

	if (hypot(pwork->hr[n-1], pwork->hi[n-1]) <=
	    eta * 10.0 * hypot(pwork->pr[n-1], pwork->pi[n-1])) {
	    /*	If the constant term is essentially zero, */
	    /*	shift h coefficients. */

	    for (i = 1; i <= nm1; i++) {
		j = pwork->nn - i;
		pwork->hr[j-1] = pwork->hr[j-2];
		pwork->hi[j-1] = pwork->hi[j-2];
	    }
	    pwork->hr[0] = 0.;
	    pwork->hi[0] = 0.;
	}
	else {
	    cdivid(-pwork->pr[pwork->nn-1], -pwork->pi[pwork->nn-1], pwork->hr[n-1], pwork->hi[n-1], &pwork->tr, &pwork->ti);
	    for (i = 1; i <= nm1; i++) {
		j = pwork->nn - i;
		t1 = pwork->hr[j-2];
		t2 = pwork->hi[j-2];
		pwork->hr[j-1] = pwork->tr * t1 - pwork->ti * t2 + pwork->pr[j-1];
		pwork->hi[j-1] = pwork->tr * t2 + pwork->ti * t1 + pwork->pi[j-1];
	    }
	    pwork->hr[0] = pwork->pr[0];
	    pwork->hi[0] = pwork->pi[0];
	}
    }
}


/*  Computes l2 fixed-shift h polynomials and tests for convergence.
 *  initiates a variable-shift iteration and returns with the
 *  approximate zero if successful.
 */
static Rboolean fxshft(work_t *pwork, int l2, double *zr, double *zi)
{
    /*  l2	  - limit of fixed shift steps
     *  zr,zi - approximate zero if convergence (result TRUE)
     *
     * Return value indicates convergence of stage 3 iteration
     *
    */

    Rboolean pasd, bool, test;
    static double svsi, svsr;
    static int i, j, n;
    static double oti, otr;

    n = pwork->nn - 1;

    /* evaluate p at s. */

    polyev(pwork->nn, pwork->sr, pwork->si, pwork->pr, pwork->pi, pwork->qpr, pwork->qpi, &pwork->pvr, &pwork->pvi);

    test = TRUE;
    pasd = FALSE;

    /* calculate first t = -p(s)/h(s). */

    calct(pwork, &bool);

    /* main loop for one second stage step. */

    for (j=1; j<=l2; j++) {

	otr = pwork->tr;
	oti = pwork->ti;

	/* compute next h polynomial and new t. */

	nexth(pwork, bool);
	calct(pwork, &bool);
	*zr = pwork->sr + pwork->tr;
	*zi = pwork->si + pwork->ti;

	/* test for convergence unless stage 3 has */
	/* failed once or this is the last h polynomial. */

	if (!bool && test && j != l2) {
	    if (hypot(pwork->tr - otr, pwork->ti - oti) >= hypot(*zr, *zi) * 0.5) {
		pasd = FALSE;
	    }
	    else if (! pasd) {
		pasd = TRUE;
	    }
	    else {

		/* the weak convergence test has been */
		/* passed twice, start the third stage */
		/* iteration, after saving the current */
		/* h polynomial and shift. */

		for (i = 0; i < n; i++) {
		    pwork->shr[i] = pwork->hr[i];
		    pwork->shi[i] = pwork->hi[i];
		}
		svsr = pwork->sr;
		svsi = pwork->si;
		if (vrshft(pwork, 10, zr, zi)) {
		    return TRUE;
		}

		/* the iteration failed to converge. */
		/* turn off testing and restore */
		/* h, s, pv and t. */

		test = FALSE;
		for (i=1 ; i<=n ; i++) {
		    pwork->hr[i-1] = pwork->shr[i-1];
		    pwork->hi[i-1] = pwork->shi[i-1];
		}
		pwork->sr = svsr;
		pwork->si = svsi;
		polyev(pwork->nn, pwork->sr, pwork->si, pwork->pr, pwork->pi, pwork->qpr, pwork->qpi, &pwork->pvr, &pwork->pvi);
		calct(pwork, &bool);
	    }
	}
    }

    /* attempt an iteration with final h polynomial */
    /* from second stage. */

    return(vrshft(pwork, 10, zr, zi));
}


/* carries out the third stage iteration.
 */
static Rboolean vrshft(work_t *pwork, int l3, double *zr, double *zi)
{
    /*  l3	    - limit of steps in stage 3.
     *  zr,zi   - on entry contains the initial iterate;
     *	      if the iteration converges it contains
     *	      the final iterate on exit.
     * Returns TRUE if iteration converges
     *
    */

    Rboolean bool, b;
    static int i, j;
    static double r1, r2, mp, ms, tp, relstp;
    static double omp;

    b = FALSE;
    pwork->sr = *zr;
    pwork->si = *zi;

    /* main loop for stage three */

    for (i = 1; i <= l3; i++) {

	/* evaluate p at s and test for convergence. */
	polyev(pwork->nn, pwork->sr, pwork->si, pwork->pr, pwork->pi, pwork->qpr, pwork->qpi, &pwork->pvr, &pwork->pvi);

	mp = hypot(pwork->pvr, pwork->pvi);
	ms = hypot(pwork->sr, pwork->si);
	if (mp <=  20. * errev(pwork->nn, pwork->qpr, pwork->qpi, ms, mp, /*are=*/eta, mre)) {
	    goto L_conv;
	}

	/* polynomial value is smaller in value than */
	/* a bound on the error in evaluating p, */
	/* terminate the iteration. */

	if (i != 1) {

	    if (!b && mp >= omp && relstp < .05) {

		/* iteration has stalled. probably a */
		/* cluster of zeros. do 5 fixed shift */
		/* steps into the cluster to force */
		/* one zero to dominate. */

		tp = relstp;
		b = TRUE;
		if (relstp < eta)
		    tp = eta;
		r1 = sqrt(tp);
		r2 = pwork->sr * (r1 + 1.) - pwork->si * r1;
		pwork->si = pwork->sr * r1 + pwork->si * (r1 + 1.);
		pwork->sr = r2;
		polyev(pwork->nn, pwork->sr, pwork->si, pwork->pr, pwork->pi, pwork->qpr, pwork->qpi, &pwork->pvr, &pwork->pvi);
		for (j = 1; j <= 5; ++j) {
		    calct(pwork, &bool);
		    nexth(pwork, bool);
		}
		omp = infin;
		goto L10;
	    }
	    else {

		/* exit if polynomial value */
		/* increases significantly. */

		if (mp * .1 > omp)
		    return FALSE;
	    }
	}
	omp = mp;

	/* calculate next iterate. */

    L10:
	calct(pwork, &bool);
	nexth(pwork,  bool);
	calct(pwork, &bool);
	if (!bool) {
	    relstp = hypot(pwork->tr, pwork->ti) / hypot(pwork->sr, pwork->si);
	    pwork->sr += pwork->tr;
	    pwork->si += pwork->ti;
	}
    }
    return FALSE;

L_conv:
    *zr = pwork->sr;
    *zi = pwork->si;
    return TRUE;
}

static void calct(work_t *pwork, Rboolean *bool)
{
    /* computes	 t = -p(s)/h(s).
     * bool   - logical, set true if h(s) is essentially zero.	*/

    int n = pwork->nn - 1;
    double hvi, hvr;

    /* evaluate h(s). */
    polyev(n, pwork->sr, pwork->si, pwork->hr, pwork->hi,
	   pwork->qhr, pwork->qhi, &hvr, &hvi);

    *bool = hypot(hvr, hvi) <= are * 10. * hypot(pwork->hr[n-1], pwork->hi[n-1]);
    if (!*bool) {
	cdivid(-pwork->pvr, -pwork->pvi, hvr, hvi, &pwork->tr, &pwork->ti);
    }
    else {
	pwork->tr = 0.;
	pwork->ti = 0.;
    }
}

static void nexth(work_t *pwork, Rboolean bool)
{
    /* calculates the next shifted h polynomial.
     * bool :	if TRUE  h(s) is essentially zero
     */
    int j, n = pwork->nn - 1;
    double t1, t2;

    if (!bool) {
	for (j=1; j < n; j++) {
	    t1 = pwork->qhr[j - 1];
	    t2 = pwork->qhi[j - 1];
	    pwork->hr[j] = pwork->tr * t1 - pwork->ti * t2 + pwork->qpr[j];
	    pwork->hi[j] = pwork->tr * t2 + pwork->ti * t1 + pwork->qpi[j];
	}
	pwork->hr[0] = pwork->qpr[0];
	pwork->hi[0] = pwork->qpi[0];
    }
    else {
	/* if h(s) is zero replace h with qh. */

	for (j=1; j < n; j++) {
	    pwork->hr[j] = pwork->qhr[j-1];
	    pwork->hi[j] = pwork->qhi[j-1];
	}
	pwork->hr[0] = 0.;
	pwork->hi[0] = 0.;
    }
}

/*--------------------- Independent Complex Polynomial Utilities ----------*/

static
void polyev(int n,
	    double s_r, double s_i,
	    double *p_r, double *p_i,
	    double *q_r, double *q_i,
	    double *v_r, double *v_i)
{
    /* evaluates a polynomial  p  at  s	 by the horner recurrence
     * placing the partial sums in q and the computed value in v_.
     */
    int i;
    double t;

    q_r[0] = p_r[0];
    q_i[0] = p_i[0];
    *v_r = q_r[0];
    *v_i = q_i[0];
    for (i = 1; i < n; i++) {
	t = *v_r * s_r - *v_i * s_i + p_r[i];
	q_i[i] = *v_i = *v_r * s_i + *v_i * s_r + p_i[i];
	q_r[i] = *v_r = t;
    }
}

static
double errev(int n, double *qr, double *qi,
	     double ms, double mp, double a_re, double m_re)
{
    /*	bounds the error in evaluating the polynomial by the horner
     *	recurrence.
     *
     *	qr,qi	 - the partial sum vectors
     *	ms	 - modulus of the point
     *	mp	 - modulus of polynomial value
     * a_re,m_re - error bounds on complex addition and multiplication
     */
    double e;
    int i;

    e = hypot(qr[0], qi[0]) * m_re / (a_re + m_re);
    for (i=0; i < n; i++)
	e = e*ms + hypot(qr[i], qi[i]);

    return e * (a_re + m_re) - mp * m_re;
}


static
double cpoly_cauchy(int n, double *pot, double *q)
{
    /* Computes a lower bound on the moduli of the zeros of a polynomial
     * pot[1:pwork->nn] is the modulus of the coefficients.
     */
    double f, x, delf, dx, xm;
    int i, n1 = n - 1;

    pot[n1] = -pot[n1];

    /* compute upper estimate of bound. */

    x = exp((log(-pot[n1]) - log(pot[0])) / (double) n1);

    /* if newton step at the origin is better, use it. */

    if (pot[n1-1] != 0.) {
	xm = -pot[n1] / pot[n1-1];
	if (xm < x)
	    x = xm;
    }

    /* chop the interval (0,x) unitl f le 0. */

    for(;;) {
	xm = x * 0.1;
	f = pot[0];
	for (i = 1; i < n; i++)
	    f = f * xm + pot[i];
	if (f <= 0.0) {
	    break;
	}
	x = xm;
    }

    dx = x;

    /* do Newton iteration until x converges to two decimal places. */

    while (fabs(dx / x) > 0.005) {
	q[0] = pot[0];
	for(i = 1; i < n; i++)
	    q[i] = q[i-1] * x + pot[i];
	f = q[n1];
	delf = q[0];
	for(i = 1; i < n1; i++)
	    delf = delf * x + q[i];
	dx = f / delf;
	x -= dx;
    }
    return x;
}

static
double cpoly_scale(int n, double *pot,
		   double eps, double BIG, double small, double base)
{
    /* Returns a scale factor to multiply the coefficients of the polynomial.
     * The scaling is done to avoid overflow and to avoid
     *	undetected underflow interfering with the convergence criterion.
     * The factor is a power of the base.

     * pot[1:n] : modulus of coefficients of p
     * eps,BIG,
     * small,base - constants describing the floating point arithmetic.
     */

    int i, ell;
    double x, high, sc, lo, min_, max_;

    /* find largest and smallest moduli of coefficients. */
    high = sqrt(BIG);
    lo = small / eps;
    max_ = 0.;
    min_ = BIG;
    for (i = 0; i < n; i++) {
	x = pot[i];
	if (x > max_) max_ = x;
	if (x != 0. && x < min_)
	    min_ = x;
    }

    /* scale only if there are very large or very small components. */

    if (min_ < lo || max_ > high) {
	x = lo / min_;
	if (x <= 1.)
	    sc = 1. / (sqrt(max_) * sqrt(min_));
	else {
	    sc = x;
	    if (BIG / sc > max_)
		sc = 1.0;
	}
	ell = (int) (log(sc) / log(base) + 0.5);
	return R_pow_di(base, ell);
    }
    else return 1.0;
}


static
void cdivid(double ar, double ai, double br, double bi,
	    double *cr, double *ci)
{
    /* complex division c = a/b, i.e., (cr +i*ci) = (ar +i*ai) / (br +i*bi),
       avoiding overflow. */

    double d, r;

    if (br == 0. && bi == 0.) {
	/* division by zero, c = infinity. */
	*cr = *ci = R_PosInf;
    }
    else if (fabs(br) >= fabs(bi)) {
	r = bi / br;
	d = br + r * bi;
	*cr = (ar + ai * r) / d;
	*ci = (ai - ar * r) / d;
    }
    else {
	r = br / bi;
	d = bi + r * br;
	*cr = (ar * r + ai) / d;
	*ci = (ai * r - ar) / d;
    }
}
