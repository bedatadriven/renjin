package org.renjin.gcc.codegen.lib.cpp;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

public class CtorCallGenerator implements CallGenerator {

	@Override
	public void emitCall(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
		// TODO Auto-generated method stub
		System.out.println("emitCall");
	}

	@Override
	public void emitCallAndPopResult(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
		// TODO Auto-generated method stub
		System.out.println("emitCallAndPopResult");
	}

	@Override
	public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
		// TODO Auto-generated method stub
		return null;
	}
}