package org.renjin.gcc.gimple;

import java.util.List;

/**
 * Common interface to function, variable declarations
 */
public interface GimpleDecl {
  
  List<String> getMangledNames();
  
  void accept(GimpleExprVisitor visitor);

}
