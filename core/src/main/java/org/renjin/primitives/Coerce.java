package org.renjin.primitives;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.*;
import org.renjin.sexp.SexpType;
import org.renjin.util.CDefines;

import static org.renjin.base.Base.R_isS4Object;
import static org.renjin.sexp.SexpType.ANYSXP;
import static org.renjin.sexp.SexpType.EXPRSXP;
import static org.renjin.sexp.SexpType.NILSXP;
import static org.renjin.util.CDefines.*;
import static org.renjin.util.CDefines.R_NamesSymbol;

public class Coerce {


  /* SHOULD BE IN Deparse CLASS: */
  /* deparse1line concatenates all lines into one long one */
  /* This is needed in terms.formula, where we must be able */
  /* to deparse a term label into a single line of text so */
  /* that it can be reparsed correctly */
  public static String deparse1line(@Current Context context, SEXP call, boolean abbrev) {
    // deparse to a single long line
    return Deparse.deparseExp(context, call);
  }

  public static boolean isVectorizable(SEXP p0) {
    /**
     * each element of a list or pairlist is a vector of length 0 or 1
     * */
    if (p0 instanceof PairList) {
      PairList.Node node = (PairList.Node) p0;
      while (node.hasNextNode()) {
        if(node.getValue().length() != 1 && node.getValue().length() != 0 ) {
          return false;
        }
        node = node.getNextNode();
      }
      return true;
    } else if (p0 instanceof ListVector) {
      if(((ListVector) p0).maxElementLength() > 1) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  public static void CoercionWarning(int warn) {
    switch (warn) {
      case 1:
        java.lang.System.err.println("NAs introduced by coercion");
        break;
      case 2:
        java.lang.System.err.println("NAs introduced by coercion to integer range");
        break;
      case 4:
        java.lang.System.err.println("imaginary parts discarded in coercion");
        break;
      case 8:
        java.lang.System.err.println("out-of-range values treated as 0 in coercion to raw");
        break;
    }
  }


  private static EvalException UNIMPLEMENTED_TYPE(String s, SEXP t) {
    return new EvalException("unimplemented type '%s' in '%s'\n", t.getTypeName(), s);
  }


  public static SEXP coerceSymbol(SEXP vector, int type) {
    SEXP rval;
    switch(type) {
      case EXPRSXP:
        ListVector.Builder listVector = new ListVector.Builder(1);
        rval = listVector.add(vector).build();
        break;
      case SexpType.CHARSXP:
      case SexpType.STRSXP:
        StringVector.Builder stringVector = new StringVector.Builder(1);
        rval = stringVector.add(vector.getNames()).build();
        break;
      default:
        throw new EvalException("(symbol) object cannot be coerced to type " + type); //+ type2char(type));
    };
    return rval;
  }

  public static SEXP coercePairList(@Current Context context, Vector vectors, int type) {
    int i, n = 0;
    // SEXP rval = null, vp, names;
    //names = vectors;
    SEXP vp;
    ListVector.Builder rval = new ListVector.Builder();
    Vector.Builder names;

    /* Hmm, this is also called to LANGSXP, and coerceVector already
       did the check of TYPEOF(v) == type */
    /* IS pairlist */
    if (type == SexpType.LISTSXP) {
      return vectors;
    }

    if (type == EXPRSXP) {
      return (SEXP) rval.add(vectors).build();
    } else if (type == SexpType.STRSXP) {
      n = vectors.length();
      for(vp = vectors, i = 0; vp != R_NilValue; vp = CDR(vp), i++) {
        if (isString(CAR(vp)) && CAR(vp).length() == 1) {
          rval.add(STRING_ELT(CAR(vp), 0));
        } else {
          StringVector.Builder stringVector = new StringVector.Builder();
          stringVector.add(deparse1line(context, STRING_ELT(CAR(vp), 0), false));
          rval.add(stringVector.build());
        }
      }
    } else if (type == SexpType.VECSXP) {
      rval.add(PairList.Node.fromVector(vectors));
      return rval.build();
    } else if (isVectorizable(vectors)) {
      n = vectors.length();
      switch(type) {
        case SexpType.LGLSXP:
        case SexpType.INTSXP:
        case SexpType.REALSXP:
        case SexpType.CPLXSXP:
        case SexpType.RAWSXP:
          for (i = 0, vp = vectors; i < n; i++, vp = CDR(vp)) {
            //   Implementations in GNU R original C code
            //   LOGICAL(rval)[i] = asLogical(CAR(vp));
            //   INTEGER(rval)[i] = asInteger(CAR(vp));
            //   REAL(rval)[i] = asReal(CAR(vp));
            //   COMPLEX(rval)[i] = asComplex(CAR(vp));
            //   RAW(rval)[i] = asRaw(CAR(vp));
            rval.set(i, CAR(vp));
          }
          break;
        default: throw UNIMPLEMENTED_TYPE("coercePairList", vectors);
      }
    } else {
      throw new EvalException("'pairlist' object cannot be coerced to type " + type); //+ type2char(type));
    }
    /* If any tags are non-null then we */
    /* need to add a names attribute. */
    for (vp = vectors, i = 0; vp != R_NilValue; vp = CDR(vp)) {
      if (TAG(vp) != R_NilValue) {
        i = 1;
      }
    }
    if (i == 1) {
      i = 0;
      names = allocVector(STRSXP, n);
      for (vp = vectors; vp != R_NilValue; vp = CDR(vp), i++) {
        if (TAG(vp) != R_NilValue) {
          names.set(i, PRINTNAME(TAG(vp)));
        }
      }
      setAttrib(rval, R_NamesSymbol, names.build());
    }
    return rval.build();
  }

  /* Coerce a vector list to the given type */
  public static SEXP coerceVectorList(@Current Context context, SEXP vector, int type) throws EvalException {
    int warn = 0;
    double tmp;
    int /*R_xlen_t*/ i, n;
    SEXP names = vector;
    SEXP rval;

    /* expression -> list, new in R 2.4.0 */
    if (type == SexpType.VECSXP && TYPEOF(vector).equals(CDefines.EXPRSXP)) {
      ListVector.Builder listVector = new ListVector.Builder();
      return listVector.add(vector).build();
    }
    if (type == SexpType.EXPRSXP && TYPEOF(vector).equals(CDefines.VECSXP)) {
      return (ExpressionVector) vector;
    }
    if (type == SexpType.STRSXP) {
      n = vector.length();
      StringVector.Builder stringVector = new StringVector.Builder(n);
      for (i = 0; i < n;  i++) {
        if (isString(VECTOR_ELT(vector, i)) && VECTOR_ELT(vector, i).length() == 1) {
          stringVector.add(STRING_ELT(VECTOR_ELT(vector, i), 0));
        } else {
          stringVector.add(deparse1line(context, VECTOR_ELT(vector, i), false));
        }
      }
      names = getAttrib(vector, R_NamesSymbol);
      if (names != R_NilValue) {
        setAttrib(stringVector, R_NamesSymbol, names);
      }
      rval = stringVector.build();
    } else if (type == SexpType.LISTSXP) {
      return PairList.Node.fromVector((Vector) vector);
    } else if (isVectorizable(vector)) {
      n = vector.length();
      switch (type) {
        case SexpType.LGLSXP:
          LogicalVector.Builder logicalVector = new ListVector.Builder();
          for (i = 0; i < n; i++) {
            logicalVector.add(VECTOR_ELT(vector, i));
          }
          names = getAttrib(vector, R_NamesSymbol);
          if (names != R_NilValue) {
            setAttrib(logicalVector, R_NamesSymbol, names);
          }
          rval = logicalVector.build();
          break;
        case SexpType.INTSXP:
          IntVector.Builder intVector = new IntArrayVector.Builder();
          for (i = 0; i < n; i++) {
            intVector.add(VECTOR_ELT(vector, i));
          }
          names = getAttrib(vector, R_NamesSymbol);
          if (names != R_NilValue) {
            setAttrib(intVector, R_NamesSymbol, names);
          }
          rval = intVector.build();
          break;
        case SexpType.REALSXP:
          DoubleVector.Builder realVector = new DoubleArrayVector.Builder();
          for (i = 0; i < n; i++) {
            realVector.add(VECTOR_ELT(vector, i));
          }
          names = getAttrib(vector, R_NamesSymbol);
          if (names != R_NilValue) {
            setAttrib(realVector, R_NamesSymbol, names);
          }
          rval = realVector.build();
          break;
        case SexpType.CPLXSXP:
          ComplexArrayVector.Builder complexVector = new ComplexArrayVector.Builder();
          for (i = 0; i < n; i++) {
            complexVector.add(VECTOR_ELT(vector, i));
          }
          names = getAttrib(vector, R_NamesSymbol);
          if (names != R_NilValue) {
            setAttrib(complexVector, R_NamesSymbol, names);
          }
          rval = complexVector.build();
          break;
        case SexpType.RAWSXP:
          RawVector.Builder rawVector = new RawVector.Builder();
          for (i = 0; i < n; i++) {
            tmp = VECTOR_ELT(vector, i).asReal();
            if (tmp < 0 || tmp > 255) { /* includes NA_INTEGER */
              rawVector.set(i, (byte) 0);
              // tmp = 0;
              warn |= 8;
            } else {
              rawVector.add(VECTOR_ELT(vector, i));
            }
          }
          names = getAttrib(vector, R_NamesSymbol);
          if (names != R_NilValue) {
            setAttrib(rawVector, R_NamesSymbol, names);
          }
          rval = rawVector.build();
          break;
        default:
          throw UNIMPLEMENTED_TYPE("coerceVectorList", vector);
      }
    } else {
      throw new EvalException("(list) object cannot be coerced to type '" + type + "'"); //+ type2char(type));
    }

    if (warn != 0) {
      CoercionWarning(warn);
    }
    return rval;
  }



  public static SEXP coerceVector(@Current Context context, SEXP vector, SexpType type) {
    SEXP op, vp, ans = R_NilValue;	/* -Wall */
    int i, n;

    if (TYPEOF(vector) == type) {
      return vector;
    }
    /* code to allow classes to extend ENVSXP, SYMSXP, etc */
    if(R_isS4Object(vector).equals(LogicalVector.TRUE) && TYPEOF(vector) == S4SXP) {
      SEXP vv = R_getS4DataSlot(vector, ANYSXP);
      if(vv == R_NilValue) {
        throw new EvalException("no method for coercing this S4 class to a vector");
      }
      else if(TYPEOF(vv) == type) {
        return vv;
      }
      vector = vv;
    }

    switch (TYPEOF(vector)) {
      case SexpType.SYMSXP:
        ans = coerceSymbol(vector, type);
        break;
      case SexpType.NILSXP:
      case SexpType.LISTSXP:
        ans = coercePairList(context, (Vector) vector, type);
        break;
      case SexpType.LANGSXP:
        if (type != STRSXP) {
          ans = coercePairList(context, (Vector) vector, type);
          break;
        }

        /* This is mostly copied from coercePairList, but we need to
         * special-case the first element so as not to get operators
         * put in backticks. */
        n = vector.length();
        ans = allocVector(type, n);
        if (n == 0) {
          /* Can this actually happen? */
          break;
        }
        i = 0;
        op = CAR(vector);
        /* The case of practical relevance is "lhs ~ rhs", which
         * people tend to split using as.character(), modify, and
         * paste() back together. However, we might as well
         * special-case all symbolic operators here. */
        if (TYPEOF(op) == SYMSXP) {
          SET_STRING_ELT(ans, i, PRINTNAME(op));
          i++;
          vector = CDR(vector);
        }

        /* The distinction between strings and other elements was
         * here "always", but is really dubious since it makes x <- a
         * and x <- "a" come out identical. Won't fix just now. */
        for (vp = vector;  vp != R_NilValue; vp = CDR(vp), i++) {
          if (isString(CAR(vp)) && length(CAR(vp)) == 1) {
            SET_STRING_ELT(ans, i, STRING_ELT(CAR(vp), 0));
          } else {
            SET_STRING_ELT(ans, i, STRING_ELT(deparse1line(CAR(vp), 0), 0));
          }
        }
        break;
      case VECSXP:
      case EXPRSXP:
        ans = coerceVectorList(context, vector, type);
        break;
      case ENVSXP:
        throw new EvalException("environments cannot be coerced to other types");
        break;
      case LGLSXP:
      case INTSXP:
      case REALSXP:
      case CPLXSXP:
      case STRSXP:
      case RAWSXP:
//        switch (type) {
//          case SYMSXP:
//            ans = coerceToSymbol(vector);
//            break;
//          case LGLSXP:
//            ans = coerceToLogical(vector);
//            break;
//          case INTSXP:
//            ans = coerceToInteger(vector);
//            break;
//          case REALSXP:
//            ans = coerceToReal(vector);
//            break;
//          case CPLXSXP:
//            ans = coerceToComplex(vector);
//            break;
//          case RAWSXP:
//            ans = coerceToRaw(vector);
//            break;
//          case STRSXP:
//            ans = coerceToString(vector);
//            break;
//          case EXPRSXP:
//            ans = coerceToExpression(vector);
//            break;
//          case VECSXP:
//            ans = coerceToVectorList(vector);
//            break;
//          case LISTSXP:
//            ans = coerceToPairList(vector);
//            break;
//          default:
//            throw new EvalException("cannot coerce type '%s' to vector of type '%s'", type2char(TYPEOF(vector)), type2char(type));
//        }
        throw new EvalException("TODO: coercion RAWSXP to other object types not yet implemented.");
        break;
      default:
        throw new EvalException("cannot coerce type '%s' to vector of type '%s'", type2char(TYPEOF(vector)), type2char(type));
    }
    return ans;
  }




}
