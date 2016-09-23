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
package org.renjin.gcc.analysis;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleResultDecl;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleReferenceType;
import org.renjin.repackaged.guava.base.Predicates;

/**
 * Rewrites {@code GimpleResultDecl} nodes into local variables.
 * 
 * <p>The {@code result_decl} lvalue is some weird fortran thing. Much easier to deal with 
 * as a simple local variable.</p>
 */
public class ResultDeclRewriter implements FunctionBodyTransformer {

  public static final ResultDeclRewriter INSTANCE = new ResultDeclRewriter();

  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {
    
    // Does this function use a return_decl expression?
    ResultDeclMatcher matcher = new ResultDeclMatcher();
    fn.accept(matcher);
    
    if(matcher.present) {
      // if so, replace it with a local variable reference
      GimpleExpr ref;
      
      // Handle the case of Return Value Optimization
      // Make sure the reference is actually initialized with a value
      // (This would normally be done by GCC by playing with the stack)
      // https://en.wikipedia.org/wiki/Return_value_optimization
      if( 
          fn.getReturnType() instanceof GimpleReferenceType &&
          fn.getReturnType().getBaseType() instanceof GimpleRecordType) {
      
        GimpleVarDecl returnVar = fn.addVarDecl(fn.getReturnType().getBaseType());
        ref = new GimpleAddressOf(new GimpleVariableRef(returnVar. getId(), returnVar.getType()));

      } else {
        GimpleVarDecl returnVar = fn.addVarDecl(fn.getReturnType());
        ref = new GimpleVariableRef(returnVar.getId(), fn.getReturnType());
      }

      fn.replaceAll(Predicates.instanceOf(GimpleResultDecl.class), ref);
      
      assertResultDeclsAreReplaced(fn);
      
      return true;

    } else {
      return false;
    }
  }

  private void assertResultDeclsAreReplaced(GimpleFunction fn) {
    ResultDeclMatcher matcher = new ResultDeclMatcher();
    fn.accept(matcher);
    if(matcher.present) {
      throw new AssertionError("ResultDecls remain in:\n" + fn);
    }
  }

  private class ResultDeclMatcher extends GimpleExprVisitor {
    
    private boolean present = false;
    
    @Override
    public void visitResultDecl(GimpleResultDecl resultDecl) {
      present = true;
    }
  }
  
}
