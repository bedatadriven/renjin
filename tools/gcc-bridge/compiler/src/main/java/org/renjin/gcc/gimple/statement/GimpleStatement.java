package org.renjin.gcc.gimple.statement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.base.Predicates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Gimple statement
 * 
 * @see <a href="https://gcc.gnu.org/onlinedocs/gccint/Basic-Statements.html#Basic-Statements">Basic Statements</a> in
 * the GCC Internals Manual
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = GimpleAssignment.class, name = "assign"),
    @Type(value = GimpleCall.class, name = "call"),
    @Type(value = GimpleConditional.class, name = "conditional"),
    @Type(value = GimpleReturn.class, name = "return"),
    @Type(value = GimpleGoto.class, name = "goto"),
    @Type(value = GimpleSwitch.class, name = "switch"),
    @Type(value = GimpleBlock.class, name = "block")})
public abstract class GimpleStatement {

  @JsonProperty("line")
  private Integer lineNumber;
  
  public abstract void visit(GimpleVisitor visitor);
    
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    return false;
  }
  
  public List<GimpleExpr> getOperands() {
    return Collections.emptyList();
  }
  
  /**
   * @return the set of {@code SymbolRef}s that are read by this statement
   */
  public Iterable<? extends GimpleSymbolRef> getUsedExpressions() {
    return Collections.emptySet();
  }
  
  public final List<GimpleExpr> findUses(Predicate<? super GimpleExpr> predicate) {
    List<GimpleExpr> set = new ArrayList<>();
    findUses(predicate, set);
    return set;
  }
  
  @SuppressWarnings("unchecked")
  public final <T extends GimpleExpr> List<T> findUses(Class<T> exprClass) {
    return (List<T>)findUses(Predicates.instanceOf(exprClass));
  }
  
  public final List<GimpleVariableRef> findVariableUses() {
    return findUses(GimpleVariableRef.class);
  }
  
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    
  }
  
  protected final void findUses(List<GimpleExpr> operands, Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    for (GimpleExpr operand : operands) {
      operand.findOrDescend(predicate, results);
    }
  }
  
  public final Integer getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(Integer lineNumber) {
    this.lineNumber = lineNumber;
  }

  protected final void replaceAll(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> operands, GimpleExpr newExpr) {
    for (int i = 0; i < operands.size(); i++) {
      if(predicate.apply(operands.get(i))) {
        operands.set(i, newExpr);
      } else {
        operands.get(i).replaceAll(predicate, newExpr);
      }
    }
  }


  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
  }

  public abstract void accept(GimpleExprVisitor visitor);

  /**
   * 
   * @return the basic block indices to which this statement can jump
   */
  public Set<Integer> getJumpTargets() {
    return Collections.emptySet();
  }
}
