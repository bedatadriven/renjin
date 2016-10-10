/*
 * Defining the _XOPEN_SOURCE feature test macro is required in order to obtain
 * declaration of tzset() and 'timezone' from <time.h> (see man tzset).
 * However, it seems that Solaris wants it to have a value (as reported by
 * Brian D. Ripley to maintainer@bioconductor.org on 2015-29-08).
 */
#define	_XOPEN_SOURCE 600

#include "S4Vectors.h"

#include <limits.h>  /* for UINT_MAX and UINT_MIN */
#include <ctype.h>   /* for isblank() and isdigit() */
#include <stdlib.h>  /* for free() */
#include <time.h> 


static char errmsg_buf[200];


/****************************************************************************
 * unstrsplit_list()
 */

/*
 * Assumes 'x' is a character vector (this is NOT checked).
 * The destination string 'dest' must be large enough to receive the result.
 */
static void join_strings_in_buf(char *dest, SEXP x,
				const char *sep, int sep_len)
{
	int x_len, i;
	SEXP x_elt;

	x_len = LENGTH(x);
	for (i = 0; i < x_len; i++) {
		if (i != 0) {
			memcpy(dest, sep, sep_len);
			dest += sep_len;
		}
		x_elt = STRING_ELT(x, i);
		memcpy(dest, CHAR(x_elt), LENGTH(x_elt));
		dest += LENGTH(x_elt);
	}
	return;
}

/*
 * Returns a CHARSXP if success, or R_NilValue if failure.
 */
