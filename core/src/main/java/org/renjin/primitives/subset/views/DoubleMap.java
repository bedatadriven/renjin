package org.renjin.primitives.subset.views;

import org.renjin.iterator.IntIterator;
import org.renjin.primitives.subset.Selection;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.Arrays;


public class DoubleMap extends DoubleVector implements DeferredComputation {

  public static final int MAX_LENGTH = 50;
  
  private Vector[] vectors;

  public DoubleMap(Vector[] vectors, AttributeMap attributes) {
    this.vectors = vectors;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleMap(vectors, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return vectors[index].getElementAsDouble(0);
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return vectors.length;
  }

  @Override
  public Vector[] getOperands() {
    return vectors;
  }

  @Override
  public String getComputationName() {
    return "map";
  }

  public Vector select(Selection selection, AttributeMap attributes) {
    int selectionLength = selection.getElementCount();
    if(selectionLength == 1) {
      return vectors[selection.intIterator().nextInt()];
    }
    Vector newMap[] = new Vector[selectionLength];
    IntIterator it = selection.intIterator();
    int newIndex = 0;
    while(it.hasNext()) {
      int oldIndex = it.nextInt();
      if(!IntVector.isNA(oldIndex) && oldIndex < vectors.length) {
        newMap[newIndex] = vectors[oldIndex];
      }
    }
    return new DoubleMap(newMap, attributes);
  }


  public static boolean accept(Vector source, Selection selection, Vector replacement) {
    return source.length() <= MAX_LENGTH && selection.getElementCount() == 1;
  }

  public static DoubleMap replace(Vector vector, Selection selection, Vector replacement) {
    int indexToReplace = selection.intIterator().nextInt();
    Vector[] map;
    
    if( vector instanceof DoubleMap) {
      // just make a copy of the slots, replace this one vector
      map = Arrays.copyOf( ((DoubleMap) vector).vectors, vector.length());
      map[indexToReplace] = replacement;    

    } else if( vector.isConstantAccessTime() ) {

      // if we can access the results in constant time, then just make a copy of the
      // individual elements.
      map = new Vector[vector.length()];
      for(int i=0;i!=map.length;++i) {
        if(i == indexToReplace) {
          map[i] = replacement;
        } else {
          map[i] = new DoubleArrayVector(vector.getElementAsDouble(i));
        }
      }
    } else {

      // otherwise avoid triggering big tasks by just creating a view to the underlying element

      map = new Vector[vector.length()];
      for(int i=0;i!=map.length;++i) {
        if(i == indexToReplace) {
          map[i] = replacement;
        } else {
          map[i] = new DoubleSelect(vector, IntArrayVector.valueOf(i), vector.getAttributes());
        }
      }
    }
    return new DoubleMap(map, vector.getAttributes());
  }

}
