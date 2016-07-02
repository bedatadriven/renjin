package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.builtins.*;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.FunctionCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private FunctionCall call;
  private final Primitives.Entry primitive;
  private final List<IRArgument> arguments;

  private final Specializer specializer;
  
  private Specialization specialization = UnspecializedCall.INSTANCE;

  public BuiltinCall(FunctionCall call, Primitives.Entry primitive, List<IRArgument> arguments) {
    this.call = call;
    this.primitive = primitive;
    this.arguments = arguments;
    this.specializer = BuiltinSpecializers.INSTANCE.get(primitive);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }
  
  @Override
  public Expression childAt(int index) {
    return arguments.get(index).getExpression();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.get(childIndex).setExpression(child);
  }
  
  @Override
  public boolean isDefinitelyPure() {
    return false;
  }


  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    try {
      specialization.load(emitContext, mv, arguments);

    } catch (FailedToSpecializeException e) {
      throw new NotCompilableException(call, e.getMessage());
    }
    return 1;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    List<ValueBounds> argumentTypes = new ArrayList<>();
    for (IRArgument argument : arguments) {
      argumentTypes.add(argument.getExpression().updateTypeBounds(typeMap));
    }
    specialization = specializer.trySpecialize(argumentTypes);
    
    return specialization.getValueBounds();
  }

  @Override
  public Type getType() {
    return specialization.getType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return specialization.getValueBounds();
  }
  
  @Override
  public String toString() {
    return "(" + primitive.name + " " + Joiner.on(" ").join(arguments) + ")";
  }
}
