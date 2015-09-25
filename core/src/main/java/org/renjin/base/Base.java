/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package org.renjin.base;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.methods.Methods;
import org.renjin.primitives.Evaluation;
import org.renjin.primitives.io.serialization.Serialization;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.primitives.matrix.MatrixBuilder;
import org.renjin.primitives.vector.BinCodeVector;
import org.renjin.sexp.*;

import java.io.IOException;


/**
 * Implementation of routines from the base dll.
 * 
 * <p>The functions implemented here are distinct from the collection of
 * primitives which make up most of the base package; these functions are
 * called from R code using the .Call("methodname", arg1, arg2, ... argn, PACKAGE="base") syntax.
 * 
 * <p>Also note that many methods use a convention whereby the values to be 
 * returned are allocated by the calling R code to save the C implementors the
 * trouble of dealing with memory management (I think), so many of these methods 
 * take what are, in renjin's context, unused arguments. 
 * 
 */
public class Base {

  private Base() { }
  
  
  public static ListVector R_getSymbolInfo(String sname, SEXP spackage, boolean withRegistrationInfo) {

    ListVector.Builder result = new ListVector.Builder();
    result.setAttribute(Symbols.CLASS, StringVector.valueOf("CRoutine"));

    return result.build();

  }

  public static ListVector R_getRegisteredRoutines(String dll) {
    ListVector.Builder builder = new ListVector.Builder();
    return builder.build();
  }

  /** @return  n if the data frame 'vec' has c(NA, n) rownames;
   *         nrow(.) otherwise;  note that data frames with nrow(.) == 0
   *          have no row.names.
   * ==> is also used in dim.data.frame()
   *
   * AB Note: I have no idea what this function really does but it is
   * so opaque that it is first against the wall when the revolution comes.
   */
  public static SEXP R_shortRowNames(SEXP vector, int type) {
    SEXP s =  vector.getAttribute(Symbols.ROW_NAMES);
    SEXP ans = s;

    if( type < 0 || type > 2) {
      throw new EvalException("invalid 'type' argument");
    }

    if(type >= 1) {
      int n;
      if (s instanceof IntVector && s.length() == 2 && ((IntVector) s).isElementNA(0)) {
        n = ((IntVector) s).getElementAsInt(1);
      } else {
        if (s == Null.INSTANCE) {
          n = 0;
        } else {
          n = s.length();
        }
      }
      if (type == 1) {
        ans = new IntArrayVector(n);
      } else {
        ans = new IntArrayVector(Math.abs(n));
      }
    }
    return ans;
  }
  
  /**
   * 
   * "native" implementation called by tabulate(), which 
   * takes the integer-valued vector bin and counts the number of times each integer occurs in it.
   * 
   * <p>There is a bin for each of the values 1, ..., nbins; values outside that range 
   * and NAs are (silently) ignored.
   * 
   * @param bin the integer vector to bin
   * @param length the length of bin
   * @param nbins the number of bins
   * @param ans not used
   * @return 
   */
  public static PairList R_tabulate(IntVector bin, int length, int nbins, SEXP ans) {
    int counts[] = new int[nbins];
    for(int i=0;i!=length;++i) {
      if(!bin.isElementNA(i)) {
        int value = bin.getElementAsInt(i);
        if(value >= 1 && value <= nbins) {
          counts[value-1]++;
        }
      }
    }
    return PairList.Node.singleton("ans", new IntArrayVector(counts));
  }
  
  public static SEXP Rrowsum_df(ListVector x, int ncol, Vector group, SEXP ugroup, boolean naRm) {
    throw new EvalException("nyi");
  }

