package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleAddressableExpr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.Collections;
import java.util.List;


public class PrimitiveValueFunction implements ValueFunction {

  private Type type;
  private int byteSize;

  public PrimitiveValueFunction(GimplePrimitiveType type) {
    this.type = type.jvmType();
    this.byteSize = type.sizeOf();
  }
  
  public PrimitiveValueFunction(Type type) {
    this(GimplePrimitiveType.fromJvmType(type));
  }
  

  @Override
  public Type getValueType() {
    return type;
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return byteSize;
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    FatPtrExpr address = new FatPtrExpr(array, offset);
    SimpleExpr value = Expressions.elementAt(array, offset);

    return new SimpleAddressableExpr(value, address);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    return Collections.singletonList((SimpleExpr)expr);
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return Optional.absent();
  }
}
