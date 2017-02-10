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
package org.renjin.primitives;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.*;

import static org.renjin.util.CDefines.*;

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
      return ((ListVector) p0).maxElementLength() <= 1;
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

  /* Return a suitable S3 object (OK, the name of the routine comes from
   an earlier version and isn't quite accurate.) If there is a .S3Class
   slot convert to that S3 class.
   Otherwise, unless mode == S4SXP, look for a .Data or .xData slot.  The
   value of mode controls what's wanted.  If it is S4SXP, then ONLY
   .S3class is used.  If it is ANYSXP, don't check except that automatic
   conversion from the current mode only applies for classes that extend
   one of the basic types (i.e., not S4SXP).  For all other types, the
   recovered data must match the mode.
   Because S3 objects can't have mode S4SXP, .S3Class slot is not searched
   for in that mode object, unless ONLY that class is wanted.
   (Obviously, this is another routine that has accumulated barnacles and
   should at some time be broken into separate parts.)
*/
//  public static SEXP R_getS4DataSlot(SEXP obj, String mode) {
//    SEXP s_xData;
//    SEXP s_dotData;
//    SEXP value = R_NilValue;
////    PROTECT_INDEX opi;
////    PROTECT_WITH_INDEX(obj, &opi);
////    if(!s_xData) {
//    s_xData = install(".xData");
//    s_dotData = install(".Data");
////    }
//    if( !(obj.getTypeName().equals("S4") || mode.equals("S4")) ) {
//      SEXP s3class = obj.getS3Class();
//      if(s3class == R_NilValue && mode.equals("S4")) {
//        return R_NilValue;
//      }
//      if(s3class != R_NilValue) {/* replace class with S3 class */
//        setAttrib(obj, R_ClassSymbol, s3class);
////        setAttrib(obj, s_dot_S3Class, R_NilValue); /* not in the S3 class */
//      } else { /* to avoid inf. recursion, must unset class attribute */
//        setAttrib(obj, R_ClassSymbol, R_NilValue);
//      }
//      if(mode.equals("S4")) {
//        return obj;
//      }
//      value = obj;
//    } else {
//      value = getAttrib(obj, Symbol.get(s_dotData.getTypeName()));
//    }
//    if(value == R_NilValue) {
//      value = getAttrib(obj, Symbol.get(s_xData));
//    }
//
///* the mechanism for extending abnormal types.  In the future, would b
//   good to consolidate under the ".Data" slot, but this has
//   been used to mean S4 objects with non-S4 mode, so for now
//   a secondary slot name, ".xData" is used to avoid confusion
//*/
//    if(value != R_NilValue && (mode.equals("any") || mode.equals(value.getTypeName()))) {
//      return value;
//    } else {
//      return R_NilValue;
//    }
//  }


  public static SEXP coerceSymbol(SEXP vector, String mode) {
    SEXP rval;
    switch (mode) {
      case "expression":
        ListVector.Builder listVector = new ListVector.Builder(1);
        rval = listVector.add(vector).build();
        break;
      case "char":
      case "character":
        StringVector.Builder stringVector = new StringVector.Builder(1);
        rval = stringVector.add(vector.getNames()).build();
        break;
      default:
        throw new EvalException("(symbol) object cannot be coerced to mode " + mode); //+ type2char(mode));
    }
    return rval;
  }

  public static SEXP coercePairList(@Current Context context, Vector vectors, String mode) {
    int i, n = 0;
    SEXP vp;
    ListVector.Builder rval = new ListVector.Builder();
    Vector.Builder names;

    /* Hmm, this is also called to LANGSXP, and coerceVector already
       did the check of TYPEOF(v) == mode */
    /* IS pairlist */
    if (mode.equals("list")) {
      return vectors;
    }

    if (mode.equals("expression")) {
      return rval.add(vectors).build();
    } else if (mode.equals("character")) {
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
    } else if (mode.equals("pairlist")) {
      rval.add(PairList.Node.fromVector(vectors));
      return rval.build();
    } else if (isVectorizable(vectors)) {
      n = vectors.length();
      switch (mode) {
        case "logical":
        case "integer":
        case "double":
        case "complex":
        case "raw":
          for (i = 0, vp = vectors; i < n; i++, vp = CDR(vp)) {
            rval.set(i, CAR(vp));
          }
          break;
        default: throw UNIMPLEMENTED_TYPE("coercePairList", vectors);
      }
    } else {
      throw new EvalException("'pairlist' object cannot be coerced to mode " + mode); //+ type2char(mode));
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

  /* Coerce a vector list to the given mode */
  public static SEXP coerceVectorList(@Current Context context, SEXP vector, String mode) throws EvalException {
    int warn = 0;
    double tmp;
    int /*R_xlen_t*/ i, n;
    SEXP names = vector;
    SEXP rval;

    /* expression -> list, new in R 2.4.0 */
    if ("list".equals(mode) && vector.getTypeName().equals("expression")) {
      ListVector.Builder listVector = new ListVector.Builder();
      return listVector.add(vector).build();
    }
    if ("expression".equals(mode) && vector.getTypeName().equals("list")) {
      return vector;
    }
    if ("character".equals(mode)) {
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
    } else if ("pairlist".equals(mode)) {
      return PairList.Node.fromVector((Vector) vector);
    } else if (isVectorizable(vector)) {
      n = vector.length();
      switch (mode) {
        case "logical":
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
        case "integer":
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
        case "double":
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
        case "complex":
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
        case "raw":
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
      throw new EvalException("(list) object cannot be coerced to mode '" + mode + "'"); //+ type2char(mode));
    }

    if (warn != 0) {
      CoercionWarning(warn);
    }
    return rval;
  }


  public static SEXP coerceVector(@Current Context context, SEXP vector, String mode) {
    SEXP op, vp, ans = R_NilValue;	/* -Wall */
    int i, n;

    if (vector.getTypeName().equals(mode)) {
      return vector;
    }
    /* code to allow classes to extend ENVSXP, SYMSXP, etc */
//    if(R_isS4Object(vector).equals(LogicalVector.TRUE) && vector.getTypeName().equals("S4")) {
//      SEXP vv = R_getS4DataSlot(vector, "any");
//      if(vv == R_NilValue) {
//        throw new EvalException("no method for coercing this S4 class to a vector");
//      } else if(vv.getTypeName().equals(mode)) {
//        return vv;
//      }
//      vector = vv;
//    }

    switch (vector.getTypeName()) {
      case "symbol":
        ans = coerceSymbol(vector, mode);
        break;
      case "NULL":
      case "pairlist":
        ans = coercePairList(context, (Vector) vector, mode);
        break;
      case "language":
        if (!"character".equals(mode)) {
          ans = coercePairList(context, (Vector) vector, mode);
          break;
        }

        /* This is mostly copied from coercePairList, but we need to
         * special-case the first element so as not to get operators
         * put in backticks. */
//        n = vector.length();
//        ans = allocVector(mode, n);
//        if (n == 0) {
//          /* Can this actually happen? */
//          break;
//        }
//        i = 0;
//        op = CAR(vector);
        /* The case of practical relevance is "lhs ~ rhs", which
         * people tend to split using as.character(), modify, and
         * paste() back together. However, we might as well
         * special-case all symbolic operators here. */
//        if (TYPEOF(op) == SYMSXP) {
//          SET_STRING_ELT(ans, i, PRINTNAME(op));
//          i++;
//          vector = CDR(vector);
//        }

        /* The distinction between strings and other elements was
         * here "always", but is really dubious since it makes x <- a
         * and x <- "a" come out identical. Won't fix just now. */
//        for (vp = vector;  vp != R_NilValue; vp = CDR(vp), i++) {
//          if (isString(CAR(vp)) && length(CAR(vp)) == 1) {
//            SET_STRING_ELT(ans, i, STRING_ELT(CAR(vp), 0));
//          } else {
//            SET_STRING_ELT(ans, i, STRING_ELT(deparse1line(CAR(vp), 0), 0));
//          }
//        }
        throw new EvalException("TODO: coercion LANGSXP to other object types not yet implemented.");
      case "list":
      case "expression":
        ans = coerceVectorList(context, vector, mode);
        break;
      case "environment":
        throw new EvalException("environments cannot be coerced to other types");
      case "logical":
      case "integer":
      case "double":
      case "complex":
      case "character":
      case "raw":
//        switch (mode) {
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
//            throw new EvalException("cannot coerce mode '%s' to vector of mode '%s'", type2char(TYPEOF(vector)), type2char(mode));
//        }
        throw new EvalException("TODO: coercion RAWSXP to other object types not yet implemented.");
      default:
        throw new EvalException("cannot coerce mode '%s' to vector of mode '%s'", vector.getTypeName(), mode);
    }
    return ans;
  }




}
