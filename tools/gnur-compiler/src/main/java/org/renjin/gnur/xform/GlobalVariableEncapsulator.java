package org.renjin.gnur.xform;

import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.util.List;

/**
 * Encapsulates local variables in per-session parameter, annotated with @Singleton
 * 
 * <p>Steps:</p>
 * <ul>
 *   <li>Identify all global variables</li>
 *   <li>Create a new {@link org.renjin.gcc.gimple.type.GimpleRecordTypeDef} <strong>R</strong> to hold each of these global variables</li>
 *   <li>For each function which reads or writes to this variable, 
 *   <ul>
 *     <li>Add a parameter of type <strong>R</strong> to the function</li>
 *     <li>Replace uses of the global variable with {@link org.renjin.gcc.gimple.expr.GimpleFieldRef} expressions</li>
 *     <li>Push the <strong>R</strong> parameter to all upstream callsites</li>
 *   </ul>
 *   </li>
 * </ul>
 */
public class GlobalVariableEncapsulator {
  
  private List<GimpleCompilationUnit> units;

  public GlobalVariableEncapsulator(List<GimpleCompilationUnit> units) {
    this.units = units;
  }
  
  
}
