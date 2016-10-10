/****************************************************************************
 *                          Auto-Extending buffers                          *
 *                            Author: H. Pag\`es                            *
 ****************************************************************************/
#include "S4Vectors.h"
#include <stdlib.h>  /* for malloc, free, realloc */

#define MAX_BUFLENGTH_INC (32 * 1024 * 1024)
#define MAX_BUFLENGTH (32 * MAX_BUFLENGTH_INC)


/****************************************************************************
 * Low-level memory management.
 */

static int use_malloc = 0;

SEXP AEbufs_use_malloc(SEXP x)
{
	use_malloc = LOGICAL(x)[0];
	return R_NilValue;
}

/* 'nmemb' must be > 0. This is NOT checked! */
static void *alloc2(int nmemb, size_t size)
{
	void *ptr;

	if (use_malloc) {
		//printf("alloc2: nmemb=%d\n", nmemb);
		size *= nmemb;
		ptr = malloc(size);
		if (ptr == NULL)
			error("S4Vectors internal error in alloc2(): "
			      "cannot allocate memory");
	} else {
		ptr = (void *) R_alloc(size, nmemb);
	}
	return ptr;
}

/* 'new_nmemb' must be > 'old_nmemb'. */
static void *realloc2(void *ptr, int new_nmemb, int old_nmemb, size_t size)
{
	void *new_ptr;

	if (new_nmemb <= old_nmemb)
		error("S4Vectors internal error in realloc2(): "
		      "'new_nmemb' <= 'old_nmemb'");
	if (old_nmemb == 0)
		return alloc2(new_nmemb, size);
	if (use_malloc) {
		//printf("realloc2: new_nmemb=%d old_nmemb=%d\n",
		//       new_nmemb, old_nmemb);
		size *= new_nmemb;
		new_ptr = realloc(ptr, size);
		if (new_ptr == NULL)
			error("S4Vectors internal error in realloc2(): "
			      "cannot reallocate memory");
	} else {
		new_ptr = (void *) R_alloc(size, new_nmemb);
		memcpy(new_ptr, ptr, size * old_nmemb);
	}
	return new_ptr;
}

/* Guaranteed to return a new buflength > 'buflength', or to raise an error. */
int _get_new_buflength(int buflength)
{
	if (buflength >= MAX_BUFLENGTH)
		error("_get_new_buflength(): MAX_BUFLENGTH reached");
	if (buflength == 0)
		return 128;
	if (buflength <= MAX_BUFLENGTH_INC)
		return 2 * buflength;
	buflength += MAX_BUFLENGTH_INC;
	if (buflength <= MAX_BUFLENGTH)
		return buflength;
	return MAX_BUFLENGTH;
}


/****************************************************************************
 * IntAE buffers
 */

#define	INTAE_POOL_MAXLEN 256
static IntAE *IntAE_pool[INTAE_POOL_MAXLEN];
static int IntAE_pool_len = 0;

int _IntAE_get_nelt(const IntAE *ae)
{
	return ae->_nelt;
}

int _IntAE_set_nelt(IntAE *ae, int nelt)
{
	return ae->_nelt = nelt;
}

static IntAE *new_empty_IntAE()
{
	IntAE *ae;

	if (use_malloc && IntAE_pool_len >= INTAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_IntAE(): "
		      "IntAE pool is full");
	ae = (IntAE *) alloc2(1, sizeof(IntAE));
	ae->_buflength = ae->_nelt = 0;
	if (use_malloc)
		IntAE_pool[IntAE_pool_len++] = ae;
	return ae;
}

void _IntAE_set_val(const IntAE *ae, int val)
{
	int nelt, i, *elt;

	nelt = _IntAE_get_nelt(ae);
	for (i = 0, elt = ae->elts; i < nelt; i++, elt++)
		*elt = val;
	return;
}

static void IntAE_extend(IntAE *ae, int new_buflength)
{
	int old_buflength;

	old_buflength = ae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	ae->elts = (int *) realloc2(ae->elts, new_buflength,
				    old_buflength, sizeof(int));
	ae->_buflength = new_buflength;
	return;
}

void _IntAE_insert_at(IntAE *ae, int at, int val)
{
	int nelt, i;
	int *elt1_p;
	const int *elt2_p;

	nelt = _IntAE_get_nelt(ae);
	if (nelt >= ae->_buflength)
		IntAE_extend(ae, -1);
	elt1_p = ae->elts + nelt;
	elt2_p = elt1_p - 1;
	for (i = nelt; i > at; i--)
		*(elt1_p--) = *(elt2_p--);
	*elt1_p = val;
	_IntAE_set_nelt(ae, nelt + 1);
	return;
}

