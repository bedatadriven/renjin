/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import java.util.function.Predicate;
import org.renjin.repackaged.guava.base.Strings;

import java.util.List;

public class GimpleComponentRef extends GimpleLValue {

  private GimpleExpr value;
  private GimpleExpr member;

  public GimpleComponentRef() {
  }

  public GimpleComponentRef(GimpleExpr value, GimpleExpr member) {
    this.value = value;
    this.member = member;
    setType(member.getType());
  }

  public GimpleExpr getValue() {
    return value;
  }

  public void setMember(GimpleExpr member) {
    this.member = member;
  }

  public GimpleFieldRef getMember() {
    return (GimpleFieldRef) member;
  }

  public String memberName() {
    if(member instanceof GimpleFieldRef) {
      return Strings.nullToEmpty(((GimpleFieldRef) member).getName());
    }
    throw new UnsupportedOperationException(member.getClass().getSimpleName());
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(value, predicate, results);
    findOrDescend(member, predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    value = replaceOrDescend(value, predicate, newExpr);
    member = replaceOrDescend(member, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitComponentRef(this);
  }

  @Override
  public String toString() {
    if(member instanceof GimpleFieldRef) {
      GimpleFieldRef memberField = (GimpleFieldRef) member;
      if(memberField.getName() == null) {
        return value + "@" + memberField.getOffset();
      }
    }
    return value + "." + member;
  }
}
