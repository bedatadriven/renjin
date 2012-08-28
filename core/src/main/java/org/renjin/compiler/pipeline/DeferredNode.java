package org.renjin.compiler.pipeline;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.Vector;

import java.util.List;
import java.util.Set;

public class DeferredNode {

  private int id;
  private List<DeferredNode> operands = Lists.newArrayList();
  private Set<DeferredNode> uses = Sets.newIdentityHashSet();
  private Vector vector;

  public DeferredNode(int id, Vector vector) {
    this.id = id;
    if(vector instanceof MemoizedComputation && ((MemoizedComputation) vector).isCalculated()) {
      this.vector = ((MemoizedComputation)vector).forceResult();
    } else {
      this.vector = vector;
    }
  }

  public boolean isComputation() {
    return vector instanceof DeferredComputation;
  }

  public DeferredComputation getComputation() {
    return (DeferredComputation) vector;
  }

  public void addOperand(DeferredNode node) {
    assertNotCircularReference(node);
    operands.add(node);
    if(vector instanceof RepDoubleVector && operands.size() > 3) {
      System.out.println("duplicates added");
    }
  }

  public void addUse(DeferredNode node) {
    assertNotCircularReference(node);
    uses.add(node);
  }

  private void assertNotCircularReference(DeferredNode node) {
    if(node == this) {
      throw new IllegalArgumentException("Circular reference: " + vector.getClass().getName());
    }
  }

  public int getId() {
    return id;
  }

  public String getDebugLabel() {
    if(vector instanceof DoubleArrayVector) {
      if(vector.length() == 1) {
        return Double.toString(vector.getElementAsDouble(0));
      } else {
        return "[" + vector.length() + "]";
      }
    } else if(vector instanceof IntArrayVector) {
      if(vector.length() == 1) {
        return Integer.toString(vector.getElementAsInt(0));
      } else {
        return "[" + vector.length() + "]";
      }
    } else if(vector instanceof DeferredComputation) {
      return getComputation().getComputationName();
    } else {
      return vector.getClass().getSimpleName();
    }
  }

  public List<DeferredNode> getOperands() {
    return operands;
  }

  public String getDebugId() {
    return "N" + getId();
  }

  /**
   * Checks if this node is "equivalent" (can replace)
   * the given {@code newNode}. Two nodes are equivalent if they are
   * <ul>
   *   <li>Are both {@link DeferredComputation}s with equal {@code class}es with equivalent operands</li>
   *   <li>Are both ArrayVectors with the same memory address</li>
   *   <li>Are both scalars with equal values</li>
   * </ul>
   * @param newNode
   * @return
   */
  public boolean equivalent(DeferredNode newNode) {
    if(!vector.getClass().equals(newNode.vector.getClass())) {
      return false;
    }
    if(isComputation()) {
      if(getOperands().size() != newNode.getOperands().size()) {
        return false;
      }

      for(int i=0;i!=getOperands().size();++i) {
        if(getOperands().get(i).getId() != newNode.getOperands().get(i).getId()) {
          return false;
        }
      }
      return true;
    } else if(vector instanceof IntArrayVector || vector instanceof DoubleArrayVector) {

      if(vector.length() != newNode.vector.length()) {
        return false;
      }
      if(vector.length() > 10) {
        return false;
      }
      for(int i=0;i!=vector.length();++i) {
        if(vector.getVectorType().compareElements(vector, i, newNode.vector, i) != 0) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    if(operands.isEmpty()) {
      return getDebugLabel();
    } else {
      return getDebugLabel() + "(" + Joiner.on(", ").join(operands) + ")";
    }
  }

  public Vector getVector() {
    return vector;
  }

  /**
   *  Flattens this subgraph into
   */
  public List<DeferredNode> flatten() {
    List<DeferredNode> nodes = Lists.newArrayList();
    flatten(nodes);
    return nodes;
  }

  public Vector[] flattenVectors() {
    List<DeferredNode> nodes = flatten();
    Vector[] vectors = new Vector[nodes.size()];
    for(int i=0;i!=vectors.length;++i) {
      vectors[i] = nodes.get(i).getVector();
    }
    return vectors;
  }

  public JitKey jitKey() {
    List<DeferredNode> nodes = flatten();
    Class[] classes = new Class[nodes.size()];
    for(int i=0;i!=classes.length;++i) {
      classes[i] = nodes.get(i).getVector().getClass();
    }
    return new JitKey(classes);
  }

  public void setResult(Vector result) {
    this.vector = result;
    for(DeferredNode operand : operands) {
      operand.removeUse(this);
    }
    operands.clear();
  }

  private void flatten(List<DeferredNode> nodes) {
    nodes.add(this);
    for(DeferredNode operand : operands) {
      operand.flatten(nodes);
    }
  }

  public DeferredNode getOperand(int index) {
    return getOperands().get(index);
  }

  public void replaceVector(Vector vector) {
    this.vector = vector;
  }



  public void replaceOperands(DeferredNode... operands) {
    this.operands = Lists.newArrayList(operands);
  }

  public boolean hasValue(double v) {
    return (vector instanceof DoubleArrayVector || vector instanceof IntArrayVector) &&
            vector.length() == 1 &&
            vector.getElementAsDouble(0) == v;
  }

  public void replaceOperand(DeferredNode node, DeferredNode replacementValue) {
    for(int i=0;i!=operands.size();++i) {
      if(operands.get(i) == node) {
        operands.get(i).removeUse(this);
        operands.set(i, replacementValue);
        replacementValue.addUse(this);
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
}