IntAE *_new_IntAE(int buflength, int nelt, int val)
{
	IntAE *ae;

	ae = new_empty_IntAE();
	if (buflength != 0) {
		IntAE_extend(ae, buflength);
		_IntAE_set_nelt(ae, nelt);
		_IntAE_set_val(ae, val);
	}
	return ae;
}

void _IntAE_append(IntAE *ae, const int *newvals, int nnewval)
{
	int new_nelt, *dest;

	new_nelt = _IntAE_get_nelt(ae) + nnewval;
	if (new_nelt > ae->_buflength)
		IntAE_extend(ae, new_nelt);
	dest = ae->elts + _IntAE_get_nelt(ae);
	memcpy(dest, newvals, nnewval * sizeof(int));
	_IntAE_set_nelt(ae, new_nelt);
	return;
}

void _IntAE_delete_at(IntAE *ae, int at)
{
	int *elt1_p;
	const int *elt2_p;
	int nelt0, i2;

	elt1_p = ae->elts + at;
	elt2_p = elt1_p + 1;
	nelt0 = _IntAE_get_nelt(ae);
	for (i2 = at + 1; i2 < nelt0; i2++)
		*(elt1_p++) = *(elt2_p++);
	_IntAE_set_nelt(ae, nelt0 - 1);
	return;
}

void _IntAE_shift(const IntAE *ae, int shift)
{
	int nelt, i, *elt;

	nelt = _IntAE_get_nelt(ae);
	for (i = 0, elt = ae->elts; i < nelt; i++, elt++)
		*elt += shift;
	return;
}

/*
 * Left and right IntAE objects must have the same length. This is
 * NOT checked!
 */
void _IntAE_sum_and_shift(const IntAE *ae1, const IntAE *ae2, int shift)
{
	int nelt, i, *elt1, *elt2;

	nelt = _IntAE_get_nelt(ae1);
	for (i = 0, elt1 = ae1->elts, elt2 = ae2->elts;
	     i < nelt;
	     i++, elt1++, elt2++)
		*elt1 += *elt2 + shift;
	return;
}

void _IntAE_append_shifted_vals(IntAE *ae, const int *newvals,
		int nnewval, int shift)
{
	int nelt, new_nelt, i, *elt1;
	const int *elt2;

	nelt = _IntAE_get_nelt(ae);
	new_nelt = nelt + nnewval;
	if (new_nelt > ae->_buflength)
		IntAE_extend(ae, new_nelt);
	for (i = 0, elt1 = ae->elts + nelt, elt2 = newvals;
	     i < nnewval;
	     i++, elt1++, elt2++)
		*elt1 = *elt2 + shift;
	_IntAE_set_nelt(ae, new_nelt);
	return;
}

void _IntAE_qsort(const IntAE *ae, int desc)
{
	_sort_int_array(ae->elts, _IntAE_get_nelt(ae), desc);
	return;
}

void _IntAE_delete_adjdups(IntAE *ae)
{
	int nelt, *elt1;
	const int *elt2;
	int i2;

	nelt = _IntAE_get_nelt(ae);
	if (nelt <= 1)
		return;
	elt1 = ae->elts;
	elt2 = elt1 + 1;
	for (i2 = 1; i2 < nelt; i2++) {
		if (*elt2 != *elt1) {
			elt1++;
			*elt1 = *elt2;
		}
		elt2++;
	}
	_IntAE_set_nelt(ae, elt1 - ae->elts + 1);
	return;
}

SEXP _new_INTEGER_from_IntAE(const IntAE *ae)
{
	int nelt;
	SEXP ans;

	nelt = _IntAE_get_nelt(ae);
	PROTECT(ans = NEW_INTEGER(nelt));
	memcpy(INTEGER(ans), ae->elts, sizeof(int) * nelt);
	UNPROTECT(1);
	return ans;
}

static void copy_INTEGER_to_IntAE(SEXP x, IntAE *ae)
{
	_IntAE_set_nelt(ae, LENGTH(x));
	memcpy(ae->elts, INTEGER(x), sizeof(int) * LENGTH(x));
	return;
}

IntAE *_new_IntAE_from_INTEGER(SEXP x)
{
	IntAE *ae;

	ae = _new_IntAE(LENGTH(x), 0, 0);
	copy_INTEGER_to_IntAE(x, ae);
	return ae;
}

IntAE *_new_IntAE_from_CHARACTER(SEXP x, int keyshift)
{
	IntAE *ae;
	int i, *elt;

	ae = _new_IntAE(LENGTH(x), 0, 0);
	_IntAE_set_nelt(ae, ae->_buflength);
	for (i = 0, elt = ae->elts; i < ae->_buflength; i++, elt++) {
		sscanf(CHAR(STRING_ELT(x, i)), "%d", elt);
		*elt += keyshift;
	}
	return ae;
}

/* Must be used on a malloc-based IntAE */
static void IntAE_free(IntAE *ae)
{
	if (ae->_buflength != 0)
		free(ae->elts);
	free(ae);
	return;
}

