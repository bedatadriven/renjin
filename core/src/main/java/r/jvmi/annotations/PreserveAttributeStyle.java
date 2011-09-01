package r.jvmi.annotations;

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
   * All attributes are copied from the longest argument.
   */
  ALL
}
