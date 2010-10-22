/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang.internal.c;

import com.google.common.base.Preconditions;
import r.lang.*;
import r.util.ArgChecker;

import static r.lang.internal.c.Error._;
import static r.lang.internal.c.Error.error;

/**
 * Macros and static methods defined originally in {@code RInternals.h}
 */
public class RInternals {

  protected RInternals() {
  }

  public static final NilExp R_NilValue = NilExp.INSTANCE;
  public static final SymbolExp R_MissingArg = SymbolExp.MISSING_ARG;
  public static final SymbolExp R_UnboundValue = SymbolExp.UNBOUND_VALUE;

  public static final Object NULL = null;

  public static final String NA_STRING = null;


  public static double R_atof(String text) {
    if (text.startsWith("0x")) {
      return Integer.parseInt(text.substring(2), 16);
    } else {
      return Double.parseDouble(text);
    }
  }

  //#define DATAPTR(x)	(((SEXPREC_ALIGN *) (x)) + 1)

  public static CharExp STRING_ELT(SEXP x, int i) {
    StringExp vector = ArgChecker.instanceOf(x, StringExp.class);
    return new CharExp(vector.get(i));
  }

  public static String CHAR(CharExp s) {
    return s.getValue();
  }

  public static CharExp PRINTNAME(SEXP s) {
    return new CharExp(((SymbolExp) s).getPrintName());
  }


//#define LOGICAL(x)	((int *) DATAPTR(x))
//#define INTEGER(x)	((int *) DATAPTR(x))
//#define RAW(x)		((Rbyte *) DATAPTR(x))
//#define COMPLEX(x)	((Rcomplex *) DATAPTR(x))
//#define REAL(x)		((double *) DATAPTR(x))
//#define STRING_ELT(x,i)	((SEXP *) DATAPTR(x))[i]
//#define VECTOR_ELT(x,i)	((SEXP *) DATAPTR(x))[i]
//##define STRING_PTR(x)	((SEXP *) DATAPTR(x))
//#define VECTOR_PTR(x)	((SEXP *) DATAPTR(x))


  //#define LISTVAL(x)	((x)->u.listsxp)

  public static SEXP TAG(SEXP e) {
    ArgChecker.notNull(e);
    return e.getTag();
  }

  /**
   * Returns the value of the given list node
   *
   * @param listExp a ListExp node (must be of ListExp type)
   * @return the value of this list node
   */
  public static SEXP CAR(SEXP listExp) {
    ListExp typedList = ArgChecker.instanceOf(listExp, ListExp.class);

    return typedList.getValue();
  }

  /**
   * Returns the next node in the linked list
   *
   * @param listExp a SEXP of type ListExp
   * @return the next node in the list, or R_NilValue
   */
  public static SEXP CDR(SEXP listExp) {
    ListExp list = ArgChecker.instanceOf(listExp, ListExp.class);

    return list.hasNextNode() ? list.getNextNode() : R_NilValue;
  }

  public static SEXP CAAR(SEXP e) {
    return CAR(CDR(e));
  }

  public static SEXP CDAR(SEXP e) {
    return CDR(CAR(e));
  }

  public static SEXP CADR(SEXP e) {
    return CAR(CDR(e));
  }

  public static SEXP CDDR(SEXP e) {
    return CDR(CDR(e));
  }

  public static SEXP CADDR(SEXP e) {
    return CAR(CDR(CDR(e)));
  }

  public static SEXP CADDDR(SEXP e) {
    return CAR(CDR(CDR(CDR(e))));
  }

  public static SEXP CAD4R(SEXP e) {
    return CAR(CDR(CDR(CDR(CDR(e)))));
  }


  /**
   * Sets the value of the given list node
   *
   * @param x the list node (must be ListExp)
   * @param y the value to set
   * @return the value y
   */
  public static SEXP SETCAR(SEXP x, SEXP y) {

    if (x == NULL || x == R_NilValue)
      error(_("bad value"));
    CHECK_OLD_TO_NEW(x, y);
    ((ListExp) x).setValue(y);
    return y;
  }

