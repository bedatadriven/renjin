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
package org.renjin.sexp;

public class S4Object extends AbstractSEXP {

  public S4Object() {
    
  }
  
  public S4Object(AttributeMap attributes) {
    super(attributes);
  }
  
  @Override
  public String getTypeName() {
    return "S4";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new S4Object(attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("S4[");
    for(PairList.Node node : getAttributes().asPairList().nodes()) {
      SEXP value = node.getValue();
      if(!(value instanceof Function) && value.length() > 0) {
        sb.append(" ").append(node.getTag()).append("=").append(value);
      }
    }
    sb.append("]");
    return sb.toString();
    
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof S4Object)) {
      return false;
    }
    S4Object other = (S4Object)obj;
    return getAttributes().equals(other.getAttributes());
  }

  @Override
  public int hashCode() {
    return getAttributes().hashCode();
  }

  
  

}
