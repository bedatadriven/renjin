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

package r.lang;

/**
 * A vector of {@link FunctionCall}s
 *
 */
public class ExpressionVector extends ListVector {
  public static final String TYPE_NAME = "expression";
  public static final int TYPE_CODE = 20;


  public ExpressionVector(SEXP[] functionCalls, PairList attributes) {
    super(functionCalls, attributes);
  }

  public ExpressionVector(SEXP... functionCalls) {
    super(functionCalls);
  }

  public ExpressionVector(Iterable<SEXP> expressions, PairList attributes) {
    super(expressions, attributes);
  }

  public ExpressionVector(Iterable<SEXP> expressions){
    super(expressions);
  }

  @Override
  public EvalResult evaluate(Context context, Environment rho) {
    EvalResult result = EvalResult.NON_PRINTING_NULL;
    for(SEXP sexp : this) {
      result = sexp.evaluate(context, context.getEnvironment());
    }
    return result;
  }

  @Override
  public Builder newBuilder(int initialSize) {
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public Builder newCopyBuilder() {
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (SEXP s : this) {
      sb.append("\t").append(s).append("\n");
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }
}
