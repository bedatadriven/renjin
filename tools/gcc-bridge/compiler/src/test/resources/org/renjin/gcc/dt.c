
#include <math.h>
#include <float.h>

#define R_D__0	(give_log ? -INFINITY : 0.)		/* 0 */
#define M_1_SQRT_2PI	0.398942280401432677939946059934	/* 1/sqrt(2pi) */
#define M_LN_SQRT_2PI	0.918938533204672741780329736406	/* log(sqrt(2*pi)) */


double dnorm(double x, double m, double sd, int give_log) {
    return 0;
}

double dt(double x, double n, int give_log)
{
#ifdef IEEE_754
    if (ISNAN(x) || ISNAN(n))
	return x + n;
#endif
    if (n <= 0) return(NAN);
    if(!isfinite(x))
	return R_D__0;
    if(!isfinite(n))
	return dnorm(x, 0., 1., give_log);

    double u, ax, t = -bd0(n/2.,(n+1)/2.) + stirlerr((n+1)/2.) - stirlerr(n/2.),
	x2n = x*x/n, // in  [0, Inf]
	l_x2n; // := log(sqrt(1 + x2n)) = log(1 + x2n)/2
    int lrg_x2n =  (x2n > 1./DBL_EPSILON);
    if (lrg_x2n) { // large x^2/n :
	ax = fabs(x);
	l_x2n = log(ax) - log(n)/2.; // = log(x2n)/2 = 1/2 * log(x^2 / n)
	u = //  log(1 + x2n) * n/2 =  n * log(1 + x2n)/2 =
	    n * l_x2n;
    }
    else if (x2n > 0.2) {
	l_x2n = log(1 + x2n)/2.;
	u = n * l_x2n;
    } else {
	l_x2n = log1p(x2n)/2.;
	u = -bd0(n/2.,(n+x*x)/2.) + x*x/2.;
    }

    //old: return  R_D_fexp(M_2PI*(1+x2n), t-u);

    // R_D_fexp(f,x) :=  (give_log ? -0.5*log(f)+(x) : exp(x)/sqrt(f))
    // f = 2pi*(1+x2n)
    //  ==> 0.5*log(f) = log(2pi)/2 + log(1+x2n)/2 = log(2pi)/2 + l_x2n
    //	     1/sqrt(f) = 1/sqrt(2pi * (1+ x^2 / n))
    //		       = 1/sqrt(2pi)/(|x|/sqrt(n)*sqrt(1+1/x2n))
    //		       = M_1_SQRT_2PI * sqrt(n)/ (|x|*sqrt(1+1/x2n))


    if(give_log)
	return t-u - (M_LN_SQRT_2PI + l_x2n);

    // else :  if(lrg_x2n) : sqrt(1 + 1/x2n) ='= sqrt(1) = 1
    double I_sqrt_ = (lrg_x2n ? sqrt(n)/ax : exp(-l_x2n));
    return exp(t-u) * M_1_SQRT_2PI * I_sqrt_;
}