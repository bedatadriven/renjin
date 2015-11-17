package org.renjin.gcc.gimple.ins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

import java.util.Collections;
import java.util.HashSet;
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

  /**
   * @return the set of {@code SymbolRef}s that are read by this statement
   */
  public Iterable<? extends GimpleSymbolRef> getUsedExpressions() {
    return Collections.emptySet();
  }
  
  public final Set<GimpleExpr> findUses(Predicate<? super GimpleExpr> predicate) {
    Set<GimpleExpr> set = new HashSet<>();
    findUses(predicate, set);
    return set;
  }
  
  @SuppressWarnings("unchecked")
  public final <T extends GimpleExpr> Set<T> findUses(Class<T> exprClass) {
    return (Set<T>)findUses(Predicates.instanceOf(exprClass));
  }
  
  public final Set<GimpleVariableRef> findVariableUses() {
    return findUses(GimpleVariableRef.class);
  }
  
  protected void findUses(Predicate<? super GimpleExpr> predicate, Set<GimpleExpr> results) {
    
  }
  
  protected final void findUses(List<GimpleExpr> operands, Predicate<? super GimpleExpr> predicate, Set<GimpleExpr> results) {
    for (GimpleExpr operand : operands) {
      operand.findOrDescend(predicate, results);
    }
  }
  
  public Integer getLineNumber() {
    return null;
  }

  protected final void replaceAll(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> operands, GimpleExpr newExpr) {
    for (int i = 0; i < operands.size(); i++) {
      if(predicate.apply(operands.get(i))) {
        operands.set(i, newExpr);
      } 
    }
  }

  /**
   * Replaces the first {@code GimpleExpr} that matches the provided predicate.
   * @param predicate the predicate that determines whether a node is replaced
   * @param replacement the replacement {@code GimpleExpr}
   * @return true if a match was found and replaced
   */
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    return false;
  }


  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
  }


  /**
   * 
   * @return the basic block indices to which this statement can jump
   */
  public Set<Integer> getJumpTargets() {
    return Collections.emptySet();
  }
}
