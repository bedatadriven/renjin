package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.List;

/**
 * References a Virtual table entry.
 */
public class GimpleObjectTypeRef extends GimpleExpr {

  private GimpleExpr expr;
  private GimpleExpr object;
  private GimpleExpr token;

  public GimpleExpr getExpr() {
    return expr;
  }

  public void setExpr(GimpleExpr expr) {
    this.expr = expr;
  }

  public GimpleExpr getObject() {
    return object;
  }

  public void setObject(GimpleExpr object) {
    this.object = object;
  }

  public GimpleExpr getToken() {
    return token;
  }

  public void setToken(GimpleExpr token) {
    this.token = token;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    expr.find(predicate, results);
    object.find(predicate, results);
    token.find(predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    return 
        expr.replace(predicate, replacement) ||
        object.replace(predicate, replacement) ||
        token.replace(predicate, replacement);
  }

  @Override
  public String toString() {
    return "ObjectTypeRef{" + expr + ", " + object + ", " + token + "}";
  }
}
