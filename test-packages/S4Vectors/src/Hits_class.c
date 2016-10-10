/****************************************************************************
 *                  Low-level manipulation of Hits objects                  *
 ****************************************************************************/
#include "S4Vectors.h"


/****************************************************************************
 * C-level constructors
 */

static SEXP new_Hits0(const char *classname,
		      SEXP from, SEXP to,
		      int nLnode, int nRnode)
{
	SEXP classdef, ans, ans_nLnode, ans_nRnode;

	PROTECT(classdef = MAKE_CLASS(classname));
	PROTECT(ans = NEW_OBJECT(classdef));

	SET_SLOT(ans, install("from"), from);
	SET_SLOT(ans, install("to"), to);

	PROTECT(ans_nLnode = ScalarInteger(nLnode));
	SET_SLOT(ans, install("nLnode"), ans_nLnode);
	UNPROTECT(1);

	PROTECT(ans_nRnode = ScalarInteger(nRnode));
	SET_SLOT(ans, install("nRnode"), ans_nRnode);
	UNPROTECT(1);

	UNPROTECT(2);
	return ans;
}

static SEXP new_Hits1(const char *classname,
		      const int *from, const int *to, int nhit,
		      int nLnode, int nRnode)
{
	SEXP ans_from, ans_to, ans;
	size_t n;

	PROTECT(ans_from = NEW_INTEGER(nhit));
	PROTECT(ans_to = NEW_INTEGER(nhit));
	n = sizeof(int) * nhit;
	memcpy(INTEGER(ans_from), from, n);
	memcpy(INTEGER(ans_to), to, n);
	ans = new_Hits0(classname, ans_from, ans_to,
				   nLnode, nRnode);
	UNPROTECT(2);
	return ans;
}


/****************************************************************************
 * High-level user-friendly constructor
 */

/* Based on qsort(). Time is O(nhit*log(nhit)).
   If 'revmap' is not NULL, then 'from_in' is not modified. */
static void qsort_hits(int *from_in, const int *to_in,
		       int *from_out, int *to_out, int nhit,
		       int *revmap)
{
	int k;

	if (revmap == NULL)
		revmap = to_out;
	_get_order_of_int_array(from_in, nhit, 0, revmap, 0);
	for (k = 0; k < nhit; k++)
		from_out[k] = from_in[revmap[k]];
	if (revmap == to_out) {
		memcpy(from_in, revmap, sizeof(int) * nhit);
		revmap = from_in;
	}
	for (k = 0; k < nhit; k++)
		to_out[k] = to_in[revmap[k]++];
	return;
}

/* Tabulated sorting. Time is O(nhit).
   WARNINGS: 'nhit' MUST be >= 'nLnode'. 'from_in' is ALWAYS modified. */
static void tsort_hits(int *from_in, const int *to_in,
		       int *from_out, int *to_out, int nhit, int nLnode,
		       int *revmap)
{
	int i, k, offset, count, prev_offset, j;

	/* Compute nb of hits per left node. We need a place for this so we
	   temporarily use 'from_out' which is assumed to have at least 'nLnode'
	   elements. */
	for (i = 0; i < nLnode; i++)
		from_out[i] = 0;
	for (k = 0; k < nhit; k++)
		from_out[--from_in[k]]++;  /* make 'from_in[k]' 0-based */
	/* Replace counts with offsets. */
	offset = 0;
	for (i = 0; i < nLnode; i++) {
		count = from_out[i];
		from_out[i] = offset;
		offset += count;
	}
	/* Fill 'to_out' and 'revmap'. */
	for (k = 0; k < nhit; k++) {
		offset = from_out[from_in[k]]++;
		to_out[offset] = to_in[k];
		if (revmap != NULL)
			revmap[offset] = k + 1;
	}
	/* Fill 'from_out'. */
	memcpy(from_in, from_out, sizeof(int) * nLnode);
	k = offset = 0;
	for (i = 1; i <= nLnode; i++) {
		prev_offset = offset;
		offset = from_in[i - 1];
		for (j = prev_offset; j < offset; j++)
			from_out[k++] = i;
	}
	return;
}

