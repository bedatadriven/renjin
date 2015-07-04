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

package org.renjin.sexp;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;

/**
 * Expression representing a call to an R function, consisting of
 * a function reference and a list of arguments.
 *
 * Note that this type is called "language" in the R vocabulary.
 *
 */
public class FunctionCall extends PairList.Node {
  public static final String TYPE_NAME = "language";
  public static final String IMPLICIT_CLASS = "call";

  public FunctionCall(SEXP function, PairList arguments) {
    super(function, arguments);
  }

  public FunctionCall(SEXP function, PairList arguments, AttributeMap attributes) {
    super(Null.INSTANCE, function, attributes, arguments);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }
  public static SEXP fromListExp(PairList.Node listExp) {
    return new FunctionCall(listExp.value, listExp.nextNode);
  }

  public SEXP getFunction() {
    return value;
  }

  public PairList getArguments() {
    return nextNode == null ? Null.INSTANCE : nextNode;
  }

  public <X extends SEXP> X getArgument(int index) {
    return getArguments().<X>getElementAsSEXP(index);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public SEXP evaluate(Context context, Environment rho) {
    context.clearInvisibleFlag();
    Function functionExpr = evaluateFunction(context, getFunction(), rho);
    return functionExpr.apply(context, rho, this, getArguments());
  }

  private Function evaluateFunction(Context context, SEXP functionExp, Environment rho) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
      Function fn = rho.findFunction(context, symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());
      }
      return fn;
    } else {
      SEXP evaluated = functionExp.evaluate(context, rho).force(context);
      if(!(evaluated instanceof Function)) {
        throw new EvalException("'function' of lang expression is of unsupported type '%s'", evaluated.getTypeName());
      }
      return (Function)evaluated;
    }
  }


  @Override
  public String toString() {
    StringBuilder sb= new StringBuilder();
    sb.append(getFunction()).append("(");
    boolean needsComma=false;
    for(PairList.Node node : getArguments().nodes()) {
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

  public static FunctionCall newCall(SEXP function, SEXP... arguments) {
    if(arguments.length == 0) {
      return new FunctionCall(function, Null.INSTANCE);
    } else {
      return new FunctionCall(function, PairList.Node.fromArray(arguments));
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new FunctionCall(getFunction(), getArguments(), attributes);
  }

  @Override
  public FunctionCall clone() {
    return new FunctionCall(getFunction(), getArguments());
  }

  @Override
  public String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  @Override
  public int hashCode() {
    return getFunction().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if(other == null) {
      return false;
    }
    if(other.getClass() != FunctionCall.class) {
      return false;
    }
    FunctionCall otherCall = (FunctionCall) other;
    return getFunction().equals(otherCall.getFunction()) &&
        getArguments().equals(otherCall.getArguments());
  }

  @Override
  public Builder newCopyBuilder() {
    Builder builder = new Builder();
    builder.withAttributes(attributes);
    for(Node node : nodes()) {
      builder.add(node.getRawTag(), node.getValue());
    }
    return builder;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public SEXP getNamedArgument(String name) {
    for(PairList.Node node : getArguments().nodes()) {
      if(node.hasTag() && node.getTag().getPrintName().equals(name)) {
        return node.getValue();
      }
    }
    return Null.INSTANCE;
  }

  public static class Builder extends PairList.Builder {

    public Builder add(SEXP tag, SEXP s) {
      if (head == null) {
        head = new FunctionCall(s, Null.INSTANCE, attributes);
        tail = head;
      } else {
        Node next = new Node(tag, s, Null.INSTANCE);
        tail.nextNode = next;
        tail = next;
      }
      return this;
    }
  }

}
