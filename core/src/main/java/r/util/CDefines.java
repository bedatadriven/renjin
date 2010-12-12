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

package r.util;

import com.google.common.base.Preconditions;
import r.lang.*;

/**
 * Macros and static methods defined in the original C implementation
 * and provided to facilitate the porting of C code.
 */
public class CDefines {

  protected CDefines() {
  }

  public static final Null R_NilValue = Null.INSTANCE;
  public static final SymbolExp R_MissingArg = SymbolExp.MISSING_ARG;
  public static final SymbolExp R_UnboundValue = SymbolExp.UNBOUND_VALUE;

  public static final Object NULL = null;

  public static final String NA_STRING = StringVector.NA;


  //#define DATAPTR(x)	(((SEXPREC_ALIGN *) (x)) + 1)

  public static CHARSEXP STRING_ELT(SEXP x, int i) {
    StringVector vector = (StringVector)x;
    return new CHARSEXP(vector.getElement(i));
  }

  public static String CHAR(CHARSEXP s) {
    return s.getValue();
  }

  public static CHARSEXP PRINTNAME(SEXP s) {
    return new CHARSEXP(((SymbolExp) s).getPrintName());
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
    Preconditions.checkNotNull(e);
    return e.getRawTag();
  }

  /**
   * Returns the value of the given list node
   *
   * @param listExp a ListExp node (must be of ListExp type)
   * @return the value of this list node
   */
  public static SEXP CAR(SEXP listExp) {
    PairList.Node typedList = (PairList.Node) listExp;
    return typedList.getValue();
  }

  /**
   * Returns the next node in the linked list
   *
   * @param listExp a SEXP of type ListExp
   * @return the next node in the list, or R_NilValue
   */
  public static SEXP CDR(SEXP listExp) {
    PairList.Node list = (PairList.Node)listExp;

    return list.hasNextNode() ? list.getNextNode() : R_NilValue;
  }

  public static SEXP CAAR(SEXP e) {
    return CAR(CDR(e));
  }

  public static SEXP CDAR(SEXP e) {
    return CDR(CAR(e));
  }

  /**
   * Returns the second value in the list
   */
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
    ((PairList.Node) x).setValue(y);
    return y;
  }

  public static SEXP SETCDR(SEXP x, SEXP y) {
    if (x == NULL || x == R_NilValue)
      error(_("bad value"));
    CHECK_OLD_TO_NEW(x, y);
    ((PairList.Node) x).setNextNode((PairList.Node) y);
    return y;
  }

  public static SEXP SETCADR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == NULL || x == R_NilValue ||
        CDR(x) == NULL || CDR(x) == R_NilValue)
      error(_("bad value"));
    cell = CDR(x);
    CHECK_OLD_TO_NEW(cell, y);
    ((PairList.Node) cell).setNextNode((PairList.Node) y);
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
    ((PairList.Node) cell).setValue(y);
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
    ((PairList.Node) cell).setValue(y);
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
    ((PairList.Node) cell).setValue(y);
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
  public static PairList.Node CONS(SEXP car, SEXP cdr) {
    Preconditions.checkNotNull(car);
    Preconditions.checkNotNull(cdr);

    if (cdr == R_NilValue) {
      return new PairList.Node(car, null);
    } else {
      return new PairList.Node(car, (PairList.Node) cdr);
    }
  }

  public static PairList.Node list1(SEXP s) {
    return CONS(s, R_NilValue);
  }

  public static PairList.Node list2(SEXP s, SEXP t) {
    return CONS(s, list1(t));
  }

  public static PairList.Node list3(SEXP s, SEXP t, SEXP u) {
    return CONS(s, list2(t, u));
  }

  public static PairList.Node list4(SEXP s, SEXP t, SEXP u, SEXP v) {
    return CONS(s, list3(t, u, v));
  }

  public static FunctionCall LCONS(SEXP car, SEXP cdr) {
    Preconditions.checkNotNull(car);

    if (cdr == R_NilValue) {
      return new FunctionCall(car, null);
    } else {
      return new FunctionCall(car, (PairList.Node) cdr);
    }
  }

  public static void UNPROTECT_PTR(SEXP s) {
    /** NO OP -- JVM is handling memory alloc **/
  }

  public static PairList.Node lang1(SEXP s) {
    return LCONS(s, R_NilValue);
  }

  public static FunctionCall lang2(SEXP s, SEXP t) {
    return LCONS(s, list1(t));
  }

  public static FunctionCall lang3(SEXP s, SEXP t, SEXP u) {
    return LCONS(s, list2(t, u));
  }

  public static FunctionCall lang4(SEXP s, SEXP t, SEXP u, SEXP v) {
    return LCONS(s, list3(t, u, v));
  }

  public static void SET_TAG(SEXP x, SEXP v) {
    // CHECK_OLD_TO_NEW(x, v);    I think this to do with GC so drop
    x.setTag(v);
  }

  public static int length(SEXP s) {
    return s.length();
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



  public static String translateChar(SEXP s) {
    CHARSEXP charVec = (CHARSEXP)s;
    return charVec.toString();
  }

  public static boolean isSymbol(SEXP s) {
    return s instanceof SymbolExp;
  }

  public static boolean isLogical(SEXP s) {
    return  s instanceof LogicalVector;
  }

  public static boolean isReal(SEXP s) {
    return s instanceof DoubleVector;
  }

  public static boolean isComplex(SEXP s) {
    return s instanceof ComplexVector;
  }

  public static boolean isExpression(SEXP s) {
    return s instanceof ExpressionVector;
  }

  public static boolean isEnvironment(SEXP s) {
    return s instanceof Environment;
  }

  public static boolean isString(SEXP s) {
    return s instanceof StringVector;
  }


  public static void error(String message, Object... args) {
    throw new RuntimeException(String.format(message, args));
  }

  public static String _(String s) {
    return s;
  }

  public enum ArithOpType {
    PLUSOP,
    MINUSOP,
    TIMESOP,
    DIVOP,
    POWOP,
    MODOP,
    IDIVOP
  }

  public enum RelOpType {
    EQOP,
    NEOP,
    LTOP,
    LEOP,
    GEOP,
    GTOP
  }
}

