/*****************************************************************************
 S4Vectors C interface: prototypes
 ---------------------------------

   The S4Vectors C interface is split in 2 files:
     1. S4Vectors_defines.h (in this directory): contains the typedefs and
        defines of the interface.
     2. S4Vectors_interface.h (this file): contains the prototypes of the
        S4Vectors C routines that are part of the interface.

 *****************************************************************************/
#include "S4Vectors_defines.h"


/*
 * Safe signed integer arithmetic.
 * (see safe_arithm.c)
 */

void reset_ovflow_flag();

int get_ovflow_flag();

int safe_int_add(
	int x,
	int y
);

int safe_int_mult(
	int x,
	int y
);

/*
 * Low-level sorting utilities.
 * (see sort_utils.c)
 */

void sort_int_array(
	int *x,
	int nelt,
	int desc
);

void get_order_of_int_array(
	const int *x,
	int nelt,
	int desc,
	int *out,
	int out_shift
);

int sort_ints(
	int *base,
	int base_len,
	const int *x,
	int desc,
	int use_radix,
	unsigned short int *rxbuf1,
	int *rxbuf2
);

void get_order_of_int_pairs(
	const int *a,
	const int *b,
	int nelt,
	int a_desc,
	int b_desc,
	int *out,
	int out_shift
);

int sort_int_pairs(
	int *base,
	int base_len,
	const int *a,
	const int *b,
	int a_desc,
	int b_desc,
	int use_radix,
	unsigned short int *rxbuf1,
	int *rxbuf2
);

void get_matches_of_ordered_int_pairs(
	const int *a1,
	const int *b1,
	const int *o1,
	int nelt1,
	const int *a2,
	const int *b2,
	const int *o2,
	int nelt2,
	int nomatch,
	int *out,
	int out_shift
);

void get_order_of_int_quads(
	const int *a,
	const int *b,
	const int *c,
	const int *d,
	int nelt,
	int a_desc,
	int b_desc,
	int c_desc,
	int d_desc,
	int *out,
	int out_shift
);

int sort_int_quads(
	int *base,
	int base_len,
	const int *a,
	const int *b,
	const int *c,
	const int *d,
	int a_desc,
	int b_desc,
	int c_desc,
	int d_desc,
	int use_radix,
	unsigned short int *rxbuf1,
	int *rxbuf2
);

void get_matches_of_ordered_int_quads(
	const int *a1,
	const int *b1,
	const int *c1,
	const int *d1,
	const int *o1,
	int nelt1,
	const int *a2,
	const int *b2,
	const int *c2,
	const int *d2,
	const int *o2,
	int nelt2,
	int nomatch,
	int *out,
	int out_shift
);

/*
 * Hash table management.
 * (see hash_utils.c)
 */

struct htab new_htab(int n);

int get_hbucket_val(
	const struct htab *htab,
	int bucket_idx
);

void set_hbucket_val(
	struct htab *htab,
	int bucket_idx,
	int val
);

/*
 * Low-level manipulation of the Auto-Extending buffers.
 * (see AEbufs.c)
 */

int get_new_buflength(int buflength);

int IntAE_get_nelt(const IntAE *ae);

int IntAE_set_nelt(
	IntAE *ae,
	int nelt
);

void IntAE_set_val(
	const IntAE *ae,
	int val
);

void IntAE_insert_at(
	IntAE *ae,
	int at,
	int val
);

IntAE *new_IntAE(
	int buflength,
	int nelt,
	int val
);

void IntAE_append(
	IntAE *ae,
	const int *newvals,
	int nnewval
);

void IntAE_delete_at(
	IntAE *ae,
	int at
);

void IntAE_shift(
	const IntAE *ae,
	int shift
);

void IntAE_sum_and_shift(
	const IntAE *ae1,
	const IntAE *ae2,
	int shift
);

void IntAE_append_shifted_vals(
	IntAE *ae,
	const int *newvals,
	int nnewval,
	int shift
);

void IntAE_qsort(
	const IntAE *ae,
	int desc
);

void IntAE_delete_adjdups(IntAE *ae);

SEXP new_INTEGER_from_IntAE(const IntAE *ae);

IntAE *new_IntAE_from_INTEGER(SEXP x);

IntAE *new_IntAE_from_CHARACTER(
	SEXP x,
	int keyshift
);

int IntAEAE_get_nelt(const IntAEAE *aeae);

int IntAEAE_set_nelt(
	IntAEAE *aeae,
	int nelt
);

void IntAEAE_insert_at(
	IntAEAE *aeae,
	int at,
	IntAE *ae
);

IntAEAE *new_IntAEAE(
	int buflength,
	int nelt
);

void IntAEAE_eltwise_append(
	const IntAEAE *aeae1,
	const IntAEAE *aeae2
);

void IntAEAE_shift(
	const IntAEAE *aeae,
	int shift
);

void IntAEAE_sum_and_shift(
	const IntAEAE *aeae1,
	const IntAEAE *aeae2,
	int shift
);

