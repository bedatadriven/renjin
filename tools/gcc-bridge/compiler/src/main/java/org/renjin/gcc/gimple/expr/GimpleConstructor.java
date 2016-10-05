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
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * GimpleExpr node representing the brace-enclosed initializers for a structure or an array. They contain a 
 * sequence of component values made out of a vector of constructor_elt, which is a (INDEX, VALUE) pair.
 */
public class GimpleConstructor extends GimpleExpr {


  public static class Element {
    private GimpleExpr field;
    private GimpleExpr value;

    public GimpleExpr getField() {
      return field;
    }
    
    public String getFieldName() {
      return ((GimpleFieldRef) field).getName();
    }

    public void setField(GimpleExpr field) {
      this.field = field;
    }

    public GimpleExpr getValue() {
      return value;
    }

    public void setValue(GimpleExpr value) {
      this.value = value;
    }

    @Override
    public String toString() {
      if(field == null) {
        return value.toString();
      } else {
        return field + " = " + value;
      }
    }
  }
 
  private List<Element> elements = new ArrayList<Element>();
  private boolean clobber;

  public List<Element> getElements() {
    return elements;
  }


  public boolean isClobber() {
    return clobber;
  }

  public void setClobber(boolean clobber) {
    this.clobber = clobber;
  }

  public <X extends GimpleExpr> X getElement(int i) {
    return (X)elements.get(i).getValue();
  }


  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (Element element : elements) {
      element.value = replaceOrDescend(element.value, predicate, newExpr);
    }
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitConstructor(this);
  }
  
  @Override
  public String toString() {
    return "{" + Joiner.on(", ").join(elements) + "}";
  }
}
