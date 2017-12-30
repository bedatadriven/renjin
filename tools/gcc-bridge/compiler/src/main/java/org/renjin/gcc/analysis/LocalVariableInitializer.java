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

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleComplexConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.gcc.gimple.type.*;

import java.util.Set;

/**
 * Ensures that local variables are initialized before use.
 * 
 * <p>GCC happily permits the use of uninitialized variables, but the JVM's byte code verifier
 * will abort if we try to load a variable that has not been initialized.</p>
 */
public class LocalVariableInitializer implements FunctionBodyTransformer {
  
  public static final LocalVariableInitializer INSTANCE = new LocalVariableInitializer();
  
  @Override
  public boolean transform(TreeLogger logger, GimpleOracle oracle, GimpleCompilationUnit unit, GimpleFunction fn) {
    
    ControlFlowGraph cfg = new ControlFlowGraph(fn);
    InitDataFlowAnalysis flowAnalysis = new InitDataFlowAnalysis(fn, cfg);
    
    flowAnalysis.solve();
    if(GimpleCompiler.TRACE) {
      flowAnalysis.dump();
    }

    Set<GimpleVarDecl> toInitialize = flowAnalysis.getVariablesUsedWithoutInitialization();

    for (GimpleVarDecl decl : toInitialize) {
      GimpleExpr defaultValue = defaultValue(decl.getType());
      decl.setValue(defaultValue);
      if(GimpleCompiler.TRACE) {
        System.out.println("INITIALIZING " + decl + " = " + defaultValue);
      }
    }
    
    // one pass is always enough
    return false;
  }

  private GimpleExpr defaultValue(GimpleType type) {
    if(type instanceof GimpleIntegerType) {
      return new GimpleIntegerConstant((GimpleIntegerType) type, 0);
   
    } else if(type instanceof GimpleRealType) {
      return new GimpleRealConstant((GimpleRealType) type, 0);
      
    } else if(type instanceof GimpleIndirectType) {
      return GimpleIntegerConstant.nullValue((GimpleIndirectType) type);

    } else if(type instanceof GimpleBooleanType) {
      GimpleIntegerConstant defaultValue = new GimpleIntegerConstant();
      defaultValue.setValue(0);
      defaultValue.setType(type);
      return defaultValue;
       
    } else if(type instanceof GimpleComplexType) {
      GimpleComplexConstant zero = new GimpleComplexConstant();
      GimpleRealType partType = ((GimpleComplexType) type).getPartType();
      zero.setType(type);
      zero.setIm(new GimpleRealConstant(partType, 0));
      zero.setReal(new GimpleRealConstant(partType, 0));
      return zero;
      
    } else {
      throw new UnsupportedOperationException("Don't know how to create default value for " + type);
    }
  }
}
