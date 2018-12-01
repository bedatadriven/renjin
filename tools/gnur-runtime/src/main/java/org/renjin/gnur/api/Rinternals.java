/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.*;
import org.renjin.gcc.annotations.GlobalVar;
import org.renjin.gcc.annotations.Noop;
import org.renjin.gcc.format.FormatArrayInput;
import org.renjin.gcc.runtime.*;
import org.renjin.gnur.api.annotations.Allocator;
import org.renjin.gnur.api.annotations.Mutee;
import org.renjin.gnur.api.annotations.PotentialMutator;
import org.renjin.methods.MethodDispatch;
import org.renjin.methods.Methods;
import org.renjin.primitives.*;
import org.renjin.primitives.match.Duplicates;
import org.renjin.primitives.packaging.Namespaces;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.lang.System;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.renjin.primitives.Types.isFactor;
import static org.renjin.primitives.vector.RowNamesVector.isCompactForm;
import static org.renjin.primitives.vector.RowNamesVector.isOldCompactForm;

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
  @Deprecated
  public static SEXP	R_GlobalEnv;

  @GlobalVar
  public static SEXP R_GlobalEnv() {
    return Native.currentContext().getGlobalEnvironment();
  }

  /**
   * The empty environment at the root of the environment tree
   */
  public static SEXP R_EmptyEnv = Environment.EMPTY;

  /**
   * The base environment; formerly R_NilEnv
   */
  @Deprecated
  public static SEXP  R_BaseEnv;

  @GlobalVar
  public static SEXP R_BaseEnv() {
    return Native.currentContext().getBaseEnvironment();
  }

  /**
   * The (fake) namespace for base
   */
  @Deprecated
  public static SEXP R_BaseNamespace;

  @GlobalVar
  public static SEXP R_BaseNamespace() {
    return Native.currentContext().getNamespaceRegistry().getBaseNamespaceEnv();
  }


  /**
   *  Registry for registered namespaces
   */
  @Deprecated
  public static SEXP R_NamespaceRegistry;

  @GlobalVar
  public static SEXP R_NamespaceRegistry() {
    return Namespaces.getNamespaceRegistry(Native.currentContext().getNamespaceRegistry());
  }

  /**
   * Current srcref for debuggers
   */
  public static SEXP	R_Srcref;

  /* Special Values */

  /**
   * The nil object
   */
  public final static SEXP	R_NilValue = Null.INSTANCE;

  /**
   * Unbound marker
   */
  public static final  SEXP	R_UnboundValue = Symbol.UNBOUND_VALUE;

  /**
   * Missing argument marker
   */
  public static final  SEXP	R_MissingArg = Symbol.MISSING_ARG;

  /**
   * Marker for restarted function calls
   */
  public static SEXP	R_RestartToken;

  /* Symbol Table Shortcuts */

  /**
   * "base" Symbol
   */
  public static final  SEXP	R_baseSymbol = Symbol.get("base");

  /**
   * "base" Symbol
   */
  public static final SEXP	R_BaseSymbol = Symbol.get("base");	// "base"

  /**
   * {@code { Symbol
   */
  public static final SEXP	R_BraceSymbol = Symbol.get("{");

  /**
   * "[[" Symbol
   */
  public static final SEXP	R_Bracket2Symbol = Symbol.get("[[");

  /**
   * "[" Symbol
   */
  public static final SEXP	R_BracketSymbol = Symbol.get("[");

  /**
   * "class" Symbol
   */
  public static final SEXP	R_ClassSymbol = Symbol.get("class");

  /**
   * ".Device" symbol
   */
  public static final  SEXP	R_DeviceSymbol = Symbol.get(".Device");

  /**
   * "dimnames" symbol
   */
  public static  final SEXP	R_DimNamesSymbol = Symbol.get("dimnames");

  /**
   * "dim" symbol
   */
  public static final SEXP	R_DimSymbol = Symbol.get("dim");

  /**
   * "$" Symbol
   */
  public static final SEXP	R_DollarSymbol = Symbol.get("$");

  /**
   * "..." Symbol
   */
  public static  final SEXP	R_DotsSymbol = Symbol.get("...");

  /**
   * "::" Symbol
   */
  public static final  SEXP	R_DoubleColonSymbol = Symbol.get("::");

  /**
   * "drop" Symbol
   */
  public static final  SEXP	R_DropSymbol = Symbol.get("drop");

  /**
   * ".Last.value" Symbol
   */
  public static final SEXP	R_LastvalueSymbol = Symbol.get(".Last.value");

  /**
   * "level" Symbol
   */
  public static final  SEXP	R_LevelsSymbol = Symbol.get("levels");

  /**
   * "mode" symbol
   */
  public static final  SEXP	R_ModeSymbol = Symbol.get("mode");

  /**
   * "na.rm" Symbol
   */
  public static final SEXP	R_NaRmSymbol = Symbol.get("na.rm");

  /**
   * "name" Symbol
   */
  public static final  SEXP	R_NameSymbol = Symbol.get("name");

  /**
   * "names" Symbol
   */
  public static final  SEXP	R_NamesSymbol = Symbol.get("names");

  /**
   * ".__NAMESPACE__." Symbol
   */
  public static  final SEXP	R_NamespaceEnvSymbol = Symbol.get(".__NAMESPACE__.");

  /**
   * "package" Symbol
   */
  public static final SEXP	R_PackageSymbol = Symbol.get("package");

  /**
   * "previous" Symbol
   */
  public static final  SEXP	R_PreviousSymbol = Symbol.get("previous");

  /**
   * "quote" Symbol
   */
  public static final SEXP	R_QuoteSymbol = Symbol.get("quote");

  /**
   * "row.names" Symbol
   */
  public static final SEXP	R_RowNamesSymbol = Symbol.get("row.names");

  /**
   * ".Random.seed" Symbol
   */
  public static final SEXP	R_SeedsSymbol = Symbol.get(".Random.seed");

  /**
   * "sort.list" Symbol
   */
  public static final  SEXP	R_SortListSymbol = Symbol.get("sort.list");

  /**
   * "source" Symbol
   */
  public static final  SEXP	R_SourceSymbol = Symbol.get("source");

  /**
   * "spec" Symbol
   */
  public static final  SEXP	R_SpecSymbol = Symbol.get("spec");

  /**
   * ":::" Symbol
   */
  public static final  SEXP	R_TripleColonSymbol = Symbol.get(":::");

  /**
   * "tsp" Symbol
   */
  public static final  SEXP	R_TspSymbol = Symbol.get("tsp");

  /**
   * ".defined" Symbol
   */
  public static final  SEXP  R_dot_defined = Symbol.get(".defined");

  /**
   * ".Method" Symbol
   */
  public static final SEXP  R_dot_Method = Symbol.get(".Method");

  /**
   * ".packageName" Symbol
   */
  public static final SEXP	R_dot_packageName = Symbol.get(".packageName");

  /**
   * ".target" Symbol
   */
  public static final SEXP  R_dot_target = Symbol.get(".target");