  public static SEXP SETCDR(SEXP x, SEXP y) {
    if (x == NULL || x == R_NilValue)
      error(_("bad value"));
    CHECK_OLD_TO_NEW(x, y);
    ((ListExp) x).setNextNode((ListExp) y);
    return y;
  }

  public static SEXP SETCADR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == NULL || x == R_NilValue ||
        CDR(x) == NULL || CDR(x) == R_NilValue)
      error(_("bad value"));
    cell = CDR(x);
    CHECK_OLD_TO_NEW(cell, y);
    ((ListExp) cell).setNextNode((ListExp) y);
    return y;
  }

  public static SEXP SETCADDR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == NULL || x == R_NilValue ||
        CDR(x) == NULL || CDR(x) == R_NilValue ||
        CDDR(x) == NULL || CDDR(x) == R_NilValue)
      error(_("bad value"));
    cell = CDDR(x);
    CHECK_OLD_TO_NEW(cell, y);
    ((ListExp) cell).setValue(y);
    return y;
  }

  public static SEXP CDDDR(SEXP x) {
    return CDR(CDR(CDR(x)));
  }

  public static SEXP SETCADDDR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == NULL || x == R_NilValue ||
        CDR(x) == NULL || CDR(x) == R_NilValue ||
        CDDR(x) == NULL || CDDR(x) == R_NilValue ||
        CDDDR(x) == NULL || CDDDR(x) == R_NilValue)
      error(_("bad value"));
    cell = CDDDR(x);
    CHECK_OLD_TO_NEW(cell, y);
    ((ListExp) cell).setValue(y);
    return y;
  }

  public static SEXP CD4R(SEXP x) {
    return CDR(CDR(CDR(CDR(x))));
  }

  public static SEXP SETCAD4R(SEXP x, SEXP y) {
    SEXP cell;
    if (x == NULL || x == R_NilValue ||
        CDR(x) == NULL || CDR(x) == R_NilValue ||
        CDDR(x) == NULL || CDDR(x) == R_NilValue ||
        CDDDR(x) == NULL || CDDDR(x) == R_NilValue ||
        CD4R(x) == NULL || CD4R(x) == R_NilValue)
      error(_("bad value"));
    cell = CD4R(x);
    CHECK_OLD_TO_NEW(cell, y);
    ((ListExp) cell).setValue(y);
    return y;
  }

  public static void CHECK_OLD_TO_NEW(SEXP x, SEXP y) {
    /** Do nothing, JVM is handling GC */
  }

  /**
   * Marks the SEXP as inelligable for garbage collection.
   * This is a NO-OP in java as we're letting the JVM handle memory management
   *
   * @param s
   */
  public static void PROTECT(SEXP s) {
    /** NO OP -- JVM is handling memory alloc **/
  }

  public static void UNPROTECT(int c) {
    /** NO OP -- JVM is handling memory alloc **/
  }

  public static void PROTECT_PTR(SEXP s) {
    /** NO OP -- JVM is handling memory alloc **/
  }

  /**
   * Creates a new linked list Lexp
   *
   * @param car the first value in the list
   * @param cdr the next node in the linked list. Either a ListExp or NilExp.INSTANCE
   * @return
   */
  public static ListExp CONS(SEXP car, SEXP cdr) {
    ArgChecker.notNull(car);
    ArgChecker.notNull(cdr);

    if (cdr == R_NilValue) {
      return new ListExp(car, null);
    } else {
      return new ListExp(car, (ListExp) cdr);
    }
  }

  public static ListExp list1(SEXP s) {
    return CONS(s, R_NilValue);
  }

  public static ListExp list2(SEXP s, SEXP t) {
    return CONS(s, list1(t));
  }

  public static ListExp list3(SEXP s, SEXP t, SEXP u) {
    return CONS(s, list2(t, u));
  }

  public static ListExp list4(SEXP s, SEXP t, SEXP u, SEXP v) {
    return CONS(s, list3(t, u, v));
  }

  public static SEXP SYMVALUE(SEXP s) {
    return ((SymbolExp) s).getValue();
  }

  public static void SET_SYMVALUE(SEXP s, SEXP v) {
    ((SymbolExp) s).setValue(v);
  }

  public static LangExp LCONS(SEXP car, SEXP cdr) {
    Preconditions.checkNotNull(car);

    if (cdr == R_NilValue) {
      return new LangExp(car, null);
    } else {
      return new LangExp(car, (ListExp) cdr);
    }
  }

  public static void UNPROTECT_PTR(SEXP s) {
    /** NO OP -- JVM is handling memory alloc **/
  }

  public static ListExp lang1(SEXP s) {
    return LCONS(s, R_NilValue);
  }

  public static LangExp lang2(SEXP s, SEXP t) {
    return LCONS(s, list1(t));
  }

  public static LangExp lang3(SEXP s, SEXP t, SEXP u) {
    return LCONS(s, list2(t, u));
  }

  public static LangExp lang4(SEXP s, SEXP t, SEXP u, SEXP v) {
    return LCONS(s, list3(t, u, v));
  }

  public static void SET_TAG(SEXP x, SEXP v) {
    // CHECK_OLD_TO_NEW(x, v);    I think this to do with GC so drop
    x.setTag(v);
  }

  public static int length(SEXP s) {
    return s.length();
  }

  public static SEXP.Type TYPEOF(SEXP s) {
    return s.getType();
  }

  public static SEXP ATTRIB(SEXP s) {
    return s.getAttributes();
  }

  public static int NAMED(SEXP s) {
    return s.getNamed();
  }

  public static void SET_NAMED(SEXP s, int flag) {
    s.setNamed(flag);
  }
