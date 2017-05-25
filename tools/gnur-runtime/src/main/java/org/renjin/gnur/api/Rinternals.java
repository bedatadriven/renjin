/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
// Initial template generated from Rinternals.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.FinalizationClosure;
import org.renjin.eval.FinalizationHandler;
import org.renjin.gcc.runtime.*;
import org.renjin.invoke.annotations.Current;
import org.renjin.methods.MethodDispatch;
import org.renjin.methods.Methods;
import org.renjin.primitives.*;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.sexp.*;

import java.lang.System;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;

/**
 * GNU R API methods defined in the "Rinternals.h" header file.
 *
 * <p>Note that Renjin's tool chain always compiles native package code as if
 * {@code USE_RINTERNALS} was not defined, so all of these symbols always resolve to function
 * calls and not macro expansions, even for packages which define {@code USE_RINTERNALS}</p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Rinternals {


  private Rinternals() { }

  /* Evaluation Environment */

  /**
   * The "global" environment
   */
  public static SEXP	R_GlobalEnv;

  /**
   * The empty environment at the root of the environment tree
   */
  public static SEXP  R_EmptyEnv;

  /**
   * The base environment; formerly R_NilEnv
   */
  public static SEXP  R_BaseEnv;

  /**
   * The (fake) namespace for base
   */
  public static SEXP	R_BaseNamespace;


  /**
   *  Registry for registered namespaces
   */
  public static SEXP	R_NamespaceRegistry;

  /**
   * Current srcref for debuggers
   */
  public static SEXP	R_Srcref;

  /* Special Values */

  /**
   * The nil object
   */
  public static SEXP	R_NilValue = Null.INSTANCE;

  /**
   * Unbound marker
   */
  public static  SEXP	R_UnboundValue = Symbol.UNBOUND_VALUE;

  /**
   * Missing argument marker
   */
  public static  SEXP	R_MissingArg = Symbol.MISSING_ARG;

  /**
   * Marker for restarted function calls
   */
  public static SEXP	R_RestartToken; 

  /* Symbol Table Shortcuts */

  /**
   * "base" Symbol
   */
  public static  SEXP	R_baseSymbol = Symbol.get("base");

  /**
   * "base" Symbol
   */
  public static  SEXP	R_BaseSymbol = Symbol.get("base");	// "base"

  /**
   * {@code { Symbol
   */
  public static  SEXP	R_BraceSymbol = Symbol.get("{");

  /**
   * "[[" Symbol
   */
  public static  SEXP	R_Bracket2Symbol = Symbol.get("[[");

  /**
   * "[" Symbol
   */
  public static  SEXP	R_BracketSymbol = Symbol.get("[");

  /**
   * "class" Symbol
   */
  public static  SEXP	R_ClassSymbol = Symbol.get("class");

  /**
   * ".Device" symbol
   */
  public static  SEXP	R_DeviceSymbol = Symbol.get(".Device");

  /**
   * "dimnames" symbol
   */
  public static  SEXP	R_DimNamesSymbol = Symbol.get("dimnames");

  /**
   * "dim" symbol
   */
  public static  SEXP	R_DimSymbol = Symbol.get("dim");

  /**
   * "$" Symbol
   */
  public static  SEXP	R_DollarSymbol = Symbol.get("$");

  /**
   * "..." Symbol
   */
  public static  SEXP	R_DotsSymbol = Symbol.get("...");

  /**
   * "::" Symbol
   */
  public static  SEXP	R_DoubleColonSymbol = Symbol.get("::");

  /**
   * "drop" Symbol
   */
  public static  SEXP	R_DropSymbol = Symbol.get("drop");

  /**
   * ".Last.value" Symbol
   */
  public static  SEXP	R_LastvalueSymbol = Symbol.get(".Last.value");

  /**
   * "level" Symbol
   */
  public static  SEXP	R_LevelsSymbol = Symbol.get("level");

  /**
   * "mode" symbol
   */
  public static  SEXP	R_ModeSymbol = Symbol.get("mode");

  /**
   * "na.rm" Symbol
   */
  public static  SEXP	R_NaRmSymbol = Symbol.get("na.rm");

  /**
   * "name" Symbol
   */
  public static  SEXP	R_NameSymbol = Symbol.get("name");

  /**
   * "names" Symbol
   */
  public static  SEXP	R_NamesSymbol = Symbol.get("names");

  /**
   * ".__NAMESPACE__." Symbol
   */
  public static  SEXP	R_NamespaceEnvSymbol = Symbol.get(".__NAMESPACE__.");

  /**
   * "package" Symbol
   */
  public static  SEXP	R_PackageSymbol = Symbol.get("package");

  /**
   * "previous" Symbol
   */
  public static  SEXP	R_PreviousSymbol = Symbol.get("previous");

  /**
   * "quote" Symbol
   */
  public static  SEXP	R_QuoteSymbol = Symbol.get("quote");

  /**
   * "row.names" Symbol
   */
  public static  SEXP	R_RowNamesSymbol = Symbol.get("row.names");

  /**
   * ".Random.seed" Symbol
   */
  public static  SEXP	R_SeedsSymbol = Symbol.get(".Random.seed");

  /**
   * "sort.list" Symbol
   */
  public static  SEXP	R_SortListSymbol = Symbol.get("sort.list");

  /**
   * "source" Symbol
   */
  public static  SEXP	R_SourceSymbol = Symbol.get("source");

  /**
   * "spec" Symbol
   */
  public static  SEXP	R_SpecSymbol = Symbol.get("spec");

  /**
   * ":::" Symbol
   */
  public static  SEXP	R_TripleColonSymbol = Symbol.get(":::");

  /**
   * "tsp" Symbol
   */
  public static  SEXP	R_TspSymbol = Symbol.get("tsp");

  /**
   * ".defined" Symbol
   */
  public static  SEXP  R_dot_defined = Symbol.get(".defined");

  /**
   * ".Method" Symbol
   */
  public static  SEXP  R_dot_Method = Symbol.get(".Method");

  /**
   * ".packageName" Symbol
   */
  public static  SEXP	R_dot_packageName = Symbol.get(".packageName");

  /**
   * ".target" Symbol
   */
  public static  SEXP  R_dot_target = Symbol.get(".target");