static void flush_IntAE_pool()
{
	IntAE *ae;

	while (IntAE_pool_len > 0) {
		IntAE_pool_len--;
		ae = IntAE_pool[IntAE_pool_len];
		IntAE_free(ae);
	}
	return;
}

static int remove_from_IntAE_pool(const IntAE *ae)
{
	int i;
	IntAE **ae1_p, **ae2_p;

	i = IntAE_pool_len;
	while (--i >= 0 && IntAE_pool[i] != ae) {;}
	if (i < 0)
		return -1;
	ae1_p = IntAE_pool + i;
	ae2_p = ae1_p + 1;
	for (i = i + 1; i < IntAE_pool_len; i++)
		*(ae1_p++) = *(ae2_p++);
	IntAE_pool_len--;
	return 0;
}


/****************************************************************************
 * IntAEAE buffers
 */

#define	INTAEAE_POOL_MAXLEN 256
static IntAEAE *IntAEAE_pool[INTAEAE_POOL_MAXLEN];
static int IntAEAE_pool_len = 0;

int _IntAEAE_get_nelt(const IntAEAE *aeae)
{
	return aeae->_nelt;
}

int _IntAEAE_set_nelt(IntAEAE *aeae, int nelt)
{
	return aeae->_nelt = nelt;
}

static IntAEAE *new_empty_IntAEAE()
{
	IntAEAE *aeae;

	if (use_malloc && IntAEAE_pool_len >= INTAEAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_IntAEAE(): "
		      "IntAEAE pool is full");
	aeae = (IntAEAE *) alloc2(1, sizeof(IntAEAE));
	aeae->_buflength = aeae->_nelt = 0;
	if (use_malloc)
		IntAEAE_pool[IntAEAE_pool_len++] = aeae;
	return aeae;
}

static void IntAEAE_extend(IntAEAE *aeae, int new_buflength)
{
	int old_buflength, i;

	old_buflength = aeae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	aeae->elts = (IntAE **) realloc2(aeae->elts, new_buflength,
					 old_buflength, sizeof(IntAE *));
	for (i = old_buflength; i < new_buflength; i++)
		aeae->elts[i] = NULL;
	aeae->_buflength = new_buflength;
	return;
}

void _IntAEAE_insert_at(IntAEAE *aeae, int at, IntAE *ae)
{
	int nelt, i;
	IntAE **ae1_p, **ae2_p;

	nelt = _IntAEAE_get_nelt(aeae);
	if (nelt >= aeae->_buflength)
		IntAEAE_extend(aeae, -1);
	if (use_malloc && remove_from_IntAE_pool(ae) == -1)
		error("S4Vectors internal error in _IntAEAE_insert_at(): "
		      "IntAE to insert cannot be found in pool for removal");
	ae1_p = aeae->elts + nelt;
	ae2_p = ae1_p - 1;
	for (i = nelt; i > at; i--)
		*(ae1_p--) = *(ae2_p--);
	*ae1_p = ae;
	_IntAEAE_set_nelt(aeae, nelt + 1);
	return;
}

IntAEAE *_new_IntAEAE(int buflength, int nelt)
{
	IntAEAE *aeae;
	int i;
	IntAE *ae;

	aeae = new_empty_IntAEAE();
	if (buflength != 0) {
		IntAEAE_extend(aeae, buflength);
		for (i = 0; i < nelt; i++) {
			ae = new_empty_IntAE();
			_IntAEAE_insert_at(aeae, i, ae);
		}
	}
	return aeae;
}

/*
 * Left and right IntAEAE objects must have the same length. This is
 * NOT checked!
 */
void _IntAEAE_eltwise_append(const IntAEAE *aeae1, const IntAEAE *aeae2)
{
	int nelt, i;
	IntAE *ae1;
	const IntAE *ae2;

	nelt = _IntAEAE_get_nelt(aeae1);
	for (i = 0; i < nelt; i++) {
		ae1 = aeae1->elts[i];
		ae2 = aeae2->elts[i];
		_IntAE_append(ae1, ae2->elts, _IntAE_get_nelt(ae2));
	}
	return;
}

void _IntAEAE_shift(const IntAEAE *aeae, int shift)
{
	int nelt, i;
	IntAE *ae;

	nelt = _IntAEAE_get_nelt(aeae);
	for (i = 0; i < nelt; i++) {
		ae = aeae->elts[i];
		_IntAE_shift(ae, shift);
	}
	return;
}

/*
 * Left and right IntAEAE objects must have the same length. This is
 * NOT checked!
 */
void _IntAEAE_sum_and_shift(const IntAEAE *aeae1, const IntAEAE *aeae2,
		int shift)
{
	int nelt, i;
	IntAE *ae1;
	const IntAE *ae2;

	nelt = _IntAEAE_get_nelt(aeae1);
	for (i = 0; i < nelt; i++) {
		ae1 = aeae1->elts[i];
		ae2 = aeae2->elts[i];
		_IntAE_sum_and_shift(ae1, ae2, shift);
	}
	return;
}

