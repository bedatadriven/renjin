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
 * A specialized {@code ListExp} used for storing 
 */
public class LangExp extends PairListExp {
  public static final int TYPE_CODE = 6;
  public static final String TYPE_NAME = "language";

  public LangExp(SEXP function, PairList arguments) {
    super(function, arguments);
  }

  public LangExp(SEXP function, PairList arguments, PairList attributes, SEXP tag) {
    super(tag, function, attributes, arguments);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    FunExp functionExpr = evaluateFunction(rho);
    return functionExpr.apply(this, getArguments(), rho);
  }

  private FunExp evaluateFunction(EnvExp rho) {
    return (FunExp) getFunction().evalToExp(rho);
  }

  public static SEXP fromListExp(PairListExp listExp) {
    return new LangExp(listExp.value, listExp.nextNode);
  }

  public SEXP getFunction() {
    return value;
  }
 
  public PairList getArguments() {
    return nextNode == null ? NullExp.INSTANCE : nextNode;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);

  }

  public <X extends SEXP> X getArgument(int index) {
    return getArguments().<X>get(index);
  }

  @Override
  public String toString() {
    StringBuilder sb= new StringBuilder();
    sb.append(getFunction()).append("(");
    boolean needsComma=false;
    for(PairListExp node : getNextNode().listNodes()) {
      if(needsComma) {
        sb.append(", ");
      } else {
        needsComma = true;
      }
      if(node.hasTag()) {
        sb.append(node.getTag().getPrintName())
            .append("=");
      }
      sb.append(node.getValue());
    }
    return sb.append(")").toString();
  }
}
