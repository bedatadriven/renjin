/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.pipeliner.node;

import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Vector;

import java.util.List;
import java.util.Set;

public abstract class DeferredNode {

  private List<DeferredNode> operands = Lists.newArrayList();
  private Set<DeferredNode> uses = Sets.newIdentityHashSet();
  
  public DeferredNode() {
  }

  public int addInput(DeferredNode node) {
//    assertNotCircularReference(node);
    operands.add(node);
//    if(vector instanceof RepDoubleVector && operands.size() > 3) {
//      System.out.println("duplicates added");
//    }
    return operands.size() - 1;
  }


  public void addInputs(DeferredNode[] array) {
    for (int i = 0; i < array.length; i++) {
      DeferredNode input = array[i];
      operands.add(input);
      input.uses.add(this);
    }
  }

  public void addOutput(DeferredNode node) {
//    assertNotCircularReference(node);
    uses.add(node);
  }

//  private void assertNotCircularReference(DeferredNode node) {
//    if(node == this) {
//      throw new IllegalArgumentException("Circular reference: " + vector.getClass().getName());
//    }
//  }

  public abstract String getDebugLabel();

  public List<DeferredNode> getOperands() {
    return operands;
  }

  public void replaceOperands(DeferredNode... operands) {
    this.operands = Lists.newArrayList(operands);
  }

  public String getDebugId() {
    return "N" + Integer.toHexString(System.identityHashCode(this));
  }


  /**
   * @return the name of the shape when plotting this node to graphviz.
   */
  public abstract NodeShape getShape();

  @Override
  public String toString() {
    if(operands.isEmpty()) {
      return getDebugLabel();
    } else {
      return getDebugLabel() + "(" + Joiner.on(", ").join(operands) + ")";
    }
  }

  public abstract Type getResultVectorType();
  
  public boolean hasValue(double x) {
    return false;
  }

//  public void setResult(Vector result) {
//    //System.out.println(this + " I got a result: " +    AggregationRecycler.isCached(this.toString()) + "/"+ this.isMemoized());
//    this.vector = result;
//    for(DeferredNode operand : operands) {
//      operand.removeUse(this);
//    }
//    operands.clear();
//  }


  public DeferredNode getOperand(int index) {
    return getOperands().get(index);
  }

  public void replaceOperand(DeferredNode toReplace, DeferredNode replacementValue) {
    for(int i=0;i!=operands.size();++i) {
      if(operands.get(i) == toReplace) {
        operands.set(i, replacementValue);
        replacementValue.addOutput(this);
      }
    }
  }

  public void removeUse(DeferredNode node) {
    uses.remove(node);
  }

  public void replaceUse(DeferredNode node, DeferredNode replacementValue) {
    if(uses.remove(node)) {
      uses.add(replacementValue);
    }
  }

  public boolean isUsed() {
    return !uses.isEmpty();
  }
  
  public Vector getVector() {
    throw new UnsupportedOperationException("getVector(): " + this.getClass().getName());
  }

  public void setResult(Vector result) {
    throw new UnsupportedOperationException();
  }


  public Set<DeferredNode> getUses() {
    return uses;
  }

  public final boolean isLeaf() {
    return operands.isEmpty();
  }
}
