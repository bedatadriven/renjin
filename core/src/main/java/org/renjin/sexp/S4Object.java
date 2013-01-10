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
    for(PairList.Node node : attributes.asPairList().nodes()) {
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
    return attributes.equals(other.attributes);
  }

  @Override
  public int hashCode() {
    return attributes.hashCode();
  }

  
  

}
