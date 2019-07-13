package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.LogicalVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Analytical function which resolves to TRUE or FALSE, depending on whether the
 * its function strictly evaluates its arguments.
 *
 * <p>This function is not actually meant to be compiled, but is inserted into the
 * 3-address code to help analyze whether arguments to a particular function </p>
 */
public class StrictFunction implements Expression {

  private Expression function;
  private List<Expression> partialArgumentList;

  private ValueBounds bounds = ValueBounds.constantValue(LogicalVector.FALSE);

  public StrictFunction(Expression function) {
    this.function = function;
    this.partialArgumentList = Collections.emptyList();
  }

  public StrictFunction(Expression function, List<IRArgument> partialArguments) {
    this.function = function;
    this.partialArgumentList = new ArrayList<>();
    for (IRArgument partialArgument : partialArguments) {
      partialArgumentList.add(partialArgument.getExpression());
    }
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    ValueBounds functionBounds = function.updateTypeBounds(typeMap);

    List<ValueBounds> argumentBounds = new ArrayList<>();
    for (Expression argument : partialArgumentList) {
      argumentBounds.add(argument.updateTypeBounds(typeMap));
    }

    if(isDefinitelyStrict(functionBounds, argumentBounds)) {
      bounds = ValueBounds.TRUE;
    } else {
      bounds = ValueBounds.FALSE;
    }

    return bounds;
  }

  private boolean isDefinitelyStrict(ValueBounds functionBounds, List<ValueBounds> argumentBounds) {
    if(functionBounds.isConstant() && functionBounds.getConstantValue() instanceof BuiltinFunction) {
      BuiltinFunction function = (BuiltinFunction) functionBounds.getConstantValue();
      PrimitiveModel model = new PrimitiveModel(Primitives.getBuiltinEntry(function.getName()));

      // First arguments are always evaluated for builtin functions,
      // even if they are generic
      if(partialArgumentList.isEmpty()) {
        return true;

      }
      // Second arguments are always evaluated for builtins in the Ops group
      boolean ops = "Ops".equals(model.getGenericGroupName());
      if(partialArgumentList.size() == 1 && ops) {
        return true;
      }

      // If the function is NOT generic, then all arguments are evaluated
      if(!model.isGeneric()) {
        return true;
      }

      // Otherwise if we have the first argument, we might be able to rule out generic dispatch, which
      // means that all arguments are evaluated
      if(ops && argumentBounds.size() >= 2) {
        return definitelyNotGeneric(argumentBounds.get(0)) &&
              definitelyNotGeneric(argumentBounds.get(1));
      } else if(argumentBounds.size() > 1) {
        return definitelyNotGeneric(argumentBounds.get(0));
      }
    }
    return false;
  }

  private boolean definitelyNotGeneric(ValueBounds bounds) {
    if(TypeSet.mightBe(bounds.getTypeSet(), TypeSet.S4)) {
      return false;
    }
    if(bounds.isFlagSet(ValueBounds.MAYBE_CLASS)) {
      return false;
    }
    return true;
  }

  @Override
  public ValueBounds getValueBounds() {
    return bounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    switch (index) {
      case 0:
        return this.function;

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    switch (childIndex) {
      case 0:
        this.function = child;
        break;

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("strict(").append(function);

    for (Expression expression : partialArgumentList) {
      s.append(", ").append(expression);
    }
    s.append(")");
    return s.toString();
  }
}
