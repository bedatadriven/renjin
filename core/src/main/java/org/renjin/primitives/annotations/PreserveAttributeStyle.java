package org.renjin.primitives.annotations;

public enum PreserveAttributeStyle {
  
  /**
   * No attributes are copied to the result
   */
  NONE,
  
  /**
   * Only the {@code dim}, {@code dimnames}, and {@code names} attributes should
   * be copied from the longest argument.
   */
  SPECIAL,
  
  /**
   * All attributes are copied from the longest argument. If there are ties among the 
   * arguments for the longest vector, attributes are copied from all elements of the tie,
   * but giving precedence to the earlier arguments.
   */
  ALL
}
