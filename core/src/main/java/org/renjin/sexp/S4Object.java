package org.renjin.sexp;

public class S4Object extends AbstractSEXP {

  public S4Object() {
    
  }
  
  private S4Object(AttributeMap attributes) {
    super(attributes);
  }
  
  @Override
  public String getTypeName() {
    return "S4";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    throw new UnsupportedOperationException("implement me");
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
      sb.append(" ").append(node.getTag()).append("=").append(node.getValue());
    }
    sb.append("]");
    return sb.toString();
    
  }
  
  

}