/* Missing Values - others from Arith.h */

  /**
   * NA_String as a CHARSXP
   */
  public static  SEXP	R_NaString = null;

  /**
   * ""as a CHARSEXP
   */
  public static  SEXP	R_BlankString = new GnuCharSexp("");

  /**
   * "" as a STRSXP
   */
  public static  SEXP	R_BlankScalarString = new GnuStringVector("");


  public static BytePtr R_CHAR(SEXP x) {
    GnuCharSexp charSexp = (GnuCharSexp) x;
    return charSexp.getValue();
  }

  public static boolean Rf_isNull(SEXP s) {
    return s == Null.INSTANCE;
  }

  public static boolean Rf_isSymbol(SEXP s) {
    return s instanceof Symbol;
  }

  public static boolean Rf_isLogical(SEXP s) {
    return s instanceof LogicalVector;
  }

  public static boolean Rf_isReal(SEXP s) {
    return s instanceof DoubleVector;
  }

  public static boolean Rf_isComplex(SEXP s) {
    return s instanceof ComplexVector;
  }

  public static boolean Rf_isExpression(SEXP s) {
    return s instanceof ExpressionVector;
  }

  public static boolean Rf_isEnvironment(SEXP s) {
    return s instanceof Environment;
  }

  public static boolean Rf_isString(SEXP s) {
    return s instanceof StringVector;
  }

  /** Does an object have a class attribute?
   *
   * @param s Pointer to an R value.
   *
   * @return TRUE iff the object pointed to by \a s has a
   * class attribute.
   */
  public static boolean Rf_isObject(SEXP s) {
    throw new UnimplementedGnuApiMethod("Rf_isObject");
  }

  public static SEXP ATTRIB(SEXP x) {
    return x.getAttributes().asPairList();
  }

  public static boolean OBJECT(SEXP x) {
    return x.isObject();
  }

  public static int MARK(SEXP x) {
    throw new UnimplementedGnuApiMethod("MARK");
  }

  public static int TYPEOF(SEXP s) {
    if(s == Null.INSTANCE) {
      return SexpType.NILSXP;
    } else if(s instanceof ExpressionVector) {
      return SexpType.EXPRSXP;
    } else if(s instanceof ListVector) {
      return SexpType.VECSXP;
    } else if(s instanceof StringVector) {
      return SexpType.STRSXP;
    } else if(s instanceof DoubleVector) {
      return SexpType.REALSXP;
    } else if(s instanceof IntVector) {
      return SexpType.INTSXP;
    } else if(s instanceof LogicalVector) {
      return SexpType.LGLSXP;
    } else if(s instanceof RawVector) {
      return SexpType.RAWSXP;
    } else if(s instanceof Environment) {
      return SexpType.ENVSXP;
    } else if(s instanceof ComplexVector) {
      return SexpType.CPLXSXP;
    } else if(s instanceof Closure) {
      return SexpType.CLOSXP;
    } else if(s instanceof FunctionCall) {
      return SexpType.LANGSXP;
    } else if(s instanceof PairList) {
      return SexpType.LISTSXP;
    } else if(s instanceof S4Object) {
      return SexpType.S4SXP;
    } else if(s instanceof Promise) {
      return SexpType.PROMSXP;
    } else if(s instanceof Symbol) {
      return SexpType.SYMSXP;
    } else {
      throw new UnsupportedOperationException("Unknown SEXP Type: " + s.getClass().getName());
    }
  }

  /**
   * @return the value of the {@code SEXP}'s named bit.
   */
  public static int NAMED(SEXP sexp) {
    // Renjin's contract for AtomicVector is that a vector's contents can never be changed
    // GNU R's contract for AtomicVector is that a vector's contents can ONLY be changed if NAMED(sexp) = 0
    // To "trick" code written for GNU R into observing Renjin's contract, we can simply always return a non-zero 
    // value here.
    return 2;
  }

  public static void SET_NAMED(SEXP sexp, int value) {
    // NOOP
  }

  public static int REFCNT(SEXP x) {
    throw new UnimplementedGnuApiMethod("REFCNT");
  }

  public static void SET_OBJECT(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_OBJECT");
  }

  public static void SET_TYPEOF(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_TYPEOF");
  }


  public static void SET_ATTRIB(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_ATTRIB");
  }

  public static void DUPLICATE_ATTRIB(SEXP to, SEXP from) {
    throw new UnimplementedGnuApiMethod("DUPLICATE_ATTRIB");
  }

  public static int IS_S4_OBJECT(SEXP x) {
    throw new UnimplementedGnuApiMethod("IS_S4_OBJECT");
  }

  public static void SET_S4_OBJECT(SEXP x) {
    AbstractSEXP abstractSEXP = (AbstractSEXP) x;
    abstractSEXP.unsafeSetAttributes(x.getAttributes().copy().setS4(true));
  }

  public static void UNSET_S4_OBJECT(SEXP x) {
    AbstractSEXP abstractSEXP = (AbstractSEXP) x;
    abstractSEXP.unsafeSetAttributes(x.getAttributes().copy().setS4(false));
  }

  public static int LENGTH(SEXP x) {
    return x.length();
  }

  /**
   * @param x Pointer to a vector object.
   *
   * @return The 'true length' of {@code x}.  According to the R Internals
   *         document for R 2.4.1, this is only used for certain hash
   *         tables, and signifies the number of used slots in the
   *         table.
   */
  public static int TRUELENGTH(SEXP x) {
    throw new UnimplementedGnuApiMethod("TRUELENGTH");
  }

  public static void SETLENGTH(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SETLENGTH");
  }

  /**
   * Set 'true length' of vector.
   *
   * @param x Pointer to a vector object.
   *
   * @param v The required new 'true length'.
   */
  public static void SET_TRUELENGTH(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_TRUELENGTH");
  }

  public static /*R_xlen_t*/ int XLENGTH(SEXP x) {
    return x.length();
  }

  public static /*R_xlen_t*/ int XTRUELENGTH(SEXP x) {
    throw new UnimplementedGnuApiMethod("XTRUELENGTH");
  }

  public static int IS_LONG_VEC(SEXP x) {
    return 0;
  }

  public static int LEVELS(SEXP x) {
    throw new UnimplementedGnuApiMethod("LEVELS");
  }

  public static int SETLEVELS(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SETLEVELS");
  }

  public static Object DATAPTR(SEXP x) {
    if(x instanceof IntVector | x instanceof LogicalVector) {
      return INTEGER(x);
    } else if(x instanceof DoubleVector) {
      return REAL(x);
    } else if(x instanceof ComplexVector) {
      return COMPLEX(x);
    } else if(x instanceof RawVector) {
      return RAW(x);
    } else {
      throw new UnsupportedOperationException("DATAPTR on type " + x.getClass().getName());
    }
  }

  public static IntPtr LOGICAL(SEXP x) {
    if(x instanceof LogicalArrayVector) {
      return new IntPtr(((LogicalArrayVector)x).toIntArrayUnsafe());
    } else if(x instanceof LogicalVector) {
      // TODO: cache arrays for the case of repeated LOGICAL() calls?
      return new IntPtr(((LogicalVector) x).toIntArray());
    } else {
      throw new EvalException("LOGICAL(): expected logical vector, found %s", x.getTypeName());
    }
  }

  public static IntPtr INTEGER(SEXP x) {
    if(x instanceof IntArrayVector) {
      return new IntPtr(((IntArrayVector) x).toIntArrayUnsafe());
    } else if(x instanceof LogicalArrayVector) {
      return new IntPtr(((LogicalArrayVector) x).toIntArrayUnsafe());
    } else if (x instanceof DoubleVector) {
      return new IntPtr(((DoubleVector) x).toIntArray());
    } else if(x instanceof IntVector) {
      // TODO: cache arrays for the case of repeated INTEGER() calls?
      return new IntPtr(((IntVector) x).toIntArray());
    } else if(x instanceof LogicalVector) {
      return new IntPtr(((LogicalVector) x).toIntArray());
    } else {
      throw new EvalException("INTEGER(): expected integer vector, found %s", x.getTypeName());
    }
  }

  public static BytePtr RAW(SEXP x) {
    throw new UnimplementedGnuApiMethod("RAW");
  }

  public static DoublePtr REAL(SEXP x) {
    if(x instanceof DoubleArrayVector) {
      // Return the array backing the double vector
      // This is inherently unsafe because the calling code can modify the contents of the array
      // and potentially break the contract of immutability of DoubleVector, but the GNU R API
      // imposes essentially the same contract. We are, however, at the mercy of the C code observing
      // this contract.
      return new DoublePtr(((DoubleArrayVector) x).toDoubleArrayUnsafe());
    } else if(x instanceof DoubleVector) {
      // Return a copy of this vector as an array.
      // TODO: Cache arrays for the case of frequent REAL() calls?
      return new DoublePtr(((DoubleVector) x).toDoubleArray());
    } else {
      throw new EvalException("REAL(): expected numeric vector, found %s", x.getTypeName());
    }
  }

  public static DoublePtr COMPLEX (SEXP x) {
    if(x instanceof ComplexArrayVector) {
      return new DoublePtr(((ComplexArrayVector) x).toComplexArrayVectorUnsafe());
//    } else if(x instanceof ComplexVector) {
//      return new DoublePtr(((ComplexVector) x).toComplexArrayUnsafe());
    } else {
      throw new EvalException("COMPLEX(): expected complex vector, found %s", x.getTypeName());
    }
  }

  /**
   * Examine element of a character vector.
   *
   * @param x Pointer to a character vector.
   *
   * @param i Index of the required element.  There is no bounds
   *          checking.
   *
   * @return Pointer to extracted {@code i} 'th element as a {@link GnuCharSexp}
   */
  public static SEXP STRING_ELT(SEXP x, /*R_xlen_t*/ int i) {
    StringVector stringVector = (StringVector) x;
    String string = stringVector.getElementAsString(i);

    return new GnuCharSexp(string);
  }

  /**
   * Examine element of a list.
   *
   * @param x Pointer to a list.
   *
   * @param i Index of the required element.  There is no bounds checking.
   *
   * @return The value of the {@code i}th element.
   */
  public static SEXP VECTOR_ELT(SEXP x, /*R_xlen_t*/ int i) {
    return ((ListVector) x).getElementAsSEXP(i);
  }


  /**
   *  Set element of character vector.
   *
   * @param x Pointer to a character vector as a {@link GnuCharSexp}.
   *
   * @param i Index of the required element.  There is no bounds checking.
   *
   * @param v Pointer to CHARSXP representing the new value.
   */
  public static void SET_STRING_ELT(SEXP x, /*R_xlen_t*/ int i, SEXP v) {
    GnuStringVector stringVector = (GnuStringVector) x;
    GnuCharSexp charValue = (GnuCharSexp) v;

    stringVector.set(i, charValue);
  }


  /**
   *  Set element of list.
   *
   * @param x Pointer to a list.
   *
   * @param i Index of the required element.
   *
   * @param v Pointer to R value representing the new value.
   *
   * @return The new value {@code v}
   */
  public static SEXP SET_VECTOR_ELT(SEXP x, /*R_xlen_t*/ int i, SEXP v) {
    ListVector listVector = (ListVector) x;
    SEXP[] elements = listVector.toArrayUnsafe();
    elements[i] = v;
    return v;
  }

  // SEXP*() STRING_PTR (SEXP x)

  // SEXP*() VECTOR_PTR (SEXP x)

  public static SEXP TAG(SEXP e) {
    return ((PairList.Node) e).getTag();
  }

  public static SEXP CAR(SEXP e) {
    return ((PairList.Node) e).getValue();
  }

  public static SEXP CDR(SEXP e) {
    return ((PairList.Node) e).getNext();
  }

  public static SEXP CAAR(SEXP e) {
    return CAR(CAR(e));
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

  public static SEXP CDDDR(SEXP e) {
    return CDR(CDR(CDR(e)));
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

  public static SEXP CD4R(SEXP x) {
    return CDR(CDR(CDR(CDR(x))));
  }

  public static int MISSING(SEXP x) {
    throw new UnimplementedGnuApiMethod("MISSING");
  }

  public static void SET_MISSING(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_MISSING");
  }

  public static void SET_TAG(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SET_TAG");
  }

  public static SEXP SETCAR(SEXP x, SEXP y) {
    if (x == null || x == R_NilValue) {
      throw new EvalException("bad value");
    }

    ((PairList.Node) x).setValue(y);
    return y;
  }

  public static SEXP SETCDR(SEXP x, SEXP y) {
    if (x == null || x == R_NilValue) {
      throw new EvalException("bad value");
    }

    ((PairList.Node) x).setNextNode((PairList.Node) y);
    return y;
  }

  public static SEXP SETCADR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == null || x == R_NilValue ||
        CDR(x) == null || CDR(x) == R_NilValue) {
      throw new EvalException("bad value");
    }
    cell = CDR(x);
    ((PairList.Node) cell).setValue(y);
    return y;
  }

  public static SEXP CHK(SEXP sexp) {
    return sexp;
  }

  public static SEXP SETCADDR(SEXP x, SEXP y) {
    SEXP cell;
    if (x == null || x == R_NilValue ||
        CDR(x) == null || CDR(x) == R_NilValue ||
        CDDR(x) == null || CDDR(x) == R_NilValue) {
      throw new EvalException("bad value");
    }

    cell = CDDR(x);
    ((PairList.Node) cell).setValue(y);
    return y;
  }

  public static SEXP SETCADDDR(SEXP x, SEXP y) {
    SEXP cell;
    if (CHK(x) == null || x == R_NilValue ||
        CHK(CDR(x)) == null || CDR(x) == R_NilValue ||
        CHK(CDDR(x)) == null || CDDR(x) == R_NilValue ||
        CHK(CDDDR(x)) == null || CDDDR(x) == R_NilValue) {
      throw new EvalException("bad value");
    }
    cell = CDDDR(x);
    ((PairList.Node) cell).setValue(y);
    return y;
  }

  public static SEXP SETCAD4R(SEXP x, SEXP y) {
    SEXP cell;
    if (CHK(x) == null || x == R_NilValue ||
        CHK(CDR(x)) == null || CDR(x) == R_NilValue ||
        CHK(CDDR(x)) == null || CDDR(x) == R_NilValue ||
        CHK(CDDDR(x)) == null || CDDDR(x) == R_NilValue ||
        CHK(CD4R(x)) == null || CD4R(x) == R_NilValue) {
      throw new EvalException("bad value");
    }
    cell = CD4R(x);
    ((PairList.Node) cell).setValue(y);
    return y;
  }

  public static SEXP CONS_NR(SEXP a, SEXP b) {
    throw new UnimplementedGnuApiMethod("CONS_NR");
  }

  public static SEXP FORMALS(SEXP x) {
    return ((Closure) x).getFormals();
  }

  public static SEXP BODY(SEXP x) {
    return ((Closure) x).getBody();
  }

  public static SEXP CLOENV(SEXP x) {
    return ((Closure) x).getEnclosingEnvironment();
  }

  public static int RDEBUG(SEXP x) {
    throw new UnimplementedGnuApiMethod("RDEBUG");
  }

  public static int RSTEP(SEXP x) {
    throw new UnimplementedGnuApiMethod("RSTEP");
  }

  public static int RTRACE(SEXP x) {
    throw new UnimplementedGnuApiMethod("RTRACE");
  }

  public static void SET_RDEBUG(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_RDEBUG");
  }

  public static void SET_RSTEP(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_RSTEP");
  }

  public static void SET_RTRACE(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_RTRACE");
  }

  public static void SET_FORMALS(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_FORMALS");
  }

  public static void SET_BODY(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_BODY");
  }

  /** Replace the environment of a closure.
   *
   * @param x Pointer to a closure object.
   *
   * @param v Pointer to the environment now to be
   *          considered as the environment of this closure.
   *          R_NilValue is not permissible.
   */
  public static void SET_CLOENV(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_CLOENV");
  }

  /** Symbol name.
   *
   * @param x Pointer to a symbol.
   *
   * @return Pointer to a CHARSXP representing {@code x}'s name.
   */
  public static SEXP PRINTNAME(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRINTNAME");
  }

  /**
   * Symbol's value in the base environment.
   *
   * @param x Pointer to a symbol.
   *
   * @return Pointer to an R value representings {@code x}'s value.
   *         Returns R_UnboundValue if no value is currently
   *         associated with the Symbol.
   */
  public static SEXP SYMVALUE(SEXP x) {
    throw new UnimplementedGnuApiMethod("SYMVALUE");
  }

  /** Get function accessed via <tt>.Internal()</tt>.
   *
   * @param x Pointer to a symbol.
   *
   * @return If {@code x} is associated with a function invoked in R
   * via <tt>.Internal()</tt>, then a pointer to the appropriate
   * function, otherwise R_NilValue.
   */
  public static SEXP INTERNAL(SEXP x) {
    throw new UnimplementedGnuApiMethod("INTERNAL");
  }

  public static int DDVAL(SEXP x) {
    throw new UnimplementedGnuApiMethod("DDVAL");
  }

  public static void SET_DDVAL(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_DDVAL");
  }

  public static void SET_PRINTNAME(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_PRINTNAME");
  }

  /**  Set symbol's value in the base environment.
   *
   * @param x Pointer to a symbol.
   *
   * @param val Pointer to the R value now to be considered as
   *            the value of this symbol.  R_NilValue or
   *            R_UnboundValue are permissible values of {@code val}
   *
   */
  public static void SET_SYMVALUE(SEXP x, SEXP val) {
    throw new UnimplementedGnuApiMethod("SET_SYMVALUE");
  }

  /**
   * Associate a Symbol with a <tt>.Internal()</tt> function.
   *
   * @param x Pointer to a symbol.
   *
   * @param v Pointer to the builtin function to be
   * associated by this symbol.  R_NilValue is permissible, and
   * signifies that any previous association of {@code sym} with a
   * function is to be removed from the table.
   */
  public static void SET_INTERNAL(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_INTERNAL");
  }

  /**  Access an environment's Frame, represented as a pairlist.
   *
   * @param x Pointer to an environment.
   *
   * @return Pointer to a pairlist representing the contents of the
   * Frame of {@code x} (may be R_NilValue).
   */
  public static SEXP FRAME(SEXP x) {
    throw new UnimplementedGnuApiMethod("FRAME");
  }

  /**
   *  Access enclosing environment.
   *
   * @param x Pointer to an environment.
   *
   * @return Pointer to the enclosing environment of {@code x}.
   */
  public static SEXP ENCLOS(SEXP x) {
    throw new UnimplementedGnuApiMethod("ENCLOS");
  }

  public static SEXP HASHTAB(SEXP x) {
    throw new UnimplementedGnuApiMethod("HASHTAB");
  }

  public static int ENVFLAGS(SEXP x) {
    throw new UnimplementedGnuApiMethod("ENVFLAGS");
  }

  public static void SET_ENVFLAGS(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_ENVFLAGS");
  }

  public static void SET_FRAME(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_FRAME");
  }

  public static void SET_ENCLOS(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_ENCLOS");
  }

  public static void SET_HASHTAB(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_HASHTAB");
  }

  /**
   *  Access the expression of a promise.
   *
   * @param x Pointer to a promise.
   *
   * @return Pointer to the expression to be evaluated by the
   *         promise.
   */
  public static SEXP PRCODE(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRCODE");
  }

  /** Access the environment of a promise.
   *
   * @param x Pointer to a promise.
   *
   * @return Pointer to the environment in which the promise
   *         is to be  evaluated.  Set to R_NilValue when the
   *         promise has been evaluated.
   */
  public static SEXP PRENV(SEXP x) {
    Promise promise = (Promise) x;
    if(promise.isEvaluated()) {
      return Null.INSTANCE;
    } else {
      return promise.getEnvironment();
    }
  }

  /** Access the value of a promise.
   *
   * @param x Pointer to a promise.
   *
   * @return Pointer to the value of the promise, or to
   *         R_UnboundValue if it has not yet been evaluated..
   */
  public static SEXP PRVALUE(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRVALUE");
  }

  public static int PRSEEN(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRSEEN");
  }

  public static void SET_PRSEEN(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_PRSEEN");
  }

  public static void SET_PRENV(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_PRENV");
  }

  /** Set the value of a promise.
   *
   * Once the value is set to something other than R_UnboundValue,
   * the environment pointer is set to R_NilValue.
   *
   * @param x Pointer to a promise.
   *
   * @param v Pointer to the value to be assigned to the promise.
   *
   */
  public static void SET_PRVALUE(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_PRVALUE");
  }

  public static void SET_PRCODE(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_PRCODE");
  }

  public static int HASHASH(SEXP x) {
    throw new UnimplementedGnuApiMethod("HASHASH");
  }

  public static int HASHVALUE(SEXP x) {
    throw new UnimplementedGnuApiMethod("HASHVALUE");
  }

  public static void SET_HASHASH(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_HASHASH");
  }

  public static void SET_HASHVALUE(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SET_HASHVALUE");
  }

  public static SEXP R_GetCurrentSrcref(int p0) {
    throw new UnimplementedGnuApiMethod("R_GetCurrentSrcref");
  }

  public static SEXP R_GetSrcFilename(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_GetSrcFilename");
  }

  public static SEXP Rf_asChar(SEXP p0) {
    return new GnuCharSexp(((AtomicVector) p0).getElementAsString(0));
  }

  public static SEXP Rf_coerceVector(SEXP p0, /*SEXPTYPE*/ int type) {
    switch(type){
      case SexpType.INTSXP:
        return Vectors.asInteger((Vector)p0).setAttributes(p0.getAttributes());
      case SexpType.REALSXP:
        return Vectors.asDouble((Vector)p0).setAttributes(p0.getAttributes());
      case SexpType.CPLXSXP:
        return Vectors.asComplex((Vector)p0).setAttributes(p0.getAttributes());
    }
    throw new UnimplementedGnuApiMethod("Rf_coerceVector");
  }

  public static SEXP Rf_PairToVectorList(SEXP x) {
    throw new UnimplementedGnuApiMethod("Rf_PairToVectorList");
  }

  public static SEXP Rf_VectorToPairList(SEXP x) {
    throw new UnimplementedGnuApiMethod("Rf_VectorToPairList");
  }

  public static SEXP Rf_asCharacterFactor(SEXP x) {
    throw new UnimplementedGnuApiMethod("Rf_asCharacterFactor");
  }

  public static int Rf_asLogical(SEXP x) {
    int warn = 0;

    if (Rf_isVectorAtomic(x)) {
      if (XLENGTH(x) < 1) {
        return IntVector.NA;
      }
      
      return ((AtomicVector) x).getElementAsRawLogical(0);
      
    } else if(x instanceof GnuCharSexp) {
      return StringVector.logicalFromString(((GnuCharSexp) x).getValue().nullTerminatedString());
    }

    return LogicalVector.NA;
  }

  public static int Rf_asInteger(SEXP x) {
    int warn = 0, res;

    if (Rf_isVectorAtomic(x) && XLENGTH(x) >= 1) {
      if(x instanceof AtomicVector) {
        return ((AtomicVector) x).getElementAsInt(0);
      } else {
        throw UNIMPLEMENTED_TYPE("asInteger", x);
      }
    } else if(x instanceof CHARSEXP) {
      throw new UnsupportedOperationException();
    }
    return IntVector.NA;
  }

  public static double Rf_asReal(SEXP x) {
    int warn = 0, res;

    if (Rf_isVectorAtomic(x) && XLENGTH(x) >= 1) {
      if(x instanceof AtomicVector) {
        return ((AtomicVector) x).getElementAsDouble(0);
      } else {
        throw UNIMPLEMENTED_TYPE("asReal", x);
      }
    } else if(x instanceof CHARSEXP) {
      throw new UnsupportedOperationException();
    }
    return DoubleVector.NA;
  }


  private static EvalException UNIMPLEMENTED_TYPE(String s, SEXP t) {
    return new EvalException("unimplemented type '%s' in '%s'\n", t.getTypeName(), s);
  }


  // Rcomplex Rf_asComplex (SEXP x)

  public static BytePtr Rf_acopy_string(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_acopy_string");
  }

  public static void Rf_addMissingVarsToNewEnv(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_addMissingVarsToNewEnv");
  }

  public static SEXP Rf_alloc3DArray(/*SEXPTYPE*/ int p0, int p1, int p2, int p3) {
    throw new UnimplementedGnuApiMethod("Rf_alloc3DArray");
  }

  public static SEXP Rf_allocArray(/*SEXPTYPE*/ int p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_allocArray");
  }

  public static SEXP Rf_allocFormalsList2(SEXP sym1, SEXP sym2) {
    throw new UnimplementedGnuApiMethod("Rf_allocFormalsList2");
  }

  public static SEXP Rf_allocFormalsList3(SEXP sym1, SEXP sym2, SEXP sym3) {
    throw new UnimplementedGnuApiMethod("Rf_allocFormalsList3");
  }

  public static SEXP Rf_allocFormalsList4(SEXP sym1, SEXP sym2, SEXP sym3, SEXP sym4) {
    throw new UnimplementedGnuApiMethod("Rf_allocFormalsList4");
  }

  public static SEXP Rf_allocFormalsList5(SEXP sym1, SEXP sym2, SEXP sym3, SEXP sym4, SEXP sym5) {
    throw new UnimplementedGnuApiMethod("Rf_allocFormalsList5");
  }

  public static SEXP Rf_allocFormalsList6(SEXP sym1, SEXP sym2, SEXP sym3, SEXP sym4, SEXP sym5, SEXP sym6) {
    throw new UnimplementedGnuApiMethod("Rf_allocFormalsList6");
  }

  public static SEXP Rf_allocMatrix(/*SEXPTYPE*/ int type, int numRows, int numCols) {
    AttributeMap attributes = AttributeMap.builder().setDim(numRows, numCols).build();
    switch (type){
      case SexpType.INTSXP:
        return new IntArrayVector(new int[numRows * numCols], attributes);
      case SexpType.REALSXP:
        return new DoubleArrayVector(new double[numRows * numCols], attributes);
      default:
        throw new IllegalArgumentException("type: " + type);
    }
  }

  /**
   * Create a pairlist of a specified length.
   *
   * This constructor creates a pairlist with a specified
   * number of elements.  On creation, each element has 'car' and 'tag'
   * set to R_NilValue.
   *
   * @param n Number of elements required in the list.
   *
   * @return The constructed list, or R_NilValue if {@code n} is zero.
   */
  public static SEXP Rf_allocList(int n) {
    throw new UnimplementedGnuApiMethod("Rf_allocList");
  }

  /** Create an S4 object.
   *
   * @return Pointer to the created object.
   */
  public static SEXP Rf_allocS4Object() {
    throw new UnimplementedGnuApiMethod("Rf_allocS4Object");
  }

  public static SEXP Rf_allocSExp(/*SEXPTYPE*/ int p0) {
    throw new UnimplementedGnuApiMethod("Rf_allocSExp");
  }

  // SEXP Rf_allocVector3 (SEXPTYPE, R_xlen_t, R_allocator_t *)

  public static /*R_xlen_t*/ int Rf_any_duplicated(SEXP x, boolean from_last) {
    throw new UnimplementedGnuApiMethod("Rf_any_duplicated");
  }

  public static /*R_xlen_t*/ int Rf_any_duplicated3(SEXP x, SEXP incomp, boolean from_last) {
    throw new UnimplementedGnuApiMethod("Rf_any_duplicated3");
  }

  public static SEXP Rf_applyClosure(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    throw new UnimplementedGnuApiMethod("Rf_applyClosure");
  }

  // SEXP Rf_arraySubscript (int, SEXP, SEXP, SEXP(*)(SEXP, SEXP), SEXP(*)(SEXP, int), SEXP)

  public static SEXP Rf_classgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_classgets");
  }

  /** Creates a pairlist with a specified car and tail.
   *
   * <p>This function protects its arguments from the garbage collector.
   *
   * @param cr Pointer to the 'car' of the element to be created.
   *
   * @param tl Pointer to the 'tail' of the element to be created,
   *          which must be a pairlist or R_NilValue.
   *
   * @return Pointer to the constructed pairlist.
   */
  public static SEXP Rf_cons(SEXP cr, SEXP tl) {
    throw new UnimplementedGnuApiMethod("Rf_cons");
  }

  public static void Rf_copyMatrix(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_copyMatrix");
  }

  public static void Rf_copyListMatrix(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_copyListMatrix");
  }


  /** Copy attributes, with some exceptions.
   *
   * <p>This is called in the case of binary operations to copy most
   * attributes from one of the input arguments to the output.
   * Note that the Dim, Dimnames and Names attributes are not
   * copied: these should have been assigned elsewhere.  The
   * function also copies the S4 object status.
   *
   * @param inp Pointer to the R value from which attributes are to
   *          be copied.
   *
   * @param ans Pointer to the R value to which attributes are to be
   *          copied.
   *
   */
  public static void Rf_copyMostAttrib(SEXP inp, SEXP ans) {
    throw new UnimplementedGnuApiMethod("Rf_copyMostAttrib");
  }

  public static void Rf_copyVector(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_copyVector");
  }

  public static int Rf_countContexts(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_countContexts");
  }

  public static SEXP Rf_CreateTag(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_CreateTag");
  }

  public static void Rf_defineVar(SEXP nameSexp, SEXP value, SEXP rhoSexp) {
    Symbol name = (Symbol) nameSexp;
    Environment rho = (Environment) rhoSexp;
    
    rho.setVariable(Native.currentContext(), name, value);
  }

  public static SEXP Rf_dimgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_dimgets");
  }

  public static SEXP Rf_dimnamesgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_dimnamesgets");
  }

  public static SEXP Rf_DropDims(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_DropDims");
  }

  public static SEXP Rf_duplicate(SEXP sexp) {
    if(sexp == Null.INSTANCE) {
      return sexp;
    }
    if(sexp instanceof DoubleVector) {
      return new DoubleArrayVector((DoubleVector)sexp);
    } else if(sexp instanceof IntVector) {
      return new IntArrayVector((IntVector)sexp);
    } else if (sexp instanceof ComplexVector) {
      return new ComplexArrayVector((ComplexVector) sexp);
    } else if(sexp instanceof StringVector) {
      return new StringArrayVector((StringVector) sexp);
    } else if(sexp instanceof S4Object) {
      return new S4Object(sexp.getAttributes());
    } else if(sexp instanceof ListVector) {
      SEXP[] elements = ((ListVector) sexp).toArrayUnsafe();
      for (int i = 0; i < elements.length; i++) {
        elements[i] = Rf_duplicate(elements[i]);
      }
      return new ListVector(elements, sexp.getAttributes());
    }
    throw new UnimplementedGnuApiMethod("Rf_duplicate: " + sexp.getTypeName());
  }

  public static SEXP Rf_shallow_duplicate(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_shallow_duplicate");
  }

  public static SEXP Rf_lazy_duplicate(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_lazy_duplicate");
  }

  public static SEXP Rf_duplicated(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("Rf_duplicated");
  }

  public static boolean R_envHasNoSpecialSymbols(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_envHasNoSpecialSymbols");
  }

  /**  Evaluate an object in a specified Environment.
   *
   * @param e Pointer to the object to be evaluated.
   *
   * @param rho Pointer to an Environment
   *
   * @return Pointer to the result of evaluating {@code e} in {@code rho}.
   */
  public static SEXP Rf_eval(SEXP e, SEXP rho) {
    return Native.currentContext().evaluate(e, (Environment) rho);
  }

  public static SEXP Rf_findFun(SEXP rho, SEXP symbol) {
    return ((Environment) rho).findFunction(Native.currentContext(), ((Symbol) symbol));
  }

  /**
   * Find the binding of a symbol in an environment and its enclosing environments.
   *
   * <p>This function calls {@link #Rf_findVarInFrame3} on the given frame and its enclosing
   * environments, until a binding is found or the empty environment is reached.
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @param rho an {@code ENVSXP} that is the starting point for the lookup
   *
   * @return Returns the binding value, or {@link #R_UnboundValue} if none was found.
   *
   */
  public static SEXP Rf_findVar(SEXP symbol, SEXP rho) {
    return ((Environment) rho).findVariable(Native.currentContext(), ((Symbol) symbol));
  }

  public static SEXP Rf_findVarInFrame(SEXP rho, SEXP symbol) {
    return Rf_findVarInFrame3(rho, symbol, true);
  }

  /**
   *  Find the binding for a symbol in a single environment.

  /**
   *
   * <p>The lookup will query user defined databases if the environment is of class
   * "UserDefinedDatabase" (using R_ObjectTable), and query active binding
   * (R_MakeActiveBinding).
   *
   * @param rho an ENVSXP in which the lookup will take place
   * @param symbol the SYMSXP that should be looked up
   * @param doGet specifies if the lookup is done to get the value (TRUE), as opposed to
   *     only determining whether there is a binding. The only effect is that, if this is
   *     FALSE, R_ObjectTable::exists is called before calling R_ObjectTable::get.
   *
   * @return Returns the binding value, or {@link #R_UnboundValue} if none was found.
   */
  public static SEXP Rf_findVarInFrame3(SEXP rho, SEXP symbol, boolean doGet) {
    return ((Environment) rho).getVariable(Native.currentContext(), (Symbol)symbol);
  }


  /**
   *  Access a named attribute.
   *
   * @param vec Pointer to the R value whose attributes are to be
   *          accessed.
   *
   * @param name Either a pointer to the symbol representing the
   *          required attribute, or a pointer to a character vector
   *          containing the required symbol name as the first element; in
   *          the latter case, as a side effect, the corresponding
   *          symbol is installed if necessary.
   *
   * @return Pointer to the requested attribute, or R_NilValue
   *         if there is no such attribute.
   *
   */
  public static SEXP Rf_getAttrib(SEXP vec, SEXP name) {
    return vec.getAttribute((Symbol)name);
  }

  public static SEXP Rf_GetArrayDimnames(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_GetArrayDimnames");
  }

  public static SEXP Rf_GetColNames(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_GetColNames");
  }

  // void Rf_GetMatrixDimnames (SEXP, SEXP *, SEXP *, const char **, const char **)

  public static SEXP Rf_GetOption(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_GetOption");
  }

  public static SEXP Rf_GetOption1(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_GetOption1");
  }

  public static int Rf_GetOptionDigits() {
    throw new UnimplementedGnuApiMethod("Rf_GetOptionDigits");
  }

  public static int Rf_GetOptionWidth() {
    throw new UnimplementedGnuApiMethod("Rf_GetOptionWidth");
  }

  public static SEXP Rf_GetRowNames(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_GetRowNames");
  }

  public static void Rf_gsetVar(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_gsetVar");
  }


  /**
   * Get a pointer to a regular Symbol object.
   *
   * <p>If no Symbol with the specified name currently exists, one will
   * be created, and a pointer to it returned.  Otherwise a pointer
   * to the existing Symbol will be returned.
   *
   * @param name The name of the required Symbol (CE_NATIVE encoding
   *          is assumed).
   *
   * @return Pointer to a Symbol (preexisting or newly created) with
   * the required name.
   */
  public static SEXP Rf_install(BytePtr name) {
    return Symbol.get(name.nullTerminatedString());
  }

  public static SEXP Rf_installChar(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_installChar");
  }

  public static SEXP Rf_installDDVAL(int i) {
    throw new UnimplementedGnuApiMethod("Rf_installDDVAL");
  }

  public static SEXP Rf_installS3Signature(BytePtr p0, BytePtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_installS3Signature");
  }

  public static boolean Rf_isFree(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isFree");
  }

  public static boolean Rf_isOrdered(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isOrdered");
  }

  public static boolean Rf_isUnordered(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isUnordered");
  }

  public static boolean Rf_isUnsorted(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("Rf_isUnsorted");
  }

  // SEXP Rf_lengthgets (SEXP, R_len_t)

  public static SEXP Rf_xlengthgets(SEXP p0, /*R_xlen_t*/ int p1) {
    throw new UnimplementedGnuApiMethod("Rf_xlengthgets");
  }

  public static SEXP R_lsInternal(SEXP env, boolean allNames) {
    return Environments.ls((Environment) env, allNames);
  }

  public static SEXP R_lsInternal3(SEXP env, boolean allNames, boolean sorted) {
    StringVector names = Environments.ls((Environment) env, allNames);
    if(sorted) {
      return Sort.sort(names, false);
    } else {
      return names;
    }
  }

  public static SEXP Rf_match(SEXP p0, SEXP p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_match");
  }

  public static SEXP Rf_matchE(SEXP p0, SEXP p1, int p2, SEXP p3) {
    throw new UnimplementedGnuApiMethod("Rf_matchE");
  }

  public static SEXP Rf_namesgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_namesgets");
  }

  public static SEXP Rf_mkChar(BytePtr string) {
    int length = string.nullTerminatedStringLength();
    byte[] copy = new byte[length+1];
    System.arraycopy(string.array, string.offset, copy, 0, length);
    
    return new GnuCharSexp(copy);
  }

  public static SEXP Rf_mkCharLen(BytePtr p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_mkCharLen");
  }

  public static boolean Rf_NonNullStringMatch(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_NonNullStringMatch");
  }

  public static int Rf_ncols(SEXP s) {
    if (Rf_isVector(s) || Rf_isList(s)) {
      Vector dim = s.getAttributes().getDim();
      if(dim.length() >= 2) {
        return dim.getElementAsInt(1);
      } else {
        return 1;
      }

    } else if (Rf_isFrame(s)) {
      return Rf_length(s);

    } else {
      throw new EvalException("object is not a matrix");
    }
  }

  public static int Rf_nrows(SEXP s) {
    if (Rf_isVector(s) || Rf_isList(s)) {
      Vector dim = s.getAttributes().getDim();
      if(dim.length() >= 1) {
        return dim.getElementAsInt(0);
      } else {
        return 1;
      }

    } else if (Rf_isFrame(s)) {
      return Rf_nrows(s.getElementAsSEXP(0));

    } else {
      throw new EvalException("object is not a matrix");
    }
  }

  public static SEXP Rf_nthcdr(SEXP p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_nthcdr");
  }

  // int R_nchar (SEXP string, nchar_type type_, Rboolean allowNA, Rboolean keepNA, const char *msg_name)

  public static boolean Rf_pmatch(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_pmatch");
  }

  public static boolean Rf_psmatch(BytePtr p0, BytePtr p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_psmatch");
  }

  public static void Rf_PrintValue(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_PrintValue");
  }

  // void Rf_readS3VarsFromFrame (SEXP, SEXP *, SEXP *, SEXP *, SEXP *, SEXP *, SEXP *)

  /**
   *  Set or remove a named attribute.
   *
   * @param vec Pointer to the value whose attributes are to be
   *          modified.
   *
   * @param name Either a pointer to the symbol representing the
   *          required attribute, or a pointer to a character vector
   *          containing the required symbol name as the first element; in
   *          the latter case, as a side effect, the corresponding
   *          symbol is installed if necessary.
   *
   * @param val Either the value to which the attribute is to be
   *          set, or R_NilValue.  In the latter case the
   *          attribute (if present) is removed.
   *
   * @return Refer to source code.  (Sometimes vec, sometimes 
   * val, sometime R_NilValue ...)
   */
  public static SEXP Rf_setAttrib(SEXP vec, SEXP name, SEXP val) {
    if(name == null) {
      throw new IllegalArgumentException("attributeName is NULL");
    }
    Symbol attributeSymbol = (Symbol) name;
    AbstractSEXP abstractSEXP = (AbstractSEXP) vec;
    abstractSEXP.unsafeSetAttributes(vec.getAttributes().copy().set(attributeSymbol, val));
    return vec;
  }

  // void Rf_setSVector (SEXP *, int, SEXP)

  public static void Rf_setVar(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_setVar");
  }

  public static SEXP Rf_stringSuffix(SEXP p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_stringSuffix");
  }

  public static /*SEXPTYPE*/ int Rf_str2type(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_str2type");
  }

  public static boolean Rf_StringBlank(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringBlank");
  }

  public static SEXP Rf_substitute(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_substitute");
  }

  public static BytePtr Rf_translateChar(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_translateChar");
  }

  public static BytePtr Rf_translateChar0(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_translateChar0");
  }

  /**
   *  Convert contents of a CHARSXP to UTF8.
   *
   * @param x Pointer to a CHARSXP.
   *
   * @return The text of {@code x} rendered in UTF8 encoding.
   */
  public static BytePtr Rf_translateCharUTF8(SEXP x) {
    throw new UnimplementedGnuApiMethod("Rf_translateCharUTF8");
  }

  /** Name of type within R.
   *
   * Translate a SEXPTYPE to the name by which it is known within R.
   *
   * @param st The SEXPTYPE whose name is required.
   *
   * @return The SEXPTYPE's name within R.
   */
  public static BytePtr Rf_type2char(/*SEXPTYPE*/ int st) {
    throw new UnimplementedGnuApiMethod("Rf_type2char");
  }

  public static SEXP Rf_type2rstr(/*SEXPTYPE*/ int p0) {
    throw new UnimplementedGnuApiMethod("Rf_type2rstr");
  }

  public static SEXP Rf_type2str(/*SEXPTYPE*/ int p0) {
    throw new UnimplementedGnuApiMethod("Rf_type2str");
  }

  public static SEXP Rf_type2str_nowarn(/*SEXPTYPE*/ int p0) {
    throw new UnimplementedGnuApiMethod("Rf_type2str_nowarn");
  }

  public static void Rf_unprotect_ptr(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_unprotect_ptr");
  }

  public static void R_signal_protect_error() {
    throw new UnimplementedGnuApiMethod("R_signal_protect_error");
  }

  public static void R_signal_unprotect_error() {
    throw new UnimplementedGnuApiMethod("R_signal_unprotect_error");
  }

  public static void R_signal_reprotect_error(/*PROTECT_INDEX*/ int i) {
    throw new UnimplementedGnuApiMethod("R_signal_reprotect_error");
  }

  public static SEXP R_tryEval(SEXP p0, SEXP p1, IntPtr p2) {
    throw new UnimplementedGnuApiMethod("R_tryEval");
  }

  public static SEXP R_tryEvalSilent(SEXP p0, SEXP p1, IntPtr p2) {
    throw new UnimplementedGnuApiMethod("R_tryEvalSilent");
  }

  public static BytePtr R_curErrorBuf() {
    throw new UnimplementedGnuApiMethod("R_curErrorBuf");
  }

  public static boolean Rf_isS4(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isS4");
  }

  public static SEXP Rf_asS4(SEXP p0, boolean p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_asS4");
  }

  public static SEXP Rf_S3Class(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_S3Class");
  }

  public static int Rf_isBasicClass(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_isBasicClass");
  }

  public static boolean R_cycle_detected(SEXP s, SEXP child) {
    throw new UnimplementedGnuApiMethod("R_cycle_detected");
  }

  // cetype_t Rf_getCharCE (SEXP)

  /**
   *  Get a pointer to a CHARSXP object.
   *
   * <p>If no CHARSXP with the specified text and encoding
   * currently exists, one will be created.  Otherwise a pointer to
   * the existing CHARSXP will be returned.
   *
   * @param str The null-terminated text of the required cached string.
   *
   * @param encoding The encoding of the required String.
   *          Only CE_NATIVE, CE_UTF8 or CE_LATIN1 are permitted in
   *          this context.
   *
   * @return Pointer to a string object representing the specified
   *         text in the specified encoding.
   */
  public static SEXP Rf_mkCharCE (BytePtr str, Object encoding) {
    throw new UnimplementedGnuApiMethod("Rf_mkCharCE");
  }

  /** Create a CHARSXP object for specified text and
   * encoding.
   *
   * <p>If no CHARSXP with the specified text and encoding
   * currently exists, one will be created.  Otherwise a pointer to
   * the existing CHARSXP will be returned.
   *
   * @param text The text of the string to be created, possibly
   *          including embedded null characters.  The encoding is
   *          assumed to be CE_NATIVE.
   *
   * @param length The length of the string pointed to by {@code text}.
   *          Must be non-negative.  The created string will comprise
   *          the text plus an appended null byte.
   *
   * @param encoding The encoding of the required String.
   *          Only CE_NATIVE, CE_UTF8 or CE_LATIN1 are permitted in
   *          this context.
   *
   * @return Pointer to the created string.
   */
  public static SEXP Rf_mkCharLenCE (BytePtr text, int length, Object encoding) {
    throw new UnimplementedGnuApiMethod("Rf_mkCharLenCE");
  }

  // const char* Rf_reEnc (const char *x, cetype_t ce_in, cetype_t ce_out, int subst)

  public static SEXP R_forceAndCall(SEXP e, int n, SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_forceAndCall");
  }

