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

package r.lang.primitive;

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.primitive.annotations.ArgumentList;

import java.util.List;

public class Combine {

  private static final SymbolExp RECURSIVE = new SymbolExp("recursive");


  public static SEXP combine(@ArgumentList PairList arguments) {

    // parse arguments
    // we need to look for
    boolean recursive = false;
    List<SEXP> expressions = Lists.newArrayList();

    for(PairList.Node node : arguments.nodes()) {
      if(node.getRawTag().equals(RECURSIVE)) {
        recursive = true;
      } else if(node.getValue() != Null.INSTANCE) {
        expressions.add(node.getValue());
      }
    }

    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(expressions);

    // Build a new vector with all the elements
    return inspector.buildResult();
  }

  public static AtomicVector unlist(AtomicVector vector, boolean recursive, boolean useNames) {
    return vector;
  }

  public static Vector unlist(ListVector vector, boolean recursive, boolean useNames) {
    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(vector);

    return inspector.buildResult();
  }


  /**
   * Finds the common type of an expression
   */
  static class Inspector extends SexpVisitor {

    private boolean recursive = false;

    private int resultLength;
    private Vector.Type resultType = Null.VECTOR_TYPE;
    private List<SEXP> results = Lists.newArrayList();

    /**
     * Visits each element of {@code ListExp}
     */
    Inspector(boolean recursive) {
      this.recursive = recursive;
    }

    @Override
    public void visit(DoubleVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      add(vector);
    }

    @Override
    public void visit(IntVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      add(vector);
    }

    @Override
    public void visit(LogicalVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      add(vector);
    }

    @Override
    public void visit(Null nullInstance) {
      // ignore
    }

    @Override
    public void visit(StringVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      add(vector);
    }

    @Override
    public void visit(ListVector vector) {
      if(recursive) {
        acceptAll(vector);
      } else {
        resultType = Vector.Type.widest(resultType, vector);
        add(vector);
      }
    }

    @Override
    public void visit(ExpressionVector vector) {
      visit((ListVector)vector);
    }

    @Override
    protected void unhandled(SEXP exp) {
      resultType = Vector.Type.widest(resultType, ListVector.VECTOR_TYPE);
      add(exp);
    }

    private void add(SEXP exp) {
      results.add(exp);
      resultLength += exp.length();
    }

    /**
     * @return the common type of the visited expressions
     */
    public Vector.Type getResultType() {
      return resultType;
    }

    public int getResultLength() {
      return resultLength;
    }

    public List<SEXP> getResults() {
      return results;
    }

    public Vector buildResult() {
      Vector.Builder vector = resultType.newBuilder();
      int vectorLength = 0;

      for(SEXP exp : results) {
        for(int i=0;i!=exp.length();++i) {
          vector.setFrom(vectorLength++, exp, i);
        }
      }
      return vector.build();
    }
  }
}
