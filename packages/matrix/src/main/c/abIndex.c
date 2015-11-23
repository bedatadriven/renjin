/** @file abIndex.c
 * C-level Methods for the ``abstract Index'' class
 *
 * Note: this heavily builds on ideas and code from  Jens Oehlschlaegel,
 * ----  as implemented (in the GPL'ed part of) package 'ff'.
 */

#include "abIndex.h"

/**
 * RLE (Run Length Encoding) -- only when it's worth
 *
 * @param x  R vector   which can be coerced to "integer"
 *
 * @return NULL or a valid R object of class "rle"
 */
#define _rle_d_
#include "t_Matrix_rle.c"
#undef _rle_d_

#define _rle_i_
#include "t_Matrix_rle.c"
#undef _rle_i_