/* External pointer interface */

  /** Create an external pointer object.
   *
   * @param p The pointer that the external pointer object is
   *          to encapsulate.  May be NULL.
   * @param tag Pointer to the tag object.  May be R_NilValue (and
   *          often is).
   * @param prot Pointer to the protege object.  May be R_NilValue
   *          (and often is).
   *
   * @return Pointer to the created external pointer object.
   */
  public static SEXP R_MakeExternalPtr(Object p, SEXP tag, SEXP prot) {
    return new ExternalPtr<>(p, tag, prot);
  }


  /**
   *  Get the encapsulated external pointer.
   *
   * @param s Pointer to an external pointer object.
   *
   * @return the external pointer encapsulated by {@code s}
   */
  public static Object R_ExternalPtrAddr(SEXP s) {
    return ((ExternalPtr) s).getInstance();
  }

  /** Get pointer to tag object.
   *
   * @param s Pointer to an external pointer object.
   *
   * @return a pointer to the tag object of {@code s}.
   */
  public static SEXP R_ExternalPtrTag(SEXP s) {
    return ((ExternalPtr) s).getTag();
  }

  /** Get pointer to protege object.
   *
   * @param s Pointer to an external pointer object.
   *
   * @return a pointer to the protege object of {@code s}.
   */
  public static SEXP R_ExternalPtrProtected(SEXP s) {
    return ((ExternalPtr) s).getProtected();
  }

  /**  Reset the encapsulated pointer to R_NilValue.
   *
   * @param s Pointer to an external pointer object.
   */
  public static void R_ClearExternalPtr(SEXP s) {
    ((ExternalPtr<?>) s).unsafeSetAddress(null);
  }

  /** Set the value of the encapsulated pointer
   *
   * @param s Pointer to an external pointer object.
   *
   * @param p New pointer value (may be NULL).
   */
  @SuppressWarnings("unchecked")
  public static void R_SetExternalPtrAddr(SEXP s, Object p) {
    ((ExternalPtr) s).unsafeSetAddress(p);
  }

  /** Designate the tag object.
   *
   * @param s Pointer to an external pointer object.
   *
   * @param tag Pointer to the new tag object (or R_NilValue).
   */
  public static void R_SetExternalPtrTag(SEXP s, SEXP tag) {
    ((ExternalPtr) s).unsafeSetTag(tag);
  }

  /** Designate the protege object.
   *
   * @param s Pointer to an external pointer object.
   *
   * @param p Pointer to the new protege object (or R_NilValue).
   */
  public static void R_SetExternalPtrProtected(SEXP s, SEXP p) {
    ((ExternalPtr) s).unsafeSetProtected(p);
  }

  public static void R_RegisterFinalizer(SEXP s, SEXP fun) {
    throw new UnimplementedGnuApiMethod("R_RegisterFinalizer");
  }

  public static void R_RegisterFinalizerEx(SEXP s, SEXP fun, boolean onexit) {
    Native.currentContext().getSession().registerFinalizer(s, new FinalizationClosure((Closure)fun), onexit);
  }

  public static void R_RegisterCFinalizer (SEXP s, final MethodHandle fun) {
    R_RegisterCFinalizerEx(s, fun, false);
  }

  public static void R_RegisterCFinalizerEx (SEXP s, final MethodHandle fun, boolean onexit) {
    FinalizationHandler handler = new FinalizationHandler() {
      @Override
      public void finalize(Context context, SEXP sexp) {
        try {
          fun.invoke(sexp);
        } catch (Throwable throwable) {
          throw new RuntimeException(throwable);
        }
      }
    };
    Native.currentContext().getSession().registerFinalizer(s, handler, onexit);
  }

  public static void R_RunPendingFinalizers() {
    Native.currentContext().getSession().runFinalizers();
  }

  public static SEXP R_MakeWeakRef(SEXP key, SEXP val, SEXP fin, boolean onexit) {
    throw new UnimplementedGnuApiMethod("R_MakeWeakRef");
  }

  // SEXP R_MakeWeakRefC (SEXP key, SEXP val, R_CFinalizer_t fin, Rboolean onexit)

  public static SEXP R_WeakRefKey(SEXP w) {
    throw new UnimplementedGnuApiMethod("R_WeakRefKey");
  }

  public static SEXP R_WeakRefValue(SEXP w) {
    throw new UnimplementedGnuApiMethod("R_WeakRefValue");
  }

  public static void R_RunWeakRefFinalizer(SEXP w) {
    throw new UnimplementedGnuApiMethod("R_RunWeakRefFinalizer");
  }

  public static SEXP R_PromiseExpr(SEXP x) {
    return ((Promise) x).getExpression();
  }

  public static SEXP R_ClosureExpr(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_ClosureExpr");
  }

  public static void R_initialize_bcode() {
    throw new UnimplementedGnuApiMethod("R_initialize_bcode");
  }

  public static SEXP R_bcEncode(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_bcEncode");
  }

  public static SEXP R_bcDecode(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_bcDecode");
  }

  // Rboolean R_ToplevelExec (void(*fun)(void *), void *data)

  // SEXP R_ExecWithCleanup (SEXP(*fun)(void *), void *data, void(*cleanfun)(void *), void *cleandata)

  public static void R_RestoreHashCount(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_RestoreHashCount");
  }

  public static boolean R_IsPackageEnv(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_IsPackageEnv");
  }

  public static SEXP R_PackageEnvName(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_PackageEnvName");
  }

  public static SEXP R_FindPackageEnv(SEXP info) {
    throw new UnimplementedGnuApiMethod("R_FindPackageEnv");
  }

  public static boolean R_IsNamespaceEnv(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_IsNamespaceEnv");
  }

  public static SEXP R_NamespaceEnvSpec(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_NamespaceEnvSpec");
  }

  public static SEXP R_FindNamespace(SEXP namespaceExp) throws Exception {
    Context context = Native.currentContext();
    return R$primitive$getNamespace.doApply(context, context.getEnvironment(), namespaceExp);
  }

  public static void R_LockEnvironment(SEXP env, boolean bindings) {
    throw new UnimplementedGnuApiMethod("R_LockEnvironment");
  }

  public static boolean R_EnvironmentIsLocked(SEXP env) {
    throw new UnimplementedGnuApiMethod("R_EnvironmentIsLocked");
  }

  public static void R_LockBinding(SEXP sym, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_LockBinding");
  }

  public static void R_unLockBinding(SEXP sym, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_unLockBinding");
  }

  public static void R_MakeActiveBinding(SEXP sym, SEXP fun, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_MakeActiveBinding");
  }

  public static boolean R_BindingIsLocked(SEXP sym, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_BindingIsLocked");
  }

  public static boolean R_BindingIsActive(SEXP sym, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_BindingIsActive");
  }

  public static boolean R_HasFancyBindings(SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_HasFancyBindings");
  }

  // void Rf_errorcall (SEXP, const char *,...)

  // void Rf_warningcall (SEXP, const char *,...)

  // void Rf_warningcall_immediate (SEXP, const char *,...)

  public static void R_XDREncodeDouble(double d, Object buf) {
    throw new UnimplementedGnuApiMethod("R_XDREncodeDouble");
  }

  public static double R_XDRDecodeDouble(Object buf) {
    throw new UnimplementedGnuApiMethod("R_XDRDecodeDouble");
  }

  public static void R_XDREncodeInteger(int i, Object buf) {
    throw new UnimplementedGnuApiMethod("R_XDREncodeInteger");
  }

  public static int R_XDRDecodeInteger(Object buf) {
    throw new UnimplementedGnuApiMethod("R_XDRDecodeInteger");
  }

  // void R_InitInPStream (R_inpstream_t stream, R_pstream_data_t data, R_pstream_format_t type, int(*inchar)(R_inpstream_t), void(*inbytes)(R_inpstream_t, void *, int), SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitOutPStream (R_outpstream_t stream, R_pstream_data_t data, R_pstream_format_t type, int version, void(*outchar)(R_outpstream_t, int), void(*outbytes)(R_outpstream_t, void *, int), SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitFileInPStream (R_inpstream_t stream, FILE *fp, R_pstream_format_t type, SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitFileOutPStream (R_outpstream_t stream, FILE *fp, R_pstream_format_t type, int version, SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_Serialize (SEXP s, R_outpstream_t ops)

  // SEXP R_Unserialize (R_inpstream_t ips)

  public static SEXP R_do_slot(SEXP obj, SEXP name) {
    Context context = Native.currentContext();
    MethodDispatch methodDispatch = context.getSingleton(MethodDispatch.class);
    return Subsetting.getSlotValue(context, methodDispatch, obj, ((Symbol) name));
  }

  public static SEXP R_do_slot_assign(SEXP obj, SEXP name, SEXP value) {

    /* Ensure that name is a symbol */
    if(name instanceof StringVector && LENGTH(name) == 1) {
      name = Symbol.get(name.asString());
    } else if(name instanceof GnuCharSexp) {
      name = Symbol.get(((GnuCharSexp) name).getValue().nullTerminatedString());
    }
    if(!(name instanceof Symbol)) {
      throw new EvalException("invalid type or length for slot name");
    }

    return Methods.R_set_slot(Native.currentContext(), obj, name.asString(), value);
  }

  public static int R_has_slot(SEXP obj, SEXP name) {
    throw new UnimplementedGnuApiMethod("R_has_slot");
  }

  public static SEXP R_do_MAKE_CLASS(BytePtr what) {
    if(what == null || what.array == null) {
      throw new EvalException("C level MAKE_CLASS macro called with NULL string pointer");
    }
    Context context = Native.currentContext();
    Namespace methodsNamespace = context.getNamespaceRegistry().getNamespace(context, "methods");

    return context.evaluate(FunctionCall.newCall(Symbol.get("getClass"),
        StringVector.valueOf(what.nullTerminatedString())));
  }

  public static SEXP R_getClassDef(BytePtr what) {
    if(what == null || what.array == null) {
      throw new EvalException("R_getClassDef(.) called with NULL string pointer");
    }

    return R_getClassDef_R(StringArrayVector.valueOf(what.nullTerminatedString()));
  }

  public static SEXP R_getClassDef_R(SEXP what) {
    Context context = Native.currentContext();
    Namespace methodsNamespace = context.getNamespaceRegistry().getNamespace(context, "methods");

    return context.evaluate(FunctionCall.newCall(Symbol.get("getClassDef"), what));
  }

  public static boolean R_has_methods_attached() {
    throw new UnimplementedGnuApiMethod("R_has_methods_attached");
  }

  public static boolean R_isVirtualClass(SEXP class_def, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_isVirtualClass");
  }

  public static boolean R_extends(SEXP class1, SEXP class2, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_extends");
  }

  public static SEXP R_do_new_object(SEXP class_def) {

    if(class_def == null) {
      throw new EvalException("C level NEW macro called with null class definition pointer");
    }
    SEXP virtual = R_do_slot(class_def, Symbol.get("virtual"));
    SEXP className = R_do_slot(class_def, Symbol.get("className"));

    if(virtual.asLogical() != Logical.FALSE)  { /* includes NA, TRUE, or anything other than FALSE */
      throw new EvalException("trying to generate an object from a virtual class (\"%s\")", className.asString());
    }
    SEXP value = Rf_duplicate(R_do_slot(class_def, Symbol.get("prototype")));
    if(value instanceof S4Object || Rf_getAttrib(className, R_PackageSymbol) != R_NilValue) {
      /* Anything but an object from a base "class" (numeric, matrix,..) */
      Rf_setAttrib(value, R_ClassSymbol, className);
      SET_S4_OBJECT(value);
    }
    return value;
  }

  /**
   * Return the 0-based index of an is() match in a vector of class-name
   * strings terminated by an empty string.  Returns -1 for no match.
   *
   * @param x  an R object, about which we want is(x, .) information.
   * @param valid vector of possible matches terminated by an empty string.
   * @param rho  the environment in which the class definitions exist.
   *
   * @return index of match or -1 for no match
   */
  public static int R_check_class_and_super (SEXP x, ObjectPtr<BytePtr> valid, SEXP rho) {
    int ans;
    SEXP cl = Rf_asChar(Rf_getAttrib(x, R_ClassSymbol));
    BytePtr class_ = R_CHAR(cl);
    for (ans = 0; ; ans++) {
      if (Stdlib.strlen(valid.get(ans)) == 0) { // empty string
        break;
      }
      if (Stdlib.strcmp(class_, valid.get(ans)) == 0) {
        return ans;
      }
    }
    /* if not found directly, now search the non-virtual super classes :*/
    if(IS_S4_OBJECT(x) != 0) {
        /* now try the superclasses, i.e.,  try   is(x, "....");  superCl :=
           .selectSuperClasses(getClass("....")@contains, dropVirtual=TRUE)  */
      SEXP classExts, superCl, _call;
      Symbol s_contains      = Symbol.get("contains");
      Symbol s_selectSuperCl = Symbol.get(".selectSuperClasses");
      SEXP classDef = R_getClassDef(class_);
      classExts = R_do_slot(classDef, s_contains);
      _call = Rf_lang3(s_selectSuperCl, classExts,
                              /* dropVirtual = */ Rf_ScalarLogical(1));
      superCl = Rf_eval(_call, rho);
      for(int i=0; i < LENGTH(superCl); i++) {
        BytePtr s_class = R_CHAR(STRING_ELT(superCl, i));
        for (ans = 0; ; ans++) {
          if (Stdlib.strlen(valid.get(ans)) == 0) {
            break;
          }
          if (Stdlib.strcmp(s_class, valid.get(ans)) == 0) {
            return ans;
          }
        }
      }
    }
    return -1;
  }

  public static int R_check_class_etc (SEXP x, ObjectPtr<BytePtr> valid) {
    SEXP cl = Rf_getAttrib(x, R_ClassSymbol);
    SEXP rho = R_GlobalEnv;
    SEXP pkg;
    Symbol meth_classEnv = Symbol.get(".classEnv");

    pkg = Rf_getAttrib(cl, R_PackageSymbol); /* ==R== packageSlot(class(x)) */
    if(!Rf_isNull(pkg)) { /* find  rho := correct class Environment */
      SEXP clEnvCall;
      // FIXME: fails if 'methods' is not loaded.
      clEnvCall = Rf_lang2(meth_classEnv, cl);
      rho = Rf_eval(clEnvCall, methodsNamespace());
      if(!Rf_isEnvironment(rho)) {
        throw new EvalException("could not find correct environment; please report!");
      }
    }
    return R_check_class_and_super(x, valid, rho);
  }

  private static Environment methodsNamespace() {
    return Native.currentContext().getNamespaceRegistry().getNamespaceIfPresent(Symbol.get("methods")).get().getNamespaceEnvironment();
  }

  public static void R_PreserveObject(SEXP p0) {
    // NOOP
    // We have a garbage collector.
  }

  public static void R_ReleaseObject(SEXP p0) {
    // NOOP
    // We have a garbage collector.
  }

  public static void R_dot_Last() {
    throw new UnimplementedGnuApiMethod("R_dot_Last");
  }

  public static void R_RunExitFinalizers() {
    throw new UnimplementedGnuApiMethod("R_RunExitFinalizers");
  }

  public static int R_system(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("R_system");
  }

  public static boolean R_compute_identical(SEXP p0, SEXP p1, int p2) {
    throw new UnimplementedGnuApiMethod("R_compute_identical");
  }

  public static void R_orderVector(IntPtr indx, int n, SEXP arglist, boolean nalast, boolean decreasing) {
    throw new UnimplementedGnuApiMethod("R_orderVector");
  }

  /**
   * Create a vector object.
   *
   *  Allocate a vector object.  This ensures only validity of
   *  SEXPTYPE values representing lists (as the elements must be
   *  initialized).
   *
   * @param stype The type of vector required.
   *
   * @param length The length of the vector to be created.
   *
   * @return Pointer to the created vector.
   */
  public static SEXP Rf_allocVector(/*SEXPTYPE*/ int stype, /*R_xlen_t*/ int length) {
    switch (stype) {
      case SexpType.INTSXP:
        return new IntArrayVector(new int[length]);
      case SexpType.REALSXP:
        return new DoubleArrayVector(new double[length]);
      case SexpType.LGLSXP:
        return new LogicalArrayVector(new int[length]);
      case SexpType.VECSXP:
        SEXP[] elements = new SEXP[length];
        Arrays.fill(elements, Null.INSTANCE);
        return new ListVector(elements);
      case SexpType.STRSXP:
        return new GnuStringVector(new BytePtr[length]);
      case SexpType.RAWSXP:
        return new RawVector(new byte[length]);
    }
    throw new UnimplementedGnuApiMethod("Rf_allocVector: type = " + stype);
  }

  public static boolean Rf_conformable(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_conformable");
  }

  public static SEXP Rf_elt(SEXP p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_elt");
  }

  public static boolean Rf_inherits(SEXP p0, BytePtr p1) {
    return p0.inherits(p1.nullTerminatedString());
  }

  public static boolean Rf_isArray(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isArray");
  }

  public static boolean Rf_isFactor(SEXP p0) {
    return Types.isFactor(p0);
  }

  public static boolean Rf_isFrame(SEXP s) {
    return s.inherits("data.frame");
  }

  public static boolean Rf_isFunction(SEXP sexp) {
    return sexp instanceof Function;
  }

  public static boolean Rf_isInteger(SEXP p0) {
    return p0 instanceof IntVector;
  }

  public static boolean Rf_isLanguage(SEXP p0) {
    return p0 instanceof FunctionCall;
  }

  public static boolean Rf_isList(SEXP s) {
    return (s == Null.INSTANCE || s instanceof PairList.Node);
  }

  public static boolean Rf_isMatrix(SEXP p0) {
    return Types.isMatrix(p0);
  }

  public static boolean Rf_isNewList(SEXP p0) {
    return p0 instanceof ListVector;
  }

  public static boolean Rf_isNumber(SEXP p0) {
    if(p0 instanceof IntVector) {
      return !p0.inherits("factor");
    } else {
      return p0 instanceof LogicalVector ||
          p0 instanceof DoubleVector ||
          p0 instanceof ComplexVector;
    }
  }

  public static boolean Rf_isNumeric(SEXP p0) {
    return Types.isNumeric(p0);
  }

  public static boolean Rf_isPairList(SEXP p0) {
    return Types.isPairList(p0);
  }

  public static boolean Rf_isPrimitive(SEXP p0) {
    return p0 instanceof PrimitiveFunction;
  }

  public static boolean Rf_isTs(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isTs");
  }

  public static boolean Rf_isUserBinop(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isUserBinop");
  }

  public static boolean Rf_isValidString(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isValidString");
  }

  public static boolean Rf_isValidStringF(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isValidStringF");
  }

  /** Is an R value a vector?
   *
   * Vector in this context embraces R matrices and arrays.
   *
   * @param s Pointer to the R value to be tested.  The value may be
   *          R_NilValue, in which case the function returns FALSE.
   *
   * @return TRUE iff {@code s} points to a vector object.
   */
  public static boolean Rf_isVector(SEXP s) {
    return s instanceof Vector;
  }

  public static boolean Rf_isVectorAtomic(SEXP s) {
    return s instanceof AtomicVector;
  }

  public static boolean Rf_isVectorList(SEXP s) {
    return s instanceof ListVector;
  }

  public static boolean Rf_isVectorizable(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isVectorizable");
  }

  public static SEXP Rf_lang1(SEXP p0) {
    return FunctionCall.newCall(p0);
  }

  public static SEXP Rf_lang2(SEXP p0, SEXP p1) {
    return FunctionCall.newCall(p0, p1);
  }

  public static SEXP Rf_lang3(SEXP p0, SEXP p1, SEXP p2) {
    return FunctionCall.newCall(p0, p1, p2);
  }

  public static SEXP Rf_lang4(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    return FunctionCall.newCall(p0, p1, p2, p3);
  }

  public static SEXP Rf_lang5(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    return FunctionCall.newCall(p0, p1, p2, p3, p4);
  }

  public static SEXP Rf_lang6(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4, SEXP p5) {
    return FunctionCall.newCall(p0, p1, p2, p3, p4, p5);
  }

  public static SEXP Rf_lastElt(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_lastElt");
  }

  /**
   *  Create an Expression with a specified car and tail.
   *
   * This function protects its arguments from the garbage collector.
   *
   * @param cr Pointer to the 'car' of the element to be created.
   *
   * @param tl Pointer to the 'tail' of the element to be created,
   *          which must be a pairlist or R_NilValue.
   *
   * @return Pointer to the constructed list.
   */
  public static SEXP Rf_lcons(SEXP cr, SEXP tl) {
    return new FunctionCall(cr, (PairList) tl);
  }

  public static int Rf_length (SEXP sexp) {
    return sexp.length();
  }

  public static SEXP Rf_list1(SEXP p0) {
    return new PairList.Node(p0, Null.INSTANCE);
  }

  public static SEXP Rf_list2(SEXP p0, SEXP p1) {
    return
        new PairList.Node(p0,
           new PairList.Node(p1, Null.INSTANCE));
  }

  public static SEXP Rf_list3(SEXP p0, SEXP p1, SEXP p2) {
    return 
        new PairList.Node(p0,
          new PairList.Node(p1, 
            new PairList.Node(p2, Null.INSTANCE)));
  }

  public static SEXP Rf_list4(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    return 
        new PairList.Node(p0,
            new PairList.Node(p1, 
                new PairList.Node(p2, 
                    new PairList.Node(p3, Null.INSTANCE))));
  }

  public static SEXP Rf_list5(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    return 
        new PairList.Node(p0,
            new PairList.Node(p1,
                new PairList.Node(p2,
                    new PairList.Node(p3,
                        new PairList.Node(p4, Null.INSTANCE)))));
  }

  public static SEXP Rf_listAppend(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_listAppend");
  }

  public static SEXP Rf_mkNamed (int sexpType, ObjectPtr<BytePtr> names) {
    if(sexpType == SexpType.VECSXP) {
      ListVector.NamedBuilder list = new ListVector.NamedBuilder();
      int i = 0;
      while(true) {
        String name = names.get(i).nullTerminatedString();
        if(name.isEmpty()) {
          break;
        }
        list.add(name, Null.INSTANCE);
        i++;
      }
      return list.build();
    } else {
      throw new UnsupportedOperationException("mkNamed: type = " + sexpType);
    }
  }

  public static SEXP Rf_mkString(BytePtr string) {
    return new StringArrayVector(string.nullTerminatedString());
  }

  public static int Rf_nlevels(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_nlevels");
  }

  public static int Rf_stringPositionTr(SEXP p0, BytePtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_stringPositionTr");
  }

  // SEXP Rf_ScalarComplex (Rcomplex)

  public static SEXP Rf_ScalarInteger(int p0) {
    return new IntArrayVector(p0);
  }

  public static SEXP Rf_ScalarLogical(int p0) {
    if(p0 == LogicalVector.NA) {
      return LogicalVector.NA_VECTOR;
    } else if(p0 == 0) {
      return LogicalVector.FALSE;
    } else {
      return LogicalVector.TRUE;
    }
  }

  // SEXP Rf_ScalarRaw (Rbyte)

  public static SEXP Rf_ScalarReal(double p0) {
    return new DoubleArrayVector(p0);
  }

  public static SEXP Rf_ScalarString(SEXP p0) {
    return new GnuStringVector(((GnuCharSexp) p0).getValue());
  }

  public static /*R_xlen_t*/ int Rf_xlength(SEXP p0) {
    return p0.length();
  }

  /** Push a node pointer onto the C pointer protection stack.
   *
   * <p>In Renjin, this is a NO-OP.</p>
   *
   * @param node Pointer to the node to be protected from the
   *          garbage collector.
   * @return a copy of {@code node}
   */
  public static SEXP Rf_protect(SEXP node) {
    // NOOP
    return node;
  }

  /**
   *  Pop cells from the C pointer protection stack.
   *
   * <p>In Renjin, this is a NO-OP.</p>
   *
   * @param count Number of cells to be popped.  Must not be
   *          larger than the current size of the C pointer
   *          protection stack.
   */
  public static void Rf_unprotect(int count) {
    // NOOP
  }


  /**
   *
   * Push a node pointer onto the C pointer protection stack.
   *
   * <p>Push a node pointer onto the C pointer protection stack, and
   * record the index of the resulting stack cell (for subsequent
   * use with R_Reprotect).
   *
   * <p>In Renjin, this is a NO-OP.
   *
   * @param node Pointer to the node to be protected from the
   *          garbage collector.
   *
   * @param iptr Pointer to a location in which the stack cell index
   *          is to be stored.
   */
  public static void R_ProtectWithIndex(SEXP node, /*PROTECT_INDEX **/ IntPtr iptr) {
    // NOOP
  }

  /** Retarget a cell in the C pointer protection stack.
   *
   * <p>Change the node that a particular cell in the C pointer
   * protection stack protects.  As a consistency check, it is
   * required that the reprotect takes place within the same
   * ProtectStack::Scope as the original protect.
   *
   * <p>In Renjin, this is a NO-OP.</p>
   *
   * @param node Pointer to the node now to be protected from
   *          the garbage collector by the designated stack
   *          cell.  (Not necessarily a different node from the
   *          one currently protected.)
   *
   * @param index Index (as returned by R_ProtectWithIndex() ) of
   *          the stack cell to be retargeted to node.  Must be less
   *          than the current size of the C pointer protection
   *          stack.
   */
  public static void R_Reprotect(SEXP node, /*PROTECT_INDEX*/ int index) {
    // NOOP
  }

  public static SEXP R_FixupRHS(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("R_FixupRHS");
  }
}
