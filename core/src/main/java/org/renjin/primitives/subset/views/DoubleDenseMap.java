package org.renjin.primitives.subset.views;

import org.renjin.iterator.IntIterator;
import org.renjin.primitives.subset.Selection;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.Arrays;


public class DoubleDenseMap extends DoubleVector implements DeferredComputation {
  
  public static int MAX_LENGTH = 100;
  
  private Vector source;
  private Vector replacement;
  private IntArrayVector indexMapVector;

  private int[] map; 

  public DoubleDenseMap(Vector source, Vector replacement, IntArrayVector indexMapVector,
                        AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.replacement = replacement;
    this.indexMapVector = indexMapVector;
    this.map = indexMapVector.toIntArrayUnsafe();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, replacement, indexMapVector };
  }

  @Override
  public String getComputationName() {
    return "replace";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleDenseMap(source, replacement, indexMapVector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    int replacementIndex = map[index];
    if(replacementIndex == -1) {
      return source.getElementAsDouble(index);
    } else {
      return replacement.getElementAsDouble(replacementIndex);
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return source.isConstantAccessTime() && replacement.isConstantAccessTime();
  }

  @Override
  public int length() {
    return source.length();
  }

  public static boolean accept(Vector source, Selection selection, Vector replacement) {
    return source.length() <= MAX_LENGTH;
  }

  public static DoubleDenseMap replace(Vector source, Selection selection, Vector replacement) {
    IntArrayVector map = denseIndexMap(selection, source.length(), replacement.length());
    return new DoubleDenseMap(source, replacement, map,  source.getAttributes());
  }


  private static IntArrayVector denseIndexMap(Selection selection, int sourceLength, int replacementLength) {
    int map[] = new int[sourceLength];
    Arrays.fill(map, -1);

    IntIterator it = selection.intIterator();
    int replacementIndex = 0;
    while(it.hasNext()) {
      map[it.nextInt()] = 0;
      if(replacementIndex >= replacementLength) {
        replacementIndex = 0;
      }
    }

    return IntArrayVector.unsafe(map);
  }
}
