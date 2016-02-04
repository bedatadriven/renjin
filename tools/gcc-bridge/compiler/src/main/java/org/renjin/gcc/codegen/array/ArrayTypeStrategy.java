package org.renjin.gcc.codegen.array;

import com.google.common.collect.Lists;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;

import java.util.List;

/**
 * Type strategy for arrays of values
 */
public class ArrayTypeStrategy extends TypeStrategy<FatPtrExpr> {
  
  private final ValueFunction valueFunction;
  private boolean parameterWrapped = false;

  public ArrayTypeStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  public boolean isParameterWrapped() {
    return parameterWrapped;
  }

  public ArrayTypeStrategy setParameterWrapped(boolean parameterWrapped) {
    this.parameterWrapped = parameterWrapped;
    return this;
  }

  @Override
  public TypeStrategy pointerTo() {
    return new FatPtrStrategy(new ArrayValueFunction(valueFunction))
        .setParametersWrapped(parameterWrapped);
  }

  @Override
  public FatPtrExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor constructor) {
    List<Value> values = Lists.newArrayList();
    for (GimpleConstructor.Element element : constructor.getElements()) {
     values.add(exprFactory.findValueGenerator(element.getValue())); 
    }
    
    Value array = Values.newArray(valueFunction.getValueType(), values);
    Value offset = Values.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public ExprGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    Value array = allocator.reserveArrayRef(decl.getName(), valueFunction.getValueType());
    Value offset = Values.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator array, ExprGenerator index) {
    FatPtrExpr arrayFatPtr = (FatPtrExpr) array;
    Value indexValue = (Value) index;
    
    // New offset  = ptr.offset + (index * value.length)
    // for arrays of doubles, for example, this will be the same as ptr.offset + index
    // but for arrays of complex numbers, this will be ptr.offset + (index * 2)
    Value newOffset = Values.sum(
        arrayFatPtr.getOffset(),
        Values.product(
            indexValue, 
            valueFunction.getElementLength()));
    
    return valueFunction.dereference(arrayFatPtr.getArray(), newOffset);
  }

  @Override
  public ExprGenerator addressOf(FatPtrExpr pointer) {
    return pointer;
  }
}
