package org.renjin.gcc.codegen.type.voidt;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;


public class VoidPtrValueFunction implements ValueFunction {
  @Override
  public Type getValueType() {
    return Type.getType(Object.class);
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return 4;
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    return new VoidPtr(Expressions.elementAt(array, offset));
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    FatPtrExpr fatPtrExpr = (FatPtrExpr) expr;
    return Collections.singletonList(fatPtrExpr.wrap());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public String toString() {
    return "VoidPtr";
  }
}
