/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

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
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    expr = replaceOrDescend(expr, predicate, newExpr);
    object = replaceOrDescend(object, predicate, newExpr);
    token = replaceOrDescend(token, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitObjectTypeRef(this);
  }

  @Override
  public String toString() {
    return "ObjectTypeRef{" + expr + ", " + object + ", " + token + "}";
  }
}
