#include "S4Vectors.h"

#include <stdlib.h>  /* for malloc, free */


static SEXP _new_Rle(SEXP values, SEXP lengths)
{
	SEXP classdef, ans;

	PROTECT(classdef = MAKE_CLASS("Rle"));
	PROTECT(ans = NEW_OBJECT(classdef));
	SET_SLOT(ans, install("values"), values);
	SET_SLOT(ans, install("lengths"), lengths);
	UNPROTECT(2);
	return ans;
}


/****************************************************************************
 * The compute_<run-type>_runs() low-level helper functions.
 *
 * To compute only the nb of runs without actually computing the runs
 * (degraded mode), set 'run_lengths' to NULL.
 */

static int compute_int_runs(const int *values, int nvalues,
		const int *lengths,
		int *run_values, int *run_lengths)
{
	int i, nrun, lengths_elt;
	int val0;

	for (i = nrun = 0, lengths_elt = 1; i < nvalues; i++, values++) {
		if (lengths != NULL) {
			lengths_elt = lengths[i];
			if (lengths_elt == 0)
				continue;
		}
		if (nrun != 0 && *values == val0) {
			if (run_lengths != NULL)
				run_lengths[nrun - 1] += lengths_elt;
			continue;
		}
		val0 = *values;
		if (run_lengths != NULL) {
			run_lengths[nrun] = lengths_elt;
			run_values[nrun] = val0;
		}
		nrun++;
	}
	return nrun;
}

static int compute_double_runs(const double *values, int nvalues,
		const int *lengths,
		double *run_values, int *run_lengths)
{
	int i, nrun, lengths_elt;
	double val0;

	for (i = nrun = 0, lengths_elt = 1; i < nvalues; i++, values++) {
		if (lengths != NULL) {
			lengths_elt = lengths[i];
			if (lengths_elt == 0)
				continue;
		}
		if (nrun != 0 && ((*values == val0) ||
				  (R_IsNA(*values) && R_IsNA(val0)) ||
				  (R_IsNaN(*values) && R_IsNaN(val0))))
		{
			if (run_lengths != NULL)
				run_lengths[nrun - 1] += lengths_elt;
			continue;
		}
		val0 = *values;
		if (run_lengths != NULL) {
			run_lengths[nrun] = lengths_elt;
			run_values[nrun] = val0;
		}
		nrun++;
	}
	return nrun;
}

static int compute_Rcomplex_runs(const Rcomplex *values, int nvalues,
		const int *lengths,
		Rcomplex *run_values, int *run_lengths)
{
	int i, nrun, lengths_elt;
	Rcomplex val0;

	for (i = nrun = 0, lengths_elt = 1; i < nvalues; i++, values++) {
		if (lengths != NULL) {
			lengths_elt = lengths[i];
			if (lengths_elt == 0)
				continue;
		}
		if (nrun != 0 && ((values->r == val0.r) ||
				  (R_IsNA(values->r) && R_IsNA(val0.r)) ||
				  (R_IsNaN(values->r) && R_IsNaN(val0.r)))
			      && ((values->i == val0.i) ||
				  (R_IsNA(values->i) && R_IsNA(val0.i)) ||
				  (R_IsNaN(values->i) && R_IsNaN(val0.i))))
		{
			if (run_lengths != NULL)
				run_lengths[nrun - 1] += lengths_elt;
			continue;
		}
		val0 = *values;
		if (run_lengths != NULL) {
			run_lengths[nrun] = lengths_elt;
			run_values[nrun] = val0;
		}
		nrun++;
	}
	return nrun;
}

static int compute_CHARSXP_runs(SEXP values,
		const int *lengths,
		SEXP run_values, int *run_lengths)
{
	int nvalues, i, nrun, lengths_elt;
	SEXP values_elt, val0 = NULL;

	nvalues = LENGTH(values);
	for (i = nrun = 0, lengths_elt = 1; i < nvalues; i++) {
		if (lengths != NULL) {
			lengths_elt = lengths[i];
			if (lengths_elt == 0)
				continue;
		}
		values_elt = STRING_ELT(values, i);
		if (nrun != 0 && values_elt == val0) {
			if (run_lengths != NULL)
				run_lengths[nrun - 1] += lengths_elt;
			continue;
		}
		val0 = values_elt;
		if (run_lengths != NULL) {
			run_lengths[nrun] = lengths_elt;
			SET_STRING_ELT(run_values, nrun, val0);
		}
		nrun++;
	}
	return nrun;
}

