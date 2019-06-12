

#include <float.h> /* for DBL_EPSILON */
#include <math.h>
#include <stdlib.h>

#include <Defn.h>
#include <Rmath.h>
#include <R_ext/PrtUtil.h>
#include <R_ext/Error.h>

#define _(x) x
#define min(x,y) (x < y ? x : y)

/* Included from printutils.c */

int attribute_hidden IndexWidth(R_xlen_t n)
{
    return (int) (log10(n + 0.5) + 1);
}



/* Included from src/main/complex.c */

/* used in format.c and printutils.c */
#define MAX_DIGITS 22
void attribute_hidden z_prec_r(Rcomplex *r, const Rcomplex *x, double digits)
{
    double m = 0.0, m1, m2;
    int dig, mag;

    r->r = x->r; r->i = x->i;
    m1 = fabs(x->r); m2 = fabs(x->i);
    if(R_FINITE(m1)) m = m1;
    if(R_FINITE(m2) && m2 > m) m = m2;
    if (m == 0.0) return;
    if (!R_FINITE(digits)) {
        if(digits > 0) return; else {r->r = r->i = 0.0; return ;}
    }
    dig = (int)floor(digits+0.5);
    if (dig > MAX_DIGITS) return; else if (dig < 1) dig = 1;
    mag = (int)floor(log10(m));
    dig = dig - mag - 1;
    if (dig > 306) {
        double pow10 = 1.0e4;
        digits = (double)(dig - 4);
        r->r = fround(pow10 * x->r, digits)/pow10;
        r->i = fround(pow10 * x->i, digits)/pow10;
    } else {
        digits = (double)(dig);
        r->r = fround(x->r, digits);
        r->i = fround(x->i, digits);
    }
}

#define NB 1000
#define NB3 NB+3

const char *EncodeReal0(double x, int w, int d, int e, const char *dec)
{
    static char buff[NB], buff2[2*NB];
    char fmt[20], *out = buff;

    /* IEEE allows signed zeros (yuck!) */
    if (x == 0.0) x = 0.0;
    if (!R_FINITE(x)) {
        if(ISNA(x)) snprintf(buff, NB, "%*s", min(w, (NB-1)), "NA");
        else if(ISNAN(x)) snprintf(buff, NB, "%*s", min(w, (NB-1)), "NaN");
        else if(x > 0) snprintf(buff, NB, "%*s", min(w, (NB-1)), "Inf");
        else snprintf(buff, NB, "%*s", min(w, (NB-1)), "-Inf");
    }
    else if (e) {
        if(d) {
            sprintf(fmt,"%%#%d.%de", min(w, (NB-1)), d);
            snprintf(buff, NB, fmt, x);
        }
        else {
            sprintf(fmt,"%%%d.%de", min(w, (NB-1)), d);
            snprintf(buff, NB, fmt, x);
        }
    }
    else { /* e = 0 */
        sprintf(fmt,"%%%d.%df", min(w, (NB-1)), d);
        snprintf(buff, NB, fmt, x);
    }
    buff[NB-1] = '\0';

    if(strcmp(dec, ".")) {
        char *p, *q;
        for(p = buff, q = buff2; *p; p++) {
            if(*p == '.') for(const char *r = dec; *r; r++) *q++ = *r;
            else *q++ = *p;
        }
        *q = '\0';
        out = buff2;
    }

    return out;
}


const char
*EncodeComplex(Rcomplex x, int wr, int dr, int er, int wi, int di, int ei,
               const char *dec)
{
    static char buff[NB3];

    /* IEEE allows signed zeros; strip these here */
    if (x.r == 0.0) x.r = 0.0;
    if (x.i == 0.0) x.i = 0.0;

    if (ISNA(x.r) || ISNA(x.i)) {
        snprintf(buff, NB,
                 "%*s", /* was "%*s%*s", R_print.gap, "", */
                 min(wr+wi+2, (NB-1)), "NA");
    } else {
        char Re[NB];
        const char *Im, *tmp;
        int flagNegIm = 0;
        Rcomplex y;
        /* formatComplex rounded, but this does not, and we need to
           keep it that way so we don't get strange trailing zeros.
           But we do want to avoid printing small exponentials that
           are probably garbage.
         */
        z_prec_r(&y, &x, /* R_print.digits = */ 7);
        /* EncodeReal has static buffer, so copy */
        tmp = EncodeReal0(y.r == 0. ? y.r : x.r, wr, dr, er, dec);
        strcpy(Re, tmp);
        if ( (flagNegIm = (x.i < 0)) ) x.i = -x.i;
        Im = EncodeReal0(y.i == 0. ? y.i : x.i, wi, di, ei, dec);
        snprintf(buff, NB3, "%s%s%si", Re, flagNegIm ? "-" : "+", Im);
    }
    buff[NB3-1] = '\0';
    return buff;
}

/* Include from util.c */

/* A version that reports failure as an error */
size_t Mbrtowc(wchar_t *wc, const char *s, size_t n, mbstate_t *ps)
{
    size_t used;

    if(n <= 0 || !*s) return (size_t)0;
    used = mbrtowc(wc, s, n, ps);
    if((int) used < 0) {
        /* This gets called from the menu setup in RGui */
//        if (!R_Is_Running) return (size_t)-1;
        /* let's try to print out a readable version */
//        R_CheckStack2(4*strlen(s) + 10);
        char err[4*strlen(s) + 1], *q;
        const char *p;
        for(p = s, q = err; *p; ) {
            /* don't do the first to keep ps state straight */
            if(p > s) used = mbrtowc(NULL, p, n, ps);
            if(used == 0) break;
            else if((int) used > 0) {
                memcpy(q, p, used);
                p += used;
                q += used;
                n -= used;
            } else {
                sprintf(q, "<%02x>", (unsigned char) *p++);
                q += 4;
                n--;
            }
        }
        *q = '\0';
        error(_("invalid multibyte string at '%s'"), err);
    }
    return used;
}

int Ri18n_wctype(const char *name)
{
    error("TODO: Ri18n_wctype");
}

int Ri18n_iswctype(int wc, int desc)
{
    error("TODO: Ri18n_iswctype");
}