static SEXP join_strings(SEXP x, const char *sep, int sep_len)
{
	SEXP ans;
	int x_len, bufsize, i;
	char *buf;

	if (!IS_CHARACTER(x)) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "join_strings() expects a character vector");
		return R_NilValue;
	}
	x_len = LENGTH(x);

	/* 1st pass: Loop over 'x' to compute the size of the buffer. */
	bufsize = 0;
	if (x_len != 0) {
		for (i = 0; i < x_len; i++)
			bufsize += LENGTH(STRING_ELT(x, i));
		bufsize += (x_len - 1) * sep_len;
	}

	/* Allocate memory for the buffer. */
	buf = (char *) malloc((size_t) bufsize);
	if (buf == NULL) {
		snprintf(errmsg_buf, sizeof(errmsg_buf), "malloc() failed");
		return R_NilValue;
	}

	/* 2nd pass: Loop over 'x' again to fill 'buf'. */
	join_strings_in_buf(buf, x, sep, sep_len);

	/* Turn 'buf' into a CHARSXP and return it. */
	PROTECT(ans = mkCharLen(buf, bufsize));
	free(buf);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP unstrsplit_list(SEXP x, SEXP sep)
{
	SEXP ans, sep0, x_elt, ans_elt, ans_names;
	int x_len, sep0_len, i;

	if (!IS_LIST(x))
		error("'x' must be a list");
	if (!(IS_CHARACTER(sep) && LENGTH(sep) == 1))
		error("'sep' must be a single string");
	x_len = LENGTH(x);
	sep0 = STRING_ELT(sep, 0);
	sep0_len = LENGTH(sep0);
	PROTECT(ans = NEW_CHARACTER(x_len));
	for (i = 0; i < x_len; i++) {
		x_elt = VECTOR_ELT(x, i);
		if (x_elt == R_NilValue)
			continue;
		PROTECT(ans_elt = join_strings(x_elt, CHAR(sep0), sep0_len));
		if (ans_elt == R_NilValue) {
			UNPROTECT(2);
			error("in list element %d: %s", i + 1, errmsg_buf);
		}
		SET_STRING_ELT(ans, i, ans_elt);
		UNPROTECT(1);
	}
	PROTECT(ans_names = duplicate(GET_NAMES(x)));
	SET_NAMES(ans, ans_names);
	UNPROTECT(2);
	return ans;
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 * We cannot rely on the strsplit() R function to split a string into single
 * characters when the string contains junk. For example:
 *   > r <- as.raw(c(10, 255))
 *   > s <- rawToChar(r)
 *   > s
 *   [1] "\n\xff"
 *   > strsplit(s, NULL, fixed=TRUE)[[1]]
 *   [1] NA
 * doesn't work!
 * The function below should be safe, whatever the content of 's' is!
 * The length of the returned string is the number of chars in single
 * string 's'. Not vectorized.
 */
SEXP safe_strexplode(SEXP s)
{
	SEXP s0, ans;
	int s0_length, i;
	char buf[2] = "X"; /* we only care about having buf[1] == 0 */

	s0 = STRING_ELT(s, 0);
	s0_length = LENGTH(s0);

	PROTECT(ans = NEW_CHARACTER(s0_length));
	for (i = 0; i < s0_length; i++) {
		buf[0] = CHAR(s0)[i];
		SET_STRING_ELT(ans, i, mkChar(buf));
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * strsplit_as_list_of_ints()
 */

static SEXP explode_string_as_integer_vector(SEXP s, char sep0, IntAE *tmp_buf)
{
	const char *str;
	int offset, n, ret;
	long int val;

	str = CHAR(s);
	offset = _IntAE_set_nelt(tmp_buf, 0);
	while (str[offset]) {
		ret = sscanf(str + offset, "%ld%n", &val, &n);
		if (ret != 1) {
			snprintf(errmsg_buf, sizeof(errmsg_buf),
				 "decimal integer expected at char %d",
				 offset + 1);
			return R_NilValue;
		}
		offset += n;
		while (isblank(str[offset])) offset++;
		if (val < INT_MIN || val > INT_MAX) {
			UNPROTECT(1);
			snprintf(errmsg_buf, sizeof(errmsg_buf),
				 "out of range integer at char %d",
				 offset + 1);
			return R_NilValue;
		}
		_IntAE_insert_at(tmp_buf, _IntAE_get_nelt(tmp_buf), (int) val);
		if (str[offset] == '\0')
			break;
		if (str[offset] != sep0) {
			snprintf(errmsg_buf, sizeof(errmsg_buf),
				 "separator expected at char %d",
				 offset + 1);
			return R_NilValue;
		}
		offset++;
	}
	return _new_INTEGER_from_IntAE(tmp_buf);
}

/* --- .Call ENTRY POINT --- */
SEXP strsplit_as_list_of_ints(SEXP x, SEXP sep)
{
	SEXP ans, x_elt, ans_elt;
	int ans_length, i;
	char sep0;
	IntAE *tmp_buf;

	ans_length = LENGTH(x);
	sep0 = CHAR(STRING_ELT(sep, 0))[0];
	if (isdigit(sep0) || sep0 == '+' || sep0 == '-')
		error("'sep' cannot be a digit, \"+\" or \"-\"");
	tmp_buf = _new_IntAE(0, 0, 0);
	PROTECT(ans = NEW_LIST(ans_length));
	for (i = 0; i < ans_length; i++) {
		x_elt = STRING_ELT(x, i);
		if (x_elt == NA_STRING) {
			UNPROTECT(1);
			error("'x' contains NAs");
		}
		PROTECT(ans_elt = explode_string_as_integer_vector(x_elt, sep0,
								   tmp_buf));
		if (ans_elt == R_NilValue) {
			UNPROTECT(2);
			error("in list element %d: %s", i + 1, errmsg_buf);
		}
		SET_VECTOR_ELT(ans, i, ans_elt);
		UNPROTECT(1);
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * svn_time() returns the time in Subversion format, e.g.:
 *   "2007-12-07 10:03:15 -0800 (Fri, 07 Dec 2007)"
 * The -0800 part will be adjusted if daylight saving time is in effect.
 *
 * TODO: Find a better home for this function.
 */

/*
 * 'out_size' should be at least 45 (for year < 10000, 44 chars will be
 * printed to it + '\0').
 */
static int get_svn_time(time_t t, char *out, size_t out_size)
{
//#if defined(__INTEL_COMPILER)
//	return -1;
//#else /* defined(__INTEL_COMPILER) */
	struct tm result;
	int utc_offset, n;

	static const char
	  *wday2str[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"},
	  *mon2str[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"},
	  *svn_format = "%d-%02d-%02d %02d:%02d:%02d %+03d00 (%s, %02d %s %d)";

	//localtime_r() not available on Windows+MinGW
	//localtime_r(&t, &result);
	result = *localtime(&t);
#if defined(__APPLE__) || defined(__FreeBSD__)
	//'struct tm' has no member named 'tm_gmtoff' on Windows+MinGW
	utc_offset = result.tm_gmtoff / 3600;
#else /* defined(__APPLE__) || defined(__FreeBSD__) */
	tzset();
	//timezone is not portable (is a function, not a long, on OS X Tiger)
	utc_offset = - (timezone / 3600);
	if (result.tm_isdst > 0)
		utc_offset++;
#endif /* defined(__APPLE__) || defined(__FreeBSD__) */
	n = snprintf(out, out_size, svn_format,
		result.tm_year + 1900,
		result.tm_mon + 1,
		result.tm_mday,
		result.tm_hour,
		result.tm_min,
		result.tm_sec,
		utc_offset,
		wday2str[result.tm_wday],
		result.tm_mday,
		mon2str[result.tm_mon],
		result.tm_year + 1900);
	return n >= out_size ? -1 : 0;
//#endif /* defined(__INTEL_COMPILER) */
}

/* --- .Call ENTRY POINT --- */
SEXP svn_time()
{
	time_t t;
	char buf[45];

	t = time(NULL);
	if (t == (time_t) -1)
		error("S4Vectors internal error in svn_time(): "
		      "time(NULL) failed");
	if (get_svn_time(t, buf, sizeof(buf)) != 0)
		error("S4Vectors internal error in svn_time(): "
		      "get_svn_time() failed");
	return mkString(buf);
}