static int compute_Rbyte_runs(const Rbyte *values, int nvalues,
		const int *lengths,
		Rbyte *run_values, int *run_lengths)
{
	int i, nrun, lengths_elt;
	Rbyte val0;

	for (i = nrun = 0, lengths_elt = 1; i < nvalues; i++, values++) {
		if (lengths != NULL) {
			lengths_elt = lengths[i];
			if (lengths_elt == 0)
				continue;
		}
		if (nrun != 0 && *values == val0) {
			if (run_lengths != NULL)
				run_lengths[nrun - 1] += lengths_elt;
			continue;
		}
		val0 = *values;
		if (run_lengths != NULL) {
			run_lengths[nrun] = lengths_elt;
			run_values[nrun] = val0;
		}
		nrun++;
	}
	return nrun;
}


/****************************************************************************
 * The C level Rle smart constructors.
 *
 * 'lengths' must be either (a) an int array of length 'nvalues' with no NA
 * or negative values, or (b) NULL. If (b) then it's treated as an array of
 * length 'nvalues' filled with 1's (i.e. each element is set to 1).
 * 'buflength' is the length of the temporary buffers allocated internally by
 * the smart constructor for computing the runs. If set to 0, then a 2-pass
 * algo is used that doesn't use any temporary buffer, typically leading to
 * 20%-30% less memory used (it also seems slightly faster on my machine).
 * Setting 'buflength' to 'nvalues' is safe because the number of runs can
 * only be <= 'nvalues'. If 'buflength' is > 'nvalues', then 'nvalues' is used
 * instead.
 * WARNING: Avoid using a 'buflength' that is > 0 and < 'nvalues' unless you
 * know what you are doing!
 */

