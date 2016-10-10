#include "S4Vectors.h"

#define CALLMETHOD_DEF(fun, numArgs) {#fun, (DL_FUNC) &fun, numArgs}

#define REGISTER_CCALLABLE(fun) \
	R_RegisterCCallable("S4Vectors", #fun, (DL_FUNC) &fun)


static const R_CallMethodDef callMethods[] = {

/* AEbufs.c */
	CALLMETHOD_DEF(AEbufs_use_malloc, 1),
	CALLMETHOD_DEF(AEbufs_free, 0),

/* anyMissing.c */
	CALLMETHOD_DEF(anyMissing, 1),

/* vector_utils.c */
	CALLMETHOD_DEF(vectorORfactor_extract_ranges, 3),
	CALLMETHOD_DEF(sapply_NROW, 1),

/* logical_utils.c */
	CALLMETHOD_DEF(logical_as_compact_bitvector, 1),
	CALLMETHOD_DEF(compact_bitvector_as_logical, 2),
	CALLMETHOD_DEF(subset_compact_bitvector, 2),
	CALLMETHOD_DEF(compact_bitvector_bit_count, 1),
	CALLMETHOD_DEF(compact_bitvector_last_bit, 1),
	CALLMETHOD_DEF(compact_bitvector_set_op, 3),

/* int_utils.c */
	CALLMETHOD_DEF(Integer_any_missing_or_outside, 3),
	CALLMETHOD_DEF(Integer_sum_non_neg_vals, 1),
	CALLMETHOD_DEF(Integer_diff_with_0, 1),
	CALLMETHOD_DEF(Integer_diff_with_last, 2),
	CALLMETHOD_DEF(Integer_order, 3),
	CALLMETHOD_DEF(Integer_pcompare2, 4),
	CALLMETHOD_DEF(Integer_sorted2, 4),
	CALLMETHOD_DEF(Integer_order2, 4),
	CALLMETHOD_DEF(Integer_match2_quick, 5),
	CALLMETHOD_DEF(Integer_selfmatch2_quick, 2),
	CALLMETHOD_DEF(Integer_match2_hash, 5),
	CALLMETHOD_DEF(Integer_selfmatch2_hash, 2),
	CALLMETHOD_DEF(Integer_sorted4, 6),
	CALLMETHOD_DEF(Integer_order4, 6),
	CALLMETHOD_DEF(Integer_match4_quick, 9),
	CALLMETHOD_DEF(Integer_selfmatch4_quick, 4),
	CALLMETHOD_DEF(Integer_match4_hash, 9),
	CALLMETHOD_DEF(Integer_selfmatch4_hash, 4),
	CALLMETHOD_DEF(Integer_tabulate2, 4),
	CALLMETHOD_DEF(Integer_explode_bits, 2),
	CALLMETHOD_DEF(Integer_sorted_merge, 2),
	CALLMETHOD_DEF(Integer_mseq, 2),
	CALLMETHOD_DEF(Integer_fancy_mseq, 3),
	CALLMETHOD_DEF(findIntervalAndStartFromWidth, 2),

/* str_utils.c */
	CALLMETHOD_DEF(unstrsplit_list, 2),
	CALLMETHOD_DEF(safe_strexplode, 1),
	CALLMETHOD_DEF(strsplit_as_list_of_ints, 2),
	CALLMETHOD_DEF(svn_time, 0),

/* eval_utils.c */
	CALLMETHOD_DEF(top_prenv, 2),
	CALLMETHOD_DEF(top_prenv_dots, 1),

/* Hits_class.c */
	CALLMETHOD_DEF(Hits_new, 6),
	CALLMETHOD_DEF(select_hits, 4),
	CALLMETHOD_DEF(make_all_group_inner_hits, 2),

/* Rle_class.c */
	CALLMETHOD_DEF(Rle_constructor, 4),
	CALLMETHOD_DEF(Rle_start, 1),
	CALLMETHOD_DEF(Rle_end, 1),
	CALLMETHOD_DEF(ranges_to_runs_mapper, 4),
	CALLMETHOD_DEF(Rle_extract_range, 3),
	CALLMETHOD_DEF(Rle_extract_ranges, 5),
	CALLMETHOD_DEF(Rle_getStartEndRunAndOffset, 3),
	CALLMETHOD_DEF(Rle_window_aslist, 5),

/* Rle_utils.c */
	CALLMETHOD_DEF(Rle_runsum, 3),
	CALLMETHOD_DEF(Rle_runwtsum, 4),
	CALLMETHOD_DEF(Rle_runq, 4),

	{NULL, NULL, 0}
};


void R_init_S4Vectors(DllInfo *info)
{
	R_registerRoutines(info, NULL, callMethods, NULL, NULL);

/* safe_arithm.c */
	REGISTER_CCALLABLE(_reset_ovflow_flag);
	REGISTER_CCALLABLE(_get_ovflow_flag);
	REGISTER_CCALLABLE(_safe_int_add);
	REGISTER_CCALLABLE(_safe_int_mult);

/* sort_utils.c */
	REGISTER_CCALLABLE(_sort_ints);
	REGISTER_CCALLABLE(_get_order_of_int_array);
	REGISTER_CCALLABLE(_sort_int_array);
	REGISTER_CCALLABLE(_get_order_of_int_pairs);
	REGISTER_CCALLABLE(_sort_int_pairs);
	REGISTER_CCALLABLE(_get_matches_of_ordered_int_pairs);
	REGISTER_CCALLABLE(_get_order_of_int_quads);
	REGISTER_CCALLABLE(_get_matches_of_ordered_int_quads);

/* hash_utils.c */
	REGISTER_CCALLABLE(_new_htab);
	REGISTER_CCALLABLE(_get_hbucket_val);
	REGISTER_CCALLABLE(_set_hbucket_val);

/* AEbufs.c */
	REGISTER_CCALLABLE(_get_new_buflength);
	REGISTER_CCALLABLE(_IntAE_get_nelt);
	REGISTER_CCALLABLE(_IntAE_set_nelt);
	REGISTER_CCALLABLE(_IntAE_set_val);
	REGISTER_CCALLABLE(_IntAE_insert_at);
	REGISTER_CCALLABLE(_new_IntAE);
	REGISTER_CCALLABLE(_IntAE_append);
	REGISTER_CCALLABLE(_IntAE_delete_at);
	REGISTER_CCALLABLE(_IntAE_shift);
	REGISTER_CCALLABLE(_IntAE_sum_and_shift);
	REGISTER_CCALLABLE(_IntAE_append_shifted_vals);
	REGISTER_CCALLABLE(_IntAE_qsort);
	REGISTER_CCALLABLE(_IntAE_delete_adjdups);
	REGISTER_CCALLABLE(_new_INTEGER_from_IntAE);
	REGISTER_CCALLABLE(_new_IntAE_from_INTEGER);
	REGISTER_CCALLABLE(_new_IntAE_from_CHARACTER);
	REGISTER_CCALLABLE(_IntAEAE_get_nelt);
	REGISTER_CCALLABLE(_IntAEAE_set_nelt);
	REGISTER_CCALLABLE(_IntAEAE_insert_at);
	REGISTER_CCALLABLE(_new_IntAEAE);
	REGISTER_CCALLABLE(_IntAEAE_eltwise_append);
	REGISTER_CCALLABLE(_IntAEAE_shift);
	REGISTER_CCALLABLE(_IntAEAE_sum_and_shift);
	REGISTER_CCALLABLE(_new_LIST_from_IntAEAE);
	REGISTER_CCALLABLE(_new_IntAEAE_from_LIST);
	REGISTER_CCALLABLE(_IntAEAE_toEnvir);
	REGISTER_CCALLABLE(_IntPairAE_get_nelt);
	REGISTER_CCALLABLE(_IntPairAE_set_nelt);
	REGISTER_CCALLABLE(_IntPairAE_insert_at);
	REGISTER_CCALLABLE(_new_IntPairAE);
	REGISTER_CCALLABLE(_IntPairAEAE_get_nelt);
	REGISTER_CCALLABLE(_IntPairAEAE_set_nelt);
	REGISTER_CCALLABLE(_IntPairAEAE_insert_at);
	REGISTER_CCALLABLE(_new_IntPairAEAE);
	REGISTER_CCALLABLE(_LLongAE_get_nelt);
	REGISTER_CCALLABLE(_LLongAE_set_nelt);
	REGISTER_CCALLABLE(_LLongAE_set_val);
	REGISTER_CCALLABLE(_LLongAE_insert_at);
	REGISTER_CCALLABLE(_new_LLongAE);
	REGISTER_CCALLABLE(_CharAE_get_nelt);
	REGISTER_CCALLABLE(_CharAE_set_nelt);
	REGISTER_CCALLABLE(_CharAE_insert_at);
	REGISTER_CCALLABLE(_new_CharAE);
	REGISTER_CCALLABLE(_new_CharAE_from_string);
	REGISTER_CCALLABLE(_append_string_to_CharAE);
	REGISTER_CCALLABLE(_CharAE_delete_at);
	REGISTER_CCALLABLE(_new_RAW_from_CharAE);
	REGISTER_CCALLABLE(_new_LOGICAL_from_CharAE);
	REGISTER_CCALLABLE(_CharAEAE_get_nelt);
	REGISTER_CCALLABLE(_CharAEAE_set_nelt);
	REGISTER_CCALLABLE(_CharAEAE_insert_at);
	REGISTER_CCALLABLE(_new_CharAEAE);
	REGISTER_CCALLABLE(_append_string_to_CharAEAE);
	REGISTER_CCALLABLE(_new_CHARACTER_from_CharAEAE);

/* SEXP_utils.c */
	REGISTER_CCALLABLE(_get_classname);

/* vector_utils.c */
	REGISTER_CCALLABLE(_vector_memcmp);
	REGISTER_CCALLABLE(_copy_vector_block);
	REGISTER_CCALLABLE(_copy_vector_ranges);
	REGISTER_CCALLABLE(_list_as_data_frame);

/* int_utils.c */
	REGISTER_CCALLABLE(_sum_non_neg_ints);
	REGISTER_CCALLABLE(_check_integer_pairs);
	REGISTER_CCALLABLE(_find_interv_and_start_from_width);

/* Hits_class.c */
	REGISTER_CCALLABLE(_new_Hits);
	REGISTER_CCALLABLE(_get_select_mode);

/* Rle_class.c */
	REGISTER_CCALLABLE(_construct_logical_Rle);
	REGISTER_CCALLABLE(_construct_integer_Rle);
	REGISTER_CCALLABLE(_construct_numeric_Rle);
	REGISTER_CCALLABLE(_construct_complex_Rle);
	REGISTER_CCALLABLE(_construct_character_Rle);
	REGISTER_CCALLABLE(_construct_raw_Rle);
	REGISTER_CCALLABLE(_construct_Rle);

/* List_class.c */
	REGISTER_CCALLABLE(_get_List_elementType);
	REGISTER_CCALLABLE(_set_List_elementType);

/* SimpleList_class.c */
	REGISTER_CCALLABLE(_new_SimpleList);

/* DataFrame_class.c */
	REGISTER_CCALLABLE(_new_DataFrame);

	return;
}

