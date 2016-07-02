package org.renjin.compiler.cfg;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Symbol;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InlinedFunctionTest extends EvalTestCase {

  
  @Test
  public void simple() {
    InlinedFunction fn = compileFunction("function() 42");

    ValueBounds returnBounds = fn.computeBounds();
    
    assertTrue(returnBounds.isConstant());
    assertThat(returnBounds.getConstantValue(), equalTo(c(42)));
  }

  @Test
  public void argument() {
    InlinedFunction fn = compileFunction("function(x = 1) length(x)");

    System.out.println(fn.getCfg());
    
    ValueBounds returnBounds = fn.computeBounds();

    assertTrue(returnBounds.isConstant());
    assertThat(returnBounds.getConstantValue(), equalTo(c_i(1)));
  }
  
  private InlinedFunction compileFunction(String functionDecl) {
    Closure closure = (Closure) eval(functionDecl);
    return new InlinedFunction(new RuntimeState(topLevelContext, topLevelContext.getGlobalEnvironment()),
        closure, Collections.<Symbol>emptySet());
  }


}