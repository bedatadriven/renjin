#include "S4Vectors.h"

#include <stdlib.h>  /* for malloc(), free() */
#include <limits.h>  /* for INT_MAX */


static int get_bucket_idx_for_int_pair(const struct htab *htab,
		int a1, int b1,
		const int *a2, const int *b2)
{
	unsigned int hval;
	int bucket_idx, i2;
	const int *buckets;

	/* use 2 consecutive prime numbers (seems to work well, no serious
	   justification for it) */
	hval = 3951551U * a1 + 3951553U * b1;
	bucket_idx = hval & htab->Mminus1;
	buckets = htab->buckets;
	while ((i2 = buckets[bucket_idx]) != NA_INTEGER) {
		if (a2[i2] == a1 && b2[i2] == b1)
			break;
		bucket_idx = (bucket_idx + 1) % htab->M;
	}
	return bucket_idx;
}

static int get_bucket_idx_for_int_quad(const struct htab *htab,
		int a1, int b1, int c1, int d1,
		const int *a2, const int *b2, const int *c2, const int *d2)
{
	unsigned int hval;
	int bucket_idx, i2;
	const int *buckets;

	/* use 4 consecutive prime numbers (seems to work well, no serious
	   justification for it) */
	hval = 3951551U * a1 + 3951553U * b1 + 3951557U * c1 + 3951559U * d1;
	bucket_idx = hval & htab->Mminus1;
	buckets = htab->buckets;
	while ((i2 = buckets[bucket_idx]) != NA_INTEGER) {
		if (a2[i2] == a1 && b2[i2] == b1 &&
		    c2[i2] == c1 && d2[i2] == d1)
			break;
		bucket_idx = (bucket_idx + 1) % htab->M;
	}
	return bucket_idx;
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 * any(is.na(x) | x < lower | x > upper)
 */

SEXP Integer_any_missing_or_outside(SEXP x, SEXP lower, SEXP upper)
{
	int x_len, lower0, upper0, ans, i;
	const int *x_p;

	x_len = length(x);
	lower0 = INTEGER(lower)[0];
	upper0 = INTEGER(upper)[0];
	ans = 0;
	for (i = 0, x_p = INTEGER(x); i < x_len; i++, x_p++) {
		if (*x_p == NA_INTEGER || *x_p < lower0 || *x_p > upper0) {
			ans = 1;
			break;
		}
	}
	return ScalarLogical(ans);
}


/****************************************************************************
 * Sum non-negative integers.
 */

/*
 * Walk 'x' and sum its elements. Stop walking at the first occurence of one
 * of the 3 following conditions: (1) the element is NA, or (2) the element is
 * negative, or (3) the partial sum is > INT_MAX (integer overflow).
 * How the function handles those conditions depends on 'varname'. If it's NULL
 * then no error is raised and a negative code is returned (indicating the kind
 * of condition that occured). Otherwise an error is raised (when not NULL,
 * 'varname' must be a C string i.e. 0-terminated).
 * If none of the 3 above conditions happen, then 'sum(x)' is returned.
 */
int _sum_non_neg_ints(const int *x, int x_len, const char *varname)
{
	int i;
	unsigned int sum;

	for (i = sum = 0; i < x_len; i++, x++) {
		if (*x == NA_INTEGER || *x < 0) {
			if (varname == NULL)
				return -1;
			error("'%s' contains NAs or negative values",
			      varname);
		}
		sum += *x;
		if (sum > (unsigned int) INT_MAX) {
			if (varname == NULL)
				return -2;
			error("integer overflow while summing elements "
			      "in '%s'", varname);
		}
	}
	return sum;
}

/*
 * --- .Call ENTRY POINT ---
 */
SEXP Integer_sum_non_neg_vals(SEXP x)
{
	return ScalarInteger(_sum_non_neg_ints(INTEGER(x), LENGTH(x), "x"));
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 * diff(c(0L, x))
 */

SEXP Integer_diff_with_0(SEXP x)
{
	int i, len, *x_ptr1, *x_ptr2, *ans_ptr;
	SEXP ans;

	len = LENGTH(x);
	PROTECT(ans = NEW_INTEGER(len));
	if (len > 0) {
		INTEGER(ans)[0] = INTEGER(x)[0];
		if (len > 1) {
			for (i = 1, x_ptr1 = INTEGER(x), x_ptr2 = INTEGER(x) + 1,
				 ans_ptr = INTEGER(ans) + 1; i < len;
				 i++, x_ptr1++, x_ptr2++, ans_ptr++) {
				*ans_ptr = *x_ptr2 - *x_ptr1;
			}
		}
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 * diff(c(x, last))
 */

SEXP Integer_diff_with_last(SEXP x, SEXP last)
{
  int i, len, *x_ptr1, *x_ptr2, *ans_ptr;
  SEXP ans;

  len = LENGTH(x);
  PROTECT(ans = NEW_INTEGER(len));
  if (len > 0) {
    for (i = 1, x_ptr1 = INTEGER(x), x_ptr2 = INTEGER(x) + 1,
           ans_ptr = INTEGER(ans); i < len;
         i++, x_ptr1++, x_ptr2++, ans_ptr++) {
      *ans_ptr = *x_ptr2 - *x_ptr1;
    }
    INTEGER(ans)[len - 1] = INTEGER(last)[0] - INTEGER(x)[len - 1];
  }
  UNPROTECT(1);
  return ans;
}


/****************************************************************************
 * Fast ordering of an integer vector.
 */

/* --- .Call ENTRY POINT --- */
SEXP Integer_order(SEXP x, SEXP decreasing, SEXP use_radix)
{
	int ans_len, i, *ans_elt_p;
	SEXP ans;

	if (LENGTH(decreasing) != 1)
		error("S4Vectors internal error in Integer_order(): "
		      "'decreasing' must be of length 1");
	ans_len = LENGTH(x);
	PROTECT(ans = NEW_INTEGER(ans_len));
	for (i = 1, ans_elt_p = INTEGER(ans); i <= ans_len; i++, ans_elt_p++)
		*ans_elt_p = i;
	i = _sort_ints(INTEGER(ans), ans_len,
		       INTEGER(x) - 1,
		       LOGICAL(decreasing)[0],
		       LOGICAL(use_radix)[0], NULL, NULL);
	UNPROTECT(1);
	if (i != 0)
		error("S4Vectors internal error in Integer_order(): "
		      "memory allocation failed");
	return ans;
}


/****************************************************************************
 * Fast ordering/comparing of integer pairs.
 *
 * The .Call entry points in this section are the workhorses behind
 * sortedIntegerPairs(), orderIntegerPairs(), matchIntegerPairs(), and
 * duplicatedIntegerPairs().
 */

/*
 * Nothing deep, just checking that 'a' and 'b' are integer vectors of the
 * same length. We don't look at the individual elements in them, and,
 * in particular, we don't check for NAs.
 */
int _check_integer_pairs(SEXP a, SEXP b,
		const int **a_p, const int **b_p,
		const char *a_argname, const char *b_argname)
{
	int len;

	if (!IS_INTEGER(a) || !IS_INTEGER(b))
		error("'%s' and '%s' must be integer vectors",
		      a_argname, b_argname);
	len = LENGTH(a);
	if (LENGTH(b) != len)
		error("'%s' and '%s' must have the same length",
		      a_argname, b_argname);
	*a_p = INTEGER(a);
	*b_p = INTEGER(b);
	return len;
}

/* --- .Call ENTRY POINT ---
 * 'a1' and 'b1': integer vectors of the same length M.
 * 'a2' and 'b2': integer vectors of the same length N.
 * The 4 integer vectors are assumed to be NA free. For efficiency reason, this
 * is not checked.
 * If M != N then the shorter object is recycled to the length of the longer
 * object, except if M or N is 0 in which case the object with length != 0 is
 * truncated to length 0.
 */
SEXP Integer_pcompare2(SEXP a1, SEXP b1, SEXP a2, SEXP b2)
{
	int npair1, npair2, ans_len;
	const int *a1_p, *b1_p, *a2_p, *b2_p;
	SEXP ans;

	npair1 = _check_integer_pairs(a1, b1, &a1_p, &b1_p, "a1", "b1");
	npair2 = _check_integer_pairs(a2, b2, &a2_p, &b2_p, "a2", "b2");
	if (npair1 == 0 || npair2 == 0)
		ans_len = 0;
	else
		ans_len = npair1 >= npair2 ? npair1 : npair2;
	PROTECT(ans = NEW_INTEGER(ans_len));
	_pcompare_int_pairs(a1_p, b1_p, npair1, a2_p, b2_p, npair2,
			    INTEGER(ans), ans_len, 1);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_sorted2(SEXP a, SEXP b, SEXP decreasing, SEXP strictly)
{
	const int *a_p, *b_p;
	int npair, ans;

	npair = _check_integer_pairs(a, b, &a_p, &b_p, "a", "b");
	ans = _int_pairs_are_sorted(a_p, b_p, npair,
				    LOGICAL(decreasing)[0],
				    LOGICAL(strictly)[0]);
	return ScalarLogical(ans);
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_order2(SEXP a, SEXP b, SEXP decreasing, SEXP use_radix)
{
	int ans_len, i, *ans_elt_p;
	const int *a_p, *b_p;
	SEXP ans;

	if (LENGTH(decreasing) != 2)
		error("S4Vectors internal error in Integer_order2(): "
		      "'decreasing' must be of length 2");
	ans_len = _check_integer_pairs(a, b, &a_p, &b_p, "a", "b");
	PROTECT(ans = NEW_INTEGER(ans_len));
	for (i = 1, ans_elt_p = INTEGER(ans); i <= ans_len; i++, ans_elt_p++)
		*ans_elt_p = i;
	i = _sort_int_pairs(INTEGER(ans), ans_len,
			    a_p - 1, b_p - 1,
			    LOGICAL(decreasing)[0],
			    LOGICAL(decreasing)[1],
			    LOGICAL(use_radix)[0], NULL, NULL);
	UNPROTECT(1);
	if (i != 0)
		error("S4Vectors internal error in Integer_order2(): "
		      "memory allocation failed");
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_match2_quick(SEXP a1, SEXP b1, SEXP a2, SEXP b2, SEXP nomatch)
{
	int len1, len2, nomatch0, *o1, *o2;
	const int *a1_p, *b1_p, *a2_p, *b2_p;
	SEXP ans;

	len1 = _check_integer_pairs(a1, b1, &a1_p, &b1_p, "a1", "b1");
	len2 = _check_integer_pairs(a2, b2, &a2_p, &b2_p, "a2", "b2");
	nomatch0 = INTEGER(nomatch)[0];
	o1 = (int *) R_alloc(sizeof(int), len1);
	o2 = (int *) R_alloc(sizeof(int), len2);
	_get_order_of_int_pairs(a1_p, b1_p, len1, 0, 0, o1, 0);
	_get_order_of_int_pairs(a2_p, b2_p, len2, 0, 0, o2, 0);
	PROTECT(ans = NEW_INTEGER(len1));
	_get_matches_of_ordered_int_pairs(a1_p, b1_p, o1, len1,
					  a2_p, b2_p, o2, len2,
					  nomatch0, INTEGER(ans), 1);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_selfmatch2_quick(SEXP a, SEXP b)
{
	int len, *o1;
	const int *a_p, *b_p;
	SEXP ans;

	len = _check_integer_pairs(a, b, &a_p, &b_p, "a", "b");
	o1 = (int *) R_alloc(sizeof(int), len);
	_get_order_of_int_pairs(a_p, b_p, len, 0, 0, o1, 0);
	PROTECT(ans = NEW_INTEGER(len));
	_get_matches_of_ordered_int_pairs(a_p, b_p, o1, len,
					  a_p, b_p, o1, len,
					  -1, INTEGER(ans), 1);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_match2_hash(SEXP a1, SEXP b1, SEXP a2, SEXP b2, SEXP nomatch)
{
	int len1, len2, nomatch0, *ans0, i, bucket_idx, i2;
	const int *a1_p, *b1_p, *a2_p, *b2_p;
	struct htab htab;
	SEXP ans;

	len1 = _check_integer_pairs(a1, b1, &a1_p, &b1_p, "a1", "b1");
	len2 = _check_integer_pairs(a2, b2, &a2_p, &b2_p, "a2", "b2");
	nomatch0 = INTEGER(nomatch)[0];
	htab = _new_htab(len2);
	for (i = 0; i < len2; i++) {
		bucket_idx = get_bucket_idx_for_int_pair(&htab,
					a2_p[i], b2_p[i],
					a2_p, b2_p);
		if (_get_hbucket_val(&htab, bucket_idx) == NA_INTEGER)
			_set_hbucket_val(&htab, bucket_idx, i);
	}
	PROTECT(ans = NEW_INTEGER(len1));
	ans0 = INTEGER(ans);
	for (i = 0; i < len1; i++) {
		bucket_idx = get_bucket_idx_for_int_pair(&htab,
					a1_p[i], b1_p[i],
					a2_p, b2_p);
		i2 = _get_hbucket_val(&htab, bucket_idx);
		if (i2 == NA_INTEGER)
			ans0[i] = nomatch0;
		else
			ans0[i] = i2 + 1;
	}
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_selfmatch2_hash(SEXP a, SEXP b)
{
	int ans_len, *ans0, i, bucket_idx, i2;
	const int *a_p, *b_p;
	struct htab htab;
	SEXP ans;

	ans_len = _check_integer_pairs(a, b, &a_p, &b_p, "a", "b");
	htab = _new_htab(ans_len);
	PROTECT(ans = NEW_INTEGER(ans_len));
	ans0 = INTEGER(ans);
	for (i = 0; i < ans_len; i++) {
		bucket_idx = get_bucket_idx_for_int_pair(&htab,
					a_p[i], b_p[i],
					a_p, b_p);
		i2 = _get_hbucket_val(&htab, bucket_idx);
		if (i2 == NA_INTEGER) {
			_set_hbucket_val(&htab, bucket_idx, i);
			ans0[i] = i + 1;
		} else {
			ans0[i] = i2 + 1;
		}
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * Fast ordering/comparing of integer quadruplets.
 *
 * The .Call entry points in this section are the workhorses behind
 * sortedIntegerQuads(), orderIntegerQuads(), matchIntegerQuads(), and
 * duplicatedIntegerQuads().
 */

/*
 * Nothing deep, just checking that 'a', 'b', 'c' and 'd' are integer vectors
 * of the same length. We don't look at the individual elements in them, and,
 * in particular, we don't check for NAs.
 */
int _check_integer_quads(SEXP a, SEXP b, SEXP c, SEXP d,
		const int **a_p, const int **b_p,
			const int **c_p, const int **d_p,
		const char *a_argname, const char *b_argname,
			const char *c_argname, const char *d_argname)
{
	int len;

	if (!IS_INTEGER(a) || !IS_INTEGER(b)
	 || !IS_INTEGER(c) || !IS_INTEGER(d))
		error("'%s', '%s', '%s' and '%s' must be integer vectors",
		      a_argname, b_argname, c_argname, d_argname);
	len = LENGTH(a);
	if (LENGTH(b) != len || LENGTH(c) != len || LENGTH(d) != len)
		error("'%s', '%s', '%s' and '%s' must have the same length",
		      a_argname, b_argname, c_argname, d_argname);
	*a_p = INTEGER(a);
	*b_p = INTEGER(b);
	*c_p = INTEGER(c);
	*d_p = INTEGER(d);
	return len;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_sorted4(SEXP a, SEXP b, SEXP c, SEXP d,
		     SEXP decreasing, SEXP strictly)
{
	const int *a_p, *b_p, *c_p, *d_p;
	int nquad, ans;

	nquad = _check_integer_quads(a, b, c, d,
				     &a_p, &b_p, &c_p, &d_p,
				     "a", "b", "c", "d");
	ans = _int_quads_are_sorted(a_p, b_p, c_p, d_p, nquad,
				    LOGICAL(decreasing)[0],
				    LOGICAL(strictly)[0]);
	return ScalarLogical(ans);
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_order4(SEXP a, SEXP b, SEXP c, SEXP d,
		    SEXP decreasing, SEXP use_radix)
{
	int ans_len, i, *ans_elt_p;
	const int *a_p, *b_p, *c_p, *d_p;
	SEXP ans;

	if (LENGTH(decreasing) != 4)
		error("S4Vectors internal error in Integer_order4(): "
		      "'decreasing' must be of length 4");
	ans_len = _check_integer_quads(a, b, c, d,
				       &a_p, &b_p, &c_p, &d_p,
				       "a", "b", "c", "d");
	PROTECT(ans = NEW_INTEGER(ans_len));
	for (i = 1, ans_elt_p = INTEGER(ans); i <= ans_len; i++, ans_elt_p++)
		*ans_elt_p = i;
	i = _sort_int_quads(INTEGER(ans), ans_len,
			    a_p - 1, b_p - 1, c_p - 1, d_p - 1,
			    LOGICAL(decreasing)[0],
			    LOGICAL(decreasing)[1],
			    LOGICAL(decreasing)[2],
			    LOGICAL(decreasing)[3],
			    LOGICAL(use_radix)[0], NULL, NULL);
	UNPROTECT(1);
	if (i != 0)
		error("S4Vectors internal error in Integer_order4(): "
		      "memory allocation failed");
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_match4_quick(SEXP a1, SEXP b1, SEXP c1, SEXP d1,
			  SEXP a2, SEXP b2, SEXP c2, SEXP d2, SEXP nomatch)
{
	int len1, len2, nomatch0, *o1, *o2;
	const int *a1_p, *b1_p, *c1_p, *d1_p, *a2_p, *b2_p, *c2_p, *d2_p;
	SEXP ans;

	len1 = _check_integer_quads(a1, b1, c1, d1,
				    &a1_p, &b1_p, &c1_p, &d1_p,
				    "a1", "b1", "c1", "d1");
	len2 = _check_integer_quads(a2, b2, c2, d2,
				    &a2_p, &b2_p, &c2_p, &d2_p,
				    "a2", "b2", "c2", "d2");
	nomatch0 = INTEGER(nomatch)[0];
	o1 = (int *) R_alloc(sizeof(int), len1);
	o2 = (int *) R_alloc(sizeof(int), len2);
	_get_order_of_int_quads(a1_p, b1_p, c1_p, d1_p, len1,
				0, 0, 0, 0, o1, 0);
	_get_order_of_int_quads(a2_p, b2_p, c2_p, d2_p, len2,
				0, 0, 0, 0, o2, 0);
	PROTECT(ans = NEW_INTEGER(len1));
	_get_matches_of_ordered_int_quads(a1_p, b1_p, c1_p, d1_p, o1, len1,
					  a2_p, b2_p, c2_p, d2_p, o2, len2,
					  nomatch0, INTEGER(ans), 1);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_selfmatch4_quick(SEXP a, SEXP b, SEXP c, SEXP d)
{
	int len, *o1;
	const int *a_p, *b_p, *c_p, *d_p;
	SEXP ans;

	len = _check_integer_quads(a, b, c, d,
				   &a_p, &b_p, &c_p, &d_p,
				   "a", "b", "c", "d");
	o1 = (int *) R_alloc(sizeof(int), len);
	_get_order_of_int_quads(a_p, b_p, c_p, d_p, len, 0, 0, 0, 0, o1, 0);
	PROTECT(ans = NEW_INTEGER(len));
	_get_matches_of_ordered_int_quads(a_p, b_p, c_p, d_p, o1, len,
					  a_p, b_p, c_p, d_p, o1, len,
					  -1, INTEGER(ans), 1);
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_match4_hash(SEXP a1, SEXP b1, SEXP c1, SEXP d1,
			 SEXP a2, SEXP b2, SEXP c2, SEXP d2, SEXP nomatch)
{
	int len1, len2, nomatch0, *ans0, i, bucket_idx, i2;
	const int *a1_p, *b1_p, *c1_p, *d1_p, *a2_p, *b2_p, *c2_p, *d2_p;
	struct htab htab;
	SEXP ans;

	len1 = _check_integer_quads(a1, b1, c1, d1,
				    &a1_p, &b1_p, &c1_p, &d1_p,
				    "a1", "b1", "c1", "d1");
	len2 = _check_integer_quads(a2, b2, c2, d2,
				    &a2_p, &b2_p, &c2_p, &d2_p,
				    "a2", "b2", "c2", "d2");
	nomatch0 = INTEGER(nomatch)[0];
	htab = _new_htab(len2);
	for (i = 0; i < len2; i++) {
		bucket_idx = get_bucket_idx_for_int_quad(&htab,
					a2_p[i], b2_p[i], c2_p[i], d2_p[i],
					a2_p, b2_p, c2_p, d2_p);
		if (_get_hbucket_val(&htab, bucket_idx) == NA_INTEGER)
			_set_hbucket_val(&htab, bucket_idx, i);
	}
	PROTECT(ans = NEW_INTEGER(len1));
	ans0 = INTEGER(ans);
	for (i = 0; i < len1; i++) {
		bucket_idx = get_bucket_idx_for_int_quad(&htab,
					a1_p[i], b1_p[i], c1_p[i], d1_p[i],
					a2_p, b2_p, c2_p, d2_p);
		i2 = _get_hbucket_val(&htab, bucket_idx);
		if (i2 == NA_INTEGER)
			ans0[i] = nomatch0;
		else
			ans0[i] = i2 + 1;
	}
	UNPROTECT(1);
	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP Integer_selfmatch4_hash(SEXP a, SEXP b, SEXP c, SEXP d)
{
	int ans_len, *ans0, i, bucket_idx, i2;
	const int *a_p, *b_p, *c_p, *d_p;
	struct htab htab;
	SEXP ans;

	ans_len = _check_integer_quads(a, b, c, d,
				       &a_p, &b_p, &c_p, &d_p,
				       "a", "b", "c", "d");
	htab = _new_htab(ans_len);
	PROTECT(ans = NEW_INTEGER(ans_len));
	ans0 = INTEGER(ans);
	for (i = 0; i < ans_len; i++) {
		bucket_idx = get_bucket_idx_for_int_quad(&htab,
					a_p[i], b_p[i], c_p[i], d_p[i],
					a_p, b_p, c_p, d_p);
		i2 = _get_hbucket_val(&htab, bucket_idx);
		if (i2 == NA_INTEGER) {
			_set_hbucket_val(&htab, bucket_idx, i);
			ans0[i] = i + 1;
		} else {
			ans0[i] = i2 + 1;
		}
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * An enhanced version of base::tabulate() that: (1) handles integer weights
 * (NA and negative weights are OK), and (2) throws an error if 'strict' is
 * TRUE and if 'x' contains NAs or values not in the [1, 'nbins'] interval.
 */

SEXP Integer_tabulate2(SEXP x, SEXP nbins, SEXP weight, SEXP strict)
{
	SEXP ans;
	int x_len, nbins0, weight_len, strict0, *one_based_ans_p,
	    i, j, x_elt, weight_elt;
	const int *x_p, *weight_p;

	x_len = LENGTH(x);
	nbins0 = INTEGER(nbins)[0];
	weight_len = LENGTH(weight);
	weight_p = INTEGER(weight);
	strict0 = LOGICAL(strict)[0];
	j = 0;
	PROTECT(ans = NEW_INTEGER(nbins0));
	memset(INTEGER(ans), 0, nbins0 * sizeof(int));
	one_based_ans_p = INTEGER(ans) - 1;
	// We do unsafe arithmetic, which is 40% faster than safe arithmetic.
	// For now, the only use case for tabulate2() is fast tabulation of
	// integer- and factor-Rle's (passing the run values and run lengths
	// to 'x' and 'weight', respectively), so we are safe (the cumulated
	// run lengths of an Rle must be < 2^31).
	//_reset_ovflow_flag();
	for (i = j = 0, x_p = INTEGER(x); i < x_len; i++, j++, x_p++) {
		if (j >= weight_len)
			j = 0; /* recycle */
		x_elt = *x_p;
		if (x_elt == NA_INTEGER || x_elt < 1 || x_elt > nbins0) {
			if (!strict0)
				continue;
			UNPROTECT(1);
			error("'x' contains NAs or values not in the "
			      "[1, 'nbins'] interval");
		}
		weight_elt = weight_p[j];
		//ans_elt = one_based_ans_p[x_elt];
		//one_based_ans_p[x_elt] = _safe_int_add(ans_elt, weight_elt);
		one_based_ans_p[x_elt] += weight_elt;
	}
	//if (_get_ovflow_flag())
	//	warning("NAs produced by integer overflow");
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * Bitwise operations.
 */

SEXP Integer_explode_bits(SEXP x, SEXP bitpos)
{
	SEXP ans;
	int ans_nrow, ans_ncol, i, j, *ans_elt, bitmask;
	const int *x_elt, *bitpos_elt;

	ans_nrow = LENGTH(x);
	ans_ncol = LENGTH(bitpos);
	PROTECT(ans = allocMatrix(INTSXP, ans_nrow, ans_ncol));
	ans_elt = INTEGER(ans);
	for (j = 0, bitpos_elt = INTEGER(bitpos);
	     j < ans_ncol;
	     j++, bitpos_elt++)
	{
		if (*bitpos_elt == NA_INTEGER || *bitpos_elt < 1)
			error("'bitpos' must contain values >= 1");
		bitmask = 1 << (*bitpos_elt - 1);
		for (i = 0, x_elt = INTEGER(x); i < ans_nrow; i++, x_elt++)
			*(ans_elt++) = (*x_elt & bitmask) != 0;
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 * Creates the (sorted) union of two sorted integer vectors
 */

SEXP Integer_sorted_merge(SEXP x, SEXP y)
{
	int x_i, y_i, x_len, y_len, ans_len;
	const int *x_ptr, *y_ptr;
	int *ans_ptr;
	SEXP ans;

	x_len = LENGTH(x);
	y_len = LENGTH(y);

	x_i = 0;
	y_i = 0;
	x_ptr = INTEGER(x);
	y_ptr = INTEGER(y);
	ans_len = 0;
	while (x_i < x_len && y_i < y_len) {
		if (*x_ptr == *y_ptr) {
			x_ptr++;
			x_i++;
			y_ptr++;
			y_i++;
		} else if (*x_ptr < *y_ptr) {
			x_ptr++;
			x_i++;
		} else {
			y_ptr++;
			y_i++;
		}
		ans_len++;
	}
	if (x_i < x_len) {
		ans_len += x_len - x_i;
	} else if (y_i < y_len) {
		ans_len += y_len - y_i;
	}

	PROTECT(ans = NEW_INTEGER(ans_len));
	x_i = 0;
	y_i = 0;
	x_ptr = INTEGER(x);
	y_ptr = INTEGER(y);
	ans_ptr = INTEGER(ans);
	while (x_i < x_len && y_i < y_len) {
		if (*x_ptr == *y_ptr) {
			*ans_ptr = *x_ptr;
			x_ptr++;
			x_i++;
			y_ptr++;
			y_i++;
		} else if (*x_ptr < *y_ptr) {
			*ans_ptr = *x_ptr;
			x_ptr++;
			x_i++;
		} else {
			*ans_ptr = *y_ptr;
			y_ptr++;
			y_i++;
		}
		ans_ptr++;
	}
	if (x_i < x_len) {
		memcpy(ans_ptr, x_ptr, (x_len - x_i) * sizeof(int));
	} else if (y_i < y_len) {
		memcpy(ans_ptr, y_ptr, (y_len - y_i) * sizeof(int));
	}
	UNPROTECT(1);

	return ans;
}


/****************************************************************************
 * --- .Call ENTRY POINT ---
 */

SEXP Integer_mseq(SEXP from, SEXP to)
{
	int i, j, n, ans_len, *from_elt, *to_elt, *ans_elt;
	SEXP ans;

	if (!IS_INTEGER(from) || !IS_INTEGER(to))
		error("'from' and 'to' must be integer vectors");

	n = LENGTH(from);
	if (n != LENGTH(to))
		error("lengths of 'from' and 'to' must be equal");

	ans_len = 0;
	for (i = 0, from_elt = INTEGER(from), to_elt = INTEGER(to); i < n;
		 i++, from_elt++, to_elt++) {
		ans_len += (*from_elt <= *to_elt ? *to_elt - *from_elt
						 : *from_elt - *to_elt) + 1;
	}

	PROTECT(ans = NEW_INTEGER(ans_len));
	ans_elt = INTEGER(ans);
	for (i = 0, from_elt = INTEGER(from), to_elt = INTEGER(to); i < n;
		 i++, from_elt++, to_elt++) {
		if (*from_elt == NA_INTEGER || *to_elt == NA_INTEGER)
			error("'from' and 'to' contain NAs");

		if (*from_elt <= *to_elt) {
			for (j = *from_elt; j <= *to_elt; j++) {
				*ans_elt = j;
				ans_elt++;
			}
		} else {
			for (j = *from_elt; j >= *to_elt; j--) {
				*ans_elt = j;
				ans_elt++;
			}
		}
	}
	UNPROTECT(1);
	return ans;
}

SEXP Integer_fancy_mseq(SEXP lengths, SEXP offset, SEXP rev)
{
	int lengths_len, offset_len, rev_len, ans_len,
	    i, length, *ans_elt, i2, i3, offset_elt, rev_elt, j;
	const int *lengths_elt;
	SEXP ans;

	lengths_len = LENGTH(lengths);
	offset_len = LENGTH(offset);
	rev_len = LENGTH(rev);
	if (lengths_len != 0) {
		if (offset_len == 0)
			error("'offset' has length 0 but not 'lengths'");
		if (rev_len == 0)
			error("'rev' has length 0 but not 'lengths'");
	}
	ans_len = 0;
	for (i = 0, lengths_elt = INTEGER(lengths);
	     i < lengths_len;
	     i++, lengths_elt++)
	{
		length = *lengths_elt;
		if (length == NA_INTEGER)
			error("'lengths' contains NAs");
		if (length < 0)
			length = -length;
		ans_len += length;
	}
	PROTECT(ans = NEW_INTEGER(ans_len));
	ans_elt = INTEGER(ans);
	for (i = i2 = i3 = 0, lengths_elt = INTEGER(lengths);
	     i < lengths_len;
	     i++, i2++, i3++, lengths_elt++)
	{
		if (i2 >= offset_len)
			i2 = 0; /* recycle */
		if (i3 >= rev_len)
			i3 = 0; /* recycle */
		length = *lengths_elt;
		offset_elt = INTEGER(offset)[i2];
		if (length != 0 && offset_elt == NA_INTEGER) {
			UNPROTECT(1);
			error("'offset' contains NAs");
		}
		rev_elt = INTEGER(rev)[i3];
		if (length >= 0) {
			if (length >= 2 && rev_elt == NA_LOGICAL) {
				UNPROTECT(1);
				error("'rev' contains NAs");
			}
			if (rev_elt) {
				for (j = length; j >= 1; j--)
					*(ans_elt++) = j + offset_elt;
			} else {
				for (j = 1; j <= length; j++)
					*(ans_elt++) = j + offset_elt;
			}
		} else {
			if (length <= -2 && rev_elt == NA_LOGICAL) {
				UNPROTECT(1);
				error("'rev' contains NAs");
			}
			if (rev_elt) {
				for (j = length; j <= -1; j++)
					*(ans_elt++) = j - offset_elt;
			} else {
				for (j = -1; j >= length; j--)
					*(ans_elt++) = j - offset_elt;
			}
		}
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * findIntervalAndStartFromWidth()
 *
 * 'x' and 'width' are integer vectors
 */

SEXP _find_interv_and_start_from_width(const int *x, int x_len,
		const int *width, int width_len)
{
	int i, interval, start;
	const int *x_elt, *width_elt;
	int *interval_elt, *start_elt, *x_order_elt;
	SEXP ans, ans_class, ans_names, ans_rownames, ans_interval, ans_start;
	SEXP x_order;

	for (i = 0, width_elt = width; i < width_len; i++, width_elt++) {
		if (*width_elt == NA_INTEGER)
			error("'width' cannot contain missing values");
		else if (*width_elt < 0)
			error("'width' must contain non-negative values");
	}

	width_elt = width;
	ans_rownames = R_NilValue;
	PROTECT(ans_interval = NEW_INTEGER(x_len));
	PROTECT(ans_start = NEW_INTEGER(x_len));
	if (x_len > 0 && width_len > 0) {
		start = 1;
		interval = 1;
		PROTECT(x_order = NEW_INTEGER(x_len));
		_get_order_of_int_array(x, x_len, 0, INTEGER(x_order), 0);
		for (i = 0, x_order_elt = INTEGER(x_order); i < x_len;
		     i++, x_order_elt++) {
			x_elt = x + *x_order_elt;
			interval_elt = INTEGER(ans_interval) + *x_order_elt;
			start_elt = INTEGER(ans_start) + *x_order_elt;
			if (*x_elt == NA_INTEGER)
				error("'x' cannot contain missing values");
			else if (*x_elt < 0)
				error("'x' must contain non-negative values");
			if (*x_elt == 0) {
				*interval_elt = 0;
				*start_elt = NA_INTEGER;
			} else {
				while (interval < width_len && *x_elt >= (start + *width_elt)) {
					interval++;
					start += *width_elt;
					width_elt++;
				}
				if (*x_elt > start + *width_elt)
					error("'x' values larger than vector length 'sum(width)'");
				*interval_elt = interval;
				*start_elt = start;
			}
		}
		UNPROTECT(1);
		PROTECT(ans_rownames = NEW_INTEGER(2));
		INTEGER(ans_rownames)[0] = NA_INTEGER;
		INTEGER(ans_rownames)[1] = -x_len;
	} else {
		PROTECT(ans_rownames = NEW_INTEGER(0));
	}

	PROTECT(ans = NEW_LIST(2));
	PROTECT(ans_class = NEW_CHARACTER(1));
	PROTECT(ans_names = NEW_CHARACTER(2));

	SET_STRING_ELT(ans_class, 0, mkChar("data.frame"));
	SET_STRING_ELT(ans_names, 0, mkChar("interval"));
	SET_STRING_ELT(ans_names, 1, mkChar("start"));

	SET_NAMES(ans, ans_names);
	SET_VECTOR_ELT(ans, 0, ans_interval);
	SET_VECTOR_ELT(ans, 1, ans_start);
	setAttrib(ans, install("row.names"), ans_rownames);
	SET_CLASS(ans, ans_class);

	UNPROTECT(6);

	return ans;
}

/* --- .Call ENTRY POINT --- */
SEXP findIntervalAndStartFromWidth(SEXP x, SEXP width)
{
	if (!IS_INTEGER(x))
		error("'x' must be an integer vector");
	if (!IS_INTEGER(width))
		error("'width' must be an integer vector");
	return _find_interv_and_start_from_width(INTEGER(x), LENGTH(x),
						 INTEGER(width), LENGTH(width));
}

