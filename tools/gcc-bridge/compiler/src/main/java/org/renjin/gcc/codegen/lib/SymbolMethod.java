package org.renjin.gcc.codegen.lib;

public class SymbolMethod {

	private String alias;
	private Class<?> targetClass;
	private String methodName;

	public SymbolMethod(String alias, Class<?> targetClass, String methodName) {
		super();
		this.alias = alias;
		this.targetClass = targetClass;
		this.methodName = methodName;
	}

	public String getAlias() {
		return alias;
	}
	public Class<?> getTargetClass() {
		return targetClass;
	}
	public String getMethodName() {
		return methodName;
	}
}