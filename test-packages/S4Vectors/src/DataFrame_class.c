/****************************************************************************
 *            Low-level manipulation of DataFrame objects            
 ****************************************************************************/
#include "S4Vectors.h"

static SEXP rownames_symbol = NULL, nrows_symbol = NULL;

static void set_DataFrame_rownames(SEXP x, SEXP value) {
  INIT_STATIC_SYMBOL(rownames)
  SET_SLOT(x, rownames_symbol, value);
}

static void set_DataFrame_nrows(SEXP x, SEXP value) {
  INIT_STATIC_SYMBOL(nrows)
  SET_SLOT(x, nrows_symbol, value);
}

SEXP _new_DataFrame(const char *classname, SEXP vars, SEXP rownames, SEXP nrows)
{
  SEXP ans;
  PROTECT(ans = _new_SimpleList(classname, vars));
  set_DataFrame_rownames(ans, rownames);
  set_DataFrame_nrows(ans, nrows);
  UNPROTECT(1);
  return ans;
}

