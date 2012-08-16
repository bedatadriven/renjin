package org.renjin.gcc.gimple;

import com.google.common.collect.Lists;

import java.util.List;

public class GimpleBasicBlock {
	
	private String name;
	private List<GimpleIns> instructions = Lists.newArrayList();

	public GimpleBasicBlock(String name) {
		this.name = name;
	}

  public String getName() {
    return name;
  }

  @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(name).append(">:\n");
		for(GimpleIns ins : instructions) {
			sb.append("  ").append(ins).append("\n");
		}
		return sb.toString();
	}

	public void addIns(GimpleIns ins) {
		instructions.add(ins);
	}

	public List<GimpleIns> getInstructions() {
		return instructions;
	}

}
