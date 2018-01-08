/**
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
package org.renjin.sexp;

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
  
  public static PairList newCallFromVector(ListVector vector) {
    FunctionCall.Builder call = new FunctionCall.Builder();
    call.addAll(vector);
    return call.build();
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
    builder.setAttributes(getAttributes());
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
        head = new FunctionCall(s, Null.INSTANCE, attributesBuilder.build());
        head.setTag(tag);
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