/* Missing Values - others from Arith.h */

  /**
   * NA_String as a CHARSXP
   */
  public static final SEXP	R_NaString = GnuCharSexp.NA_STRING;

  /**
   * ""as a CHARSEXP
   */
  public static final SEXP	R_BlankString = GnuCharSexp.BLANK_STRING;

  /**
   * "" as a STRSXP
   */
  public static final SEXP	R_BlankScalarString = new GnuStringVector(GnuCharSexp.BLANK_STRING);

  public static final int CE_NATIVE = 0;
  public static final int CE_UTF8 = 1;
  public static final int CE_LATIN1 = 2;
  public static final int CE_BYTES = 3;
  public static final int CE_SYMBOL = 5;
  public static final int CE_ANY = 99;


  public static BytePtr R_CHAR(SEXP x) {
    GnuCharSexp charSexp = (GnuCharSexp) x;
    return charSexp.getValue();
  }

  public static boolean Rf_isNull(SEXP s) {
    return Types.isNull(s);
  }

  public static boolean Rf_isSymbol(SEXP s) {
    return Types.isSymbol(s);
  }

  public static boolean Rf_isLogical(SEXP s) {
    return Types.isLogical(s);
  }

  public static boolean Rf_isReal(SEXP s) {
    return Types.isReal(s);
  }

  public static boolean Rf_isComplex(SEXP s) {
    return Types.isComplex(s);
  }

  public static boolean Rf_isExpression(SEXP s) {
    return Types.isExpression(s);
  }

  public static boolean Rf_isEnvironment(SEXP s) {
    return Types.isEnvironment(s);
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
    return Types.isObject(s);
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
    } else if(s instanceof GnuCharSexp) {
      return SexpType.CHARSXP;
    } else if(s instanceof SpecialFunction) {
      return SexpType.SPECIALSXP;
    } else if(s instanceof Function) {
      return SexpType.BUILTINSXP;
    } else if(s instanceof ExternalPtr) {
      return SexpType.EXTPTRSXP;
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

  @Noop
  public static void SET_NAMED(SEXP sexp, int value) {
    // NOOP
  }

  public static int REFCNT(SEXP x) {
    throw new UnimplementedGnuApiMethod("REFCNT");
  }

  public static void SET_OBJECT(SEXP x, int v) {
    if(x.isObject() && v == 0) {
      throw new EvalException("SET_OBJECT: value SEXP Object field doesn't match expected value");
    }
  }

  public static void SET_TYPEOF(@Mutee SEXP x, int v) {
    if(TYPEOF(x) != v) {
      throw new UnimplementedGnuApiMethod(String.format("Cannot change SEXP of type '%s' to '%s'",
          SexpType.typeName(TYPEOF(x)),
          SexpType.typeName(v)));
    }
  }

  public static void SET_ATTRIB(SEXP x, SEXP v) {
    if(v instanceof PairList) {
      ((AbstractSEXP)x).unsafeSetAttributes(AttributeMap.fromPairList((PairList) v));
    } else {
      ((AbstractSEXP)x).unsafeSetAttributes(v.getAttributes());
    }
  }

  public static void DUPLICATE_ATTRIB(SEXP to, SEXP from) {
    AbstractSEXP abstractSEXP = (AbstractSEXP) to;
    if (Types.isS4(from) && !Types.isS4(to)) {
      abstractSEXP.unsafeSetAttributes(from.getAttributes().copy().setS4(true));
    } else {
      abstractSEXP.unsafeSetAttributes(from.getAttributes().copy());
    }
  }

  public static int IS_S4_OBJECT(SEXP x) {
    return Types.isS4(x) ? 1 : 0;
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
    return 0;
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
    final SEXP levels = x.getAttribute(Symbols.LEVELS);
    if (Null.INSTANCE == levels) {
      return IntVector.NA;
    } else {
      return levels.asInt();
    }
  }

  public static int SETLEVELS(SEXP x, int v) {
    AbstractSEXP abstractSEXP = (AbstractSEXP) x;
    abstractSEXP.unsafeSetAttributes(x.getAttributes().copy().set(Symbols.LEVELS, IntVector.valueOf(v)));
    return LEVELS(x);
  }

  public static Object DATAPTR(SEXP x) {
    if (x instanceof IntVector || x instanceof LogicalVector) {
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

  /**
   * @deprecated Return type changed, see {@link Rinternals2}
   */
  @Deprecated
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
    } else if(x == Null.INSTANCE) {
      return new IntPtr(0);
    } else {
      throw new EvalException("INTEGER(): expected integer vector, found %s", x.getTypeName());
    }
  }

  public static BytePtr RAW(SEXP x) {
    if(x instanceof RawVector) {
      return new BytePtr(((RawVector) x).toByteArrayUnsafe());
    }
    throw new EvalException("RAW(): Expected raw vector, found %s", x.getTypeName());
  }

  /**
   * Return type changed. See {@link Rinternals2#REAL}
   */
  @Deprecated
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
    if(x instanceof GnuStringVector) {
      return ((GnuStringVector) x).getElementAsCharSexp(i);
    }
    StringVector stringVector = (StringVector) x;
    String string = stringVector.getElementAsString(i);

    return GnuCharSexp.valueOf(string);
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
    if(x instanceof FunctionCall || x instanceof PairList){
      return ((PairList) x).getElementAsSEXP(i);
    }
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
  public static void SET_STRING_ELT(@Mutee SEXP x, /*R_xlen_t*/ int i, SEXP v) {
    if(x instanceof GnuStringVector) {
      GnuStringVector stringVector = (GnuStringVector) x;
      GnuCharSexp charValue = (GnuCharSexp) v;

      stringVector.set(i, charValue);
    } else {
      throw new IllegalStateException("Attempt to modify a shared SEXP");
    }
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
    return ((PairList.Node) e).getRawTag();
  }

  public static SEXP CAR(SEXP e) {

    if(e == Null.INSTANCE) {
      return Null.INSTANCE;
    }

    // Other SEXPRECs share a similar structure to PairList
    if(e instanceof Symbol) {
      return new GnuCharSexp((Symbol) e);
    } else if(e instanceof Closure) {
      return ((Closure) e).getFormals();
    } else {
      return ((PairList.Node) e).getValue();
    }
  }

  public static SEXP CDR(SEXP e) {
    if(e instanceof Null) {
      return Null.INSTANCE;
    }
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
    ((PairList)x).setTag(y);
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

    ((PairList.Node) x).setNextNode((PairList)y);
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
    ((Closure) x).unsafeSetFormals(((PairList) v));
  }

  public static void SET_BODY(SEXP x, SEXP v) {
    ((Closure) x).unsafeSetBody(v);
  }

  public static int IS_CACHED(SEXP x) {
    return 0;
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
    ((Closure) x).unsafeSetEnclosingEnvironment(((Environment) v));
  }

  /** Symbol name.
   *
   * @param x Pointer to a symbol.
   *
   * @return Pointer to a CHARSXP representing {@code x}'s name.
   */
  public static SEXP PRINTNAME(SEXP x) {
    return GnuCharSexp.valueOf(((Symbol) x).getPrintName());
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
    return ((Environment) x).getParent();
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

  public static void SET_ENCLOS(SEXP env, SEXP parent) {
    ((Environment) env).setParent(((Environment) parent));
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
    if(p0.length() == 0) {
      return R_NaString;
    }
    return GnuCharSexp.valueOf(((AtomicVector) p0).getElementAsString(0));
  }

  /**
   * Coerces a vector from one type to another. Calling code expects to be
   * able to safely modify the resulting vector.
   */
  public static SEXP Rf_coerceVector(SEXP p0, /*SEXPTYPE*/ int type) {
    switch(type) {
      case SexpType.LISTSXP:
        return PairList.Node.fromVector((Vector)p0);
      case SexpType.LGLSXP:
        return Vectors.asLogical(((Vector) p0)).setAttributes(p0.getAttributes());
      case SexpType.INTSXP:
        return asIntArrayVector((Vector)p0);
      case SexpType.REALSXP:
        return asDoubleArrayVector((Vector) p0);
      case SexpType.CPLXSXP:
        return Vectors.asComplex((Vector)p0).setAttributes(p0.getAttributes());
      case SexpType.STRSXP:
        return Vectors.asCharacter(Native.currentContext(), (Vector)p0).setAttributes(p0.getAttributes());
      case SexpType.EXPRSXP:
        return toExpressionList(p0);
    }
    throw new UnimplementedGnuApiMethod("Rf_coerceVector: " + type);
  }

  private static SEXP asIntArrayVector(Vector vector) {
    Vectors.checkForListThatCannotBeCoercedToAtomicVector(vector, "integer");

    IntArrayVector.Builder builder = new IntArrayVector.Builder(0, vector.length());
    Vector integerVector = Vectors.convertToAtomicVector(builder, vector);
    return integerVector.setAttributes(vector.getAttributes());
  }

  private static SEXP asDoubleArrayVector(Vector vector) {
    Vectors.checkForListThatCannotBeCoercedToAtomicVector(vector, "double");

    DoubleArrayVector.Builder builder = new DoubleArrayVector.Builder(0, vector.length());
    Vector integerVector = Vectors.convertToAtomicVector(builder, vector);
    return integerVector.setAttributes(vector.getAttributes());
  }

  private static SEXP toExpressionList(SEXP sexp) {
    if(sexp instanceof Vector) {
      return Vectors.asVector(((Vector) sexp), "expression");
    } else if(sexp instanceof PairList) {
      return toExpressionList(((PairList) sexp).toVector());
    } else {
      throw new UnsupportedOperationException("Rf_coerceVector: from: " + sexp.getTypeName() + " to EXPRSXP");
    }
  }

  public static SEXP Rf_PairToVectorList(SEXP x) {
    PairList pairList = (PairList) x;
    return pairList.toVector();
  }

  public static SEXP Rf_VectorToPairList(SEXP x) {
    return PairList.Node.fromVector(((Vector) x));
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
      case SexpType.LGLSXP:
        return new LogicalArrayVector(new int[numRows * numCols], attributes);
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
  @Allocator
  public static SEXP Rf_allocList(int n) {
    PairList.Builder list = new PairList.Builder();
    for(int i = 0; i < n; i++) {
      list.add(R_NilValue, R_NilValue);
    }
    return list.build();
  }

  @Allocator
  public static SEXP Rf_allocLang(int n) {
    FunctionCall.Builder lang = new FunctionCall.Builder();
    for(int i = 0; i < n; i++) {
      lang.add(R_NilValue, R_NilValue);
    }
    return lang.build();
  }

  /** Create an S4 object.
   *
   * @return Pointer to the created object.
   */
  @Allocator
  public static SEXP Rf_allocS4Object() {
    throw new UnimplementedGnuApiMethod("Rf_allocS4Object");
  }

  @Allocator
  public static SEXP Rf_allocSExp(/*SEXPTYPE*/ int type) {
    switch (type) {
      case SexpType.CLOSXP:
        return new Closure(Environment.EMPTY, Null.INSTANCE, Null.INSTANCE);
    }
    throw new UnimplementedGnuApiMethod("Rf_allocSExp: " + type);
  }

  // SEXP Rf_allocVector3 (SEXPTYPE, R_xlen_t, R_allocator_t *)

  public static /*R_xlen_t*/ int Rf_any_duplicated(SEXP x, boolean from_last) {
    return Duplicates.anyDuplicated((Vector)x, LogicalVector.FALSE, from_last);
  }

  public static /*R_xlen_t*/ int Rf_any_duplicated3(SEXP x, SEXP incomp, boolean from_last) {
    return Duplicates.anyDuplicated((Vector)x, (AtomicVector)incomp, from_last);
  }

  public static SEXP Rf_applyClosure(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    throw new UnimplementedGnuApiMethod("Rf_applyClosure");
  }

  // SEXP Rf_arraySubscript (int, SEXP, SEXP, SEXP(*)(SEXP, SEXP), SEXP(*)(SEXP, int), SEXP)

  @PotentialMutator
  public static SEXP Rf_classgets(@Mutee SEXP object, SEXP classNames) {
    return Native.currentContext().evaluate(FunctionCall.newCall(Symbol.get("class<-"), object, classNames));
  }

  /** Creates a pairlist with a specified car and tail.
   *
   * <p>This function protects its arguments from the garbage collector.
   *
   * @param cr Pointer to the 'car' of the element to be created.
   *
   * @param tail Pointer to the 'tail' of the element to be created,
   *          which must be a pairlist or R_NilValue.
   *
   * @return Pointer to the constructed pairlist.
   */
  public static SEXP Rf_cons(SEXP cr, SEXP tail) {
    assert tail instanceof PairList : "tail argument must be a pairlist";
    return new PairList.Node(cr, (PairList)tail);
  }



  public static void Rf_copyMatrix(SEXP s, SEXP t, boolean byrow) {
    int nr = Rf_nrows(s), nc = Rf_ncols(s);
    int nt = XLENGTH(t);

    if (byrow) {
      throw new UnimplementedGnuApiMethod("copyMatrix(byrow=TRUE)");
    } else {
      Rf_copyVector(s, t);
    }
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
    final AttributeMap inpAttrib = inp.getAttributes().copy().removeDim().removeDimnames().remove(Symbols.NAMES).build();
    ((AbstractSEXP) ans).unsafeSetAttributes(ans.getAttributes().copy().combineFrom(inpAttrib));
  }

  public static void Rf_copyVector(SEXP s, SEXP t) {

    int sT = TYPEOF(s), tT = TYPEOF(t);
    if (sT != tT) {
      throw new EvalException("vector types do not match in copyVector");
    }

    int ns = XLENGTH(s), nt = XLENGTH(t);
    switch (sT) {
      case SexpType.STRSXP:
        xcopyStringWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.LGLSXP:
        xcopyLogicalWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.INTSXP:
        xcopyIntegerWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.REALSXP:
        xcopyRealWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.CPLXSXP:
        xcopyComplexWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.EXPRSXP:
      case SexpType.VECSXP:
        xcopyVectorWithRecycle(s, t, 0, ns, nt);
        break;
      case SexpType.RAWSXP:
        xcopyRawWithRecycle(s, t, 0, ns, nt);
        break;
      default:
        UNIMPLEMENTED_TYPE("copyVector", s);
    }
  }

  private static void xcopyRawWithRecycle(SEXP s, SEXP t, int i, int ns, int nt) {
    throw new UnimplementedGnuApiMethod("xcopyRawWithRecycle");
  }


  private static void xcopyVectorWithRecycle(SEXP s, SEXP t, int i, int ns, int nt) {
    throw new UnimplementedGnuApiMethod("xcopyVectorWithRecycle");
  }

  private static void xcopyComplexWithRecycle(SEXP s, SEXP t, int i, int ns, int nt) {
    throw new UnimplementedGnuApiMethod("xcopyComplexWithRecycle");

  }

  private static void xcopyRealWithRecycle(SEXP dst, SEXP src, int dstart, int n, int nsrc) {
    DoubleVector sv = (DoubleVector) src;
    if(!(dst instanceof DoubleArrayVector)) {
      throw new EvalException("Illegal modification of target vector: " + src.getClass().getName());
    }
    DoubleArrayVector dv = (DoubleArrayVector) dst;

    double sa[];
    double da[] = dv.toDoubleArrayUnsafe();
    if(sv instanceof DoubleArrayVector) {
      sa = ((DoubleArrayVector) sv).toDoubleArrayUnsafe();
    } else {
      sa = sv.toDoubleArray();
    }

    if (nsrc >= n) { /* no recycle needed */
      System.arraycopy(sa, 0, da, dstart, n);

    } else if (nsrc == 1) {
      Arrays.fill(da, dstart, dstart + n, sa[0]);

    } else {
      /* recycle needed */
      int sidx = 0;
      for (int i = 0; i < n; i++, sidx++) {
        if (sidx == nsrc) {
          sidx = 0;
        }
        da[dstart + i] = sa[sidx];
      }
    }
  }

  private static void xcopyIntegerWithRecycle(SEXP dst, SEXP src, int dstart, int n, int nsrc) {
    IntVector sv = (IntVector) src;
    if(!(dst instanceof IntArrayVector)) {
      throw new EvalException("Illegal modification of target vector: " + src.getClass().getName());
    }
    IntArrayVector dv = (IntArrayVector) dst;

    int sa[];
    int da[] = dv.toIntArrayUnsafe();
    if(sv instanceof IntArrayVector) {
      sa = ((IntArrayVector) sv).toIntArrayUnsafe();
    } else {
      sa = sv.toIntArray();
    }
    copy(sa, da, dstart, n, nsrc);
  }

  private static void xcopyLogicalWithRecycle(SEXP dst, SEXP src, int dstart, int n, int nsrc) {
    LogicalVector sv = (LogicalVector) src;
    if(!(dst instanceof LogicalArrayVector)) {
      throw new EvalException("Illegal modification of target vector: " + dst.getClass().getName());
    }
    LogicalArrayVector dv = (LogicalArrayVector) dst;

    int sa[];
    int da[] = dv.toIntArrayUnsafe();
    if(sv instanceof LogicalArrayVector) {
      sa = ((LogicalArrayVector) sv).toIntArrayUnsafe();
    } else {
      sa = sv.toIntArray();
    }
    copy(sa, da, dstart, n, nsrc);
  }

  private static void copy(int[] sa, int[] da, int dstart, int n, int nsrc) {
    if (nsrc >= n) {
      // No recycling required
      System.arraycopy(sa, 0, da, dstart, n);

    } else if (nsrc == 1) {
      // Fill with scalar
      Arrays.fill(da, dstart, dstart + n, sa[0]);

    } else {
      // Recycling need for source vector
      int sidx = 0;
      for (int i = 0; i < n; i++, sidx++) {
        if (sidx == nsrc) {
          sidx = 0;
        }
        da[dstart + i] = sa[sidx];
      }
    }
  }

  private static void xcopyStringWithRecycle(SEXP s, SEXP t, int i, int ns, int nt) {
    throw new UnsupportedOperationException("xcopyStringWithRecycle: not yet supported by Renjin");
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

  public static SEXP Rf_dimgets(SEXP sexp, SEXP dim) {
    return sexp.setAttributes(sexp.getAttributes().copy().setDim(dim));
  }

  public static SEXP Rf_dimnamesgets(SEXP sexp, SEXP dimnames) {
    return sexp.setAttributes(sexp.getAttributes().copy().setDimNames(dimnames));
  }

  public static SEXP Rf_DropDims(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_DropDims");
  }

  public static SEXP Rf_duplicate(SEXP sexp) {
    return duplicate(sexp, true);
  }

  private static SEXP duplicate(SEXP sexp, boolean deep) {
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
      return GnuStringVector.copyOf((StringVector) sexp);

    } else if(sexp instanceof LogicalVector) {
      return new LogicalArrayVector(((LogicalArrayVector) sexp).toIntArray(), sexp.getAttributes());

    } else if(sexp instanceof RawVector) {
      return new RawVector(((RawVector) sexp).toByteArrayUnsafe(), sexp.getAttributes());

    } else if(sexp instanceof S4Object) {
      return new S4Object(duplicate(sexp.getAttributes()));

    } else if(sexp instanceof ListVector) {
      SEXP[] elements = ((ListVector) sexp).toArrayUnsafe();
      for (int i = 0; i < elements.length; i++) {
        elements[i] = deep ? duplicate(elements[i], deep) : elements[i];
      }
      return new ListVector(elements, sexp.getAttributes());

    } else if(sexp instanceof FunctionCall) {
      return duplicateCall(((FunctionCall) sexp), deep);

    } else if(sexp instanceof PairList) {
      return duplicatePairList(((PairList) sexp), deep);

    } else if(
        sexp instanceof Symbol |
        sexp instanceof PrimitiveFunction |
        sexp instanceof ExternalPtr |
        sexp instanceof Environment |
        sexp instanceof Promise) {

      return sexp;

    }
    throw new UnimplementedGnuApiMethod("Rf_duplicate: " + sexp.getTypeName());
  }

  private static AttributeMap duplicate(AttributeMap attributes) {
    AttributeMap.Builder copy = AttributeMap.builder();
    for (Symbol symbol : attributes.names()) {
      copy.set(symbol, Rf_duplicate(attributes.get(symbol)));
    }
    return copy.build();
  }

  private static SEXP duplicatePairList(PairList pairlist, boolean deep) {
    PairList.Builder copy = new PairList.Builder();
    for (PairList.Node node : pairlist.nodes()) {
      copy.add(node.getRawTag(), deep ? Rf_duplicate(node.getValue()) : node.getValue());
    }
    return copy.build();
  }

  private static SEXP duplicateCall(FunctionCall call, boolean deep) {
    FunctionCall.Builder copy = new FunctionCall.Builder();
    for (PairList.Node node : call.nodes()) {
      copy.add(node.getRawTag(), deep ? Rf_duplicate(node.getValue()) : node.getValue());
    }
    return copy.build();
  }

  public static SEXP Rf_shallow_duplicate(SEXP p0) {
    return duplicate(p0, false);
  }

  public static SEXP Rf_lazy_duplicate(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_lazy_duplicate");
  }

  public static SEXP Rf_duplicated(SEXP p0, boolean p1) {
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    if(!(p0.getElementAsSEXP(0) instanceof IntArrayVector)) {
      throw new UnsupportedOperationException("argument to internal function 'Rf_duplicated' is not of type 'IntArrayVector'");
    }
    Set<IntArrayVector> elementsHash = new HashSet<>();
    for(int i = 0; i < p0.length(); i++) {
      IntArrayVector element = p0.getElementAsSEXP(i);
      if (elementsHash.contains(element)) {
        result.add(LogicalVector.TRUE);
      } else {
        result.add(LogicalVector.FALSE);
        elementsHash.add(element);
      }
    }
    return result.build();
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

  public static SEXP Rf_findFun(SEXP symbol, SEXP rho) {
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

  public static SEXP Rf_GetOption1(SEXP optionNameSexp) {
    Symbol optionName = (Symbol) optionNameSexp;

    Options options = Native.currentContext().getSession().getSingleton(org.renjin.eval.Options.class);
    return options.get(optionName.getPrintName());
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

  public static void Rf_gsetVar(SEXP symbolName, SEXP value, SEXP environment) {
    if(environment == Null.INSTANCE) {
      environment = Native.currentContext().getBaseEnvironment();
    }

    ((Environment) environment).setVariable(Native.currentContext(), (Symbol)symbolName, value);
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

  public static SEXP Rf_installChar(SEXP charSexp) {
    return Rf_install(((GnuCharSexp) charSexp).getValue());
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

  @Deprecated
  public static SEXP Rf_mkChar(BytePtr string) {
    return Rf_mkChar((Ptr)string);
  }

  public static SEXP Rf_mkChar(Ptr string) {
    if(string.isNull()) {
      return GnuCharSexp.NA_STRING;
    }
    return Rf_mkCharLen(string, Stdlib.strlen(string));
  }

  @Deprecated
  public static SEXP Rf_mkCharLen(BytePtr string, int length) {
    return Rf_mkCharLen((Ptr)string, length);
  }

  public static SEXP Rf_mkCharLen(Ptr string, int length) {
    if(string.isNull()) {
      return GnuCharSexp.NA_STRING;
    }

    if(length == 0) {
      return R_BlankString;
    }

    BytePtr copy = BytePtr.malloc(length+1);
    copy.memcpy(string, length);

    return new GnuCharSexp(copy.array);
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
      if(dim == Null.INSTANCE) {
        return s.length();
      } else {
        return dim.getElementAsInt(0);
      }

    } else if (Rf_isFrame(s)) {
      return Rf_nrows(s.getElementAsSEXP(0));

    } else {
      throw new EvalException("object is not a matrix");
    }
  }

  public static SEXP Rf_nthcdr(SEXP p0, int p1) {
    if (Rf_isList(p0) || Rf_isLanguage(p0) || Rf_isFrame(p0) || TYPEOF(p0) == SexpType.DOTSXP) {
      while (p1-- > 0) {
        if (p0 == R_NilValue) {
          throw new EvalException(String.format("'nthcdr' list shorter than %d", p1));
        }
        p0 = CDR(p0);
      }
      return p0;
    } else {
      throw new EvalException("'nthcdr' needs a list to CDR down");
    }
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
  public static SEXP Rf_setAttrib(@Mutee SEXP vec, SEXP name, SEXP val) {
    if(name == null) {
      throw new IllegalArgumentException("attributeName is NULL");
    }
    if(name == R_RowNamesSymbol) {
      if(isOldCompactForm(val) || isCompactForm(val) ) {
        val = new RowNamesVector(Math.abs(val.getElementAsSEXP(1).asInt()));
      }
    }
    Symbol attributeSymbol;
    if(name instanceof StringVector) {
      attributeSymbol = Symbol.get(((StringVector) name).getElementAsString(0));
    } else {
      attributeSymbol = (Symbol) name;
    }
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

  @Deprecated
  public static /*SEXPTYPE*/ int Rf_str2type(BytePtr p0) {
    return Rf_str2type((Ptr)p0);
  }

  public static int Rf_str2type(Ptr string) {
    switch (Stdlib.nullTerminatedString(string)) {
      case Null.TYPE_NAME:
        return SexpType.NILSXP;
      case PairList.TYPE_NAME:
        return SexpType.LISTSXP;
      case FunctionCall.TYPE_NAME:
        return SexpType.LANGSXP;
      case ListVector.TYPE_NAME:
        return SexpType.VECSXP;
      case StringVector.TYPE_NAME:
        return SexpType.STRSXP;
      case IntVector.TYPE_NAME:
        return SexpType.INTSXP;
      case DoubleVector.TYPE_NAME:
        return SexpType.REALSXP;
      case RawVector.TYPE_NAME:
        return SexpType.RAWSXP;
      case LogicalVector.TYPE_NAME:
        return SexpType.LGLSXP;
      case Environment.TYPE_NAME:
        return SexpType.ENVSXP;
      case Promise.TYPE_NAME:
        return SexpType.PROMSXP;
      case Symbol.TYPE_NAME:
        return SexpType.SYMSXP;
    }
    throw new UnimplementedGnuApiMethod("Rf_str2type: " + Stdlib.nullTerminatedString(string));
  }

  public static boolean Rf_StringBlank(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringBlank");
  }

  public static SEXP Rf_substitute(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_substitute");
  }

  public static BytePtr Rf_translateChar(SEXP p0) {
    return ((GnuCharSexp)p0).getValue();
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
    // Renjin strings are always UTF-8 encoded.
    GnuCharSexp charsexp = (GnuCharSexp) x;
    return charsexp.getValue();
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
    return BytePtr.nullTerminatedString(SexpType.typeName(st), StandardCharsets.UTF_8);
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

  @Noop
  public static void Rf_unprotect_ptr(SEXP p0) {
    // NOOP
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

  public static int Rf_getCharCE(SEXP s) {
    return 1; // Always UTF8!
  }

  @Deprecated
  public static SEXP Rf_mkCharCE (BytePtr str, int encoding) {
    return Rf_mkCharCE((Ptr)str, encoding);
  }

  @Deprecated
  public static SEXP Rf_mkCharLenCE (BytePtr text, int length, int encoding) {
    return Rf_mkCharLenCE((Ptr)text, length, encoding);
  }


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
  public static SEXP Rf_mkCharCE (Ptr str, int encoding) {
    return Rf_mkCharLenCE(str, Stdlib.strlen(str), encoding);
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
  public static SEXP Rf_mkCharLenCE (Ptr text, int length, int encoding) {
    if(text.isNull()) {
      return GnuCharSexp.NA_STRING;
    }

    if(length == 0) {
      return R_BlankString;
    }

    if(encoding != CE_UTF8) {
      throw new UnsupportedOperationException("encoding: " + encoding);
    }

    BytePtr copy = BytePtr.malloc(length + 1);
    copy.memcpy(text, length);

    return new GnuCharSexp(copy.array);
  }

  public static Ptr Rf_reEnc(BytePtr x, int ce_in, int ce_out, int subst) {
    if(ce_in == ce_out) {
      return x;
    } else if(ce_in == -1 && ce_out == CE_UTF8) {
      return x;
    } else if(ce_in == CE_NATIVE && ce_out == CE_UTF8) {
      return x;
    } else if(ce_in == CE_ANY && ce_out == CE_UTF8) {
      return x;
    } else {
      throw new UnsupportedOperationException(String.format("Rf_reEnc: from %d to %d", ce_in, ce_out));
    }
  }

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
      public void finalizeSexp(Context context, SEXP sexp) {
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

  public static boolean R_ToplevelExec(MethodHandle fun, Ptr data) throws Throwable {
    fun.invoke(data);
    return true;
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

  public static void Rf_errorcall (SEXP call, Ptr format, Object... args) {
    String errorMessage = Stdlib.format(format, args);
    throw new EvalException(errorMessage);
  }

  public static void Rf_warningcall (SEXP call, Ptr format, Object... args) {
    Warning.warning(Native.currentContext(), call, false, Stdlib.format(format, new FormatArrayInput(args)));
  }

  public static void Rf_warningcall_immediate(SEXP call, Ptr format, Object... args) {
    Warning.warning(Native.currentContext(), call, true, Stdlib.format(format, new FormatArrayInput(args)));
  }

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

    // In GNU R calls from C code to SET_SLOT will mutate the object
    // depending on whether the 'named' flag is set. In Renjin we don't
    // have this flag and therefor assume that all calls from C code to
    // SET_SLOT will mutate the object. However, calls from R code
    // should not mutate, hence we do not change Methods.R_set_slot()
    // to use unsafesetAttributes(), but duplicate the logic here and
    // use the unsafesetAttributes() here instead.
    if(name.asString().equals(".Data")) {
      // the .Data slot actually refers to the object value itself, for
      // example the double values contained in a double vector
      // So we copy the slots from 'object' to the new value
      return Native.currentContext().evaluate(FunctionCall.newCall(Symbol.get("setDataPart"), obj, value),
        Native.currentContext().getSingleton(MethodDispatch.class).getMethodsNamespace());
    } else {
      // When set via S4 methods, R attributes can contain
      // invalid values, for example the 'class' attribute
      // might contain a double vector of arbitrary length.
      // For this reason we have to be careful to avoid attribute
      // validation.
      SEXP slotValue = value == Null.INSTANCE ? Symbols.S4_NULL : value;
      ((AbstractSEXP)obj).unsafeSetAttributes(obj.getAttributes().copy().set(name.asString(), slotValue));
      return obj;
    }
  }

  public static int R_has_slot(SEXP obj, SEXP name) {
    return Methods.R_has_slot(obj, name);
  }

  public static SEXP R_do_MAKE_CLASS(BytePtr what) {
    if(what == null || what.array == null) {
      throw new EvalException("C level MAKE_CLASS macro called with NULL string pointer");
    }
    Context context = Native.currentContext();

    return Methods.getClass(context, StringVector.valueOf(what.nullTerminatedString()), false, Null.INSTANCE);
  }

  public static SEXP R_getClassDef(BytePtr what) {
    if(what == null || what.array == null) {
      throw new EvalException("R_getClassDef(.) called with NULL string pointer");
    }

    return R_getClassDef_R(StringArrayVector.valueOf(what.nullTerminatedString()));
  }

  public static SEXP R_getClassDef_R(StringVector what) {
    Context context = Native.currentContext();
    return Methods.getClassDef(context, what, Null.INSTANCE, Null.INSTANCE, true);
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

  @Deprecated
  public static int R_check_class_and_super (SEXP x, ObjectPtr<BytePtr> valid, SEXP rho) {
    return R_check_class_and_super(x, new PointerPtr((Ptr[])valid.array, valid.offset), rho);
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
  public static int R_check_class_and_super (SEXP x, Ptr valid, SEXP rho) {
    int ans;
    SEXP cl = Rf_asChar(Rf_getAttrib(x, R_ClassSymbol));
    BytePtr class_ = R_CHAR(cl);
    for (ans = 0; ; ans++) {
      if (Stdlib.strlen(valid.getAlignedPointer(ans)) == 0) { // empty string
        break;
      }
      if (Stdlib.strcmp(class_, valid.getAlignedPointer(ans)) == 0) {
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
          if (Stdlib.strlen(valid.getAlignedPointer(ans)) == 0) {
            break;
          }
          if (Stdlib.strcmp(s_class, valid.getAlignedPointer(ans)) == 0) {
            return ans;
          }
        }
      }
    }
    return -1;
  }

  @Deprecated
  public static int R_check_class_etc (SEXP x, ObjectPtr<BytePtr> valid) {
    return R_check_class_etc(x, new PointerPtr((Ptr[]) valid.array, valid.offset));
  }

  /**
   * Return the 0-based index of an is() match in a vector of class-name
   * strings terminated by an empty string.  Returns -1 for no match.
   * Strives to find the correct environment() for is(), using .classEnv()
   * (from \pkg{methods}).
   *
   * @param x  an R object, about which we want is(x, .) information.
   * @param valid vector of possible matches terminated by an empty string.
   *
   * @return index of match or -1 for no match
   */
  public static int R_check_class_etc (SEXP x, Ptr valid) {
    SEXP cl = Rf_getAttrib(x, R_ClassSymbol);
    SEXP rho = R_GlobalEnv();
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

  @Noop
  public static void R_PreserveObject(SEXP p0) {
    // NOOP
    // We have a garbage collector.
  }

  @Noop
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

  private static final boolean NUM_EQ(int flags) { return (flags & 1) == 0; }

  private static final boolean SINGLE_NA(int flags) { return (flags & 2) == 0; }

  private static final boolean ATTR_AS_SET(int flags) { return (flags & 4) == 0; }

  private static final boolean IGNORE_BYTECODE(int flags) { return (flags & 8) == 0; }

  public static boolean R_compute_identical(SEXP x, SEXP y, int flags) {
    return Identical.identical(x, y,
        NUM_EQ(flags),
        SINGLE_NA(flags),
        ATTR_AS_SET(flags),
        IGNORE_BYTECODE(flags));
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
  @Allocator
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
        GnuCharSexp[] strings = new GnuCharSexp[length];
        Arrays.fill(strings, GnuCharSexp.NA_STRING);
        return new GnuStringVector(strings);

      case SexpType.LISTSXP:
        return new ListVector(elements(length));

      case SexpType.EXPRSXP:
        return new ExpressionVector(elements(length));

      case SexpType.RAWSXP:
        return new RawVector(new byte[length]);
    }
    throw new UnimplementedGnuApiMethod("Rf_allocVector: type = " + stype);
  }

  private static SEXP[] elements(int length) {
    SEXP[] array = new SEXP[length];
    Arrays.fill(array, Null.INSTANCE);
    return array;
  }

  public static boolean Rf_conformable(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_conformable");
  }

  public static SEXP Rf_elt(SEXP vector, int index) {
    return vector.getElementAsSEXP(index);
  }

  public static boolean Rf_inherits(SEXP p0, BytePtr p1) {
    return p0.inherits(p1.nullTerminatedString());
  }

  public static boolean Rf_isArray(SEXP p0) {
    return Types.isArray(p0);
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
    if (TYPEOF(p0) == SexpType.SYMSXP) {
      BytePtr str = R_CHAR(PRINTNAME(p0));
      final int strlen = str.nullTerminatedStringLength();
      return (strlen >= 2 && str.getChar(0) == '%' && str.getChar(strlen - 1) == '%');
    }
    return false;
  }

  public static boolean Rf_isValidString(SEXP p0) {
    return TYPEOF(p0) == SexpType.STRSXP && LENGTH(p0) > 0 && TYPEOF(STRING_ELT(p0, 0)) != SexpType.NILSXP;
  }

  public static boolean Rf_isValidStringF(SEXP p0) {
    return Rf_isValidString(p0) && R_CHAR(STRING_ELT(p0, 0)).getChar(0) != '\0';
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

  public static SEXP Rf_lastElt(SEXP list) {
    SEXP result = Null.INSTANCE;
    while (list != Null.INSTANCE) {
      result = list;
      list = CDR(list);
    }
    return result;
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

  public static SEXP Rf_lengthgets(SEXP sexp, int length) {
    return Vectors.setLength((Vector)sexp, length);
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

  public static SEXP Rf_listAppend(SEXP s, SEXP t) {
    SEXP r;
    if(s == R_NilValue){
      return t;
    }
    r = s;
    while(CDR(r) != R_NilValue) {
      r = CDR(r);
    }
    SETCDR(r, t);
    return s;
  }

  @Deprecated
  public static SEXP Rf_mkNamed (int sexpType, ObjectPtr<BytePtr> names) {
    return Rf_mkNamed(sexpType, new PointerPtr((Ptr[]) names.array, names.offset));
  }

  public static SEXP Rf_mkNamed (int sexpType, Ptr names) {
    if(sexpType == SexpType.VECSXP) {
      ListVector.NamedBuilder list = new ListVector.NamedBuilder();
      int i = 0;
      while(true) {
        String name = Stdlib.nullTerminatedString(names.getAlignedPointer(i));
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

  @Deprecated
  public static SEXP Rf_mkString(BytePtr string) {
    return Rf_mkString((Ptr)string);
  }

  public static SEXP Rf_mkString(Ptr string) {
    return new StringArrayVector(Stdlib.nullTerminatedString(string));
  }

  public static int Rf_nlevels(SEXP p0) {
    if(!isFactor(p0)) {
      return 0;
    }
    return LENGTH(p0.getAttribute(Symbol.get("levels")));
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
    return new GnuStringVector(((GnuCharSexp) p0));
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
  @Noop
  public static void Rf_unprotect(int count) {
    // NOOP
  }


  /**
   * @deprecated See {@link #R_ProtectWithIndex(SEXP, Ptr)}, which accepts any pointer type
   */
  @Deprecated
  public static void R_ProtectWithIndex(SEXP node, /*PROTECT_INDEX **/ IntPtr iptr) {
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
  @Noop
  public static void R_ProtectWithIndex(SEXP node, /*PROTECT_INDEX **/ Ptr iptr) {
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
  @Noop
  public static void R_Reprotect(SEXP node, /*PROTECT_INDEX*/ int index) {
    // NOOP
  }

  public static SEXP R_FixupRHS(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("R_FixupRHS");
  }
}
