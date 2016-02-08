package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a parameter that is a pointer to a single record value, implemented with a simple JVM reference type.
 */
public class RecordUnitPtrParamStrategy implements ParamStrategy {

    private RecordClassTypeStrategy strategy;

    public RecordUnitPtrParamStrategy(RecordClassTypeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.singletonList(Type.getType(strategy.getJvmType().getDescriptor()));
    }

    @Override
    public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
        return paramVars.get(0);
    }

    @Override
    public void emitPushParameter(MethodGenerator mv, Expr parameterValueGenerator) {
        ((SimpleExpr) parameterValueGenerator).load(mv);
    }

}
