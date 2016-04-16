package org.renjin.gcc.codegen.lib;

import org.renjin.gcc.codegen.call.CallGenerator;

public class SymbolFunction {

	private String alias;
	private CallGenerator call;
	private boolean memoryAllocation;

	public SymbolFunction(String alias, CallGenerator call) {
		this(alias, call, false);
	}

	public SymbolFunction(String alias, CallGenerator call, boolean memoryAllocation) {
		super();
		this.alias = alias;
		this.call = call;
		this.memoryAllocation = memoryAllocation;
	}

	public String getAlias() {
		return alias;
	}

	public CallGenerator getCall() {
		return call;
	}

	public boolean isMemoryAllocation() {
		return memoryAllocation;
	}
}