/*
 * 'mode' controls how empty list elements should be represented:
 *   0 -> integer(0); 1 -> NULL; 2 -> NA
 */
SEXP _new_LIST_from_IntAEAE(const IntAEAE *aeae, int mode)
{
	int nelt, i;
	SEXP ans, ans_elt;
	const IntAE *ae;

	nelt = _IntAEAE_get_nelt(aeae);
	PROTECT(ans = NEW_LIST(nelt));
	for (i = 0; i < nelt; i++) {
		ae = aeae->elts[i];
		if (_IntAE_get_nelt(ae) != 0 || mode == 0) {
			PROTECT(ans_elt = _new_INTEGER_from_IntAE(ae));
		} else if (mode == 1) {
			continue;
		} else {
			// Not sure new LOGICALs are initialized with NAs,
			// need to check! If not, then LOGICAL(ans_elt)[0] must
			// be set to NA but I don't know how to do this :-/
			PROTECT(ans_elt = NEW_LOGICAL(1));
		}
		SET_VECTOR_ELT(ans, i, ans_elt);
		UNPROTECT(1);
	}
	UNPROTECT(1);
	return ans;
}

IntAEAE *_new_IntAEAE_from_LIST(SEXP x)
{
	IntAEAE *aeae;
	int i;
	SEXP x_elt;
	IntAE *ae;

	aeae = _new_IntAEAE(LENGTH(x), 0);
	for (i = 0; i < aeae->_buflength; i++) {
		x_elt = VECTOR_ELT(x, i);
		if (TYPEOF(x_elt) != INTSXP)
			error("S4Vectors internal error in "
			      "_new_IntAEAE_from_LIST(): "
			      "not all elements in the list "
			      "are integer vectors");
		ae = _new_IntAE_from_INTEGER(x_elt);
		_IntAEAE_insert_at(aeae, i, ae);
	}
	return aeae;
}

SEXP _IntAEAE_toEnvir(const IntAEAE *aeae, SEXP envir, int keyshift)
{
	int nelt, i;
	const IntAE *ae;
	char key[11];
	SEXP value;

	nelt = _IntAEAE_get_nelt(aeae);
	for (i = 0; i < nelt; i++) {
		ae = aeae->elts[i];
		if (_IntAE_get_nelt(ae) == 0)
			continue;
		//snprintf(key, sizeof(key), "%d", i + keyshift);
		snprintf(key, sizeof(key), "%010d", i + keyshift);
		PROTECT(value = _new_INTEGER_from_IntAE(ae));
		defineVar(install(key), value, envir);
		UNPROTECT(1);
	}
	return envir;
}

/* Must be used on a malloc-based IntAEAE */
static void IntAEAE_free(IntAEAE *aeae)
{
	int buflength, i;
	IntAE *ae;

	buflength = aeae->_buflength;
	for (i = 0; i < buflength; i++) {
		ae = aeae->elts[i];
		if (ae != NULL)
			IntAE_free(ae);
	}
	if (buflength != 0)
		free(aeae->elts);
	free(aeae);
	return;
}

static void flush_IntAEAE_pool()
{
	IntAEAE *aeae;

	while (IntAEAE_pool_len > 0) {
		IntAEAE_pool_len--;
		aeae = IntAEAE_pool[IntAEAE_pool_len];
		IntAEAE_free(aeae);
	}
	return;
}


/****************************************************************************
 * IntPairAE buffers
 */

#define	INTPAIRAE_POOL_MAXLEN 256
static IntPairAE *IntPairAE_pool[INTPAIRAE_POOL_MAXLEN];
static int IntPairAE_pool_len = 0;

int _IntPairAE_get_nelt(const IntPairAE *ae)
{
	return _IntAE_get_nelt(ae->a);
}

int _IntPairAE_set_nelt(IntPairAE *ae, int nelt)
{
	_IntAE_set_nelt(ae->a, nelt);
	_IntAE_set_nelt(ae->b, nelt);
	return nelt;
}

static IntPairAE *new_empty_IntPairAE()
{
	IntAE *a, *b;
	IntPairAE *ae;

	if (use_malloc && IntPairAE_pool_len >= INTPAIRAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_IntPairAE(): "
		      "IntPairAE pool is full");
	a = new_empty_IntAE();
	b = new_empty_IntAE();
	ae = (IntPairAE *) alloc2(1, sizeof(IntPairAE));
	ae->a = a;
	ae->b = b;
	if (use_malloc) {
		if (remove_from_IntAE_pool(a) == -1 ||
		    remove_from_IntAE_pool(b) == -1)
			error("S4Vectors internal error "
			      "in new_empty_IntPairAE(): "
			      "IntAEs to stick in IntPairAE cannot be found "
			      "in pool for removal");
		IntPairAE_pool[IntPairAE_pool_len++] = ae;
	}
	return ae;
}

