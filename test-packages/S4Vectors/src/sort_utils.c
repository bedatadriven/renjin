/****************************************************************************
 * Low-level sorting utilities                                              *
 * ---------------------------                                              *
 ****************************************************************************/
#include "S4Vectors.h"
#include <stdlib.h>  /* for qsort() */
#include <limits.h>  /* for INT_MIN, INT_MAX, UCHAR_MAX, USHRT_MAX */


/****************************************************************************
 * Low-level wrappers to qsort()
 */

static const int *aa, *bb, *cc, *dd;
static int aa_desc, bb_desc, cc_desc, dd_desc;

#define	COMPARE_TARGET_INTS(target, i1, i2, desc) \
	((desc) ? (target)[(i2)] - (target)[(i1)] \
		: (target)[(i1)] - (target)[(i2)])

static int compar1_stable(const void *p1, const void *p2)
{
	int i1, i2, ret;

	i1 = *((const int *) p1);
	i2 = *((const int *) p2);
	ret = COMPARE_TARGET_INTS(aa, i1, i2, aa_desc);
	if (ret != 0)
		return ret;
	/* Break tie by position so the ordering is "stable". */
	return i1 - i2;
}

static int compar2_stable(const void *p1, const void *p2)
{
	int i1, i2, ret;

	i1 = *((const int *) p1);
	i2 = *((const int *) p2);
	ret = COMPARE_TARGET_INTS(aa, i1, i2, aa_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(bb, i1, i2, bb_desc);
	if (ret != 0)
		return ret;
	/* Break tie by position so the ordering is "stable". */
	return i1 - i2;
}

static int compar3_stable(const void *p1, const void *p2)
{
	int i1, i2, ret;

	i1 = *((const int *) p1);
	i2 = *((const int *) p2);
	ret = COMPARE_TARGET_INTS(aa, i1, i2, aa_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(bb, i1, i2, bb_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(cc, i1, i2, cc_desc);
	if (ret != 0)
		return ret;
	/* Break tie by position so the ordering is "stable". */
	return i1 - i2;
}

static int compar4_stable(const void *p1, const void *p2)
{
	int i1, i2, ret;

	i1 = *((const int *) p1);
	i2 = *((const int *) p2);
	ret = COMPARE_TARGET_INTS(aa, i1, i2, aa_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(bb, i1, i2, bb_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(cc, i1, i2, cc_desc);
	if (ret != 0)
		return ret;
	ret = COMPARE_TARGET_INTS(dd, i1, i2, dd_desc);
	/* Break tie by position so the ordering is "stable". */
	return i1 - i2;
}

static void qsort1(int *base, int base_len, const int *a, int a_desc)
{
	aa = a;
	aa_desc = a_desc;
	qsort(base, base_len, sizeof(int), compar1_stable);
}

static void qsort2(int *base, int base_len,
		   const int *a, const int *b,
		   int a_desc, int b_desc)
{
	aa = a;
	bb = b;
	aa_desc = a_desc;
	bb_desc = b_desc;
	qsort(base, base_len, sizeof(int), compar2_stable);
}

static void qsort3(int *base, int base_len,
		   const int *a, const int *b, const int *c,
		   int a_desc, int b_desc, int c_desc)
{
	aa = a;
	bb = b;
	cc = c;
	aa_desc = a_desc;
	bb_desc = b_desc;
	cc_desc = c_desc;
	qsort(base, base_len, sizeof(int), compar3_stable);
}

static void qsort4(int *base, int base_len,
		   const int *a, const int *b, const int *c, const int *d,
		   int a_desc, int b_desc, int c_desc, int d_desc)
{
	aa = a;
	bb = b;
	cc = c;
	dd = d;
	aa_desc = a_desc;
	bb_desc = b_desc;
	cc_desc = c_desc;
	dd_desc = d_desc;
	qsort(base, base_len, sizeof(int), compar4_stable);
}


/****************************************************************************
 * sorted_targets() and qsort_targets()
 */

static int sorted_target(const int *base, int base_len,
			 const int *target, int desc)
{
	int prev_tval, tval, i;

	if (base_len == 0)
		return 1;
	prev_tval = target[base[0]];
	if (desc) {
		for (i = 1; i < base_len; i++) {
			tval = target[base[i]];
			if (tval > prev_tval)
				return 0;
			prev_tval = tval;
		}
	} else {
		for (i = 1; i < base_len; i++) {
			tval = target[base[i]];
			if (tval < prev_tval)
				return 0;
			prev_tval = tval;
		}
	}
	return 1;
}

static int sorted_targets(const int *base, int base_len,
		const int **targets, const int *descs, int ntarget)
{
	int i, j, desc, tval, prev_tval;
	const int *target;

	if (ntarget == 1)
		return sorted_target(base, base_len, targets[0], descs[0]);
	for (i = 1; i < base_len; i++) {
		for (j = 0; j < ntarget; j++) {
			target = targets[j];
			desc = descs[j];
			tval = target[base[i]];
			prev_tval = target[base[i - 1]];
			if (tval != prev_tval) {
				if (desc != (tval < prev_tval))
					return 0;
				break;
			}
		}
	}
	return 1;
}

/* Pretty dummy and doesn't scale :-(  Should be easy to change. */
static void qsort_targets(int *base, int base_len,
		const int **targets, const int *descs, int ntarget)
{
	if (ntarget == 1) {
		qsort1(base, base_len, targets[0], descs[0]);
		return;
	}
	if (ntarget == 2) {
		qsort2(base, base_len, targets[0], targets[1],
				       descs[0], descs[1]);
		return;
	}
	if (ntarget == 3) {
		qsort3(base, base_len, targets[0], targets[1], targets[2],
				       descs[0], descs[1], descs[2]);
		return;
	}
	if (ntarget == 4) {
		qsort4(base, base_len, targets[0], targets[1],
				       targets[2], targets[3],
				       descs[0], descs[1], descs[2], descs[3]);
		return;
	}
	error("S4Vectors internal error in qsort_targets(): "
	      "ntarget must be between >= 1 and <= 4");
	return;
}

static int lucky_sort_targets(int *base, int base_len,
		const int **targets, const int *descs, int ntarget,
		int qsort_cutoff)
{
	int tmp;

	/* Find out whether 'base' is already sorted with respect to all
	   remaining targets (including current). */
	if (sorted_targets(base, base_len, targets, descs, ntarget))
		return 1;

	if (base_len == 2) {
		tmp = base[0];
		base[0] = base[1];
		base[1] = tmp;
		return 1;
	}

	if (base_len <= qsort_cutoff) {
		qsort_targets(base, base_len, targets, descs, ntarget);
		return 1;
	}
	return 0;
}


/****************************************************************************
 * Sorting an array of *distinct* unsigned chars
 */

static int compar_uchars_for_asc_sort(const void *p1, const void *p2)
{
	return ((int) *((const unsigned char *) p1)) -
	       ((int) *((const unsigned char *) p2));
}

static int compar_uchars_for_desc_sort(const void *p1, const void *p2)
{
	return ((int) *((const unsigned char *) p2)) -
	       ((int) *((const unsigned char *) p1));
}

/* The qsort() solution doesn't take advantage of the fact that the values
   in 'x' are distinct. */
static void sort_uchar_array(unsigned char *x, int nelt, int desc)
{
	int (*compar)(const void *, const void *);

	compar = desc ? compar_uchars_for_desc_sort
		      : compar_uchars_for_asc_sort;
	qsort(x, nelt, sizeof(unsigned char), compar);
	return;
}


/****************************************************************************
 * sorted_ushort_buf()
 */

/* Don't call on an empty buffer (i.e. when 'buf_len' is 0). */
static int sorted_ushort_buf(const unsigned short int *ushort_buf, int buf_len,
			     int desc)
{
	unsigned short int prev_uidx, uidx;
	int i;

	prev_uidx = ushort_buf[0];
	if (desc) {
		for (i = 1; i < buf_len; i++) {
			uidx = ushort_buf[i];
			if (uidx > prev_uidx)
				return 0;
			prev_uidx = uidx;
		}
	} else {
		for (i = 1; i < buf_len; i++) {
			uidx = ushort_buf[i];
			if (uidx < prev_uidx)
				return 0;
			prev_uidx = uidx;
		}
	}
	return 1;
}


/****************************************************************************
 * Mini radix: A simple radix-based sort of a single array of *distinct*
 * unsigned short ints
 */

#define	MINIRX_NBUCKET		(1 << CHAR_BIT)
static int			minirx_desc;

static void minirx_sort_lsb(unsigned short int *base, int base_len,
			    unsigned short int *out, int swapped)
{
	static unsigned char bucket2base[MINIRX_NBUCKET];

	int i;
	unsigned char uidx, min_uidx, max_uidx;
	unsigned short int *out_p;

	if (base_len == 1) {
		if (swapped)
			*out = *base;
		return;
	}
	if (sorted_ushort_buf(base, base_len, minirx_desc)) {
		if (swapped)
			memcpy(out, base,
			       sizeof(unsigned short int) * base_len);
		return;
	}

	out_p = out;
	if (base_len == MINIRX_NBUCKET) {
		for (i = 0; i < base_len; i++) {
			uidx = (unsigned char) base[i];
			bucket2base[uidx] = i;
		}
		if (minirx_desc) {
			uidx = UCHAR_MAX;  /* 0xff */
			do {
				i = bucket2base[uidx];
				*(out_p++) = base[i];
			} while (uidx-- != 0x00);
		} else {
			uidx = 0x00;
			do {
				i = bucket2base[uidx];
				*(out_p++) = base[i];
			} while (uidx++ != UCHAR_MAX);
		}
	} else {
		min_uidx = UCHAR_MAX;  /* 0xff */
		max_uidx = 0x00;
		memset(bucket2base, UCHAR_MAX,
		       sizeof(unsigned char) * MINIRX_NBUCKET);
		/* Use 8 less significant bits of the base values (unsigned
		   short ints) to compute the bucket indices. */
		for (i = 0; i < base_len; i++) {
			uidx = (unsigned char) base[i];
			bucket2base[uidx] = i;
			if (uidx < min_uidx)
				min_uidx = uidx;
			if (uidx > max_uidx)
				max_uidx = uidx;
		}
		if (minirx_desc) {
			uidx = max_uidx;
			do {
				i = bucket2base[uidx];
				if (i != UCHAR_MAX)
					*(out_p++) = base[i];
			} while (uidx-- != min_uidx);
		} else {
			uidx = min_uidx;
			do {
				i = bucket2base[uidx];
				if (i != UCHAR_MAX)
					*(out_p++) = base[i];
			} while (uidx++ != max_uidx);
		}
	}
	if (!swapped)
		memcpy(base, out,
		       sizeof(unsigned short int) * base_len);
	return;
}

#define	MINIRX_BASE_MAXLENGTH	(1 << (2 * CHAR_BIT))
static unsigned char		minirx_base_uidx_buf[MINIRX_BASE_MAXLENGTH];

/* Populate 'bucket_counts_buf', 'bucket_used_buf', and
   'minirx_base_uidx_buf'. */
static int minirx_compute_bucket_counts(
		const unsigned short int *base, int base_len,
		int *bucket_counts_buf,
		unsigned char *bucket_used_buf)
{
	int nbucket, i;
	unsigned char uidx;

	memset(bucket_counts_buf, 0, sizeof(int) * MINIRX_NBUCKET);
	nbucket = 0;
	/* Use 8 most significant bits of the base values (unsigned
	   short ints) to compute the bucket indices. */
	for (i = 0; i < base_len; i++) {
		uidx = (unsigned char) (base[i] >> CHAR_BIT);
		minirx_base_uidx_buf[i] = uidx;
		if (bucket_counts_buf[uidx]++ == 0)
			bucket_used_buf[nbucket++] = uidx;
	}
	return nbucket;
}

static int sorted_uchar_buf(const unsigned char *uchar_buf, int buf_len,
			    int desc)
{
	unsigned char prev_uidx, uidx;
	int i;

	prev_uidx = uchar_buf[0];
	if (desc) {
		for (i = 1; i < buf_len; i++) {
			uidx = uchar_buf[i];
			if (uidx > prev_uidx)
				return 0;
			prev_uidx = uidx;
		}
	} else {
		for (i = 1; i < buf_len; i++) {
			uidx = uchar_buf[i];
			if (uidx < prev_uidx)
				return 0;
			prev_uidx = uidx;
		}
	}
	return 1;
}

/* Walk only on buckets IN USE. */
static void minirx_compute_bucket_offsets_fast(
		const unsigned char *bucket_used_buf, int nbucket,
		const int *bucket_counts_buf,
		int *bucket_offsets_buf)
{
	int offset, i;
	unsigned char uidx;

	offset = 0;
	for (i = 0; i < nbucket; i++) {
		uidx = bucket_used_buf[i];
		offset += bucket_counts_buf[uidx];
		bucket_offsets_buf[uidx] = offset;
	}
	return;
}

static void compute_min_max_uchar_buf(
		const unsigned char *uchar_buf, int buf_len,
		unsigned char *min_uidx, unsigned char *max_uidx)
{
	unsigned char min, max, uidx;
	int i;

	min = UCHAR_MAX;  /* 0xff */
	max = 0x00;
	for (i = 0; i < buf_len; i++) {
		uidx = uchar_buf[i];
		if (uidx < min)
			min = uidx;
		if (uidx > max)
			max = uidx;
	}
	*min_uidx = min;
	*max_uidx = max;
	return;
}

static void minirx_compute_bucket_offsets(int desc,
		unsigned char min_uidx, unsigned char max_uidx,
		const int *bucket_counts_buf,
		int *bucket_offsets_buf)
{
	int offset;
	unsigned char uidx;

	offset = 0;
	if (desc) {
		uidx = max_uidx;
		do {
			offset += bucket_counts_buf[uidx];
			bucket_offsets_buf[uidx] = offset;
		} while (uidx-- != min_uidx);
	} else {
		uidx = min_uidx;
		do {
			offset += bucket_counts_buf[uidx];
			bucket_offsets_buf[uidx] = offset;
		} while (uidx++ != max_uidx);
	}
	return;
}

static int minirx_sort_base_by_bucket(unsigned short int *base, int base_len,
		unsigned short int *out,
		const int *bucket_counts_buf,
		int *bucket_offsets_buf,
		unsigned char *bucket_used_buf, int nbucket, int desc)
{
	int bucket_used_buf_is_sorted, i;
	unsigned char min_uidx, max_uidx;

	/* Should we sort 'bucket_used_buf'? */
	bucket_used_buf_is_sorted =
		sorted_uchar_buf(bucket_used_buf, nbucket, desc);

	if (!bucket_used_buf_is_sorted) {
		//if (nbucket == 2) {
		//	min_uidx = bucket_used_buf[0];
		//	bucket_used_buf[0] = bucket_used_buf[1];
		//	bucket_used_buf[1] = min_uidx;
		//	bucket_used_buf_is_sorted = 1;
		//} else if (nbucket >= 0xe0) {  /* 14/16 * 256 = 224 */
		//	/* Too expensive to find the real min/max uidx. */
		//	min_uidx = 0x00;
		//	max_uidx = UCHAR_MAX;  /* 0xff */
		//} else {
			compute_min_max_uchar_buf(
					bucket_used_buf, nbucket,
					&min_uidx, &max_uidx);
			/* Don't bother sorting if that's going to cost more
			   than just walking on the range of buckets. */
			//if (nbucket <= 4) {
			//	/* Cut-off value of 240 based on empirical
			//	   observation. */
			//	if ((int) max_uidx - min_uidx >= 240) {
			//		sort_uchar_array(bucket_used_buf,
			//				 nbucket,
			//				 desc);
			//		bucket_used_buf_is_sorted = 1;
			//	}
			//}
		//}
	}

	/* Compute bucket offsets. */
	if (bucket_used_buf_is_sorted) {
		minirx_compute_bucket_offsets_fast(bucket_used_buf, nbucket,
					bucket_counts_buf, bucket_offsets_buf);
	} else {
		minirx_compute_bucket_offsets(desc, min_uidx, max_uidx,
					bucket_counts_buf, bucket_offsets_buf);
	}

	/* Sort 'base' by bucket. */
	for (i = base_len - 1; i >= 0; i--)
		out[--bucket_offsets_buf[minirx_base_uidx_buf[i]]] = base[i];

	return bucket_used_buf_is_sorted;
}

static void minirx_sort(unsigned short int *base, int base_len,
			unsigned short int *out, int swapped)
{
	static int bucket_counts_buf[MINIRX_NBUCKET],
		   bucket_offsets_buf[MINIRX_NBUCKET];
	static unsigned char bucket_used_buf[MINIRX_NBUCKET];

	static int base_uidx_buf_is_sorted, bucket_used_buf_is_sorted;
	static unsigned short int *tmp;

	int nbucket, i, offset;
	unsigned char uidx;

	/* --- HANDLE THE EASY SITUATIONS --- */

	if (base_len == 0)
		return;

	if (base_len == 1) {
		if (swapped)
			*out = *base;
		return;
	}

	/* --- COMPUTE BUCKET INDICES, BUCKET COUNTS, AND LIST OF
	       USED BUCKETS --- */

	nbucket = minirx_compute_bucket_counts(base, base_len,
					       bucket_counts_buf,
					       bucket_used_buf);

	/* --- SORT 'base' BY BUCKET --- */

	base_uidx_buf_is_sorted = nbucket > 1 ?
	    sorted_uchar_buf(minirx_base_uidx_buf, base_len, minirx_desc) : 1;

	if (base_uidx_buf_is_sorted) {
		bucket_used_buf_is_sorted = 1;
	} else {
		bucket_used_buf_is_sorted = minirx_sort_base_by_bucket(
						base, base_len, out,
						bucket_counts_buf,
						bucket_offsets_buf,
						bucket_used_buf, nbucket,
						minirx_desc);
		/* Swap 'base' and 'out'. */
		tmp = out;
		out = base;
		base = tmp;
		swapped = !swapped;
	}

	/* --- ORDER EACH BUCKET --- */

	if (bucket_used_buf_is_sorted) {
		for (i = 0; i < nbucket; i++) {
			uidx = bucket_used_buf[i];
			base_len = bucket_counts_buf[uidx];
			minirx_sort_lsb(base, base_len, out, swapped);
			base += base_len;
			out += base_len;
		}
	} else {
		for (i = 0; i < nbucket; i++) {
			uidx = bucket_used_buf[i];
			offset = bucket_offsets_buf[uidx];
			base_len = bucket_counts_buf[uidx];
			minirx_sort_lsb(base + offset, base_len, out + offset,
					swapped);
		}
	}
	return;
}

/* Sort an array of *distinct* unsigned short ints.
   Between 10x (for small 'nelt') and 25x (for big 'nelt') faster than using
   qsort(). */
static void sort_ushort_array(unsigned short int *x, int nelt, int desc)
{
	static unsigned short int out[MINIRX_BASE_MAXLENGTH];

	minirx_desc = desc;
	minirx_sort(x, nelt, out, 0);
	return;
}


/****************************************************************************
 * RADIX SORT of arrays of integers
 *
 * The current implementation assumes that sizeof(int) is 4 and
 * sizeof(unsigned short int) is 2.
 */

static int can_use_rxsort()
{
	return sizeof(int) == 4 && sizeof(unsigned short int) == 2;
}

/* Dummy qsort_targets() above would need to be modified if were to support
   more than 4 targets. */
#define	MAX_RXTARGETS		4
static const int *		rxtargets[MAX_RXTARGETS];
static int			rxdescs[MAX_RXTARGETS];
static int			last_rxlevel;
static unsigned short int *	base_uidx_buf;

#define	RXLEVELS_PER_RXTARGET	2
#define	BITS_PER_RXLEVEL	(sizeof(unsigned short int) * CHAR_BIT)
#define	MAX_RXLEVELS		(MAX_RXTARGETS * RXLEVELS_PER_RXTARGET)
#define	RXNBUCKET		(1 << BITS_PER_RXLEVEL)

static int			rxbucket_counts_bufs[RXNBUCKET * MAX_RXLEVELS],
				rxbucket_offsets_bufs[RXNBUCKET * MAX_RXLEVELS];
static unsigned short int	rxbucket_used_bufs[RXNBUCKET * MAX_RXLEVELS];

/* Populate 'bucket_counts_buf', 'bucket_used_buf', and 'base_uidx_buf'. */
static int compute_bucket_counts(
		const int *base, int base_len, const int *target, int use_msb,
		int *bucket_counts_buf, unsigned short int *bucket_used_buf)
{
	int nbucket, i, tval;
	unsigned short int uidx;

	memset(bucket_counts_buf, 0, sizeof(int) * RXNBUCKET);
	nbucket = 0;
	if (use_msb) {
		/* Use 16 most significant bits of the target values to
		   compute the bucket indices. */
		for (i = 0; i < base_len; i++) {
			tval = target[base[i]];
			uidx = (unsigned short int) (tval >> BITS_PER_RXLEVEL);
			uidx += 0x8000;
			base_uidx_buf[i] = uidx;
			if (bucket_counts_buf[uidx]++ == 0)
				bucket_used_buf[nbucket++] = uidx;
		}
	} else {
		/* Use 16 less significant bits of the target values to
		   compute the bucket indices. */
		for (i = 0; i < base_len; i++) {
			tval = target[base[i]];
			uidx = (unsigned short int) tval;
			base_uidx_buf[i] = uidx;
			if (bucket_counts_buf[uidx]++ == 0)
				bucket_used_buf[nbucket++] = uidx;
		}
	}
	return nbucket;
}

/* Walk only on buckets IN USE. */
static void compute_bucket_offsets_fast(
		const unsigned short int *bucket_used_buf, int nbucket,
		const int *bucket_counts_buf, int *bucket_offsets_buf)
{
	int offset, i;
	unsigned short int uidx;

	offset = 0;
	for (i = 0; i < nbucket; i++) {
		uidx = bucket_used_buf[i];
		offset += bucket_counts_buf[uidx];
		bucket_offsets_buf[uidx] = offset;
	}
	return;
}

static void compute_min_max_ushort_buf(
		const unsigned short int *ushort_buf, int buf_len,
		unsigned short int *min_uidx, unsigned short int *max_uidx)
{
	unsigned short int min, max, uidx;
	int i;

	min = USHRT_MAX;  /* 0xffff */
	max = 0x0000;
	for (i = 0; i < buf_len; i++) {
		uidx = ushort_buf[i];
		if (uidx < min)
			min = uidx;
		if (uidx > max)
			max = uidx;
	}
	*min_uidx = min;
	*max_uidx = max;
	return;
}

static void compute_bucket_offsets(int desc,
		unsigned short int min_uidx, unsigned short int max_uidx,
		const int *bucket_counts_buf, int *bucket_offsets_buf)
{
	int offset;
	unsigned short int uidx;

	offset = 0;
	if (desc) {
		uidx = max_uidx;
		do {
			offset += bucket_counts_buf[uidx];
			bucket_offsets_buf[uidx] = offset;
		} while (uidx-- != min_uidx);
	} else {
		uidx = min_uidx;
		do {
			offset += bucket_counts_buf[uidx];
			bucket_offsets_buf[uidx] = offset;
		} while (uidx++ != max_uidx);
	}
	return;
}

static int sort_base_by_bucket(int *base, int base_len,
		int *out,
		const int *bucket_counts_buf, int *bucket_offsets_buf,
		unsigned short int *bucket_used_buf, int nbucket, int desc)
{
	int bucket_used_buf_is_sorted, i;
	unsigned short int min_uidx, max_uidx;

	/* Should we sort 'bucket_used_buf'? */
	bucket_used_buf_is_sorted =
		sorted_ushort_buf(bucket_used_buf, nbucket, desc);

	if (!bucket_used_buf_is_sorted) {
		//if (nbucket == 2) {
		//	min_uidx = bucket_used_buf[0];
		//	bucket_used_buf[0] = bucket_used_buf[1];
		//	bucket_used_buf[1] = min_uidx;
		//	bucket_used_buf_is_sorted = 1;
		//} else if (nbucket >= 0xe000) {  /* 14/16 * 65536 = 57344 */
		//	/* Too expensive to find the real min/max uidx. */
		//	min_uidx = 0x0000;
		//	max_uidx = USHRT_MAX;  /* 0xffff */
		//} else {
			compute_min_max_ushort_buf(
					bucket_used_buf, nbucket,
					&min_uidx, &max_uidx);
			/* Don't bother sorting if that's going to cost more
			   than just walking on the range of buckets. */
			if (nbucket < 4096) {
			   	/* Cut-off value of 4 based on empirical
				   observation. */
				if ((int) max_uidx - min_uidx >= 4 * nbucket) {
					sort_ushort_array(bucket_used_buf,
							  nbucket,
							  desc);
					bucket_used_buf_is_sorted = 1;
				}
			}
		//}
	}

	/* Compute bucket offsets. */
	if (bucket_used_buf_is_sorted) {
		compute_bucket_offsets_fast(bucket_used_buf, nbucket,
					bucket_counts_buf, bucket_offsets_buf);
	} else {
		compute_bucket_offsets(desc, min_uidx, max_uidx,
					bucket_counts_buf, bucket_offsets_buf);
	}

	/* Sort 'base' by bucket. */
	for (i = base_len - 1; i >= 0; i--)
		out[--bucket_offsets_buf[base_uidx_buf[i]]] = base[i];

	return bucket_used_buf_is_sorted;
}

static void rxsort_rec(int *base, int base_len, int *out,
		       int level, int swapped)
{
	static int target_no, qsort_cutoff, desc,
		   base_uidx_buf_is_sorted, bucket_used_buf_is_sorted, *tmp;
	static const int *target;

	int *bucket_counts_buf, *bucket_offsets_buf, nbucket, i, offset;
	unsigned short int *bucket_used_buf, uidx;

	/* --- HANDLE THE EASY SITUATIONS --- */

	if (base_len == 0)
		return;

	if (base_len == 1) {
		if (swapped)
			*out = *base;
		return;
	}

	target_no = level / RXLEVELS_PER_RXTARGET;

	/* The formula for computing the qsort cut-off makes the bold
	   assumption that the cost of qsort_targets() is linear with respect
	   to the number of targets involved in the sort ('ntarget' argument).
	   That tends to be the case when there is a high percentage of ties
	   but the reality is more complex.
	   The current formula leads to the following cut-off values:

	          target_no |     0     |     1     |     2     |     3
	     --------------------------------------------------------------
	      with 1 target | 512 * 1/1 |           |           |
	     with 2 targets | 512 * 1/2 | 512 * 2/2 |           |
	     with 4 targets | 512 * 1/4 | 512 * 2/4 | 512 * 3/4 | 512 * 4/4

	   The choice of 512 as max cut-off is based on empirical observation.
	   TODO: All these things need more fine tuning...
	*/
	qsort_cutoff = 512 * (target_no + 1) /
			     ((last_rxlevel + 1) / RXLEVELS_PER_RXTARGET);
	if (lucky_sort_targets(base, base_len,
			rxtargets + target_no, rxdescs + target_no,
			((last_rxlevel - level) / RXLEVELS_PER_RXTARGET) + 1,
			qsort_cutoff))
	{
		if (swapped)
			memcpy(out, base, sizeof(int) * base_len);
		return;
	}

	/* --- COMPUTE BUCKET INDICES, BUCKET COUNTS, AND LIST OF
	       USED BUCKETS --- */

	target = rxtargets[target_no];
	desc = rxdescs[target_no];

	bucket_counts_buf = rxbucket_counts_bufs + RXNBUCKET * level;
	bucket_used_buf = rxbucket_used_bufs + RXNBUCKET * level;
	nbucket = compute_bucket_counts(base, base_len, target, level % 2 == 0,
					bucket_counts_buf, bucket_used_buf);

	/* --- SORT 'base' BY BUCKET --- */

	base_uidx_buf_is_sorted = nbucket > 1 ?
		sorted_ushort_buf(base_uidx_buf, base_len, desc) : 1;

	if (base_uidx_buf_is_sorted) {
		bucket_used_buf_is_sorted = 1;
	} else {
		bucket_offsets_buf = rxbucket_offsets_bufs + RXNBUCKET * level;
		bucket_used_buf_is_sorted = sort_base_by_bucket(
						base, base_len, out,
						bucket_counts_buf,
						bucket_offsets_buf,
						bucket_used_buf, nbucket,
						desc);
		/* Swap 'base' and 'out'. */
		tmp = out;
		out = base;
		base = tmp;
		swapped = !swapped;
	}

	if (level == last_rxlevel) {
		if (swapped)
			memcpy(out, base, sizeof(int) * base_len);
		return;
	}

	/* --- ORDER EACH BUCKET --- */

	level++;
	if (bucket_used_buf_is_sorted) {
		for (i = 0; i < nbucket; i++) {
			uidx = bucket_used_buf[i];
			base_len = bucket_counts_buf[uidx];
			rxsort_rec(base, base_len, out, level, swapped);
			base += base_len;
			out += base_len;
		}
	} else {
		for (i = 0; i < nbucket; i++) {
			uidx = bucket_used_buf[i];
			offset = bucket_offsets_buf[uidx];
			base_len = bucket_counts_buf[uidx];
			rxsort_rec(base + offset, base_len, out + offset,
				   level, swapped);
		}
	}
	return;
}

static unsigned short int *alloc_rxbuf1(int base_len)
{
	return (unsigned short int *)
		malloc(sizeof(unsigned short int) * base_len);
}

static int *alloc_rxbuf2(int base_len,
		unsigned short int *rxbuf1, int auto_rxbuf1)
{
	int *rxbuf2;

	rxbuf2 = (int *) malloc(sizeof(int) * base_len);
	if (rxbuf2 == NULL && auto_rxbuf1)
		free(rxbuf1);
	return rxbuf2;
}


/****************************************************************************
 * Sorting or getting the order of an int array
 */

static int compar_ints_for_asc_sort(const void *p1, const void *p2)
{
	return *((const int *) p1) - *((const int *) p2);
}

static int compar_ints_for_desc_sort(const void *p1, const void *p2)
{
	return compar_ints_for_asc_sort(p2, p1);
}

/* If efficiency matters, use _sort_ints() in radix mode instead. */
void _sort_int_array(int *x, int nelt, int desc)
{
	int (*compar)(const void *, const void *);

	compar = desc ? compar_ints_for_desc_sort : compar_ints_for_asc_sort;
	qsort(x, nelt, sizeof(int), compar);
	return;
}

/* If efficiency matters, use _sort_ints() in radix mode instead. */
void _get_order_of_int_array(const int *x, int nelt,
		int desc, int *out, int out_shift)
{
	int i;

	for (i = 0; i < nelt; i++)
		out[i] = i + out_shift;
	qsort1(out, nelt, x - out_shift, desc);
	return;
}

/* base: 0-based indices into 'x'.
   rxbuf1, rxbuf2: NULL or user-allocated buffers of length 'base_len'. */
int _sort_ints(int *base, int base_len,
	       const int *x,
	       int desc,
	       int use_radix, unsigned short int *rxbuf1, int *rxbuf2)
{
	int qsort_cutoff, auto_rxbuf1, auto_rxbuf2;

	rxtargets[0] = x;
	rxdescs[0] = desc;

	qsort_cutoff = (use_radix && can_use_rxsort()) ? 1024 : base_len;
	if (lucky_sort_targets(base, base_len, rxtargets, rxdescs, 1,
			       qsort_cutoff))
		return 0;

	auto_rxbuf1 = rxbuf1 == NULL;
	if (auto_rxbuf1) {
		rxbuf1 = alloc_rxbuf1(base_len);
		if (rxbuf1 == NULL)
			return -1;
	}
	auto_rxbuf2 = rxbuf2 == NULL;
	if (auto_rxbuf2) {
		rxbuf2 = alloc_rxbuf2(base_len, rxbuf1, auto_rxbuf1);
		if (rxbuf2 == NULL)
			return -2;
	}

	last_rxlevel = 1;
	base_uidx_buf = rxbuf1;
	rxsort_rec(base, base_len, rxbuf2, 0, 0);

	if (auto_rxbuf2)
		free(rxbuf2);
	if (auto_rxbuf1)
		free(rxbuf1);
	return 0;
}


/****************************************************************************
 * Getting the order of 2 int arrays of the same length
 *
 * The second array (b) is used to break ties in the first array (a).
 */

static int compar_int_pairs(int a1, int b1, int a2, int b2)
{
	int ret;

	ret = a1 - a2;
	if (ret != 0)
		return ret;
	ret = b1 - b2;
	return ret;
}

/* Vectorized comparison of 2 vectors of integer pairs. */
void _pcompare_int_pairs(const int *a1, const int *b1, int nelt1,
			 const int *a2, const int *b2, int nelt2,
			 int *out, int out_len, int with_warning)
{
	int i, j, k;

	for (i = j = k = 0; k < out_len; i++, j++, k++) {
		if (i >= nelt1)
			i = 0; /* recycle i */
		if (j >= nelt2)
			j = 0; /* recycle j */
		out[k] = compar_int_pairs(a1[i], b1[i], a2[j], b2[j]);
	}
	/* This warning message is meaningful only when 'out_len' is
	   'max(nelt1, nelt2)' and is consistent with the warning we get from
	   binary arithmetic/comparison operations on numeric vectors. */
	if (with_warning && out_len != 0 && (i != nelt1 || j != nelt2))
		warning("longer object length is not a multiple "
			"of shorter object length");
	return;
}

int _int_pairs_are_sorted(const int *a, const int *b, int nelt,
			  int desc, int strict)
{
	int a1, b1, a2, b2, i, ret;

	if (nelt == 0)
		return 1;
	a2 = a[0];
	b2 = b[0];
	for (i = 1; i < nelt; i++) {
		a1 = a2;
		b1 = b2;
		a2 = a[i];
		b2 = b[i];
		ret = compar_int_pairs(a1, b1, a2, b2);
		if (ret == 0) {
			if (strict) return 0;
			continue;
		}
		if (desc != (ret > 0))
			return 0;
	}
	return 1;
}

/* If efficiency matters, use _sort_int_pairs() in radix mode instead. */
void _get_order_of_int_pairs(const int *a, const int *b, int nelt,
		int a_desc, int b_desc, int *out, int out_shift)
{
	int i;

	for (i = 0; i < nelt; i++)
		out[i] = i + out_shift;
	qsort2(out, nelt, a - out_shift, b - out_shift, a_desc, b_desc);
	return;
}

/* base: 0-based indices into 'a' and 'b'.
   rxbuf1, rxbuf2: NULL or user-allocated buffers of length 'base_len'. */
int _sort_int_pairs(int *base, int base_len,
		const int *a, const int *b,
		int a_desc, int b_desc,
		int use_radix, unsigned short int *rxbuf1, int *rxbuf2)
{
	int qsort_cutoff, auto_rxbuf1, auto_rxbuf2;

	rxtargets[0] = a;
	rxtargets[1] = b;
	rxdescs[0] = a_desc;
	rxdescs[1] = b_desc;

	qsort_cutoff = (use_radix && can_use_rxsort()) ? 512 : base_len;
	if (lucky_sort_targets(base, base_len, rxtargets, rxdescs, 2,
			       qsort_cutoff))
		return 0;

	auto_rxbuf1 = rxbuf1 == NULL;
	if (auto_rxbuf1) {
		rxbuf1 = alloc_rxbuf1(base_len);
		if (rxbuf1 == NULL)
			return -1;
	}
	auto_rxbuf2 = rxbuf2 == NULL;
	if (auto_rxbuf2) {
		rxbuf2 = alloc_rxbuf2(base_len, rxbuf1, auto_rxbuf1);
		if (rxbuf2 == NULL)
			return -2;
	}

	last_rxlevel = 3;
	base_uidx_buf = rxbuf1;
	rxsort_rec(base, base_len, rxbuf2, 0, 0);

	if (auto_rxbuf2)
		free(rxbuf2);
	if (auto_rxbuf1)
		free(rxbuf1);
	return 0;
}

void _get_matches_of_ordered_int_pairs(
		const int *a1, const int *b1, const int *o1, int nelt1,
		const int *a2, const int *b2, const int *o2, int nelt2,
		int nomatch, int *out, int out_shift)
{
	int i1, i2, ret;

	i2 = 0;
	ret = 0;
	for (i1 = 0; i1 < nelt1; i1++, o1++) {
		while (i2 < nelt2) {
			ret = compar_int_pairs(
				a1[*o1], b1[*o1],
				a2[*o2], b2[*o2]);
			if (ret <= 0)
				break;
			i2++, o2++;
		}
		out[*o1] = ret == 0 ? *o2 + out_shift : nomatch;
	}
	return;
}


/****************************************************************************
 * Getting the order of 4 int arrays of the same length
 *
 * 2nd, 3rd and 4th arrays are used to successively break ties.
 */

static int compar_int_quads(int a1, int b1, int c1, int d1,
			    int a2, int b2, int c2, int d2)
{
	int ret;

	ret = compar_int_pairs(a1, b1, a2, b2);
	if (ret != 0)
		return ret;
	ret = c1 - c2;
	if (ret != 0)
		return ret;
	ret = d1 - d2;
	return ret;
}

int _int_quads_are_sorted(const int *a, const int *b,
			  const int *c, const int *d, int nelt,
			  int desc, int strict)
{
	int a1, b1, c1, d1, a2, b2, c2, d2, i, ret;

	if (nelt == 0)
		return 1;
	a2 = a[0];
	b2 = b[0];
	c2 = c[0];
	d2 = d[0];
	for (i = 1; i < nelt; i++) {
		a1 = a2;
		b1 = b2;
		c1 = c2;
		d1 = d2;
		a2 = a[i];
		b2 = b[i];
		c2 = c[i];
		d2 = d[i];
		ret = compar_int_quads(a1, b1, c1, d1, a2, b2, c2, d2);
		if (ret == 0) {
			if (strict) return 0;
			continue;
		}
		if (desc != (ret > 0))
			return 0;
	}
	return 1;
}

void _get_order_of_int_quads(const int *a, const int *b,
		const int *c, const int *d, int nelt,
		int a_desc, int b_desc, int c_desc, int d_desc,
		int *out, int out_shift)
{
	int i;

	for (i = 0; i < nelt; i++)
		out[i] = i + out_shift;
	qsort4(out, nelt, a - out_shift, b - out_shift,
			  c - out_shift, d - out_shift,
			  a_desc, b_desc, c_desc, d_desc);
	return;
}

/* base: 0-based indices into 'a' and 'b'.
   rxbuf1, rxbuf2: NULL or user-allocated buffers of length 'base_len'. */
int _sort_int_quads(int *base, int base_len,
		const int *a, const int *b, const int *c, const int *d,
		int a_desc, int b_desc, int c_desc, int d_desc,
		int use_radix, unsigned short int *rxbuf1, int *rxbuf2)
{
	int qsort_cutoff, auto_rxbuf1, auto_rxbuf2;

	rxtargets[0] = a;
	rxtargets[1] = b;
	rxtargets[2] = c;
	rxtargets[3] = d;
	rxdescs[0] = a_desc;
	rxdescs[1] = b_desc;
	rxdescs[2] = c_desc;
	rxdescs[3] = d_desc;

	qsort_cutoff = (use_radix && can_use_rxsort()) ? 256 : base_len;
	if (lucky_sort_targets(base, base_len, rxtargets, rxdescs, 4,
			       qsort_cutoff))
		return 0;

	auto_rxbuf1 = rxbuf1 == NULL;
	if (auto_rxbuf1) {
		rxbuf1 = alloc_rxbuf1(base_len);
		if (rxbuf1 == NULL)
			return -1;
	}
	auto_rxbuf2 = rxbuf2 == NULL;
	if (auto_rxbuf2) {
		rxbuf2 = alloc_rxbuf2(base_len, rxbuf1, auto_rxbuf1);
		if (rxbuf2 == NULL)
			return -2;
	}

	last_rxlevel = 7;
	base_uidx_buf = rxbuf1;
	rxsort_rec(base, base_len, rxbuf2, 0, 0);

	if (auto_rxbuf2)
		free(rxbuf2);
	if (auto_rxbuf1)
		free(rxbuf1);
	return 0;
}

void _get_matches_of_ordered_int_quads(
		const int *a1, const int *b1, const int *c1, const int *d1,
		const int *o1, int nelt1,
		const int *a2, const int *b2, const int *c2, const int *d2,
		const int *o2, int nelt2,
		int nomatch, int *out, int out_shift)
{
	int i1, i2, ret;

	i2 = 0;
	ret = 0;
	for (i1 = 0; i1 < nelt1; i1++, o1++) {
		while (i2 < nelt2) {
			ret = compar_int_quads(
				a1[*o1], b1[*o1], c1[*o1], d1[*o1],
				a2[*o2], b2[*o2], c2[*o2], d2[*o2]);
			if (ret <= 0)
				break;
			i2++, o2++;
		}
		out[*o1] = ret == 0 ? *o2 + out_shift : nomatch;
	}
	return;
}

