// Initial template generated from Rinternals.h from R 3.2.2
package org.renjin.gnur.api;

import com.google.common.base.Charsets;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.*;
import org.renjin.sexp.*;

import java.util.Arrays;

@SuppressWarnings("unused")
public final class Rinternals {

  private Rinternals() { }

  /* Evaluation Environment */
  public static SEXP	R_GlobalEnv;	    /* The "global" environment */

  public static SEXP  R_EmptyEnv;	    /* An empty environment at the root of the
				    	environment tree */
  public static SEXP  R_BaseEnv;	    /* The base environment; formerly R_NilValue */
  public static SEXP	R_BaseNamespace;    /* The (fake) namespace for base */
  public static SEXP	R_NamespaceRegistry;/* Registry for registered namespaces */

  public static SEXP	R_Srcref;           /* Current srcref, for debuggers */

  /* Special Values */
  public static SEXP	R_NilValue = Null.INSTANCE;	    /* The nil object */
  public static  SEXP	R_UnboundValue = Symbol.UNBOUND_VALUE;	    /* Unbound marker */
  public static  SEXP	R_MissingArg = Symbol.MISSING_ARG;	    /* Missing argument marker */

  public static SEXP	R_RestartToken;     /* Marker for restarted function calls */

  /* Symbol Table Shortcuts */
  public static  SEXP	R_baseSymbol = Symbol.get("base"); // <-- backcompatible version of:
  public static  SEXP	R_BaseSymbol = Symbol.get("base");	// "base"
  public static  SEXP	R_BraceSymbol = Symbol.get("{");	    /* "{" */
  public static  SEXP	R_Bracket2Symbol = Symbol.get("[[");   /* "[[" */
  public static  SEXP	R_BracketSymbol = Symbol.get("[");    /* "[" */
  public static  SEXP	R_ClassSymbol = Symbol.get("class");	    /* "class" */
  public static  SEXP	R_DeviceSymbol = Symbol.get(".Device");	    /* ".Device" */
  public static  SEXP	R_DimNamesSymbol = Symbol.get("dimnames");   /* "dimnames" */
  public static  SEXP	R_DimSymbol = Symbol.get("dim");	    /* "dim" */
  public static  SEXP	R_DollarSymbol = Symbol.get("$");	    /* "$" */
  public static  SEXP	R_DotsSymbol = Symbol.get("...");	    /* "..." */
  public static  SEXP	R_DoubleColonSymbol = Symbol.get("::"); // "::"
  public static  SEXP	R_DropSymbol = Symbol.get("drop");	    /* "drop" */
  public static  SEXP	R_LastvalueSymbol = Symbol.get(".Last.value");  /* ".Last.value" */
  public static  SEXP	R_LevelsSymbol = Symbol.get("level");	    /* "levels" */
  public static  SEXP	R_ModeSymbol = Symbol.get("mode");	    /* "mode" */
  public static  SEXP	R_NaRmSymbol = Symbol.get("na.rm");	    /* "na.rm" */
  public static  SEXP	R_NameSymbol = Symbol.get("name");	    /* "name" */
  public static  SEXP	R_NamesSymbol = Symbol.get("names");	    /* "names" */
  public static  SEXP	R_NamespaceEnvSymbol = Symbol.get(".__NAMESPACE__.");// ".__NAMESPACE__."
  public static  SEXP	R_PackageSymbol = Symbol.get("package");    /* "package" */
  public static  SEXP	R_PreviousSymbol = Symbol.get("previous");   /* "previous" */
  public static  SEXP	R_QuoteSymbol = Symbol.get("quote");	    /* "quote" */
  public static  SEXP	R_RowNamesSymbol = Symbol.get("row.names");   /* "row.names" */
  public static  SEXP	R_SeedsSymbol = Symbol.get(".Random.seed");	    /* ".Random.seed" */
  public static  SEXP	R_SortListSymbol = Symbol.get("sort.list");   /* "sort.list" */
  public static  SEXP	R_SourceSymbol = Symbol.get("source");	    /* "source" */
  public static  SEXP	R_SpecSymbol = Symbol.get("spec");	// "spec"
  public static  SEXP	R_TripleColonSymbol = Symbol.get(":::");// ":::"
  public static  SEXP	R_TspSymbol = Symbol.get("tsp");	    /* "tsp" */