  public static Vector Rrowsum_matrix(Vector x, int ncol, AtomicVector groups, AtomicVector ugroup, boolean naRm) {
    
    int numGroups = ugroup.length();
    
    Matrix source = new Matrix(x, ncol);
    MatrixBuilder result = source.newBuilder(numGroups, ncol);
    
    for(int col=0;col!=ncol;++col) {
      
      // sum the rows in this column by group
      
      double groupSums[] = new double[numGroups];
      for(int row=0;row!=source.getNumRows();++row) {
        int group = ugroup.indexOf(groups, row, 0);
        groupSums[group] += source.getElementAsDouble(row, col);
      }
      
      // copy sums to matrix
      for(int group=0;group!=ugroup.length();++group) {
        result.setValue(group, col, groupSums[group]);
      }
      
    }
    return result.build();
  } 
   
  
  public static SEXP R_copyDFattr(SEXP in, SEXP out) { 
    
    // NOTE: the equivalent C code actuallly modifies 'out' which
    // is not actually possible in Renjin-- we can only return a modified
    // copy. Not clear whether this will
    // have consequences -- this is called from [.data.frame
    ListVector attributesToCopy;
    if(in.hasAttributes()) {
      attributesToCopy = in.getAttributes().toVector();
    } else {
      attributesToCopy = new ListVector();
    }
    //IS_S4_OBJECT(in) ?  SET_S4_OBJECT(out) : UNSET_S4_OBJECT(out);
    //SET_OBJECT(out, OBJECT(in));

    return out.setAttributes(AttributeMap.fromListVector(attributesToCopy));
  }
  

  /* Gets the binding values of variables from a frame and returns them
   as a list.  If the force argument is true, promises are forced;
   otherwise they are not. */

  public static SEXP  R_getVarsFromFrame(@Current Context context, StringVector vars, Environment env, boolean force) {

    ListVector.NamedBuilder val = new ListVector.NamedBuilder();
    for(String var : vars) {
      SEXP boundValue = env.getVariable(var);
      if(boundValue == Symbol.UNBOUND_VALUE) {
        throw new EvalException("object %s not found", boundValue);
      }
      if(force) {
        boundValue = boundValue.force(context);
      }
      val.add(var, boundValue);
    }
    return val.build();
  }



  
  public static ListVector str_signif(Vector x, int n, String type, int width, int digits, String format, String flag, StringVector resultVector) {
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    result.add("result", StrSignIf.str_signif(x, width, digits, format, flag));
    return result.build();
  }


  public static SEXP R_serialize(@Current Context context, SEXP object, SEXP connection, boolean ascii,
      SEXP version, SEXP refhook) throws IOException {
    return Serialization.serialize(context, object, connection, ascii, version, refhook);
  }
  
  public static SEXP R_unserialize(@Current Context context, SEXP connection, SEXP refhook) throws IOException {
      return Serialization.unserialize(context, connection, refhook);
  }

  public static String crc64ToString(String value) {
    return Crc64.getCrc64(value);
  }
  public static SEXP R_isS4Object(SEXP exp) {
    if(exp instanceof S4Object) {
      return LogicalVector.TRUE;
    } else if(exp.getAttribute(Symbols.S4_BIT) != Null.INSTANCE) {
      return LogicalVector.TRUE;
    } else {
      return LogicalVector.FALSE;
    }
  }
  
  public static SEXP R_do_new_object(S4Object classRepresentation) {
    return Methods.R_do_new_object(classRepresentation);
  }
  
  public static ListVector do_mapply(@Current Context context, Function fun, ListVector varyingArgs, Vector constantArgs, Environment rho) {
    return Evaluation.mapply(context, fun, varyingArgs, constantArgs, rho);
  }

  /* bincode  cuts up the data using half open intervals defined as [a,b)
     (if right = FALSE) or (a, b] (if right = TRUE)
  */
  public static ListVector bincode(DoubleVector x, int n, DoubleVector breaks, int nb, IntVector code_,
                             boolean right, boolean include_border, boolean naok) {

    // if we NAs are not ok, we have to check and throw an error now, not later
    if(!naok) {
      if(x.indexOfNA() != -1) {
        throw new EvalException("NA's in bincode(NAOK=FALSE)");
      }
    }
    IntVector codedVector = new BinCodeVector(x, breaks.toDoubleArray(), !right, include_border, AttributeMap.EMPTY);

    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    result.add("code", codedVector);
    return result.build();
  }

}