static void IntPairAE_extend(IntPairAE *ae, int new_buflength)
{
	IntAE_extend(ae->a, new_buflength);
	IntAE_extend(ae->b, new_buflength);
	return;
}

void _IntPairAE_insert_at(IntPairAE *ae, int at, int a, int b)
{
	_IntAE_insert_at(ae->a, at, a);
	_IntAE_insert_at(ae->b, at, b);
	return;
}

IntPairAE *_new_IntPairAE(int buflength, int nelt)
{
	IntPairAE *ae;

	ae = new_empty_IntPairAE();
	if (buflength != 0) {
		IntPairAE_extend(ae, buflength);
		/* Elements are NOT initialized. */
		_IntPairAE_set_nelt(ae, nelt);
	}
	return ae;
}

/* Must be used on a malloc-based IntPairAE */
static void IntPairAE_free(IntPairAE *ae)
{
	IntAE_free(ae->a);
	IntAE_free(ae->b);
	free(ae);
	return;
}

static void flush_IntPairAE_pool()
{
	IntPairAE *ae;

	while (IntPairAE_pool_len > 0) {
		IntPairAE_pool_len--;
		ae = IntPairAE_pool[IntPairAE_pool_len];
		IntPairAE_free(ae);
	}
	return;
}

static int remove_from_IntPairAE_pool(const IntPairAE *ae)
{
	int i;
	IntPairAE **ae1_p, **ae2_p;

	i = IntPairAE_pool_len;
	while (--i >= 0 && IntPairAE_pool[i] != ae) {;}
	if (i < 0)
		return -1;
	ae1_p = IntPairAE_pool + i;
	ae2_p = ae1_p + 1;
	for (i = i + 1; i < IntPairAE_pool_len; i++)
		*(ae1_p++) = *(ae2_p++);
	IntPairAE_pool_len--;
	return 0;
}


/****************************************************************************
 * IntPairAEAE buffers
 */

#define	INTPAIRAEAE_POOL_MAXLEN 256
static IntPairAEAE *IntPairAEAE_pool[INTPAIRAEAE_POOL_MAXLEN];
static int IntPairAEAE_pool_len = 0;

int _IntPairAEAE_get_nelt(const IntPairAEAE *aeae)
{
	return aeae->_nelt;
}

int _IntPairAEAE_set_nelt(IntPairAEAE *aeae, int nelt)
{
	return aeae->_nelt = nelt;
}

static IntPairAEAE *new_empty_IntPairAEAE()
{
	IntPairAEAE *aeae;

	if (use_malloc && IntPairAEAE_pool_len >= INTPAIRAEAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_IntPairAEAE(): "
		      "IntPairAEAE pool is full");
	aeae = (IntPairAEAE *) alloc2(1, sizeof(IntPairAEAE));
	aeae->_buflength = aeae->_nelt = 0;
	if (use_malloc)
		IntPairAEAE_pool[IntPairAEAE_pool_len++] = aeae;
	return aeae;
}

static void IntPairAEAE_extend(IntPairAEAE *aeae, int new_buflength)
{
	int old_buflength, i;

	old_buflength = aeae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	aeae->elts = (IntPairAE **) realloc2(aeae->elts, new_buflength,
					old_buflength, sizeof(IntPairAE *));
	for (i = old_buflength; i < new_buflength; i++)
		aeae->elts[i] = NULL;
	aeae->_buflength = new_buflength;
	return;
}

void _IntPairAEAE_insert_at(IntPairAEAE *aeae, int at, IntPairAE *ae)
{
	int nelt, i;
	IntPairAE **ae1_p, **ae2_p;

	nelt = _IntPairAEAE_get_nelt(aeae);
	if (nelt >= aeae->_buflength)
		IntPairAEAE_extend(aeae, -1);
	if (use_malloc && remove_from_IntPairAE_pool(ae) == -1)
		error("S4Vectors internal error in _IntPairAEAE_insert_at(): "
		      "IntPairAE to insert cannot be found in pool for "
		      "removal");
	ae1_p = aeae->elts + nelt;
	ae2_p = ae1_p - 1;
	for (i = nelt; i > at; i--)
		*(ae1_p--) = *(ae2_p--);
	*ae1_p = ae;
	_IntPairAEAE_set_nelt(aeae, nelt + 1);
	return;
}

IntPairAEAE *_new_IntPairAEAE(int buflength, int nelt)
{
	IntPairAEAE *aeae;
	int i;
	IntPairAE *ae;

	aeae = new_empty_IntPairAEAE();
	if (buflength != 0) {
		IntPairAEAE_extend(aeae, buflength);
		for (i = 0; i < nelt; i++) {
			ae = new_empty_IntPairAE();
			_IntPairAEAE_insert_at(aeae, i, ae);
		}
	}
	return aeae;
}

