package org.renjin.primitives.annotations.processor;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;

public class IfElseBuilder {

  private final JBlock block;
  private JConditional conditional;

  public IfElseBuilder(JBlock block) {
    this.block = block;
  }

  public JBlock _if(JExpression expr) {
    if(conditional == null) {
      conditional = block._if(expr);
    } else {
      conditional = conditional._elseif(expr);
    }
    return conditional._then();
  }

  public JBlock _else() {
    if(conditional == null) {
      return block;
    } else {
      return conditional._else();
    }
  }
}
