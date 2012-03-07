package org.renjin.primitives.subset;

public class NullSubscript extends Subscript {

  public static final NullSubscript INSTANCE = new NullSubscript();
  
  private NullSubscript() { }
  
  @Override
  public int getCount() {
     return 0;
  }

  @Override
  public int getAt(int i) {
    throw new IllegalArgumentException("a null subscript selects no elements (i=" + i + ")");
  }  
  
  
}
