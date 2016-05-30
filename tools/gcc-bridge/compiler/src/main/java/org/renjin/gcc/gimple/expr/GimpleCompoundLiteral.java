package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.List;

/**
 * A literal record value. 
 * 
 * <p><a href="https://gcc.gnu.org/onlinedocs/gcc/Compound-Literals.html">Compound Literals</a> are translated
 * to gimple by creating a new global variable initialized to the value of the literal, and replacing the 
 * references to the literal with references to the global variable.
 * </ul>
 * 
 */
public class GimpleCompoundLiteral extends GimpleLValue {

  private GimpleVariableRef decl;

  /**
   * 
   * @return the reference to the global variable initialized to the value of the literal
   */
  public GimpleVariableRef getDecl() {
    return decl;
  }

  public void setDecl(GimpleVariableRef decl) {
    this.decl = decl;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    decl.find(predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    decl = (GimpleVariableRef) replaceOrDescend(decl, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitCompoundLiteral(this);
  }
}
