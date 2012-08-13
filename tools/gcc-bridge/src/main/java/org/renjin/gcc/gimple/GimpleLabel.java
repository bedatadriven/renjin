package org.renjin.gcc.gimple;

public class GimpleLabel {
	private String name;
  private int basicBlockNumber;

	public GimpleLabel(String name) {
		super();
		this.name = name;
    int space = name.indexOf(' ');
    assert space != -1;
    basicBlockNumber = Integer.parseInt(name.substring(space+1));

	}

  public int getBasicBlockNumber() {
    return basicBlockNumber;
  }

  @Override
	public String toString() {
		return name;
	}
}
