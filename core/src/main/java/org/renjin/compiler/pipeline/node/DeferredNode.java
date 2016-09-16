package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.specialization.SpecializationKey;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
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
      operands.add(array[i]);
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

  public void replaceOperand(DeferredNode node, DeferredNode replacementValue) {
    for(int i=0;i!=operands.size();++i) {
      if(operands.get(i) == node) {
        operands.get(i).removeUse(this);
        operands.set(i, replacementValue);
        replacementValue.addOutput(this);
      }
    }
  }
  
  public void removeAllInputs() {
    for (DeferredNode input : getOperands()) {
      input.removeUse(this);
    }
    getOperands().clear();
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


  public SpecializationKey jitKey() {
    throw new UnsupportedOperationException();
  }
  


  /**
   *  Flattens this subgraph into
   */
  public List<DeferredNode> flatten(Predicate<DeferredNode> predicate) {
    List<DeferredNode> nodes = Lists.newArrayList();
    flatten(predicate, nodes);
    return nodes;
  }

  private void flatten(Predicate<DeferredNode> predicate, List<DeferredNode> nodes) {
    nodes.add(this);
    if(predicate.apply(this)) {
      for (DeferredNode operand : operands) {
        operand.flatten(predicate, nodes);
      }
    }
  }

  public Vector[] flattenVectors() {
    List<DeferredNode> nodes = flatten(null);
    Vector[] vectors = new Vector[nodes.size()];
    for(int i=0;i!=vectors.length;++i) {
      vectors[i] = nodes.get(i).getVector();
    }
    return vectors;
  }

  public Set<DeferredNode> getUses() {
    return uses;
  }
}