/* Must be used on a malloc-based IntPairAEAE */
static void IntPairAEAE_free(IntPairAEAE *aeae)
{
	int buflength, i;
	IntPairAE *ae;

	buflength = aeae->_buflength;
	for (i = 0; i < buflength; i++) {
		ae = aeae->elts[i];
		if (ae != NULL)
			IntPairAE_free(ae);
	}
	if (buflength != 0)
		free(aeae->elts);
	free(aeae);
	return;
}

static void flush_IntPairAEAE_pool()
{
	IntPairAEAE *aeae;

	while (IntPairAEAE_pool_len > 0) {
		IntPairAEAE_pool_len--;
		aeae = IntPairAEAE_pool[IntPairAEAE_pool_len];
		IntPairAEAE_free(aeae);
	}
	return;
}


/****************************************************************************
 * LLongAE buffers
 */

#define	LLONGAE_POOL_MAXLEN 256
static LLongAE *LLongAE_pool[LLONGAE_POOL_MAXLEN];
static int LLongAE_pool_len = 0;

int _LLongAE_get_nelt(const LLongAE *ae)
{
	return ae->_nelt;
}

int _LLongAE_set_nelt(LLongAE *ae, int nelt)
{
	return ae->_nelt = nelt;
}

static LLongAE *new_empty_LLongAE()
{
	LLongAE *ae;

	if (use_malloc && LLongAE_pool_len >= LLONGAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_LLongAE(): "
		      "LLongAE pool is full");
	ae = (LLongAE *) alloc2(1, sizeof(LLongAE));
	ae->_buflength = ae->_nelt = 0;
	if (use_malloc)
		LLongAE_pool[LLongAE_pool_len++] = ae;
	return ae;
}

void _LLongAE_set_val(const LLongAE *ae, long long val)
{
	int nelt, i;
	long long *elt;

	nelt = _LLongAE_get_nelt(ae);
	for (i = 0, elt = ae->elts; i < nelt; i++, elt++)
		*elt = val;
	return;
}

static void LLongAE_extend(LLongAE *ae, int new_buflength)
{
	int old_buflength;

	old_buflength = ae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	ae->elts = (long long *) realloc2(ae->elts, new_buflength,
					  old_buflength, sizeof(long long));
	ae->_buflength = new_buflength;
	return;
}

void _LLongAE_insert_at(LLongAE *ae, int at, long long val)
{
	int nelt, i;
	long long *elt1_p;
	const long long *elt2_p;

	nelt = _LLongAE_get_nelt(ae);
	if (nelt >= ae->_buflength)
		LLongAE_extend(ae, -1);
	elt1_p = ae->elts + nelt;
	elt2_p = elt1_p - 1;
	for (i = nelt; i > at; i--)
		*(elt1_p--) = *(elt2_p--);
	*elt1_p = val;
	_LLongAE_set_nelt(ae, nelt + 1);
	return;
}

LLongAE *_new_LLongAE(int buflength, int nelt, long long val)
{
	LLongAE *ae;

	ae = new_empty_LLongAE();
	if (buflength != 0) {
		LLongAE_extend(ae, buflength);
		_LLongAE_set_nelt(ae, nelt);
		_LLongAE_set_val(ae, val);
	}
	return ae;
}

/* Must be used on a malloc-based LLongAE */
static void LLongAE_free(LLongAE *ae)
{
	if (ae->_buflength != 0)
		free(ae->elts);
	free(ae);
	return;
}

static void flush_LLongAE_pool()
{
	LLongAE *ae;

	while (LLongAE_pool_len > 0) {
		LLongAE_pool_len--;
		ae = LLongAE_pool[LLongAE_pool_len];
		LLongAE_free(ae);
	}
	return;
}


/****************************************************************************
 * CharAE buffers
 */

#define	CHARAE_POOL_MAXLEN 256
static CharAE *CharAE_pool[CHARAE_POOL_MAXLEN];
static int CharAE_pool_len = 0;

int _CharAE_get_nelt(const CharAE *ae)
{
	return ae->_nelt;
}

int _CharAE_set_nelt(CharAE *ae, int nelt)
{
	return ae->_nelt = nelt;
}

static CharAE *new_empty_CharAE()
{
	CharAE *ae;

	if (use_malloc && CharAE_pool_len >= CHARAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_CharAE(): "
		      "CharAE pool is full");
	ae = (CharAE *) alloc2(1, sizeof(CharAE));
	ae->_buflength = ae->_nelt = 0;
	if (use_malloc)
		CharAE_pool[CharAE_pool_len++] = ae;
	return ae;
}

