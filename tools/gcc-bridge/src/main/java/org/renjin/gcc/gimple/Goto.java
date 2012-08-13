package org.renjin.gcc.gimple;

public class Goto extends GimpleIns {
	private GimpleLabel target;
	
	public Goto(GimpleLabel label) {
		this.target = label;
	}
	

	public GimpleLabel getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return "goto <" + target + ">";
	}

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitGoto(this);

  }
}