SEXP _construct_logical_Rle(const int *values, int nvalues,
		const int *lengths, int buflength)
{
	int nrun, *buf_lengths;
	int *buf_values;
	SEXP ans_lengths, ans_values, ans;

	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		buf_values = (int *) R_alloc(buflength, sizeof(int));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_int_runs(values, nvalues, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_LOGICAL(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_int_runs(values, nvalues, lengths,
				LOGICAL(ans_values), INTEGER(ans_lengths));
	} else {
		memcpy(LOGICAL(ans_values), buf_values, nrun * sizeof(int));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

SEXP _construct_integer_Rle(const int *values, int nvalues,
		const int *lengths, int buflength)
{
	int nrun, *buf_lengths;
	int *buf_values;
	SEXP ans_lengths, ans_values, ans;

	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		buf_values = (int *) R_alloc(buflength, sizeof(int));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_int_runs(values, nvalues, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_INTEGER(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_int_runs(values, nvalues, lengths,
				INTEGER(ans_values), INTEGER(ans_lengths));
	} else {
		memcpy(INTEGER(ans_values), buf_values, nrun * sizeof(int));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

SEXP _construct_numeric_Rle(const double *values, int nvalues,
		const int *lengths, int buflength)
{
	int nrun, *buf_lengths;
	double *buf_values;
	SEXP ans_lengths, ans_values, ans;

	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		buf_values = (double *) R_alloc(buflength, sizeof(double));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_double_runs(values, nvalues, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_NUMERIC(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_double_runs(values, nvalues, lengths,
				REAL(ans_values), INTEGER(ans_lengths));
	} else {
		memcpy(REAL(ans_values), buf_values, nrun * sizeof(double));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

SEXP _construct_complex_Rle(const Rcomplex *values, int nvalues,
		const int *lengths, int buflength)
{
	int nrun, *buf_lengths;
	Rcomplex *buf_values;
	SEXP ans_lengths, ans_values, ans;

	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		buf_values = (Rcomplex *) R_alloc(buflength, sizeof(Rcomplex));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_Rcomplex_runs(values, nvalues, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_COMPLEX(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_Rcomplex_runs(values, nvalues, lengths,
				COMPLEX(ans_values), INTEGER(ans_lengths));
	} else {
		memcpy(COMPLEX(ans_values), buf_values,
					    nrun * sizeof(Rcomplex));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

SEXP _construct_character_Rle(SEXP values,
		const int *lengths, int buflength)
{
	int nvalues, nrun, *buf_lengths, i;
	SEXP buf_values, ans_lengths, ans_values, ans;

	nvalues = LENGTH(values);
	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		PROTECT(buf_values = NEW_CHARACTER(buflength));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_CHARSXP_runs(values, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_CHARACTER(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_CHARSXP_runs(values, lengths,
				ans_values, INTEGER(ans_lengths));
	} else {
		for (i = 0; i < nrun; i++)
		    SET_STRING_ELT(ans_values, i, STRING_ELT(buf_values, i));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(buflength == 0 ? 3 : 4);
	return ans;
}

SEXP _construct_raw_Rle(const Rbyte *values, int nvalues,
		const int *lengths, int buflength)
{
	int nrun, *buf_lengths;
	Rbyte *buf_values;
	SEXP ans_lengths, ans_values, ans;

	if (buflength > nvalues)
		buflength = nvalues;
	if (buflength == 0) {
		/* 1st pass: compute only the nb of runs */
		buf_values = NULL;
		buf_lengths = NULL;
	} else {
		buf_values = (Rbyte *) R_alloc(buflength, sizeof(Rbyte));
		buf_lengths = (int *) R_alloc(buflength, sizeof(int));
	}
	nrun = compute_Rbyte_runs(values, nvalues, lengths,
				buf_values, buf_lengths);
	PROTECT(ans_values = NEW_RAW(nrun));
	PROTECT(ans_lengths = NEW_INTEGER(nrun));
	if (buflength == 0) {
		/* 2nd pass: fill 'ans_values' and 'ans_lengths' */
		compute_Rbyte_runs(values, nvalues, lengths,
				RAW(ans_values), INTEGER(ans_lengths));
	} else {
		memcpy(RAW(ans_values), buf_values, nrun * sizeof(Rbyte));
		memcpy(INTEGER(ans_lengths), buf_lengths, nrun * sizeof(int));
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

SEXP _construct_Rle(SEXP values, const int *lengths, int buflength)
{
	int nvalues;
	SEXP ans, ans_values, ans_values_class, ans_values_levels;

	nvalues = LENGTH(values);
	switch (TYPEOF(values)) {
	    case LGLSXP:
		PROTECT(ans = _construct_logical_Rle(
					LOGICAL(values), nvalues,
					lengths, buflength));
		break;
	    case INTSXP:
		PROTECT(ans = _construct_integer_Rle(
					INTEGER(values), nvalues,
					lengths, buflength));
		/* 'values' could be a factor in which case we need to
		   propagate its levels.  */
		if (isFactor(values)) {
			ans_values = GET_SLOT(ans, install("values"));
			/* Levels must be set before class. */
			PROTECT(ans_values_levels =
					duplicate(GET_LEVELS(values)));
			SET_LEVELS(ans_values, ans_values_levels);
			UNPROTECT(1);
			PROTECT(ans_values_class =
					duplicate(GET_CLASS(values)));
			SET_CLASS(ans_values, ans_values_class);
			UNPROTECT(1);
		}
		break;
	    case REALSXP:
		PROTECT(ans = _construct_numeric_Rle(
					REAL(values), nvalues,
					lengths, buflength));
		break;
	    case CPLXSXP:
		PROTECT(ans = _construct_complex_Rle(
					COMPLEX(values), nvalues,
					lengths, buflength));
		break;
	    case STRSXP:
		PROTECT(ans = _construct_character_Rle(values,
					lengths, buflength));
		break;
	    case RAWSXP:
		PROTECT(ans = _construct_raw_Rle(RAW(values),
					nvalues, lengths, buflength));
		break;
	    default:
		error("Rle of type '%s' is not supported",
		      CHAR(type2str(TYPEOF(values))));
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * The Rle constructor.
 */

/* --- .Call ENTRY POINT --- */
SEXP Rle_constructor(SEXP values, SEXP lengths, SEXP check, SEXP buflength)
{
	int nvalues, buflength0;
	const int *lengths_p;

	nvalues = LENGTH(values);
	if (LOGICAL(check)[0] && LENGTH(lengths) > 0) {
		if (LENGTH(lengths) != nvalues)
			error("'length(lengths)' != 'length(values)'");
		_sum_non_neg_ints(INTEGER(lengths), LENGTH(lengths),
				  "lengths");
	}
	lengths_p = LENGTH(lengths) > 0 ? INTEGER(lengths) : NULL;
	buflength0 = INTEGER(buflength)[0];
	return _construct_Rle(values, lengths_p, buflength0);
}


/****************************************************************************
 * The Rle start() and end() getters (.Call ENTRY POINTS).
 */

SEXP Rle_start(SEXP x)
{
	int i, nrun, *len_elt, *prev_start, *curr_start;
	SEXP lengths, ans;

	lengths = GET_SLOT(x, install("lengths"));
	nrun = LENGTH(lengths);

	PROTECT(ans = NEW_INTEGER(nrun));

	if (nrun > 0) {
		INTEGER(ans)[0] = 1;
		for(i = 1, len_elt = INTEGER(lengths),
			prev_start = INTEGER(ans), curr_start = INTEGER(ans) + 1;
		    i < nrun; i++, len_elt++, prev_start++, curr_start++) {
			*curr_start = *prev_start + *len_elt;
		}
	}

	UNPROTECT(1);

	return ans;
}

SEXP Rle_end(SEXP x)
{
	int i, nrun, *len_elt, *prev_end, *curr_end;
	SEXP lengths, ans;

	lengths = GET_SLOT(x, install("lengths"));
	nrun = LENGTH(lengths);

	PROTECT(ans = NEW_INTEGER(nrun));

	if (nrun > 0) {
		INTEGER(ans)[0] = INTEGER(lengths)[0];
		for(i = 1, len_elt = INTEGER(lengths) + 1,
			prev_end = INTEGER(ans), curr_end = INTEGER(ans) + 1;
		    i < nrun; i++, len_elt++, prev_end++, curr_end++) {
			*curr_end = *prev_end + *len_elt;
		}
	}

	UNPROTECT(1);

	return ans;
}


/****************************************************************************
 * ranges_to_runs_mapper()
 */

static char errmsg_buf[200];

static const char *range2runs_mapper1(const int *run_lengths, int nrun,
		int range_start, int range_end,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim)
{
	int offset, i, j;

	if (range_start == NA_INTEGER || range_start < 1) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'start' must be >= 1");
		return errmsg_buf;
	}
	if (range_end == NA_INTEGER || range_end < range_start - 1) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'end' must be >= 'start' - 1");
		return errmsg_buf;
	}
	offset = 0;
	if (range_end >= range_start) {
		for (i = 0; i < nrun; i++) {
			offset += run_lengths[i];
			if (offset >= range_start)
				break;
		}
		if (i < nrun)
			*Ltrim = range_start - offset + run_lengths[i] - 1;
		if (offset >= range_end) {
			j = i;
		} else {
			for (j = i + 1; j < nrun; j++) {
				offset += run_lengths[j];
				if (offset >= range_end)
					break;
			}
		}
		*Rtrim = offset - range_end;
		*spanned_nrun = j - i + 1;
	} else {
		/* Zero-width range. */
		*spanned_nrun = 0;
		j = -1;
		while (offset < range_end) {
			j++;
			if (j >= nrun)
				break;
			offset += run_lengths[j];
		}
		if (offset == range_end)
			i = j + 1;
		else
			i = j;
	}
	if (range_end > offset) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'end' must be <= 'length(x)'");
		return errmsg_buf;
	}
	*offset_nrun = i;
	return NULL;
}

static int int_bsearch(int x, const int *breakpoints, int nbreakpoints)
{
	int n1, n2, n, bp;

	if (nbreakpoints == 0)
		return nbreakpoints;

	/* Check last element. */
	n2 = nbreakpoints - 1;
	bp = breakpoints[n2];
	if (x > bp)
		return nbreakpoints;
	if (x == bp)
		return n2;

	/* Check first element. */
	n1 = 0;
	bp = breakpoints[n1];
	if (x <= bp)
		return n1;

	/* Binary search.
	   Seems that using >> 1 instead of / 2 is faster, even when compiling
	   with 'gcc -O2' (one would hope that the optimizer is able to do that
	   kind of optimization). */
	while ((n = (n1 + n2) >> 1) != n1) {
		bp = breakpoints[n];
		if (x == bp)
			return n;
		if (x > bp)
			n1 = n;
		else
			n2 = n;
	}
	return n2;
}

/*
 * Like range2runs_mapper1() but takes 'run_breakpoints' (obtained with
 * 'cumsum(run_lengths)') instead of 'run_lengths' as input and uses a binary
 * search.
 */
static const char *range2runs_mapper2(const int *run_breakpoints, int nrun,
		int range_start, int range_end,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim)
{
	int x_len, end_run;

	if (range_start == NA_INTEGER || range_start < 1) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'start' must be >= 1");
		return errmsg_buf;
	}
	x_len = nrun == 0 ? 0 : run_breakpoints[nrun - 1];
	if (range_end == NA_INTEGER || range_end > x_len) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'end' must be <= 'length(x)'");
		return errmsg_buf;
	}
	if (range_end < range_start - 1) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "'end' must be >= 'start' - 1");
		return errmsg_buf;
	}
	*offset_nrun = int_bsearch(range_start, run_breakpoints, nrun);
	if (range_end >= range_start) {
		end_run = int_bsearch(range_end, run_breakpoints, nrun);
		*spanned_nrun = end_run - *offset_nrun + 1;
		*Ltrim = range_start - 1;
		if (*offset_nrun >= 1)
			*Ltrim -= run_breakpoints[*offset_nrun - 1];
		*Rtrim = run_breakpoints[end_run] - range_end;
	} else {
		/* Zero-width range. */
		*spanned_nrun = 0;
	}
	return NULL;
}

/* Method 1: Naive algo (inefficient if more than 1 range). */
static const char *ranges2runs_mapper1(const int *run_lengths, int nrun,
		const int *start, const int *width, int nranges,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim)
{
	int i, start_i, end_i;
	const char *errmsg;

	errmsg = NULL;
	for (i = 0; i < nranges; i++) {
		start_i = start[i];
		end_i = start_i - 1 + width[i];
		errmsg = range2runs_mapper1(run_lengths, nrun,
					start_i, end_i,
					offset_nrun + i, spanned_nrun + i,
					Ltrim + i, Rtrim + i);
		if (errmsg != NULL)
			break;
	}
	return errmsg;
}

/* Method 2: Binary search. */
static const char *ranges2runs_mapper2(const int *run_lengths, int nrun,
		const int *start, const int *width, int nranges,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim)
{
	int *run_breakpoints, breakpoint, i, start_i, end_i;
	const char *errmsg;

	run_breakpoints = (int *) malloc(sizeof(int) * nrun);
	if (run_breakpoints == NULL) {
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "ranges2runs_mapper2: memory allocation failed");
		return errmsg_buf;
	}
	breakpoint = 0;
	for (i = 0; i < nrun; i++) {
		breakpoint += run_lengths[i];
		run_breakpoints[i] = breakpoint;
	}
	errmsg = NULL;
	for (i = 0; i < nranges; i++) {
		start_i = start[i];
		end_i = start_i - 1 + width[i];
		errmsg = range2runs_mapper2(run_breakpoints, nrun,
					start_i, end_i,
					offset_nrun + i, spanned_nrun + i,
					Ltrim + i, Rtrim + i);
		if (errmsg != NULL)
			break;
	}
	free(run_breakpoints);
	return errmsg; 
}

/* Method 3: Sort the starts and ends of the ranges. */
static const char *ranges2runs_mapper3(const int *run_lengths, int nrun,
		const int *start, const int *width, int nranges,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim)
{
	int SEbuf_len, *SEbuf, *SEorder,
	    *SEbuf2, SE, breakpoint, i, j, k, SE_run;

	SEbuf_len = 2 * nranges;
	SEbuf = (int *) malloc(sizeof(int) * SEbuf_len);
	SEorder = (int *) malloc(sizeof(int) * SEbuf_len);
	if (SEbuf == NULL || SEorder == NULL) {
		if (SEbuf != NULL)
			free(SEbuf);
		if (SEorder != NULL)
			free(SEorder);
		snprintf(errmsg_buf, sizeof(errmsg_buf),
			 "ranges2runs_mapper3: memory allocation failed");
		return errmsg_buf;
	}
	memcpy(SEbuf, start, sizeof(int) * nranges);
	SEbuf2 = SEbuf + nranges;
	for (i = 0; i < nranges; i++)
		SEbuf2[i] = start[i] - 1 + width[i];
	_get_order_of_int_array(SEbuf, SEbuf_len, 0, SEorder, 0);
	breakpoint = j = 0;
	for (k = 0; k < SEbuf_len; k++) {
		i = SEorder[k];
		SE = SEbuf[i];
		while (breakpoint < SE && j < nrun)
			breakpoint += run_lengths[j++];
		if (i < nranges) {
			/* SE is a start. */
			if (SE < 1) {
				free(SEbuf);
				free(SEorder);
				snprintf(errmsg_buf, sizeof(errmsg_buf),
					 "'start' must be >= 1");
				return errmsg_buf;
			}
			Ltrim[i] = - breakpoint;
			if (SE > breakpoint) {
				SE_run = j;
			} else {
				SE_run = j - 1;
				Ltrim[i] += run_lengths[SE_run];
			}
			offset_nrun[i] = SE_run;
		} else {
			/* SE is an end. */
			if (SE > breakpoint) {
				free(SEbuf);
				free(SEorder);
				snprintf(errmsg_buf, sizeof(errmsg_buf),
					 "'end' must be <= 'length(x)'");
				return errmsg_buf;
			}
			i -= nranges;
			Rtrim[i] = breakpoint;
			SE_run = j - 1;
			spanned_nrun[i] = SE_run;
		}
	}
	for (i = 0; i < nranges; i++) {
		if (width[i] != 0) {
			spanned_nrun[i] -= offset_nrun[i] - 1;
			Ltrim[i] += start[i] - 1;
			Rtrim[i] -= SEbuf2[i];
		} else {
			/* Zero-width range. */
			spanned_nrun[i] = 0;
		}
	}
	free(SEbuf);
	free(SEorder);
	return NULL; 
}

/* If 'method' is not >= 0 and <= 3, then the function does nothing (no-op). */
static const char *ranges2runs_mapper(const int *run_lengths, int nrun,
		const int *start, const int *width, int nranges,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim,
		int method)
{
	const char *(*fun)(const int *run_lengths, int nrun,
		const int *start, const int *width, int nranges,
		int *offset_nrun, int *spanned_nrun, int *Ltrim, int *Rtrim);

	if (method == 0) {
		if (nranges == 1) {
			method = 1;
		} else {
			/* If nranges > 0.05 * nrun then use algo based on
			   binary search (method 2), otherwise use algo based
			   on qsort (method 3). The 5% cutoff is empirical
			   (based on timings obtained in January 2016 on a
			   Dell LATITUDE E6440 laptop running 64-bit Ubuntu
			   14.04.3 LTS). */
			method = nranges > 0.05 * nrun ? 2 : 3;
		}
	}
	switch (method) {
		case 1: fun = ranges2runs_mapper1; break;
		case 2: fun = ranges2runs_mapper2; break;
		case 3: fun = ranges2runs_mapper3; break;
		default: return NULL;  /* do nothing */
	}
	return fun(run_lengths, nrun,
		   start, width, nranges,
		   offset_nrun, spanned_nrun, Ltrim, Rtrim);
}

/* --- .Call ENTRY POINT --- */
SEXP ranges_to_runs_mapper(SEXP run_lengths, SEXP start, SEXP width,
		SEXP method)
{
	SEXP offset_nrun, spanned_nrun, Ltrim, Rtrim, ans;
	int nrun, nranges;
	const int *start_p, *width_p;
	const char *errmsg;

	nrun = LENGTH(run_lengths);
	nranges = _check_integer_pairs(start, width,
				       &start_p, &width_p,
				       "start", "width");
	PROTECT(offset_nrun = NEW_INTEGER(nranges));
	PROTECT(spanned_nrun = NEW_INTEGER(nranges));
	PROTECT(Ltrim = NEW_INTEGER(nranges));
	PROTECT(Rtrim = NEW_INTEGER(nranges));
	errmsg = ranges2runs_mapper(INTEGER(run_lengths), nrun,
				start_p, width_p, nranges,
				INTEGER(offset_nrun), INTEGER(spanned_nrun),
				INTEGER(Ltrim), INTEGER(Rtrim),
				INTEGER(method)[0]);
	if (errmsg != NULL) {
		UNPROTECT(4);
		error(errmsg);
	}
	PROTECT(ans = NEW_LIST(4));
	SET_VECTOR_ELT(ans, 0, offset_nrun);
	SET_VECTOR_ELT(ans, 1, spanned_nrun);
	SET_VECTOR_ELT(ans, 2, Ltrim);
	SET_VECTOR_ELT(ans, 3, Rtrim);
	UNPROTECT(5);
	return ans;
}


/****************************************************************************
 * Rle_extract_range() and Rle_extract_ranges()
 */

static SEXP extract_Rle_range(SEXP x_values, const int *x_lengths,
		int start_nrun, int spanned_nrun, int Ltrim, int Rtrim)
{
	SEXP ans_values, ans_lengths, ans;

	PROTECT(ans_values = _subset_vectorORfactor_by_ranges(x_values,
					&start_nrun, &spanned_nrun, 1));
	PROTECT(ans_lengths = NEW_INTEGER(spanned_nrun));
	if (spanned_nrun != 0) {
		memcpy(INTEGER(ans_lengths),
		       x_lengths + start_nrun - 1,
		       sizeof(int) * spanned_nrun);
		INTEGER(ans_lengths)[0] -= Ltrim;
		INTEGER(ans_lengths)[spanned_nrun - 1] -= Rtrim;
	}
	PROTECT(ans = _new_Rle(ans_values, ans_lengths));
	UNPROTECT(3);
	return ans;
}

/*
 * Extract 'nranges' Rle's from 'x'. Each Rle to extract is specified by
 * 'start_nrun[i]', 'spanned_nrun[i]', 'Ltrim[i]', and 'Rtrim[i]'.
 * If 'as_list' is TRUE, then the extracted Rle's are returned in a list of
 * length 'nranges'. Otherwise, the single Rle obtained by concatenating them
 * all together is returned.
 */
static SEXP subset_Rle_by_runs(SEXP x,
		const int *start_nrun, const int *spanned_nrun,
		const int *Ltrim, const int *Rtrim, int nranges,
		int as_list)
{
	SEXP x_values, x_lengths, tmp_values, ans, ans_elt;
	int tmp_nrun, *tmp_lengths, i, n;

	x_values = GET_SLOT(x, install("values"));
	x_lengths = GET_SLOT(x, install("lengths"));
	if (as_list == 1) {
		PROTECT(ans = NEW_LIST(nranges));
		for (i = 0; i < nranges; i++) {
			PROTECT(ans_elt = extract_Rle_range(x_values,
						INTEGER(x_lengths),
						start_nrun[i], spanned_nrun[i],
						Ltrim[i], Rtrim[i]));
			SET_VECTOR_ELT(ans, i, ans_elt);
			UNPROTECT(1);
		}
		UNPROTECT(1);
		return ans;
	}
	if (nranges == 1)
		return extract_Rle_range(x_values, INTEGER(x_lengths),
				start_nrun[0], spanned_nrun[0],
				Ltrim[0], Rtrim[0]);
	PROTECT(tmp_values = _subset_vectorORfactor_by_ranges(x_values,
					start_nrun, spanned_nrun, nranges));
	tmp_nrun = LENGTH(tmp_values);
	tmp_lengths = (int *) R_alloc(sizeof(int), tmp_nrun);
	for (i = tmp_nrun = 0; i < nranges; i++) {
		n = spanned_nrun[i];
		if (n == 0)
			continue;
		memcpy(tmp_lengths + tmp_nrun,
		       INTEGER(x_lengths) + start_nrun[i] - 1,
		       sizeof(int) * n);
		tmp_lengths[tmp_nrun] -= Ltrim[i];
		tmp_nrun += n;
		tmp_lengths[tmp_nrun - 1] -= Rtrim[i];
	}
	PROTECT(ans = _construct_Rle(tmp_values, tmp_lengths, 0));
	UNPROTECT(2);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Rle_extract_range(SEXP x, SEXP start, SEXP end)
{
	int nranges, x_nrun, offset_nrun, spanned_nrun, Ltrim, Rtrim;
	const int *range_start_p, *range_end_p;
	SEXP x_values, x_lengths;
	const char *errmsg;

	nranges = _check_integer_pairs(start, end,
				       &range_start_p, &range_end_p,
				       "start", "end");
	if (nranges != 1)
		error("'start' and 'end' must be of length 1");
	x_values = GET_SLOT(x, install("values"));
	x_lengths = GET_SLOT(x, install("lengths"));
	x_nrun = LENGTH(x_lengths);
	errmsg = range2runs_mapper1(INTEGER(x_lengths), x_nrun,
				range_start_p[0], range_end_p[0],
				&offset_nrun, &spanned_nrun, &Ltrim, &Rtrim);
	if (errmsg != NULL)
		error(errmsg);
	offset_nrun++;  /* add 1 to get the start */
	return extract_Rle_range(x_values, INTEGER(x_lengths),
				 offset_nrun, spanned_nrun, Ltrim, Rtrim);
}

SEXP _subset_Rle_by_ranges(SEXP x,
		const int *start, const int *width, int nranges,
		int method, int as_list)
{
	SEXP x_lengths;
	int x_nrun, *offset_nrun, *spanned_nrun, *Ltrim, *Rtrim, i;
	const char *errmsg;

	x_lengths = GET_SLOT(x, install("lengths"));
	x_nrun = LENGTH(x_lengths);
	offset_nrun = (int *) R_alloc(sizeof(int), nranges);
	spanned_nrun = (int *) R_alloc(sizeof(int), nranges);
	Ltrim = (int *) R_alloc(sizeof(int), nranges);
	Rtrim = (int *) R_alloc(sizeof(int), nranges);
	errmsg = ranges2runs_mapper(INTEGER(x_lengths), x_nrun,
				start, width, nranges,
				offset_nrun, spanned_nrun, Ltrim, Rtrim,
				method);
	if (errmsg != NULL)
		error(errmsg);
	for (i = 0; i < nranges; i++)
		offset_nrun[i]++;  /* add 1 to get the start */
	return subset_Rle_by_runs(x, offset_nrun, spanned_nrun,
				     Ltrim, Rtrim, nranges, as_list);
}

/* --- .Call ENTRY POINT --- */
SEXP Rle_extract_ranges(SEXP x, SEXP start, SEXP width,
			SEXP method, SEXP as_list)
{
	int nranges;
	const int *start_p, *width_p;

	nranges = _check_integer_pairs(start, width,
				       &start_p, &width_p,
				       "start", "width");
	return _subset_Rle_by_ranges(x, start_p, width_p, nranges,
				     INTEGER(method)[0], LOGICAL(as_list)[0]);
}


/****************************************************************************
 * Rle_getStartEndRunAndOffset()
 */

static SEXP get_StartEndRunAndOffset_from_runLength(
		const int *runlength, int runlength_len,
		const int *start, const int *end, int length)
{
	int i, *soff_elt, *eoff_elt;
	const int *start_elt, *end_elt, *erun_elt;
	SEXP info_start, info_end, ans, ans_names;
	SEXP ans_start, ans_start_names, ans_end, ans_end_names;
	SEXP start_run, start_offset, end_run, end_offset;

	PROTECT(info_start = _find_interv_and_start_from_width(start, length,
					runlength, runlength_len));
	PROTECT(info_end = _find_interv_and_start_from_width(end, length,
					runlength, runlength_len));

	start_run = VECTOR_ELT(info_start, 0);
	start_offset = VECTOR_ELT(info_start, 1);
	end_run = VECTOR_ELT(info_end, 0);
	end_offset = VECTOR_ELT(info_end, 1);
	for (i = 0, start_elt = start, end_elt = end,
		    soff_elt = INTEGER(start_offset),
		    eoff_elt = INTEGER(end_offset),
		    erun_elt = INTEGER(end_run);
	     i < length;
	     i++, start_elt++, end_elt++, soff_elt++, eoff_elt++, erun_elt++)
	{
		*soff_elt = *start_elt - *soff_elt;
		*eoff_elt = *eoff_elt + runlength[*erun_elt - 1] - 1 - *end_elt;
	}

	PROTECT(ans_start = NEW_LIST(2));
	PROTECT(ans_start_names = NEW_CHARACTER(2));
	SET_VECTOR_ELT(ans_start, 0, start_run);
	SET_VECTOR_ELT(ans_start, 1, start_offset);
	SET_STRING_ELT(ans_start_names, 0, mkChar("run"));
	SET_STRING_ELT(ans_start_names, 1, mkChar("offset"));
	SET_NAMES(ans_start, ans_start_names);

	PROTECT(ans_end = NEW_LIST(2));
	PROTECT(ans_end_names = NEW_CHARACTER(2));
	SET_VECTOR_ELT(ans_end, 0, end_run);
	SET_VECTOR_ELT(ans_end, 1, end_offset);
	SET_STRING_ELT(ans_end_names, 0, mkChar("run"));
	SET_STRING_ELT(ans_end_names, 1, mkChar("offset"));
	SET_NAMES(ans_end, ans_end_names);

	PROTECT(ans = NEW_LIST(2));
	PROTECT(ans_names = NEW_CHARACTER(2));
	SET_VECTOR_ELT(ans, 0, ans_start);
	SET_VECTOR_ELT(ans, 1, ans_end);
	SET_STRING_ELT(ans_names, 0, mkChar("start"));
	SET_STRING_ELT(ans_names, 1, mkChar("end"));
	SET_NAMES(ans, ans_names);

	UNPROTECT(8);

	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Rle_getStartEndRunAndOffset(SEXP x, SEXP start, SEXP end)
{
	int n;
	SEXP lengths;

	n = LENGTH(start);
	if (LENGTH(end) != n)
		error("length of 'start' must equal length of 'end'");
	lengths = GET_SLOT(x, install("lengths"));
	return get_StartEndRunAndOffset_from_runLength(
			INTEGER(lengths), LENGTH(lengths),
			INTEGER(start), INTEGER(end), n);
}

/* --- .Call ENTRY POINT --- */
SEXP Rle_window_aslist(SEXP x, SEXP runStart, SEXP runEnd,
		               SEXP offsetStart, SEXP offsetEnd)
{
	SEXP values, lengths, runWidth, ans, ans_names, ans_values, ans_lengths;

	values = GET_SLOT(x, install("values"));
	lengths = GET_SLOT(x, install("lengths"));

	if (!IS_INTEGER(runStart) || LENGTH(runStart) != 1 ||
		INTEGER(runStart)[0] == NA_INTEGER || INTEGER(runStart)[0] < 1)
		error("invalid 'runStart' argument");

	if (!IS_INTEGER(runEnd) || LENGTH(runEnd) != 1 ||
		INTEGER(runEnd)[0] == NA_INTEGER ||
		(INTEGER(runEnd)[0] + 1) < INTEGER(runStart)[0] ||
		INTEGER(runEnd)[0] > LENGTH(values))
		error("invalid 'runWidth' argument");

	PROTECT(runWidth = NEW_INTEGER(1));
	INTEGER(runWidth)[0] = INTEGER(runEnd)[0] - INTEGER(runStart)[0] + 1;

	PROTECT(ans = NEW_LIST(2));
	PROTECT(ans_names = NEW_CHARACTER(2));
	PROTECT(ans_values = vectorORfactor_extract_ranges(values,
					runStart, runWidth));
	PROTECT(ans_lengths = vectorORfactor_extract_ranges(lengths,
					runStart, runWidth));

    if (INTEGER(runWidth)[0] > 0) {
        INTEGER(ans_lengths)[0] -= INTEGER(offsetStart)[0];
    	INTEGER(ans_lengths)[INTEGER(runWidth)[0] - 1] -= INTEGER(offsetEnd)[0];
    }

	SET_VECTOR_ELT(ans, 0, ans_values);
	SET_VECTOR_ELT(ans, 1, ans_lengths);
	SET_STRING_ELT(ans_names, 0, mkChar("values"));
	SET_STRING_ELT(ans_names, 1, mkChar("lengths"));
	SET_NAMES(ans, ans_names);

	UNPROTECT(5);

	return ans;
}