SEXP _new_Hits(int *from, const int *to, int nhit,
	       int nLnode, int nRnode, int already_sorted)
{
	SEXP ans_from, ans_to, ans;
	int *from_out, *to_out;

	if (already_sorted || nhit <= 1 || nLnode <= 1)
		return new_Hits1("SortedByQueryHits", from, to, nhit,
						      nLnode, nRnode);
	PROTECT(ans_from = NEW_INTEGER(nhit));
	PROTECT(ans_to = NEW_INTEGER(nhit));
	from_out = INTEGER(ans_from);
	to_out = INTEGER(ans_to);
	if (nhit >= nLnode)
		tsort_hits(from, to, from_out, to_out, nhit, nLnode, NULL);
	else
		qsort_hits(from, to, from_out, to_out, nhit, NULL);
	ans = new_Hits0("SortedByQueryHits", ans_from, ans_to,
					     nLnode, nRnode);
	UNPROTECT(2);
	return ans;
}

static SEXP new_Hits_with_revmap(const char *classname,
		const int *from, const int *to, int nhit,
		int nLnode, int nRnode, int *revmap)
{
	SEXP ans_from, ans_to, ans;
	int *from2, *from_out, *to_out;

	if (revmap == NULL || nhit >= nLnode) {
		from2 = (int *) R_alloc(sizeof(int), nhit);
		memcpy(from2, from, sizeof(int) * nhit);
	}
	if (revmap == NULL)
		return _new_Hits(from2, to, nhit, nLnode, nRnode, 0);
	PROTECT(ans_from = NEW_INTEGER(nhit));
	PROTECT(ans_to = NEW_INTEGER(nhit));
	from_out = INTEGER(ans_from);
	to_out = INTEGER(ans_to);
	if (nhit >= nLnode) {
		tsort_hits(from2, to, from_out, to_out, nhit,
			   nLnode, revmap);
	} else {
		qsort_hits((int *) from, to, from_out, to_out, nhit,
			   revmap);
	}
	ans = new_Hits0(classname, ans_from, ans_to, nLnode, nRnode);
	UNPROTECT(2);
	return ans;
}

static int get_nnode(SEXP len, const char *side)
{
	int len0;

	if (!IS_INTEGER(len) || LENGTH(len) != 1)
		error("'n%snode(x)' must be a single integer",
                      side);
	len0 = INTEGER(len)[0];
	if (len0 == NA_INTEGER || len0 < 0)
		error("'n%snode(x)' must be a single non-negative integer",
                      side);
	return len0;
}

/* Return 1 if 'from' is already sorted and 0 otherwise. */
static int check_hits(const int *from, const int *to, int nhit,
		      int nLnode, int nRnode)
{
	int already_sorted, prev_i, k, i, j;

	already_sorted = 1;
	prev_i = -1;
	for (k = 0; k < nhit; k++, from++, to++) {
		i = *from;
		if (i == NA_INTEGER || i < 1 || i > nLnode)
			error("'from(x)' must contain non-NA values "
			      ">= 1 and <= 'nLnode(x)'");
		if (i < prev_i)
			already_sorted = 0;
		prev_i = i;
		j = *to;
		if (j == NA_INTEGER || j < 1 || j > nRnode)
			error("'to(x)' must contain non-NA values "
			      ">= 1 and <= 'nRnode(x)'");
	}
	return already_sorted;
}

/* --- .Call ENTRY POINT --- */
SEXP Hits_new(SEXP Class, SEXP from, SEXP to, SEXP nLnode, SEXP nRnode,
	      SEXP revmap_envir)
{
	const char *classname;
	int nhit, nLnode0, nRnode0, already_sorted, *revmap_p;
	const int *from_p, *to_p;
	SEXP ans, revmap, symbol;

	classname = CHAR(STRING_ELT(Class, 0));
	nhit = _check_integer_pairs(from, to, &from_p, &to_p,
				    "from(x)", "to(x)");
	nLnode0 = get_nnode(nLnode, "L");
	nRnode0 = get_nnode(nRnode, "R");
	already_sorted = check_hits(from_p, to_p, nhit, nLnode0, nRnode0);
	if (already_sorted)
		return new_Hits1(classname, from_p, to_p, nhit,
					    nLnode0, nRnode0);
	if (revmap_envir == R_NilValue) {
		revmap_p = NULL;
	} else {
		PROTECT(revmap = NEW_INTEGER(nhit));
		revmap_p = INTEGER(revmap);
	}
	PROTECT(ans = new_Hits_with_revmap(classname,
					   from_p, to_p, nhit,
					   nLnode0, nRnode0, revmap_p));
	if (revmap_envir == R_NilValue) {
		UNPROTECT(1);
		return ans;
	}
	PROTECT(symbol = mkChar("revmap"));
	defineVar(install(translateChar(symbol)), revmap, revmap_envir);
	UNPROTECT(3);
	return ans;
}


