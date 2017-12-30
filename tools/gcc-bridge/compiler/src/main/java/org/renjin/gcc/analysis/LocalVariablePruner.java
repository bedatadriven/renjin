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
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.ListIterator;
import java.util.Set;

/**
 * Removes unused variables. 
 * 
 * <p>These can include arrays which are actually dynamically allocated and not used.</p>
 */
public class LocalVariablePruner implements FunctionBodyTransformer {
  
  public static final LocalVariablePruner INSTANCE = new LocalVariablePruner();
  
  @Override
  public boolean transform(TreeLogger logger, GimpleOracle oracle, GimpleCompilationUnit unit, GimpleFunction fn) {

    VariableRefFinder refFinder = new VariableRefFinder();
    fn.accept(refFinder);
    
    ListIterator<GimpleVarDecl> it = fn.getVariableDeclarations().listIterator();
    boolean updated = false;
    while(it.hasNext()) {
      GimpleVarDecl decl = it.next();
      if(!refFinder.used.contains(decl.getId())) {
        it.remove();
        updated = true;
      }
    }
    return updated;
  }
  
  private class VariableRefFinder extends GimpleExprVisitor {

    private Set<Long> used = Sets.newHashSet();
    
    @Override
    public void visitVariableRef(GimpleVariableRef variableRef) {
      used.add(variableRef.getId());
    }
  }
}