static void CharAE_extend(CharAE *ae, int new_buflength)
{
	int old_buflength;

	old_buflength = ae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	ae->elts = (char *) realloc2(ae->elts, new_buflength,
				     old_buflength, sizeof(char));
	ae->_buflength = new_buflength;
	return;
}

void _CharAE_insert_at(CharAE *ae, int at, char c)
{
	int nelt, i;
	char *elt1_p;
	const char *elt2_p;

	nelt = _CharAE_get_nelt(ae);
	if (nelt >= ae->_buflength)
		CharAE_extend(ae, -1);
	elt1_p = ae->elts + nelt;
	elt2_p = elt1_p - 1;
	for (i = nelt; i > at; i--)
		*(elt1_p--) = *(elt2_p--);
	*elt1_p = c;
	_CharAE_set_nelt(ae, nelt + 1);
	return;
}

CharAE *_new_CharAE(int buflength)
{
	CharAE *ae;

	ae = new_empty_CharAE();
	if (buflength != 0)
		CharAE_extend(ae, buflength);
	return ae;
}

CharAE *_new_CharAE_from_string(const char *string)
{
	CharAE *ae;

	ae = _new_CharAE(strlen(string));
	_CharAE_set_nelt(ae, ae->_buflength);
	memcpy(ae->elts, string, ae->_buflength);
	return ae;
}

void _append_string_to_CharAE(CharAE *ae, const char *string)
{
	int nnewval, nelt, new_nelt;
	char *dest;

	nnewval = strlen(string);
	nelt = _CharAE_get_nelt(ae);
	new_nelt = nelt + nnewval;
	if (new_nelt > ae->_buflength)
		CharAE_extend(ae, new_nelt);
	dest = ae->elts + nelt;
	memcpy(dest, string, sizeof(char) * nnewval);
	_CharAE_set_nelt(ae, new_nelt);
	return;
}

/*
 * Delete 'nelt' elements, starting at position 'at'.
 * Doing _CharAE_delete_at(x, at, nelt) is equivalent to doing
 * _CharAE_delete_at(x, at, 1) 'nelt' times.
 */
void _CharAE_delete_at(CharAE *ae, int at, int nelt)
{
	char *c1_p;
	const char *c2_p;
	int nelt0, i2;

	if (nelt == 0)
		return;
	c1_p = ae->elts + at;
	c2_p = c1_p + nelt;
	nelt0 = _CharAE_get_nelt(ae);
	for (i2 = at + nelt; i2 < nelt0; i2++)
		*(c1_p++) = *(c2_p++);
	_CharAE_set_nelt(ae, nelt0 - nelt);
	return;
}

SEXP _new_RAW_from_CharAE(const CharAE *ae)
{
	int nelt;
	SEXP ans;

	if (sizeof(Rbyte) != sizeof(char)) // should never happen!
		error("_new_RAW_from_CharAE(): sizeof(Rbyte) != sizeof(char)");
	nelt = _CharAE_get_nelt(ae);
	PROTECT(ans = NEW_RAW(nelt));
	memcpy(RAW(ans), ae->elts, sizeof(char) * nelt);
	UNPROTECT(1);
	return ans;
}

/* only until we have a bitset or something smaller than char */
SEXP _new_LOGICAL_from_CharAE(const CharAE *ae)
{
	int nelt, i, *ans_elt;
	SEXP ans;
	const char *elt;

	nelt = _CharAE_get_nelt(ae);
	PROTECT(ans = NEW_LOGICAL(nelt));
	for (i = 0, ans_elt = LOGICAL(ans), elt = ae->elts;
	     i < nelt;
	     i++, ans_elt++, elt++)
	{
		*ans_elt = *elt;
	}
	UNPROTECT(1);
	return ans;
}

/* Must be used on a malloc-based CharAE */
static void CharAE_free(CharAE *ae)
{
	if (ae->_buflength != 0)
		free(ae->elts);
	free(ae);
	return;
}

static void flush_CharAE_pool()
{
	CharAE *ae;

	while (CharAE_pool_len > 0) {
		CharAE_pool_len--;
		ae = CharAE_pool[CharAE_pool_len];
		CharAE_free(ae);
	}
	return;
}

static int remove_from_CharAE_pool(const CharAE *ae)
{
	int i;
	CharAE **ae1_p, **ae2_p;

	i = CharAE_pool_len;
	while (--i >= 0 && CharAE_pool[i] != ae) {;}
	if (i < 0)
		return -1;
	ae1_p = CharAE_pool + i;
	ae2_p = ae1_p + 1;
	for (i = i + 1; i < CharAE_pool_len; i++)
		*(ae1_p++) = *(ae2_p++);
	CharAE_pool_len--;
	return 0;
}


/****************************************************************************
 * CharAEAE buffers
 */

