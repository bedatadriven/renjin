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
package org.renjin.compiler;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.sexp.ExpressionVector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class CompilerTestCase {


  public static void print(int i, int j, int k, int l) {
    System.out.println(Joiner.on(", ").join(Arrays.asList(i, j, k, l)));
  }


  protected IRBody buildBody(String rcode) {
    Session session = new SessionBuilder().build();
    
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    RuntimeState runtimeState = new RuntimeState(session.getTopLevelContext(), session.getGlobalEnvironment());
    return new IRBodyBuilder(runtimeState).build(ast);
  }
  

  protected final IRBody parseCytron() throws IOException {
    return buildBody(Resources.toString(Resources.getResource(ControlFlowGraph.class, "cytron.R"), Charsets.UTF_8));
  }
  
  protected final Matcher<Collection<BasicBlock>> itemsEqualTo(final BasicBlock... blocks) {
    return new TypeSafeMatcher<Collection<BasicBlock>>() {

      @Override
      public void describeTo(Description d) {
        d.appendValue(Arrays.toString(blocks));
      }

      @Override
      public boolean matchesSafely(Collection<BasicBlock> collection) {
        if(collection.size() != blocks.length) {
          return false;
        }
        for(BasicBlock bb : blocks) {
          if(!collection.contains(bb)) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
