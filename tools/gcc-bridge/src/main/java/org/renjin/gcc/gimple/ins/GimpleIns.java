package org.renjin.gcc.gimple.ins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = GimpleAssign.class, name = "assign"),
    @Type(value = GimpleCall.class, name = "call"),
    @Type(value = GimpleConditional.class, name = "conditional"),
    @Type(value = GimpleReturn.class, name = "return"),
    @Type(value = GimpleGoto.class, name = "goto"),
    @Type(value = GimpleSwitch.class, name = "switch"),
    @Type(value = GimpleOffset.class, name = "offset_type"),
    @Type(value = GimpleComplexType.class, name = "complex_type"),
    @Type(value = GimpleVectorTypeIns.class, name = "vector_type"),
    @Type(value = GimpleBlock.class, name = "block")})
public abstract class GimpleIns {

  public abstract void visit(GimpleVisitor visitor);
    
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    return false;
  }


  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  protected final void replaceAll(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> operands, GimpleExpr newExpr) {
    for (int i = 0; i < operands.size(); i++) {
      if(predicate.apply(operands.get(i))) {
        operands.set(i, newExpr);
      }
    }
  }
}