#define	CHARAEAE_POOL_MAXLEN 256
static CharAEAE *CharAEAE_pool[CHARAEAE_POOL_MAXLEN];
static int CharAEAE_pool_len = 0;

int _CharAEAE_get_nelt(const CharAEAE *aeae)
{
	return aeae->_nelt;
}

int _CharAEAE_set_nelt(CharAEAE *aeae, int nelt)
{
	return aeae->_nelt = nelt;
}

static CharAEAE *new_empty_CharAEAE()
{
	CharAEAE *aeae;

	if (use_malloc && CharAEAE_pool_len >= CHARAEAE_POOL_MAXLEN)
		error("S4Vectors internal error in new_empty_CharAEAE(): "
		      "CharAEAE pool is full");
	aeae = (CharAEAE *) alloc2(1, sizeof(CharAEAE));
	aeae->_buflength = aeae->_nelt = 0;
	if (use_malloc)
		CharAEAE_pool[CharAEAE_pool_len++] = aeae;
	return aeae;
}

static void CharAEAE_extend(CharAEAE *aeae, int new_buflength)
{
	int old_buflength, i;

	old_buflength = aeae->_buflength;
	if (new_buflength == -1)
		new_buflength = _get_new_buflength(old_buflength);
	aeae->elts = (CharAE **) realloc2(aeae->elts, new_buflength,
					  old_buflength, sizeof(CharAE *));
	for (i = old_buflength; i < new_buflength; i++)
		aeae->elts[i] = NULL;
	aeae->_buflength = new_buflength;
	return;
}

void _CharAEAE_insert_at(CharAEAE *aeae, int at, CharAE *ae)
{
	int nelt, i;
	CharAE **ae1_p, **ae2_p;

	nelt = _CharAEAE_get_nelt(aeae);
	if (nelt >= aeae->_buflength)
		CharAEAE_extend(aeae, -1);
	if (use_malloc && remove_from_CharAE_pool(ae) == -1)
		error("S4Vectors internal error in _CharAEAE_insert_at(): "
		      "CharAE to insert cannot be found in pool for removal");
	ae1_p = aeae->elts + nelt;
	ae2_p = ae1_p - 1;
	for (i = nelt; i > at; i--)
		*(ae1_p--) = *(ae2_p--);
	*ae1_p = ae;
	_CharAEAE_set_nelt(aeae, nelt + 1);
	return;
}

CharAEAE *_new_CharAEAE(int buflength, int nelt)
{
	CharAEAE *aeae;
	int i;
	CharAE *ae;

	aeae = new_empty_CharAEAE();
	if (buflength != 0) {
		CharAEAE_extend(aeae, buflength);
		for (i = 0; i < nelt; i++) {
			ae = new_empty_CharAE();
			_CharAEAE_insert_at(aeae, i, ae);
		}
	}
	return aeae;
}

void _append_string_to_CharAEAE(CharAEAE *aeae, const char *string)
{
	CharAE *ae;

	ae = _new_CharAE_from_string(string);
	_CharAEAE_insert_at(aeae, _CharAEAE_get_nelt(aeae), ae);
	return;
}

SEXP _new_CHARACTER_from_CharAEAE(const CharAEAE *aeae)
{
	int nelt, i;
	SEXP ans, ans_elt;
	CharAE *ae;

	nelt = _CharAEAE_get_nelt(aeae);
	PROTECT(ans = NEW_CHARACTER(nelt));
	for (i = 0; i < nelt; i++) {
		ae = aeae->elts[i];
		PROTECT(ans_elt = mkCharLen(ae->elts, _CharAE_get_nelt(ae)));
		SET_STRING_ELT(ans, i, ans_elt);
		UNPROTECT(1);
	}
	UNPROTECT(1);
	return ans;
}

/* Must be used on a malloc-based CharAEAE */
static void CharAEAE_free(CharAEAE *aeae)
{
	int buflength, i;
	CharAE *ae;

	buflength = aeae->_buflength;
	for (i = 0; i < buflength; i++) {
		ae = aeae->elts[i];
		if (ae != NULL)
			CharAE_free(ae);
	}
	if (buflength != 0)
		free(aeae->elts);
	free(aeae);
	return;
}

static void flush_CharAEAE_pool()
{
	CharAEAE *aeae;

	while (CharAEAE_pool_len > 0) {
		CharAEAE_pool_len--;
		aeae = CharAEAE_pool[CharAEAE_pool_len];
		CharAEAE_free(aeae);
	}
	return;
}


/****************************************************************************
 * Freeing the malloc-based AEbufs.
 */

SEXP AEbufs_free()
{
	flush_IntAE_pool();
	flush_IntAEAE_pool();
	flush_IntPairAE_pool();
	flush_IntPairAEAE_pool();
	flush_LLongAE_pool();
	flush_CharAE_pool();
	flush_CharAEAE_pool();
	return R_NilValue;
}

