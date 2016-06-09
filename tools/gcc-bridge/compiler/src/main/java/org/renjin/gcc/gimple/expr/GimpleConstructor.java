package org.renjin.gcc.gimple.expr;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleExprVisitor;

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

  public List<Element> getElements() {
    return elements;
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
