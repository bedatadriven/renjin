#include "../inst/include/S4Vectors_defines.h"
#include <string.h>

#define INIT_STATIC_SYMBOL(NAME) \
{ \
	if (NAME ## _symbol == NULL) \
		NAME ## _symbol = install(# NAME); \
}


/* safe_arithm.c */

void _reset_ovflow_flag();

int _get_ovflow_flag();

int _safe_int_add(
	int x,
	int y
);

int _safe_int_mult(
	int x,
	int y
);


/* sort_utils.c */

void _sort_int_array(
	int *x,
	int nelt,
	int desc
);

void _get_order_of_int_array(
	const int *x,
	int nelt,
	int desc,
	int *out,
	int out_shift
);

int _sort_ints(
	int *base,
	int base_len,
	const int *x,
	int desc,
	int use_radix,
	unsigned short int *rxbuf1,
	int *rxbuf2
);

void _pcompare_int_pairs(
	const int *a1,
	const int *b1,
	int nelt1,
	const int *a2,
	const int *b2,
	int nelt2,
	int *out,
	int out_len,
	int with_warning
);

int _int_pairs_are_sorted(
	const int *a,
	const int *b,
	int nelt,
	int desc,
	int strict
);

void _get_order_of_int_pairs(
	const int *a,
	const int *b,
	int nelt,
	int a_desc,
	int b_desc,
	int *out,
	int out_shift
);

int _sort_int_pairs(
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

void _get_matches_of_ordered_int_pairs(
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

int _int_quads_are_sorted(
	const int *a,
	const int *b,
	const int *c,
	const int *d,
	int nelt,
	int desc,
	int strict
);

void _get_order_of_int_quads(
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

int _sort_int_quads(
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

void _get_matches_of_ordered_int_quads(
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


/* hash_utils.c */

struct htab _new_htab(int n);

int _get_hbucket_val(
	const struct htab *htab,
	int bucket_idx
);

void _set_hbucket_val(
	struct htab *htab,
	int bucket_idx,
	int val
);


/* AEbufs.c */

SEXP AEbufs_use_malloc(SEXP x);

int _get_new_buflength(int buflength);

int _IntAE_get_nelt(const IntAE *ae);

int _IntAE_set_nelt(
	IntAE *ae,
	int nelt
);

void _IntAE_set_val(
	const IntAE *ae,
	int val
);

void _IntAE_insert_at(
	IntAE *ae,
	int at,
	int val
);

IntAE *_new_IntAE(
	int buflength,
	int nelt,
	int val
);

void _IntAE_append(
	IntAE *ae,
	const int *newvals,
	int nnewval
);

void _IntAE_delete_at(
	IntAE *ae,
	int at
);

void _IntAE_shift(
	const IntAE *ae,
	int shift
);

void _IntAE_sum_and_shift(
	const IntAE *ae1,
	const IntAE *ae2,
	int shift
);

void _IntAE_append_shifted_vals(
	IntAE *ae,
	const int *newvals,
	int nnewval,
	int shift
);

void _IntAE_qsort(
	const IntAE *ae,
	int desc
);

void _IntAE_delete_adjdups(IntAE *ae);

SEXP _new_INTEGER_from_IntAE(const IntAE *ae);

IntAE *_new_IntAE_from_INTEGER(SEXP x);

IntAE *_new_IntAE_from_CHARACTER(
	SEXP x,
	int keyshift
);

int _IntAEAE_get_nelt(const IntAEAE *aeae);

int _IntAEAE_set_nelt(
	IntAEAE *aeae,
	int nelt
);

void _IntAEAE_insert_at(
	IntAEAE *aeae,
	int at,
	IntAE *ae
);

IntAEAE *_new_IntAEAE(
	int buflength,
	int nelt
);

void _IntAEAE_eltwise_append(
	const IntAEAE *aeae1,
	const IntAEAE *aeae2
);

void _IntAEAE_shift(
	const IntAEAE *aeae,
	int shift
);

void _IntAEAE_sum_and_shift(
	const IntAEAE *aeae1,
	const IntAEAE *aeae2,
	int shift
);

SEXP _new_LIST_from_IntAEAE(
	const IntAEAE *aeae,
	int mode
);

IntAEAE *_new_IntAEAE_from_LIST(SEXP x);

SEXP _IntAEAE_toEnvir(
	const IntAEAE *aeae,
	SEXP envir,
	int keyshift
);

int _IntPairAE_get_nelt(const IntPairAE *ae);

int _IntPairAE_set_nelt(
	IntPairAE *ae,
	int nelt
);

void _IntPairAE_insert_at(
	IntPairAE *ae,
	int at,
	int a,
	int b
);

IntPairAE *_new_IntPairAE(
	int buflength,
	int nelt
);

int _IntPairAEAE_get_nelt(const IntPairAEAE *aeae);

int _IntPairAEAE_set_nelt(
	IntPairAEAE *aeae,
	int nelt
);

void _IntPairAEAE_insert_at(
	IntPairAEAE *aeae,
	int at,
	IntPairAE *ae
);

IntPairAEAE *_new_IntPairAEAE(
	int buflength,
	int nelt
);

int _LLongAE_get_nelt(const LLongAE *ae);

int _LLongAE_set_nelt(
	LLongAE *ae,
	int nelt
);

void _LLongAE_set_val(
	const LLongAE *ae,
	long long val
);

void _LLongAE_insert_at(
	LLongAE *ae,
	int at,
	long long val
);

LLongAE *_new_LLongAE(
	int buflength,
	int nelt,
	long long val
);

int _CharAE_get_nelt(const CharAE *ae);

int _CharAE_set_nelt(
	CharAE *ae,
	int nelt
);

void _CharAE_insert_at(
	CharAE *ae,
	int at,
	char c
);

CharAE *_new_CharAE(int buflength);

CharAE *_new_CharAE_from_string(const char *string);

void _append_string_to_CharAE(
	CharAE *ae,
	const char *string
);

void _CharAE_delete_at(
	CharAE *ae,
	int at,
	int nelt
);

SEXP _new_RAW_from_CharAE(const CharAE *ae);

SEXP _new_LOGICAL_from_CharAE(const CharAE *ae);

int _CharAEAE_get_nelt(const CharAEAE *aeae);

int _CharAEAE_set_nelt(
	CharAEAE *aeae,
	int nelt
);

void _CharAEAE_insert_at(
	CharAEAE *aeae,
	int at,
	CharAE *ae
);

CharAEAE *_new_CharAEAE(
	int buflength,
	int nelt
);

void _append_string_to_CharAEAE(
	CharAEAE *aeae,
	const char *string
);

SEXP _new_CHARACTER_from_CharAEAE(const CharAEAE *aeae);

SEXP AEbufs_free();


/* SEXP_utils.c */

const char *_get_classname(SEXP x);


/* anyMissing.c */

SEXP anyMissing(SEXP x);


/* vector_utils.c */

int _vector_memcmp(
	SEXP x1,
	int x1_offset,
	SEXP x2,
	int x2_offset,
	int nelt
);

int _copy_vector_block(
	SEXP dest,
	int dest_offset,
	SEXP src,
	int src_offset,
	int block_width
);

int _copy_vector_ranges(
	SEXP dest,
	int dest_offset,
	SEXP src,
	const int *start,
	const int *width,
	int nranges
);

SEXP _subset_vectorORfactor_by_ranges(
	SEXP x,
	const int *start,
	const int *width,
	int nranges
);

SEXP vectorORfactor_extract_ranges(
	SEXP x,
	SEXP start,
	SEXP width
);

SEXP sapply_NROW(SEXP x);

SEXP _list_as_data_frame(
	SEXP x,
	int nrow
);


/* logical_utils.c */

SEXP logical_as_compact_bitvector(SEXP x);

SEXP compact_bitvector_as_logical(SEXP x, SEXP length_out);

SEXP subset_compact_bitvector(SEXP x, SEXP subscript);

SEXP compact_bitvector_bit_count(SEXP x);

SEXP compact_bitvector_last_bit(SEXP x);

SEXP compact_bitvector_set_op(SEXP query, SEXP ref, SEXP align);


/* int_utils.c */

SEXP Integer_any_missing_or_outside(SEXP x, SEXP lower, SEXP upper);

int _sum_non_neg_ints(
	const int *x,
	int x_len,
	const char *varname
);

SEXP Integer_sum_non_neg_vals(SEXP x);

SEXP Integer_diff_with_0(SEXP x);

SEXP Integer_diff_with_last(SEXP x, SEXP last);

SEXP Integer_order(
	SEXP x,
	SEXP decreasing,
	SEXP use_radix
);

int _check_integer_pairs(
	SEXP a,
	SEXP b,
	const int **a_p,
	const int **b_p,
	const char *a_argname,
	const char *b_argname
);

SEXP Integer_pcompare2(
	SEXP a1,
	SEXP b1,
	SEXP a2,
	SEXP b2
);

SEXP Integer_sorted2(
	SEXP a,
	SEXP b,
	SEXP decreasing,
	SEXP strictly
);

SEXP Integer_order2(
	SEXP a,
	SEXP b,
	SEXP decreasing,
	SEXP use_radix
);

SEXP Integer_match2_quick(
	SEXP a1,
	SEXP b1,
	SEXP a2,
	SEXP b2,
	SEXP nomatch
);

SEXP Integer_selfmatch2_quick(
	SEXP a,
	SEXP b
);

SEXP Integer_match2_hash(
	SEXP a1,
	SEXP b1,
	SEXP a2,
	SEXP b2,
	SEXP nomatch
);

SEXP Integer_selfmatch2_hash(
	SEXP a,
	SEXP b
);

int _check_integer_quads(
	SEXP a,
	SEXP b,
	SEXP c,
	SEXP d,
	const int **a_p,
	const int **b_p,
	const int **c_p,
	const int **d_p,
	const char *a_argname,
	const char *b_argname,
	const char *c_argname,
	const char *d_argname
);

SEXP Integer_sorted4(
	SEXP a,
	SEXP b,
	SEXP c,
	SEXP d,
	SEXP decreasing,
	SEXP strictly
);

SEXP Integer_order4(
	SEXP a,
	SEXP b,
	SEXP c,
	SEXP d,
	SEXP decreasing,
	SEXP use_radix
);

SEXP Integer_match4_quick(
	SEXP a1,
	SEXP b1,
	SEXP c1,
	SEXP d1,
	SEXP a2,
	SEXP b2,
	SEXP c2,
	SEXP d2,
	SEXP nomatch
);

SEXP Integer_selfmatch4_quick(
	SEXP a,
	SEXP b,
	SEXP c,
	SEXP d
);

SEXP Integer_match4_hash(
	SEXP a1,
	SEXP b1,
	SEXP c1,
	SEXP d1,
	SEXP a2,
	SEXP b2,
	SEXP c2,
	SEXP d2,
	SEXP nomatch
);

SEXP Integer_selfmatch4_hash(
	SEXP a,
	SEXP b,
	SEXP c,
	SEXP d
);

SEXP Integer_tabulate2(
	SEXP x,
	SEXP nbins,
	SEXP weight,
	SEXP strict
);

SEXP Integer_explode_bits(
	SEXP x,
	SEXP bitpos
);

SEXP Integer_sorted_merge(
	SEXP x,
	SEXP y
);

SEXP Integer_mseq(
	SEXP from,
	SEXP to
);

SEXP Integer_fancy_mseq(
	SEXP lengths,
	SEXP offset,
	SEXP rev
);

SEXP _find_interv_and_start_from_width(
	const int *x,
	int x_len,
	const int *width,
	int width_len
);

SEXP findIntervalAndStartFromWidth(
	SEXP x,
	SEXP vec
);


/* str_utils.c */

SEXP unstrsplit_list(SEXP x, SEXP sep);

SEXP safe_strexplode(SEXP s);

SEXP strsplit_as_list_of_ints(SEXP x, SEXP sep);

SEXP svn_time();


/* eval_utils.c */

SEXP top_prenv(SEXP nm, SEXP env);

SEXP top_prenv_dots(SEXP env);


/* Hits_class.c */

SEXP _new_Hits(
	int *from,
	const int *to,
	int nhit,
	int nLnode,
	int nRnode,
	int already_sorted
);

SEXP Hits_new(
	SEXP Class,
	SEXP from,
	SEXP to,
	SEXP nLnode,
	SEXP nRnode,
	SEXP revmap_envir
);

int _get_select_mode(SEXP select);

SEXP select_hits(
	SEXP from,
	SEXP to,
	SEXP nLnode,
	SEXP select
);

SEXP make_all_group_inner_hits(
	SEXP group_sizes,
	SEXP hit_type
);


/* Rle_class.c */

SEXP _construct_logical_Rle(
	const int *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP _construct_integer_Rle(
	const int *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP _construct_numeric_Rle(
	const double *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP _construct_complex_Rle(
	const Rcomplex *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP _construct_character_Rle(
	SEXP values,
	const int *lengths,
	int buflength
);

SEXP _construct_raw_Rle(
	const Rbyte *values,
	int nvalues,
	const int *lengths,
	int buflength
);

SEXP _construct_Rle(
	SEXP values,
	const int *lengths,
	int buflength
);

SEXP Rle_constructor(
	SEXP values,
	SEXP lengths,
	SEXP check,
	SEXP buflength
);

SEXP Rle_start(SEXP x);

SEXP Rle_end(SEXP x);

SEXP ranges_to_runs_mapper(
	SEXP run_lengths,
	SEXP start,
	SEXP width,
	SEXP method
);

SEXP Rle_extract_range(
	SEXP x,
	SEXP start,
	SEXP end
);

SEXP _subset_Rle_by_ranges(
	SEXP x,
	const int *start,
	const int *width,
	int nranges,
	int method,
	int as_list
);

SEXP Rle_extract_ranges(
	SEXP x,
	SEXP start,
	SEXP width,
	SEXP method,
	SEXP as_list
);

SEXP Rle_getStartEndRunAndOffset(
	SEXP x,
	SEXP start,
	SEXP end
);

SEXP Rle_window_aslist(
	SEXP x,
	SEXP runStart,
	SEXP runEnd,
	SEXP offsetStart,
	SEXP offsetEnd
);


/* Rle_utils.c */

SEXP Rle_runsum(
	SEXP x,
	SEXP k,
	SEXP na_rm
);

SEXP Rle_runwtsum(
	SEXP x,
	SEXP k,
	SEXP wt,
	SEXP na_rm
);

SEXP Rle_runq(
	SEXP x,
	SEXP k,
	SEXP which,
	SEXP na_rm
);


/* List_class.c */

const char *_get_List_elementType(SEXP x);

void _set_List_elementType(
	SEXP x,
	const char *type
);


/* SimpleList_class.c */

SEXP _new_SimpleList(
	const char *classname,
	SEXP listData
);


/* DataFrame_class.c */

SEXP _new_DataFrame(
	const char *classname,
	SEXP vars,
	SEXP rownames,
	SEXP nrows
);