/****************************************************************************
 * select_hits()
 */

int _get_select_mode(SEXP select)
{
	const char *select0;

	if (!IS_CHARACTER(select) || LENGTH(select) != 1)
		error("'select' must be a single string");
	select = STRING_ELT(select, 0);
	if (select == NA_STRING)
		error("'select' cannot be NA");
	select0 = CHAR(select);
	if (strcmp(select0, "all") == 0)
		return ALL_HITS;
	if (strcmp(select0, "first") == 0)
		return FIRST_HIT;
	if (strcmp(select0, "last") == 0)
		return LAST_HIT;
	if (strcmp(select0, "arbitrary") == 0)
		return ARBITRARY_HIT;
	if (strcmp(select0, "count") == 0)
		return COUNT_HITS;
	error("'select' must be \"all\", \"first\", "
	      "\"last\", \"arbitrary\", or \"count\"");
	return 0;
}

/* --- .Call ENTRY POINT --- */
SEXP select_hits(SEXP from, SEXP to, SEXP nLnode, SEXP select)
{
	int nhit, annRnode, select_mode, init_val, i, k, j1;
	const int *from_p, *to_p;
	SEXP ans;

	nhit = _check_integer_pairs(from, to,
				    &from_p, &to_p,
				    "from(x)", "to(x)");
	annRnode = INTEGER(nLnode)[0];
	select_mode = _get_select_mode(select);
	PROTECT(ans = NEW_INTEGER(annRnode));
	init_val = select_mode == COUNT_HITS ? 0 : NA_INTEGER;
	for (i = 0; i < annRnode; i++)
		INTEGER(ans)[i] = init_val;
	for (k = 0; k < nhit; k++, from_p++, to_p++) {
		i = *from_p - 1;
		if (select_mode == COUNT_HITS) {
			INTEGER(ans)[i]++;
			continue;
		}
		j1 = *to_p;
		if (INTEGER(ans)[i] == NA_INTEGER
		 || (select_mode == FIRST_HIT) == (j1 < INTEGER(ans)[i]))
			INTEGER(ans)[i] = j1;
	}
	UNPROTECT(1);
	return ans;
}


/****************************************************************************
 * make_all_group_inner_hits()
 *
 * --- .Call ENTRY POINT ---
 * 'hit_type' must be 0, -1 or 1 (single integer).
 */
SEXP make_all_group_inner_hits(SEXP group_sizes, SEXP hit_type)
{
	int ngroup, htype, annRnode, i, j, k, gs, nhit,
	    iofeig, *left, *right;
	const int *group_sizes_elt;
	SEXP ans_from, ans_to, ans;

	ngroup = LENGTH(group_sizes);
	htype = INTEGER(hit_type)[0];
	for (i = annRnode = 0, group_sizes_elt = INTEGER(group_sizes);
	     i < ngroup;
	     i++, group_sizes_elt++)
	{
		gs = *group_sizes_elt;
		if (gs == NA_INTEGER || gs < 0)
			error("'group_sizes' contains NAs or negative values");
		nhit = htype == 0 ? gs * gs : (gs * (gs - 1)) / 2;
		annRnode += nhit;
		
	}
	PROTECT(ans_from = NEW_INTEGER(annRnode));
	PROTECT(ans_to = NEW_INTEGER(annRnode));
	left = INTEGER(ans_from);
	right = INTEGER(ans_to);
	iofeig = 0; /* 0-based Index Of First Element In Group */
	for (i = 0, group_sizes_elt = INTEGER(group_sizes);
	     i < ngroup;
	     i++, group_sizes_elt++)
	{
		gs = *group_sizes_elt;
		if (htype > 0) {
			for (j = 1; j < gs; j++) {
				for (k = j + 1; k <= gs; k++) {
					*(left++) = j + iofeig;
					*(right++) = k + iofeig;
				}
			}
		} else if (htype < 0) {
			for (j = 2; j <= gs; j++) {
				for (k = 1; k < j; k++) {
					*(left++) = j + iofeig;
					*(right++) = k + iofeig;
				}
			}
		} else {
			for (j = 1; j <= gs; j++) {
				for (k = 1; k <= gs; k++) {
					*(left++) = j + iofeig;
					*(right++) = k + iofeig;
				}
			}
		}
		iofeig += gs;
	}
	ans = new_Hits0("SortedByQuerySelfHits", ans_from, ans_to,
						 iofeig, iofeig);
	UNPROTECT(2);
	return ans;
}

