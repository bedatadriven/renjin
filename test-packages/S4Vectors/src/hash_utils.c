/****************************************************************************
 *                          Hash table management                           *
 ****************************************************************************/
#include "S4Vectors.h"

/*
 * Author: Martin Morgan
 * Modified from R_HOME/src/main/unique.c
 */
static void htab_init(struct htab *htab, int n)
{
	int n2, i;

	/* max supported value for n is 2^29 */
	if (n < 0 || n > 536870912) /* protect against overflow to -ve */
		error("length %d is too large for hashing", n);
	n2 = 2 * n;
	htab->M = 2;
	htab->K = 1;
	while (htab->M < n2) {
		htab->M *= 2;
		htab->K += 1;
	}
	htab->Mminus1 = htab->M - 1;
	htab->buckets = (int *) R_alloc(sizeof(int), htab->M);
	for (i = 0; i < htab->M; i++)
		htab->buckets[i] = NA_INTEGER;
	return;
}

struct htab _new_htab(int n)
{
	struct htab htab;

	htab_init(&htab, n);
	return htab;
}

int _get_hbucket_val(const struct htab *htab, int bucket_idx)
{
	return htab->buckets[bucket_idx];
}

void _set_hbucket_val(struct htab *htab, int bucket_idx, int val)
{
	htab->buckets[bucket_idx] = val;
	return;
}

