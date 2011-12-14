package r.compiler.ir.ssa;

import java.util.List;
import java.util.Set;

import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PhiFunction implements Operand {

  private List<Variable> arguments;
  
  public PhiFunction(Variable variable) {
      this.arguments = Lists.newArrayList(variable, variable);
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Sets.newHashSet(arguments);
  }

  @Override
  public String toString() {
    return "\u03A6(" + Joiner.on(", ").join(arguments) + ")";
  }   
}