  public static  SEXP  R_dot_defined = Symbol.get(".defined");      /* ".defined" */
  public static  SEXP  R_dot_Method = Symbol.get(".Method");       /* ".Method" */
  public static  SEXP	R_dot_packageName = Symbol.get(".packageName");// ".packageName"
  public static  SEXP  R_dot_target = Symbol.get(".target");       /* ".target" */

/* Missing Values - others from Arith.h */
  public static  SEXP	R_NaString = null;	    /* NA_STRING as a CHARSXP */
  public static  SEXP	R_BlankString = new GnuCharSexp("");	    /* "" as a CHARSXP */
  public static  SEXP	R_BlankScalarString = new GnuStringVector("");	    /* "" as a STRSXP */


  public static BytePtr R_CHAR(SEXP x) {
    throw new UnimplementedGnuApiMethod("R_CHAR");
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
    throw new UnimplementedGnuApiMethod("Rf_isString");
  }

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
    } else {
      throw new UnsupportedOperationException("Unknown SEXP Type: " + s.getClass().getName());
    }
  }

  /**
   * @param sexp
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
    throw new UnimplementedGnuApiMethod("SET_S4_OBJECT");
  }

  public static void UNSET_S4_OBJECT(SEXP x) {
    throw new UnimplementedGnuApiMethod("UNSET_S4_OBJECT");
  }

  public static int LENGTH(SEXP x) {
    return x.length();
  }

  public static int TRUELENGTH(SEXP x) {
    throw new UnimplementedGnuApiMethod("TRUELENGTH");
  }

  public static void SETLENGTH(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SETLENGTH");
  }

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
    throw new UnimplementedGnuApiMethod("IS_LONG_VEC");
  }

  public static int LEVELS(SEXP x) {
    throw new UnimplementedGnuApiMethod("LEVELS");
  }

  public static int SETLEVELS(SEXP x, int v) {
    throw new UnimplementedGnuApiMethod("SETLEVELS");
  }

  public static IntPtr LOGICAL(SEXP x) {
    throw new UnimplementedGnuApiMethod("LOGICAL");
  }

  public static IntPtr INTEGER(SEXP x) {
    if(x instanceof IntArrayVector) {
      return new IntPtr(((IntArrayVector) x).toIntArrayUnsafe());
    } else if(x instanceof IntVector) {
      // TODO: cache arrays for the case of repeated INTEGER() calls?
      return new IntPtr(((IntVector) x).toIntArray());
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

  // Rcomplex*() COMPLEX (SEXP x)

  public static SEXP STRING_ELT(SEXP x, /*R_xlen_t*/ int i) {
    throw new UnimplementedGnuApiMethod("STRING_ELT");
  }

  public static SEXP VECTOR_ELT(SEXP x, /*R_xlen_t*/ int i) {
    return ((ListVector) x).getElementAsSEXP(i);
  }

  public static void SET_STRING_ELT(SEXP x, /*R_xlen_t*/ int index, SEXP value) {
    GnuStringVector stringVector = (GnuStringVector) x;
    GnuCharSexp charValue = (GnuCharSexp) value;

    stringVector.set(index, charValue);
  }

  public static SEXP SET_VECTOR_ELT(SEXP x, /*R_xlen_t*/ int index, SEXP value) {
    ListVector listVector = (ListVector) x;
    SEXP[] elements = listVector.toArrayUnsafe();
    elements[index] = value;
    return value;
  }

  // SEXP*() STRING_PTR (SEXP x)

  // SEXP*() VECTOR_PTR (SEXP x)

  public static SEXP TAG(SEXP e) {
    throw new UnimplementedGnuApiMethod("TAG");
  }

  public static SEXP CAR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CAR");
  }

  public static SEXP CDR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CDR");
  }

  public static SEXP CAAR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CAAR");
  }

  public static SEXP CDAR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CDAR");
  }

  public static SEXP CADR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CADR");
  }

  public static SEXP CDDR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CDDR");
  }

  public static SEXP CDDDR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CDDDR");
  }

  public static SEXP CADDR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CADDR");
  }

  public static SEXP CADDDR(SEXP e) {
    throw new UnimplementedGnuApiMethod("CADDDR");
  }

  public static SEXP CAD4R(SEXP e) {
    throw new UnimplementedGnuApiMethod("CAD4R");
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
    throw new UnimplementedGnuApiMethod("SETCAR");
  }

  public static SEXP SETCDR(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SETCDR");
  }

  public static SEXP SETCADR(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SETCADR");
  }

  public static SEXP SETCADDR(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SETCADDR");
  }

  public static SEXP SETCADDDR(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SETCADDDR");
  }

  public static SEXP SETCAD4R(SEXP e, SEXP y) {
    throw new UnimplementedGnuApiMethod("SETCAD4R");
  }

  public static SEXP CONS_NR(SEXP a, SEXP b) {
    throw new UnimplementedGnuApiMethod("CONS_NR");
  }

  public static SEXP FORMALS(SEXP x) {
    throw new UnimplementedGnuApiMethod("FORMALS");
  }

  public static SEXP BODY(SEXP x) {
    throw new UnimplementedGnuApiMethod("BODY");
  }

  public static SEXP CLOENV(SEXP x) {
    throw new UnimplementedGnuApiMethod("CLOENV");
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

  public static void SET_CLOENV(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_CLOENV");
  }

  public static SEXP PRINTNAME(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRINTNAME");
  }

  public static SEXP SYMVALUE(SEXP x) {
    throw new UnimplementedGnuApiMethod("SYMVALUE");
  }

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

  public static void SET_SYMVALUE(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_SYMVALUE");
  }

  public static void SET_INTERNAL(SEXP x, SEXP v) {
    throw new UnimplementedGnuApiMethod("SET_INTERNAL");
  }

  public static SEXP FRAME(SEXP x) {
    throw new UnimplementedGnuApiMethod("FRAME");
  }

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

  public static SEXP PRCODE(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRCODE");
  }

  public static SEXP PRENV(SEXP x) {
    throw new UnimplementedGnuApiMethod("PRENV");
  }

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
    throw new UnimplementedGnuApiMethod("Rf_asChar");
  }

  public static SEXP Rf_coerceVector(SEXP p0, /*SEXPTYPE*/ int p1) {
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
    throw new UnimplementedGnuApiMethod("Rf_asLogical");
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
//      res = IntegerFromString(x, &warn);
//      CoercionWarning(warn);
//      return res;
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
//      res = IntegerFromString(x, &warn);
//      CoercionWarning(warn);
//      return res;
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

  public static SEXP Rf_allocMatrix(/*SEXPTYPE*/ int p0, int p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_allocMatrix");
  }

  public static SEXP Rf_allocList(int p0) {
    throw new UnimplementedGnuApiMethod("Rf_allocList");
  }

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

  public static SEXP Rf_cons(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_cons");
  }

  public static void Rf_copyMatrix(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_copyMatrix");
  }

  public static void Rf_copyListMatrix(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_copyListMatrix");
  }

  public static void Rf_copyMostAttrib(SEXP p0, SEXP p1) {
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

  public static void Rf_defineVar(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_defineVar");
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
    if(sexp instanceof DoubleArrayVector) {
      return new DoubleArrayVector((DoubleVector)sexp);
    }
    throw new UnimplementedGnuApiMethod("Rf_duplicate");
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

  public static SEXP Rf_eval(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_eval");
  }

  public static SEXP Rf_findFun(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_findFun");
  }

  public static SEXP Rf_findVar(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_findVar");
  }

  public static SEXP Rf_findVarInFrame(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_findVarInFrame");
  }

  public static SEXP Rf_findVarInFrame3(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("Rf_findVarInFrame3");
  }

  public static SEXP Rf_getAttrib(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_getAttrib");
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

  public static SEXP Rf_install(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_install");
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

  public static SEXP R_lsInternal(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("R_lsInternal");
  }

  public static SEXP R_lsInternal3(SEXP p0, boolean p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("R_lsInternal3");
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
    return new GnuCharSexp(string);
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

  public static SEXP Rf_setAttrib(SEXP sexp, SEXP attributeName, SEXP newValue) {
    if(attributeName == null) {
      throw new IllegalArgumentException("attributeName is NULL");
    }
    Symbol attributeSymbol = (Symbol) attributeName;
    AbstractSEXP abstractSEXP = (AbstractSEXP) sexp;
    abstractSEXP.unsafeSetAttributes(sexp.getAttributes().copy().set(attributeSymbol, newValue).build());
    return sexp;
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

  public static BytePtr Rf_translateCharUTF8(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_translateCharUTF8");
  }

  public static BytePtr Rf_type2char(/*SEXPTYPE*/ int p0) {
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

  // SEXP Rf_mkCharCE (const char *, cetype_t)

  // SEXP Rf_mkCharLenCE (const char *, int, cetype_t)

  // const char* Rf_reEnc (const char *x, cetype_t ce_in, cetype_t ce_out, int subst)

  public static SEXP R_forceAndCall(SEXP e, int n, SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_forceAndCall");
  }

  public static SEXP R_MakeExternalPtr(Ptr p, SEXP tag, SEXP prot) {
    throw new UnimplementedGnuApiMethod("R_MakeExternalPtr");
  }

  public static Ptr R_ExternalPtrAddr(SEXP s) {
    throw new UnimplementedGnuApiMethod("R_ExternalPtrAddr");
  }

  public static SEXP R_ExternalPtrTag(SEXP s) {
    throw new UnimplementedGnuApiMethod("R_ExternalPtrTag");
  }

  public static SEXP R_ExternalPtrProtected(SEXP s) {
    throw new UnimplementedGnuApiMethod("R_ExternalPtrProtected");
  }

  public static void R_ClearExternalPtr(SEXP s) {
    throw new UnimplementedGnuApiMethod("R_ClearExternalPtr");
  }

  public static void R_SetExternalPtrAddr(SEXP s, Ptr p) {
    throw new UnimplementedGnuApiMethod("R_SetExternalPtrAddr");
  }

  public static void R_SetExternalPtrTag(SEXP s, SEXP tag) {
    throw new UnimplementedGnuApiMethod("R_SetExternalPtrTag");
  }

  public static void R_SetExternalPtrProtected(SEXP s, SEXP p) {
    throw new UnimplementedGnuApiMethod("R_SetExternalPtrProtected");
  }

  public static void R_RegisterFinalizer(SEXP s, SEXP fun) {
    throw new UnimplementedGnuApiMethod("R_RegisterFinalizer");
  }

  // void R_RegisterCFinalizer (SEXP s, R_CFinalizer_t fun)

  public static void R_RegisterFinalizerEx(SEXP s, SEXP fun, boolean onexit) {
    throw new UnimplementedGnuApiMethod("R_RegisterFinalizerEx");
  }

  // void R_RegisterCFinalizerEx (SEXP s, R_CFinalizer_t fun, Rboolean onexit)

  public static void R_RunPendingFinalizers() {
    throw new UnimplementedGnuApiMethod("R_RunPendingFinalizers");
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

  public static SEXP R_PromiseExpr(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_PromiseExpr");
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

  public static SEXP R_FindNamespace(SEXP info) {
    throw new UnimplementedGnuApiMethod("R_FindNamespace");
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

  public static void R_XDREncodeDouble(double d, Ptr buf) {
    throw new UnimplementedGnuApiMethod("R_XDREncodeDouble");
  }

  public static double R_XDRDecodeDouble(Ptr buf) {
    throw new UnimplementedGnuApiMethod("R_XDRDecodeDouble");
  }

  public static void R_XDREncodeInteger(int i, Ptr buf) {
    throw new UnimplementedGnuApiMethod("R_XDREncodeInteger");
  }

  public static int R_XDRDecodeInteger(Ptr buf) {
    throw new UnimplementedGnuApiMethod("R_XDRDecodeInteger");
  }

  // void R_InitInPStream (R_inpstream_t stream, R_pstream_data_t data, R_pstream_format_t type, int(*inchar)(R_inpstream_t), void(*inbytes)(R_inpstream_t, void *, int), SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitOutPStream (R_outpstream_t stream, R_pstream_data_t data, R_pstream_format_t type, int version, void(*outchar)(R_outpstream_t, int), void(*outbytes)(R_outpstream_t, void *, int), SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitFileInPStream (R_inpstream_t stream, FILE *fp, R_pstream_format_t type, SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_InitFileOutPStream (R_outpstream_t stream, FILE *fp, R_pstream_format_t type, int version, SEXP(*phook)(SEXP, SEXP), SEXP pdata)

  // void R_Serialize (SEXP s, R_outpstream_t ops)

  // SEXP R_Unserialize (R_inpstream_t ips)

  public static SEXP R_do_slot(SEXP obj, SEXP name) {
    throw new UnimplementedGnuApiMethod("R_do_slot");
  }

  public static SEXP R_do_slot_assign(SEXP obj, SEXP name, SEXP value) {
    throw new UnimplementedGnuApiMethod("R_do_slot_assign");
  }

  public static int R_has_slot(SEXP obj, SEXP name) {
    throw new UnimplementedGnuApiMethod("R_has_slot");
  }

  public static SEXP R_do_MAKE_CLASS(BytePtr what) {
    throw new UnimplementedGnuApiMethod("R_do_MAKE_CLASS");
  }

  public static SEXP R_getClassDef(BytePtr what) {
    throw new UnimplementedGnuApiMethod("R_getClassDef");
  }

  public static SEXP R_getClassDef_R(SEXP what) {
    throw new UnimplementedGnuApiMethod("R_getClassDef_R");
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
    throw new UnimplementedGnuApiMethod("R_do_new_object");
  }

  // int R_check_class_and_super (SEXP x, const char **valid, SEXP rho)

  // int R_check_class_etc (SEXP x, const char **valid)

  public static void R_PreserveObject(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_PreserveObject");
  }

  public static void R_ReleaseObject(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_ReleaseObject");
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

  public static SEXP Rf_allocVector(/*SEXPTYPE*/ int type, /*R_xlen_t*/ int length) {
    switch (type) {
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
    }
    throw new UnimplementedGnuApiMethod("Rf_allocVector: type = " + type);
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
    throw new UnimplementedGnuApiMethod("Rf_isFactor");
  }

  public static boolean Rf_isFrame(SEXP s) {
    return s.inherits("data.frame");
  }

  public static boolean Rf_isFunction(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isFunction");
  }

  public static boolean Rf_isInteger(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isInteger");
  }

  public static boolean Rf_isLanguage(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isLanguage");
  }

  public static boolean Rf_isList(SEXP s) {
    return (s == Null.INSTANCE || s instanceof PairList.Node);
  }

  public static boolean Rf_isMatrix(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isMatrix");
  }

  public static boolean Rf_isNewList(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isNewList");
  }

  public static boolean Rf_isNumber(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isNumber");
  }

  public static boolean Rf_isNumeric(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isNumeric");
  }

  public static boolean Rf_isPairList(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isPairList");
  }

  public static boolean Rf_isPrimitive(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_isPrimitive");
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
    throw new UnimplementedGnuApiMethod("Rf_lang1");
  }

  public static SEXP Rf_lang2(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_lang2");
  }

  public static SEXP Rf_lang3(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_lang3");
  }

  public static SEXP Rf_lang4(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    throw new UnimplementedGnuApiMethod("Rf_lang4");
  }

  public static SEXP Rf_lang5(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    throw new UnimplementedGnuApiMethod("Rf_lang5");
  }

  public static SEXP Rf_lang6(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4, SEXP p5) {
    throw new UnimplementedGnuApiMethod("Rf_lang6");
  }

  public static SEXP Rf_lastElt(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_lastElt");
  }

  public static SEXP Rf_lcons(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_lcons");
  }

  public static int Rf_length (SEXP sexp) {
    return sexp.length();
  }

  public static SEXP Rf_list1(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_list1");
  }

  public static SEXP Rf_list2(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_list2");
  }

  public static SEXP Rf_list3(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_list3");
  }

  public static SEXP Rf_list4(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    throw new UnimplementedGnuApiMethod("Rf_list4");
  }

  public static SEXP Rf_list5(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4) {
    throw new UnimplementedGnuApiMethod("Rf_list5");
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

  public static SEXP Rf_mkString(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_mkString");
  }


  public static int Rf_nlevels(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_nlevels");
  }

  public static int Rf_stringPositionTr(SEXP p0, BytePtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_stringPositionTr");
  }

  // SEXP Rf_ScalarComplex (Rcomplex)

  public static SEXP Rf_ScalarInteger(int p0) {
    throw new UnimplementedGnuApiMethod("Rf_ScalarInteger");
  }

  public static SEXP Rf_ScalarLogical(int p0) {
    throw new UnimplementedGnuApiMethod("Rf_ScalarLogical");
  }

  // SEXP Rf_ScalarRaw (Rbyte)

  public static SEXP Rf_ScalarReal(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_ScalarReal");
  }

  public static SEXP Rf_ScalarString(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_ScalarString");
  }

  public static /*R_xlen_t*/ int Rf_xlength(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_xlength");
  }

  public static SEXP Rf_protect(SEXP p0) {
    // NOOP
    return p0;
  }

  public static void Rf_unprotect(int p0) {
    // NOOP
  }

  public static void R_ProtectWithIndex(SEXP p0, /*PROTECT_INDEX **/ IntPtr p1) {
    // NOOP
  }

  public static void R_Reprotect(SEXP p0, /*PROTECT_INDEX*/ int p1) {
    // NOOP
  }

  public static SEXP R_FixupRHS(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("R_FixupRHS");
  }
}
