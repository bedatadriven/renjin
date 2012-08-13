package org.renjin.gcc.gimple;

import com.google.common.collect.Lists;

import java.util.List;

public class GimpleBasicBlock {
	
	private int number;
	private List<GimpleIns> instructions = Lists.newArrayList();

	public GimpleBasicBlock(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<BB ").append(number).append(">:\n");
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
