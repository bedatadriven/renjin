/* ========================================================================== */
/* === Core/cholmod_memory ================================================== */
/* ========================================================================== */

/* -----------------------------------------------------------------------------
 * CHOLMOD/Core Module.  Copyright (C) 2005-2013,
 * Univ. of Florida.  Author: Timothy A. Davis
 * The CHOLMOD/Core Module is licensed under Version 2.1 of the GNU
 * Lesser General Public License.  See lesser.txt for a text of the license.
 * CHOLMOD is also available under other licenses; contact authors for details.
 * -------------------------------------------------------------------------- */

/*----------------------------------------------------------------------------
 * Renjin-specific implementation
 * 
 * GCC-Bridge cannot compile a call to malloc() without type information.
 * Normally, the expected result type of a call to malloc() can be inferred
 * using simple data flow analysis, but not, at least at this time, if the
 * call to malloc is wrapped in another void-typed function. 
 * 
 * We primarily work around this limitation by using preproccessor defines
 * to replace calls to chmod_malloc, chmod_free, and chmod_realloc with 
 * calls to malloc(), free(), and realloc(), which GCC-Bridge will replace
 * typed array allocation at each call site depending on the inferred type.
 *
 * cholmod_realloc_multiple() is adapted to provide the compiler with explict
 * type information.
 *
 * As a result of this, cholmod's memory allocation statistics will not
 * be available.
 */
 
#include "cholmod_internal.h"
#include "cholmod_core.h"



/* ========================================================================== */
/* === cholmod_add_size_t =================================================== */
/* ========================================================================== */

/* Safely compute a+b, and check for integer overflow.  If overflow occurs,
 * return 0 and set OK to FALSE.  Also return 0 if OK is FALSE on input. */

size_t CHOLMOD(add_size_t) (size_t a, size_t b, int *ok)
{
    size_t s = a + b ;
    (*ok) = (*ok) && (s >= a) ;
    return ((*ok) ? s : 0) ;
}

/* ========================================================================== */
/* === cholmod_mult_size_t ================================================== */
/* ========================================================================== */

/* Safely compute a*k, where k should be small, and check for integer overflow.
 * If overflow occurs, return 0 and set OK to FALSE.  Also return 0 if OK is
 * FALSE on input. */

size_t CHOLMOD(mult_size_t) (size_t a, size_t k, int *ok)
{
    size_t p = 0, s ;
    while (*ok)
    {
	if (k % 2)
	{
	    p = p + a ;
	    (*ok) = (*ok) && (p >= a) ;
	}
	k = k / 2 ;
	if (!k) return (p) ;
	s = a + a ;
	(*ok) = (*ok) && (s >= a) ;
	a = s ;
    }
    return (0) ;
}


/* These functions exist soly to make it obvious to the compiler what
 * sort of array needs to be allocated
 */

double * malloc_double(int count) {
  return malloc(count * sizeof(double));
}

int * malloc_int(int count) {
  return malloc(count * sizeof(int));
}
//
//void *cholmod_realloc	/* returns pointer to reallocated block */
//(
//    /* ---- input ---- */
//    size_t nnew,	/* requested # of items in reallocated block */
//    size_t size,	/* size of each item */
//    /* ---- in/out --- */
//    void *p,		/* block of memory to realloc */
//    size_t *n,		/* current size on input, nnew on output if successful*/
//    /* --------------- */
//    cholmod_common *Common
//) {
//    
//    return NULL;
//
//}
//



/* ========================================================================== */
/* === cholmod_realloc_multiple ============================================= */
/* ========================================================================== */

/* reallocate multiple blocks of memory, all of the same size (up to two integer
 * and two real blocks).  Either reallocations all succeed, or all are returned
 * in the original size (they are freed if the original size is zero).  The nnew
 * blocks are of size 1 or more.
 */

int CHOLMOD(realloc_multiple)
(
    /* ---- input ---- */
    size_t nnew,	/* requested # of items in reallocated blocks */
    int nint,		/* number of int/SuiteSparse_long blocks */
    int xtype,		/* CHOLMOD_PATTERN, _REAL, _COMPLEX, or _ZOMPLEX */
    /* ---- in/out --- */
    Int **Iblock,	/* int or SuiteSparse_long block */
    Int **Jblock,	/* int or SuiteSparse_long block */
    double **Xblock,	/* complex or double block */
    double **Zblock,	/* zomplex case only: double block */
    size_t *nold_p,	/* current size of the I,J,X,Z blocks on input,
			 * nnew on output if successful */
    /* --------------- */
    cholmod_common *Common
)
{
    double *xx, *zz ;
    size_t i, j, x, z, nold ;

    RETURN_IF_NULL_COMMON (FALSE) ;

    if (xtype < CHOLMOD_PATTERN || xtype > CHOLMOD_ZOMPLEX)
    {
	ERROR (CHOLMOD_INVALID, "invalid xtype") ;
	return (FALSE) ;
    }

    nold = *nold_p ;

    if (nint < 1 && xtype == CHOLMOD_PATTERN)
    {
	/* nothing to do */
	return (TRUE) ;
    }

    i = nold ;
    j = nold ;
    x = nold ;
    z = nold ;

    if (nint > 0)
    {
	*Iblock = malloc_int(nnew);
    }
    if (nint > 1)
    {
	*Jblock = malloc_int(nnew);
    }

    switch (xtype)
    {
	case CHOLMOD_REAL:
	    *Xblock = malloc_double(nnew);
	    break ;

	case CHOLMOD_COMPLEX:
	    *Xblock = malloc_double(nnew*2);
	    break ;

	case CHOLMOD_ZOMPLEX:
	    *Xblock = malloc_double(nnew);
	    *Zblock = malloc_double(nnew);
	    break ;
    }

    /* all realloc's succeeded, change size to reflect realloc'ed size. */
    *nold_p = nnew ;
    return (TRUE) ;
}