SEXP new_LIST_from_IntAEAE(
	const IntAEAE *aeae,
	int mode
);

IntAEAE *new_IntAEAE_from_LIST(SEXP x);

SEXP IntAEAE_toEnvir(
	const IntAEAE *aeae,
	SEXP envir,
	int keyshift
);

int IntPairAE_get_nelt(const IntPairAE *ae);

int IntPairAE_set_nelt(
	IntPairAE *ae,
	int nelt
);

void IntPairAE_insert_at(
	IntPairAE *ae,
	int at,
	int a,
	int b
);

IntPairAE *new_IntPairAE(
	int buflength,
	int nelt
);

int IntPairAEAE_get_nelt(const IntPairAEAE *aeae);

int IntPairAEAE_set_nelt(
	IntPairAEAE *aeae,
	int nelt
);

void IntPairAEAE_insert_at(
	IntPairAEAE *aeae,
	int at,
	IntPairAE *ae
);

IntPairAEAE *new_IntPairAEAE(
	int buflength,
	int nelt
);

int LLongAE_get_nelt(const LLongAE *ae);

int LLongAE_set_nelt(
	LLongAE *ae,
	int nelt
);

void LLongAE_set_val(
	const LLongAE *ae,
	long long val
);

void LLongAE_insert_at(
	LLongAE *ae,
	int at,
	long long val
);

LLongAE *new_LLongAE(
	int buflength,
	int nelt,
	long long val
);

int CharAE_get_nelt(const CharAE *ae);

int CharAE_set_nelt(
	CharAE *ae,
	int nelt
);

void CharAE_insert_at(
	CharAE *ae,
	int at,
	char c
);

CharAE *new_CharAE(int buflength);

CharAE *new_CharAE_from_string(const char *string);

void append_string_to_CharAE(
	CharAE *ae,
	const char *string
);

void CharAE_delete_at(
	CharAE *ae,
	int at,
	int nelt
);

SEXP new_RAW_from_CharAE(const CharAE *ae);

SEXP new_LOGICAL_from_CharAE(const CharAE *ae);

int CharAEAE_get_nelt(const CharAEAE *aeae);

int CharAEAE_set_nelt(
	CharAEAE *aeae,
	int nelt
);

void CharAEAE_insert_at(
	CharAEAE *aeae,
	int at,
	CharAE *ae
);

CharAEAE *new_CharAEAE(
	int buflength,
	int nelt
);

void append_string_to_CharAEAE(
	CharAEAE *aeae,
	const char *string
);

SEXP new_CHARACTER_from_CharAEAE(const CharAEAE *aeae);

/*
 * SEXP_utils.c
 */

const char *get_classname(SEXP x);

/*
 * vector_utils.c
 */

int vector_memcmp(
	SEXP x1,
	int x1_offset,
	SEXP x2,
	int x2_offset,
	int nelt
);

int copy_vector_block(
	SEXP dest,
	int dest_offset,
	SEXP src,
	int src_offset,
	int block_width
);

int copy_vector_ranges(
	SEXP dest,
	int dest_offset,
	SEXP src,
	const int *start,
	const int *width,
	int nranges
);

SEXP list_as_data_frame(
	SEXP x,
	int nrow
);

/*
 * int_utils.c
 */

int sum_non_neg_ints(
	const int *x,
	int x_len,
	const char *varname
);

int check_integer_pairs(
	SEXP a,
	SEXP b,
	const int **a_p,
	const int **b_p,
	const char *a_argname,
	const char *b_argname
);

SEXP find_interv_and_start_from_width(
	const int *x,
	int x_len,
	const int *width,
	int width_len
);

/*
 * Low-level manipulation of Hits objects.
 * (see Hits_class.c)
 */

SEXP new_Hits(
	int *from,
	const int *to,
	int nhit,
	int nLnode,
	int nRnode,
	int already_sorted
);

int get_select_mode(SEXP select);

/*
 * Low-level manipulation of Rle objects.
 * (see Rle_class.c)
 */

SEXP construct_logical_Rle(
	const int *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP construct_integer_Rle(
	const int *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP construct_numeric_Rle(
	const double *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP construct_complex_Rle(
	const Rcomplex *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP construct_character_Rle(
	SEXP values,
	const int *lengths,
	int buflength
);

SEXP construct_raw_Rle(
	const Rbyte *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP construct_Rle(
	SEXP values,
	const int *lengths,
	int buflength
);

/*
 * Low-level manipulation of Vector objects.
 * (see List_class.c)
 */

const char *get_List_elementType(SEXP x);

void set_List_elementType(SEXP x, const char *type);

/*
 * Low-level manipulation of SimpleList objects.
 * (see SimpleList_class.c)
 */

SEXP new_SimpleList(const char *classname, SEXP listData);

/*
 * Low-level manipulation of DataFrame objects.
 * (see DataFrame_class.c)
 */

SEXP new_DataFrame(const char *classname, SEXP vars, SEXP rownames, SEXP nrows);