//
//  define ATTRIB(x)	((x)->attrib)
//#define OBJECT(x)	((x)->sxpinfo.obj)
//#define MARK(x)		((x)->sxpinfo.mark)
//#define TYPEOF(x)	((x)->sxpinfo.type)
//#define NAMED(x)	((x)->sxpinfo.named)
//#define RTRACE(x)	((x)->sxpinfo.trace)
//#define LEVELS(x)	((x)->sxpinfo.gp)
//#define SET_OBJECT(x,v)	(((x)->sxpinfo.obj)=(v))
//#define SET_TYPEOF(x,v)	(((x)->sxpinfo.type)=(v))
//#define SET_NAMED(x,v)	(((x)->sxpinfo.named)=(v))
//#define SET_RTRACE(x,v)	(((x)->sxpinfo.trace)=(v))
//#define SETLEVELS(x,v)	(((x)->sxpinfo.gp)=(v))

  public static boolean DDVAL(SEXP x) {
    return x.isDotDotValue();
  }

  public static void SET_DDVAL_BIT(SEXP x) {
    x.setDotDotValue(true);
  }

  public static void UNSET_DDVAL_BIT(SEXP x) {
    x.setDotDotValue(false);
  }

  public static String translateChar(SEXP s) {
    CharExp charVec = ArgChecker.instanceOf(s, CharExp.class);
    return charVec.toString();
  }

  public static boolean isNull(SEXP s) {
    return s.getType() == SEXP.Type.NILSXP;
  }

  public static boolean isSymbol(SEXP s) {
    return s.getType() == SEXP.Type.SYMSXP;
  }

  public static boolean isLogical(SEXP s) {
    return s.getType() == SEXP.Type.LGLSXP;
  }

  public static boolean isReal(SEXP s) {
    return s.getType() == SEXP.Type.REALSXP;
  }

  public static boolean isComplex(SEXP s) {
    return s.getType() == SEXP.Type.REALSXP;
  }

  public static boolean isExpression(SEXP s) {
    return s.getType() == SEXP.Type.EXPRSXP;
  }

  public static boolean isEnvironment(SEXP s) {
    return s.getType() == SEXP.Type.ENVSXP;
  }

  public static boolean isString(SEXP s) {
    return s.getType() == SEXP.Type.STRSXP;
  }


}

