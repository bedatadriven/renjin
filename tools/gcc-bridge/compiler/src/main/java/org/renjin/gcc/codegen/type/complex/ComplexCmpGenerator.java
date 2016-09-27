/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;


/**
 * Generates a comparison of complex values
 */
public class ComplexCmpGenerator implements ConditionGenerator {

  private GimpleOp op;
  private ComplexValue x;
  private ComplexValue y;
  private Type type;
  
  public ComplexCmpGenerator(GimpleOp op, ComplexValue x, ComplexValue y) {
    this.op = op;
    this.x = x;
    this.y = y;
    this.type = x.getComponentType();
  }


  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    switch (op) {
      case EQ_EXPR:
        emitJumpIfEqual(mv, trueLabel, falseLabel);
        break;
      case NE_EXPR:
        emitJumpIfEqual(mv, falseLabel, trueLabel);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported comparison " + op + " between complex values");
    }
  }

  private void emitJumpIfEqual(MethodGenerator mv, Label equalLabel, Label notEqualLabel) {
    
    // First check real part 
    x.getRealJExpr().load(mv);
    y.getRealJExpr().load(mv);

    // Compare the two real values.
    // If they are equal, ZERO is pushed onto the stack
    mv.cmpg(type);
    
    // If the real values are not equal 
    // then jump to false immediately, no need to check imaginary
    mv.ifne(notEqualLabel);

    // Now check imaginary part
    x.getImaginaryJExpr().load(mv);
    y.getImaginaryJExpr().load(mv);

    // Compare the two imaginary values.
    // If they are equal, ZERO is pushed onto the stack
    mv.cmpg(type);
    
    // If the imaginary values are not equal 
    // then jump to false
    mv.ifne(notEqualLabel);
    
    // if we reach here, then both real and imaginary parts are equal
    mv.goTo(equalLabel);
  }

}
