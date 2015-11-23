
#define CHOLMOD_PATTERN 0	/* pattern only, no numerical values */
#define CHOLMOD_REAL 1		/* a real matrix */
#define CHOLMOD_COMPLEX 2	/* a complex matrix (ANSI C99 compatible) */
#define CHOLMOD_ZOMPLEX 3	/* a complex matrix (MATLAB compatible) */



typedef struct {
  void * p;
  void * i;
  void * j;
  void * x;
  void * z;
  int n;
} sparse_matrix;


test_int() {
  
  sparse_matrix m;
  m.p = 

}

int realloc_multiple(
    /* ---- input ---- */
    size_t nnew,	/* requested # of items in reallocated blocks */
    int nint,		/* number of int/SuiteSparse_long blocks */
    int xtype,		/* CHOLMOD_PATTERN, _REAL, _COMPLEX, or _ZOMPLEX */
    /* ---- in/out --- */
    void **Iblock,	/* int or SuiteSparse_long block */
    void **Jblock,	/* int or SuiteSparse_long block */
    void **Xblock,	/* complex or double block */
    void **Zblock,	/* zomplex case only: double block */
    size_t *nold_p,	/* current size of the I,J,X,Z blocks on input,
			              /* nnew on output if successful */
)
{
    double *xx, *zz ;
    size_t i, j, x, z, nold ;


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

int test() {
  
  sparse_matrix m;
  
  realloc_multiple(1000, 10, CHOLMOD_ZOMPLEX, &m.i, &m.j, &m.x, &m.z, &m.n );

  if(m.n != 1000) {
    return -1;
  }
}
