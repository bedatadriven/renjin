/****************************************************************************
 *            Low-level manipulation of SimpleList objects            
 ****************************************************************************/
#include "S4Vectors.h"

static SEXP listData_symbol = NULL;

static void set_SimpleList_listData(SEXP x, SEXP value)
{
  INIT_STATIC_SYMBOL(listData)
  SET_SLOT(x, listData_symbol, value);
  return;
}

SEXP _new_SimpleList(const char *classname, SEXP listData)
{
  SEXP classdef, ans;

  PROTECT(classdef = MAKE_CLASS(classname));
  PROTECT(ans = NEW_OBJECT(classdef));
  set_SimpleList_listData(ans, listData);
  UNPROTECT(2);
  return ans;
}
