package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleVoidType;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a parameter that is a pointer to a single record value, implemented with a simple JVM reference type.
 */
public class RecordUnitPtrParamStrategy implements ParamStrategy {

    private RecordTypeStrategy strategy;

    public RecordUnitPtrParamStrategy(RecordTypeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.singletonList(Type.getType(strategy.getJvmType().getDescriptor()));
    }

    @Override
    public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
        return new RecordUnitPtrVarGenerator(strategy, paramVars.get(0));
    }

    @Override
    public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
        parameterValueGenerator.emitPushRecordRef(mv);
        if(parameterValueGenerator.getGimpleType().isPointerTo(GimpleVoidType.class)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, strategy.getJvmType().getInternalName());
        }
    }

}
