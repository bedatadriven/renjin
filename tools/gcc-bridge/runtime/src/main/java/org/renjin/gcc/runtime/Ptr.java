package org.renjin.gcc.runtime;


public interface Ptr {
  
  Object getArray();
  
  int getOffset();
  
  Ptr realloc(int newSizeInBytes);
  
  Ptr pointerPlus(int bytes);
  
}
